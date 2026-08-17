package local.jarios.variants.pliegos;

import static org.assertj.core.api.Assertions.assertThat;

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
import local.jarios.entity.auxiliares.Historico;
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
  void persist_all_passes_context_historicos_to_import_plan() throws Exception {
    CapturingServicePrincipal servicePrincipal = new CapturingServicePrincipal();
    TestOpenDataPliegos openData = new TestOpenDataPliegos();
    injectServicePrincipal(openData, servicePrincipal);

    OpenDataExecutionContext context = new OpenDataExecutionContext();
    context.setLugarImportacion(LugarImportacion.INTERNET);
    context.setTipoSindicacion(TipoSindicacion.MAYORES);
    context.setDuracionParseo("0s");
    context.setMapEntriesToBaseDatos(Map.of("entry-1", new Entry()));
    context.addHistorico(historico("entry-1", EntryOpcion.ACTUALIZAR));
    context.getConjuntoFeedsFromAtoms().add(feed("feed-1"));

    openData.persistAll(context);

    assertThat(servicePrincipal.plan).isNotNull();
    assertThat(servicePrincipal.plan.historicoList()).isSameAs(context.getListHistoricos());
    assertThat(servicePrincipal.plan.historicoList())
        .extracting(Historico::getEntryOpcion)
        .containsExactly(EntryOpcion.ACTUALIZAR);
  }

  private static void injectServicePrincipal(
      AbstractOpenDataBase openData, ServicePrincipal servicePrincipal) throws Exception {
    Field field = AbstractOpenDataBase.class.getDeclaredField("servicePrincipal");
    field.setAccessible(true);
    field.set(openData, servicePrincipal);
  }

  private static Historico historico(String entryId, EntryOpcion opcion) {
    Historico historico = new Historico();
    historico.setEntryId(entryId);
    historico.setEntryOpcion(opcion);
    historico.setEntryMotivo("test");
    return historico;
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
    public void persistirListaHistoricos(Log miLog, List<Historico> listHistorico)
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
      return 0;
    }
  }
}
