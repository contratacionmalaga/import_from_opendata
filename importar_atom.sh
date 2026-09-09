#!/usr/bin/env bash
set -euo pipefail

GRUPO="con_filtros"
MODE="local"
TIPOS=""
DRY_RUN=false
CONTINUE_ON_ERROR=false
BASE_DIR="/home/contratacion/java/import-from-opendata-ejecutables"
JAVA_EXE="/home/contratacion/java/jdk-21.0.11/bin/java"
DEFAULT_JAVA_OPTS=("-Xms12g" "-Xmx12g")
JAVA_OPTS=("${DEFAULT_JAVA_OPTS[@]}")

usage() {
  cat <<USAGE
Uso: $0 [opciones]

Opciones:
  --grupo con_filtros|sin_filtros|all
  --mode local|internet|all
  --tipo MAYORES[,MENORES,CONSULTAS,ENCARGOS,AGREGRADAS]
  --base-dir /home/contratacion/java/import-from-opendata-ejecutables
  --java /ruta/a/java
  --dry-run
  --continue-on-error
  -h, --help

Para ejecutar una importacion real hay que confirmar escribiendo I.
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --grupo) GRUPO="${2:-}"; shift 2 ;;
    --mode) MODE="${2:-}"; shift 2 ;;
    --tipo) TIPOS="${2:-}"; shift 2 ;;
    --base-dir) BASE_DIR="${2:-}"; shift 2 ;;
    --java) JAVA_EXE="${2:-}"; shift 2 ;;
    --dry-run) DRY_RUN=true; shift ;;
    --continue-on-error) CONTINUE_ON_ERROR=true; shift ;;
    -h|--help) usage; exit 0 ;;
    *) echo "ERROR: Opcion no reconocida: $1" >&2; usage; exit 1 ;;
  esac
done

case "$GRUPO" in con_filtros|sin_filtros|all) ;; *) echo "ERROR: grupo no valido: $GRUPO" >&2; exit 1 ;; esac
case "$MODE" in local|internet|all) ;; *) echo "ERROR: mode no valido: $MODE" >&2; exit 1 ;; esac

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [[ ! -d "$BASE_DIR" && -d "$SCRIPT_DIR/properties" ]]; then
  BASE_DIR="$SCRIPT_DIR"
fi

LOG_DIR="$BASE_DIR/logs"
PROPERTIES_DIR="$BASE_DIR/properties"
APP_PROPERTIES="$PROPERTIES_DIR/app.properties"
FILTER_PROPERTIES="$PROPERTIES_DIR/filter.properties"
HIBERNATE_PROPERTIES="$PROPERTIES_DIR/hibernate.properties"
DATABASE_PROPERTIES="$PROPERTIES_DIR/bd.properties"
RUNTIME_PROPERTIES="$PROPERTIES_DIR/runtime.properties"
TIMESTAMP="$(date '+%Y%m%d_%H%M%S')"
LOG_FILE="$LOG_DIR/importacion_$TIMESTAMP.log"
mkdir -p "$LOG_DIR"

log() {
  printf '%s - %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$1" >> "$LOG_FILE"
}

fail() {
  echo "$1" >&2
  log "$1"
  exit 1
}

require_file() {
  [[ -f "$1" ]] || fail "ERROR: No se encuentra el fichero de configuracion: $1"
}

property_value() {
  local file="$1"
  local key="$2"
  local line
  line="$(grep -E "^[[:space:]]*${key//./\.}[[:space:]]*=" "$file" | tail -n 1 || true)"
  [[ -n "$line" ]] || return 0
  printf '%s' "${line#*=}" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//'
}

resolve_java_opts() {
  local configured_opts
  configured_opts="$(property_value "$RUNTIME_PROPERTIES" "java.opts")"
  if [[ -z "${configured_opts// }" ]]; then
    JAVA_OPTS=("${DEFAULT_JAVA_OPTS[@]}")
    return 0
  fi
  read -r -a JAVA_OPTS <<< "$configured_opts"
}

set_property_value() {
  local file="$1"
  local key="$2"
  local value="$3"
  if grep -qE "^[[:space:]]*${key//./\.}[[:space:]]*=" "$file"; then
    sed -i "s|^[[:space:]]*${key//./\.}[[:space:]]*=.*|$key=$value|" "$file"
  else
    printf '\n%s=%s\n' "$key" "$value" >> "$file"
  fi
}

version_from_jar() {
  local name
  name="$(basename "$1")"
  if [[ "$name" =~ ^(opendata_con_filtros|opendata_sin_filtros)-([0-9]+(\.[0-9]+){1,3})-(local|internet)\.jar$ ]]; then
    printf '%s' "${BASH_REMATCH[2]}"
  else
    printf '0.0.0'
  fi
}

resolve_jar() {
  local group="$1"
  local mode="$2"
  local artifact
  case "$group" in
    con_filtros) artifact="opendata_con_filtros" ;;
    sin_filtros) artifact="opendata_sin_filtros" ;;
    *) fail "ERROR: grupo interno no valido: $group" ;;
  esac

  local best=""
  local best_version="0.0.0"
  shopt -s nullglob
  for jar in "$BASE_DIR"/${artifact}-*-"$mode".jar; do
    [[ "$jar" =~ -(sources|javadoc)\.jar$ ]] && continue
    local version
    version="$(version_from_jar "$jar")"
    if [[ "$(printf '%s\n%s\n' "$best_version" "$version" | sort -V | tail -n 1)" == "$version" ]]; then
      best="$jar"
      best_version="$version"
    fi
  done
  shopt -u nullglob

  [[ -n "$best" ]] || fail "ERROR: No se encuentra ningun JAR para '$group' y modo '$mode' en $BASE_DIR."
  printf '%s' "$best"
}

