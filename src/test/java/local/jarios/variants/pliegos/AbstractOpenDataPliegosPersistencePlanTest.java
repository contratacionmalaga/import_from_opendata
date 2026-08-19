package local.jarios.variants.pliegos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import local.jarios.core.abstracts.AbstractOpenDataBase;
import local.jarios.core.enums.LugarImportacion;
import local.jarios.core.enums.TipoSindicacion;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Configuracion;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.HistoricoEntry;
import local.jarios.entity.auxiliares.Log;
import local.jarios.entity.auxiliares.OrganoContratacion;
import local.jarios.enums.EntryOpcion;
import local.jarios.exceptions.MiParseException;
import local.jarios.exceptions.MiServiceException;
import local.jarios.repositories.EntrySnapshot;
import local.jarios.services.ImportPersistencePlan;
import local.jarios.services.ServicePrincipal;
import org.junit.jupiter.api.Test;

class AbstractOpenDataPliegosPersistencePlanTest {

  @Test
  void persist_all_uses_historicos_only_for_replacements_without_persisting_them()
      throws Exception {
    CapturingServicePrincipal servicePrincipal = new CapturingServicePrincipal();
    TestOpenDataPliegos openData = new TestOpenDataPliegos();
    injectServicePrincipal(openData, servicePrincipal);

    OpenDataExecutionContext context = new OpenDataExecutionContext();
    context.setLugarImportacion(LugarImportacion.INTERNET);
    context.setTipoSindicacion(TipoSindicacion.MAYORES);
    context.setDuracionParseo("0s");
    context.setMapEntriesToBaseDatos(Map.of("entry-1", new Entry()));
    context.addHistorico(historicoEntry("entry-1", EntryOpcion.ACTUALIZAR));
    context.getConjuntoFeedsFromAtoms().add(feed("feed-1"));

    openData.persistAll(context);

    assertThat(servicePrincipal.plan).isNotNull();
    assertThat(servicePrincipal.plan.historicoList()).isEmpty();
    assertThat(servicePrincipal.plan.replacementEntryIds()).containsExactly("entry-1");
    assertThat(context.getListHistoricos()).isEmpty();
  }

  @Test
  void local_import_rejects_non_empty_database_before_parsing() throws Exception {
    CapturingServicePrincipal servicePrincipal = new CapturingServicePrincipal();
    servicePrincipal.entriesCount = 1L;
    OpenDataPliegosLocal openData = new OpenDataPliegosLocal();
    injectServicePrincipal(openData, servicePrincipal);

    assertThatThrownBy(() -> invokeValidateEmptyDatabase(openData, TipoSindicacion.MAYORES))
        .isInstanceOf(MiServiceException.class)
        .hasMessageContaining("La importacion LOCAL requiere una base de datos vacia")
        .hasMessageContaining("MAYORES")
        .hasMessageContaining("1 entries existentes");
  }

  private static void injectServicePrincipal(
      AbstractOpenDataBase openData, ServicePrincipal servicePrincipal) throws Exception {
    Field field = AbstractOpenDataBase.class.getDeclaredField("servicePrincipal");
    field.setAccessible(true);
    field.set(openData, servicePrincipal);
  }

  private static void invokeValidateEmptyDatabase(
      OpenDataPliegosLocal openData, TipoSindicacion tipoSindicacion) throws Exception {
    var method =
        OpenDataPliegosLocal.class.getDeclaredMethod(
            "validarBaseDatosVaciaParaCargaLocal", TipoSindicacion.class);
    method.setAccessible(true);
    try {
      method.invoke(openData, tipoSindicacion);
    } catch (java.lang.reflect.InvocationTargetException ex) {
      Throwable cause = ex.getCause();
      if (cause instanceof Exception exception) {
        throw exception;
      }
      throw ex;
    }
  }

  private static HistoricoEntry historicoEntry(String entryId, EntryOpcion opcion) {
    HistoricoEntry historicoEntry = new HistoricoEntry();
    historicoEntry.setEntryId(entryId);
    historicoEntry.setEntryOpcion(opcion);
    historicoEntry.setEntryMotivo("test");
    return historicoEntry;
  }

  private static Feed feed(String linkSelf) {
    Feed feed = new Feed();
    feed.setLinkSelf(linkSelf);
    return feed;
  }

  private static final class TestOpenDataPliegos extends AbstractOpenDataPliegos {

    @Override
    protected void initVariantContext(OpenDataExecutionContext context) {
      // No-op.
    }

    @Override
    protected String parsearAtomsFeeds(OpenDataExecutionContext context) throws MiParseException {
      return "0s";
    }

    @Override
    public Map<String, Entry> resolveEntriesToPersist(OpenDataExecutionContext context) {
      return context.getMapEntriesFromAtoms();
    }
  }

  private static final class CapturingServicePrincipal implements ServicePrincipal {
    private ImportPersistencePlan plan;
    private long entriesCount;

    @Override
    public void persistirLog(Log miLog) throws MiServiceException {}

    @Override
    public void persistirConfiguracion(Configuracion configuracion) throws MiServiceException {}

    @Override
    public void persistirListaOcFiltro(Log miLog, List<OrganoContratacion> organoContratacionList)
        throws MiServiceException {}

    @Override
    public void persistirListaNifFiltro(Log miLog, List<String> nifList)
        throws MiServiceException {}

    @Override
    public void persistirListaHistoricos(Log miLog, List<HistoricoEntry> listHistorico)
        throws MiServiceException {}

    @Override
    public void persistirEstadistica(Estadistica estadistica) throws MiServiceException {}

    @Override
    public void persistirSetFeeds(Log miLog, Set<Feed> feedSet) throws MiServiceException {}

    @Override
    public void persistirImportacion(ImportPersistencePlan plan) throws MiServiceException {
      this.plan = plan;
    }

    @Override
    public Map<String, EntrySnapshot> getEntrySnapshots(TipoSindicacion tipoSindicacion)
        throws MiServiceException {
      return Map.of();
    }

    @Override
    public long countEntries(TipoSindicacion tipoSindicacion) throws MiServiceException {
      return entriesCount;
    }
  }
}
