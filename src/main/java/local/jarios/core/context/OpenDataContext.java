package local.jarios.core.context;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import local.jarios.entity.atom.DeletedEntry;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Configuracion;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.Historico;
import local.jarios.entity.auxiliares.Log;
import local.jarios.entity.auxiliares.OrganoContratacion;

/**
 * Contexto compartido para la ejecución del proceso OpenData.
 *
 * <p>Esta primera versión evita dependencias con enums y clases de estrategia para que pueda
 * introducirse sin afectar todavía a la jerarquía actual.
 */
public class OpenDataContext {

  private String appName;
  private String appVersion;
  private String configDir;

  private String duracionParseo;
  private String duracionPersistencia;

  private Entry newestEntry;

  private Log log;
  private Configuracion configuracion;
  private Estadistica estadistica;

  private final HashMap<String, Entry> mapEntriesFromAtoms = new HashMap<>();
  private final HashMap<String, Entry> mapEntriesFromBaseDatos = new HashMap<>();
  private final HashMap<String, Entry> mapEntriesToBaseDatos = new HashMap<>();
  private final HashMap<String, DeletedEntry> mapDeletedEntriesFromAtoms = new HashMap<>();

  private final HashSet<Feed> setFeedsFromAtoms = new HashSet<>();
  private final ArrayList<Historico> listHistoricos = new ArrayList<>();
  private final ArrayList<String> listNifs = new ArrayList<>();
  private final ArrayList<OrganoContratacion> listOrganosContratacionEnExcel = new ArrayList<>();
  private final ArrayList<OrganoContratacion> listOrganoContratacionFiltro = new ArrayList<>();

  private LocalDateTime fechaInicio;
  private LocalDateTime fechaFin;

  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public String getAppVersion() {
    return appVersion;
  }

  public void setAppVersion(String appVersion) {
    this.appVersion = appVersion;
  }

  public String getConfigDir() {
    return configDir;
  }

  public void setConfigDir(String configDir) {
    this.configDir = configDir;
  }

  public String getDuracionParseo() {
    return duracionParseo;
  }

  public void setDuracionParseo(String duracionParseo) {
    this.duracionParseo = duracionParseo;
  }

  public String getDuracionPersistencia() {
    return duracionPersistencia;
  }

  public void setDuracionPersistencia(String duracionPersistencia) {
    this.duracionPersistencia = duracionPersistencia;
  }

  public Entry getNewestEntry() {
    return newestEntry;
  }

  public void setNewestEntry(Entry newestEntry) {
    this.newestEntry = newestEntry;
  }

  public Log getLog() {
    return log;
  }

  public void setLog(Log log) {
    this.log = log;
  }

  public Configuracion getConfiguracion() {
    return configuracion;
  }

  public void setConfiguracion(Configuracion configuracion) {
    this.configuracion = configuracion;
  }

  public Estadistica getEstadistica() {
    return estadistica;
  }

  public void setEstadistica(Estadistica estadistica) {
    this.estadistica = estadistica;
  }

  public HashMap<String, Entry> getMapEntriesFromAtoms() {
    return mapEntriesFromAtoms;
  }

  public HashMap<String, Entry> getMapEntriesFromBaseDatos() {
    return mapEntriesFromBaseDatos;
  }

  public HashMap<String, Entry> getMapEntriesToBaseDatos() {
    return mapEntriesToBaseDatos;
  }

  public HashMap<String, DeletedEntry> getMapDeletedEntriesFromAtoms() {
    return mapDeletedEntriesFromAtoms;
  }

  public HashSet<Feed> getSetFeedsFromAtoms() {
    return setFeedsFromAtoms;
  }

  public ArrayList<Historico> getListHistoricos() {
    return listHistoricos;
  }

  public ArrayList<String> getListNifs() {
    return listNifs;
  }

  public ArrayList<OrganoContratacion> getListOrganosContratacionEnExcel() {
    return listOrganosContratacionEnExcel;
  }

  public ArrayList<OrganoContratacion> getListOrganoContratacionFiltro() {
    return listOrganoContratacionFiltro;
  }

  public LocalDateTime getFechaInicio() {
    return fechaInicio;
  }

  public void setFechaInicio(LocalDateTime fechaInicio) {
    this.fechaInicio = fechaInicio;
  }

  public LocalDateTime getFechaFin() {
    return fechaFin;
  }

  public void setFechaFin(LocalDateTime fechaFin) {
    this.fechaFin = fechaFin;
  }

  public void clearExecutionData() {
    mapEntriesFromAtoms.clear();
    mapEntriesFromBaseDatos.clear();
    mapEntriesToBaseDatos.clear();
    mapDeletedEntriesFromAtoms.clear();
    setFeedsFromAtoms.clear();
    listHistoricos.clear();
  }

  public int getNumeroFeeds() {
    return setFeedsFromAtoms.size();
  }

  public int getNumeroEntriesFromAtoms() {
    return mapEntriesFromAtoms.size();
  }

  public int getNumeroEntriesToPersist() {
    return mapEntriesToBaseDatos.size();
  }

  public boolean hasEntriesToPersist() {
    return !mapEntriesToBaseDatos.isEmpty();
  }
}