tipos_por_grupo() {
  case "$1" in
    con_filtros) printf '%s\n' CONSULTAS ENCARGOS MAYORES MENORES ;;
    sin_filtros) printf '%s\n' MAYORES ;;
  esac
}

selected_groups() {
  if [[ "$GRUPO" == "all" ]]; then printf '%s\n' con_filtros sin_filtros; else printf '%s\n' "$GRUPO"; fi
}

selected_modes() {
  if [[ "$MODE" == "all" ]]; then printf '%s\n' local internet; else printf '%s\n' "$MODE"; fi
}

validate_tipo_for_group() {
  local group="$1"
  local tipo="$2"
  tipos_por_grupo "$group" | grep -qx "$tipo" || fail "ERROR: TipoSindicacion no valido para '$group': $tipo. Permitidos: $(tipos_por_grupo "$group" | paste -sd ', ' -)."
}

build_plan() {
  PLAN_GROUPS=()
  PLAN_MODES=()
  PLAN_TIPOS=()
  PLAN_JARS=()

  while IFS= read -r group; do
    while IFS= read -r mode; do
      local jar
      jar="$(resolve_jar "$group" "$mode")"
      if [[ -n "$TIPOS" ]]; then
        IFS=',' read -ra requested_tipos <<< "$TIPOS"
        for tipo in "${requested_tipos[@]}"; do
          tipo="$(printf '%s' "$tipo" | tr -d '[:space:]')"
          validate_tipo_for_group "$group" "$tipo"
          PLAN_GROUPS+=("$group"); PLAN_MODES+=("$mode"); PLAN_TIPOS+=("$tipo"); PLAN_JARS+=("$jar")
        done
      else
        while IFS= read -r tipo; do
          PLAN_GROUPS+=("$group"); PLAN_MODES+=("$mode"); PLAN_TIPOS+=("$tipo"); PLAN_JARS+=("$jar")
        done < <(tipos_por_grupo "$group")
      fi
    done < <(selected_modes)
  done < <(selected_groups)
}

assert_required_filters_for_filtered_run() {
  local requires=false
  for group in "${PLAN_GROUPS[@]}"; do
    [[ "$group" == "con_filtros" ]] && requires=true
  done
  [[ "$requires" == false ]] && return 0

  local nifs postal_codes
  nifs="$(property_value "$FILTER_PROPERTIES" "filter.nifs")"
  postal_codes="$(property_value "$FILTER_PROPERTIES" "filter.codigosPostales")"
  if [[ -z "${nifs// }" && -z "${postal_codes// }" ]]; then
    fail "ERROR: opendata_con_filtros requiere informar filter.nifs o filter.codigosPostales en $FILTER_PROPERTIES. No se ejecuta la importacion."
  fi
}

db_summary() {
  local url="$1"
  DB_MOTOR="no identificado"; DB_HOST="no indicado"; DB_PORT="no indicado"; DB_NAME="no indicada"
  if [[ "$url" =~ ^jdbc:(mariadb|postgresql)://([^/:?]+)(:([0-9]+))?/([^?]+) ]]; then
    [[ "${BASH_REMATCH[1]}" == "mariadb" ]] && DB_MOTOR="MariaDB" || DB_MOTOR="PostgreSQL"
    DB_HOST="${BASH_REMATCH[2]}"
    DB_PORT="${BASH_REMATCH[4]:-por defecto}"
    DB_NAME="${BASH_REMATCH[5]}"
  fi
}

hibernate_message() {
  case "$1" in
    validate) echo "Solo comprueba que la base de datos tiene la estructura esperada. No deberia cambiar tablas." ;;
    update) echo "Puede ajustar la estructura de la base de datos si detecta diferencias." ;;
    create) echo "ATENCION: puede borrar y crear de nuevo tablas al arrancar. Usarlo contra datos reales puede eliminar informacion existente." ;;
    create-drop) echo "ATENCION: crea tablas y puede eliminarlas al terminar. Es destructivo para datos reales." ;;
    none) echo "No cambia la estructura de la base de datos." ;;
    *) echo "No se reconoce el modo. Revise hibernate.properties antes de continuar." ;;
  esac
}

