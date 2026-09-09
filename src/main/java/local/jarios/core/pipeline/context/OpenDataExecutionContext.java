package local.jarios.core.pipeline.context;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import local.jarios.core.enums.LugarImportacion;
import local.jarios.core.enums.TipoSindicacion;
import local.jarios.entity.atom.DeletedEntry;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.HistoricoEntry;
import local.jarios.entity.auxiliares.OrganoContratacion;
import local.jarios.enums.EntryOpcion;
import local.jarios.helpers.HistoricoTotales;
import local.jarios.imports.ImportResult;
import local.jarios.repositories.EntrySnapshot;
import lombok.Getter;
import lombok.Setter;

@Getter
public class OpenDataExecutionContext {

  @Setter @Getter private ImportResult<OrganoContratacion> excelResult;
  @Setter @Getter private HistoricoTotales historicoTotales;
  @Setter private String configDir;
  @Setter private String duracionParseo;
  @Setter private LugarImportacion lugarImportacion;
  @Setter private TipoSindicacion tipoSindicacion;
  @Setter private Entry newestEntry;

  // //////////////////
  // FILTROS
  // //////////////////
  @Getter @Setter
  private List<OrganoContratacion> listOrganoContratacionEnExcel = new ArrayList<>();

  @Getter @Setter private Set<String> conjuntoNifsEnFiltro = new HashSet<>();
  @Getter @Setter private List<OrganoContratacion> listOrganoContratacionFiltro = new ArrayList<>();
  @Setter @Getter private boolean filtrosCargados;
  @Setter private LocalDateTime filtroFechaInicial;
  @Setter private LocalDateTime filtroFechaFinal;
  @Setter private String filtroCodigosPostales;
  @Setter private String filtroNifs;
  @Setter @Getter private boolean aplicarFiltros;
  @Setter @Getter private boolean persistirHistoricosRechazados;
  @Getter private long historicosRechazadosOmitidos;

  // //////////////////
  // EXCEL
  // //////////////////
  @Setter private Date fechaGeneracionExcel;

  @Setter @Getter private boolean compararConExistentes;
  @Setter private Estadistica estadistica;
  @Setter private Map<String, Entry> mapEntriesToBaseDatos = new HashMap<>();
  @Setter private Map<Feed, List<Entry>> mapFeedsToBaseDatos = new HashMap<>();

  private final Map<String, Entry> mapEntriesFromAtoms = new HashMap<>();
  private final Set<Feed> conjuntoFeedsFromAtoms = new HashSet<>();
  private final Map<String, DeletedEntry> mapDeletedEntriesFromAtoms = new HashMap<>();
  private final Map<String, EntrySnapshot> mapEntrySnapshotsFromBaseDatos = new HashMap<>();
  private final List<HistoricoEntry> listHistoricos = new ArrayList<>();

  public void addHistorico(HistoricoEntry historicoEntry) {
    if (historicoEntry == null) {
      return;
    }

    if (historicoEntry.getEntryOpcion() == EntryOpcion.RECHAZAR && !persistirHistoricosRechazados) {
      historicosRechazadosOmitidos++;
      return;
    }

    listHistoricos.add(historicoEntry);
  }

  public long getTotalHistoricos() {
    return listHistoricos.size() + historicosRechazadosOmitidos;
  }

  @Getter @Setter private List<String> listNifs = new ArrayList<>();

  public OpenDataExecutionContext() {}

  public OpenDataExecutionContext(String configDir) {
    this.configDir = configDir;
  }
}