origin_description() {
  local mode="$1"
  local tipo="$2"
  local local_key internet_key subdir
  case "$tipo" in
    MAYORES) local_key="app.local.mayores"; internet_key="app.internet.mayores"; subdir="may" ;;
    MENORES) local_key="app.local.menores"; internet_key="app.internet.menores"; subdir="men" ;;
    CONSULTAS) local_key="app.local.cpms"; internet_key="app.internet.cpms"; subdir="cpm" ;;
    ENCARGOS) local_key="app.local.emps"; internet_key="app.internet.emps"; subdir="emp" ;;
    AGREGRADAS) local_key="app.local.agregadas"; internet_key="app.internet.agregadas"; subdir="agr" ;;
  esac
  if [[ "$mode" == "local" ]]; then
    local base file
    base="$(property_value "$APP_PROPERTIES" "app.local.path" | sed 's|\\\\|/|g;s|\\|/|g')"
    file="$(property_value "$APP_PROPERTIES" "$local_key")"
    printf '%s/%s/%s' "${base%/}" "$subdir" "$file"
  else
    property_value "$APP_PROPERTIES" "$internet_key"
  fi
}

confirm_plan() {
  [[ "$DRY_RUN" == true ]] && return 0

  local url user ddl batch pool stats persist_rejected retries retry_delay nifs postal_codes
  url="$(property_value "$DATABASE_PROPERTIES" "jakarta.persistence.jdbc.url")"
  user="$(property_value "$DATABASE_PROPERTIES" "jakarta.persistence.jdbc.user")"
  ddl="$(property_value "$HIBERNATE_PROPERTIES" "hibernate.hbm2ddl.auto")"
  batch="$(property_value "$HIBERNATE_PROPERTIES" "hibernate.jdbc.batch_size")"
  pool="$(property_value "$HIBERNATE_PROPERTIES" "hibernate.hikari.maximumPoolSize")"
  stats="$(property_value "$HIBERNATE_PROPERTIES" "hibernate.generate_statistics")"
  persist_rejected="$(property_value "$APP_PROPERTIES" "app.persistir_historicos_rechazados")"
  retries="$(property_value "$APP_PROPERTIES" "app.http.max_retries")"
  retry_delay="$(property_value "$APP_PROPERTIES" "app.http.retry_delay_ms")"
  nifs="$(property_value "$FILTER_PROPERTIES" "filter.nifs")"; [[ -n "$nifs" ]] || nifs="sin filtro"
  postal_codes="$(property_value "$FILTER_PROPERTIES" "filter.codigosPostales")"; [[ -n "$postal_codes" ]] || postal_codes="sin filtro"
  db_summary "$url"

  echo
  echo "=== Revision antes de importar ==="
  echo "Se ha validado que los parametros indicados son coherentes y que existe el ejecutable correspondiente."
  echo
  echo "Destino de los datos"
  echo "- Se escribira en la base de datos '$DB_NAME' de $DB_MOTOR, servidor $DB_HOST, puerto $DB_PORT."
  echo "- Usuario configurado: $user. La contrasena no se muestra."
  echo "- Fichero de conexion: $DATABASE_PROPERTIES"
  echo
  echo "Configuracion de Hibernate"
  echo "- Modo de preparacion de tablas: $ddl. $(hibernate_message "$ddl")"
  echo "- Trabajo por lotes: hasta $batch operaciones juntas para ir mas rapido."
  echo "- Conexiones simultaneas maximas a la base de datos: $pool."
  echo "- Estadisticas internas activadas: $stats."
  echo "- Opciones Java: ${JAVA_OPTS[*]}. Fichero opcional: $RUNTIME_PROPERTIES"
  echo
  echo "Filtros configurados"
  echo "- NIFs: $nifs"
  echo "- Codigos postales: $postal_codes"
  echo
  echo "Origen e importaciones previstas"
  for i in "${!PLAN_GROUPS[@]}"; do
    echo "- ${PLAN_GROUPS[$i]} / ${PLAN_MODES[$i]} / ${PLAN_TIPOS[$i]}. Origen: $(origin_description "${PLAN_MODES[$i]}" "${PLAN_TIPOS[$i]}")"
    echo "  Ejecutable: ${PLAN_JARS[$i]}"
  done
  echo
  echo "Consecuencias"
  echo "- El script cambiara app.tipo_sindicacion antes de cada bloque de importacion."
  echo "- opendata_con_filtros solo continua si hay NIFs o codigos postales definidos."
  echo "- opendata_sin_filtros no limita por NIF ni por codigo postal."
  echo "- La importacion LOCAL exige que no haya datos previos para ese tipo; si los hay, la aplicacion deberia detenerse."
  echo "- La importacion INTERNET es incremental: compara con lo existente y puede insertar o actualizar registros."
  echo "- Si Hibernate esta en modo 'create' o 'create-drop', hay riesgo de recrear tablas y perder datos existentes."
  echo "- Historicos de rechazados persistidos: $persist_rejected. Si estuviera en true, podria generar muchos registros."
  echo "- Reintentos HTTP para modo internet: $retries; espera base entre reintentos: $retry_delay ms."
  echo
  read -r -p "Para continuar escribe I y pulsa Enter. Cualquier otra respuesta cancela la importacion: " answer
  if [[ "$answer" != "I" ]]; then
    log "Importacion cancelada por el usuario en la confirmacion previa."
    echo "Importacion cancelada. No se ha ejecutado Java."
    exit 0
  fi
  log "Importacion confirmada por el usuario."
}

if [[ "$DRY_RUN" == false && ! -x "$JAVA_EXE" ]]; then
  fail "ERROR: No se encuentra Java ejecutable: $JAVA_EXE"
fi
for file in "$APP_PROPERTIES" "$FILTER_PROPERTIES" "$HIBERNATE_PROPERTIES" "$DATABASE_PROPERTIES"; do
  require_file "$file"
done
resolve_java_opts

build_plan
assert_required_filters_for_filtered_run

log "Grupo solicitado: $GRUPO"
log "Modo solicitado: $MODE"
log "Tipos solicitados: ${TIPOS:-por defecto del grupo}"
log "DryRun: $DRY_RUN"
log "ContinueOnError: $CONTINUE_ON_ERROR"
log "Properties: $PROPERTIES_DIR"
log "Runtime properties: $RUNTIME_PROPERTIES"
log "Java opts: ${JAVA_OPTS[*]}"

echo
echo "=== Importador OpenData ==="
echo "Grupo: $GRUPO | Modo: $MODE | DryRun: $DRY_RUN"
echo "Properties: $PROPERTIES_DIR"
echo "Java opts: ${JAVA_OPTS[*]}"

confirm_plan

failed=0
for i in "${!PLAN_GROUPS[@]}"; do
  group="${PLAN_GROUPS[$i]}"; mode="${PLAN_MODES[$i]}"; tipo="${PLAN_TIPOS[$i]}"; jar="${PLAN_JARS[$i]}"
  log "Preparando importacion: grupo=$group modo=$mode tipo=$tipo jar=$jar"
  echo
  echo "=== Ejecutando $group / $mode / $tipo ==="
  echo "JAR: $jar"
  if [[ "$DRY_RUN" == true ]]; then
    echo "DryRun: se usaria app.tipo_sindicacion=$tipo"
    echo "DryRun: $JAVA_EXE ${JAVA_OPTS[*]} -jar \"$jar\" --configDir=\"$PROPERTIES_DIR\""
    log "DryRun: no se modifica app.properties ni se ejecuta Java."
    continue
  fi

  set_property_value "$APP_PROPERTIES" "app.tipo_sindicacion" "$tipo"
  log "app.tipo_sindicacion actualizado a $tipo"

  set +e
  "$JAVA_EXE" "${JAVA_OPTS[@]}" -jar "$jar" "--configDir=$PROPERTIES_DIR"
  exit_code=$?
  set -e

  if [[ $exit_code -eq 0 ]]; then
    echo "Importacion completada: $group / $mode / $tipo"
    log "Importacion completada: grupo=$group modo=$mode tipo=$tipo"
  else
    echo "ERROR en importacion: grupo=$group modo=$mode tipo=$tipo exitCode=$exit_code" >&2
    log "ERROR en importacion: grupo=$group modo=$mode tipo=$tipo exitCode=$exit_code"
    failed=1
    [[ "$CONTINUE_ON_ERROR" == true ]] || exit "$exit_code"
  fi
done

if [[ $failed -ne 0 ]]; then
  log "Proceso finalizado con errores"
  echo
  echo "=== Proceso finalizado con errores ==="
  echo "Log: $LOG_FILE"
  exit 1
fi

log "Todas las importaciones completadas."
echo
echo "=== Proceso finalizado ==="
echo "Log: $LOG_FILE"
