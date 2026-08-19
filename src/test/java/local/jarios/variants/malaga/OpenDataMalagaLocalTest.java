package local.jarios.variants.malaga;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import local.jarios.core.abstracts.AbstractOpenDataBase;
import local.jarios.core.enums.TipoSindicacion;
import local.jarios.entity.atom.Feed;
import local.jarios.entity.auxiliares.Configuracion;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.Historico;
import local.jarios.entity.auxiliares.Log;
import local.jarios.entity.auxiliares.OrganoContratacion;
import local.jarios.exceptions.MiServiceException;
import local.jarios.properties.exception.PropertiesManagerException;
import local.jarios.repositories.EntrySnapshot;
import local.jarios.services.ImportPersistencePlan;
import local.jarios.services.ServicePrincipal;
import org.junit.jupiter.api.Test;

class OpenDataMalagaLocalTest {

  @Test
  void local_import_rejects_non_empty_database_before_parsing() throws Exception {
    CountingServicePrincipal servicePrincipal = new CountingServicePrincipal(1L);
    OpenDataMalagaLocal openData = new OpenDataMalagaLocal();
    injectServicePrincipal(openData, servicePrincipal);

    assertThatThrownBy(() -> invokeValidateEmptyDatabase(openData, TipoSindicacion.MAYORES))
        .isInstanceOf(MiServiceException.class)
        .hasMessageContaining("La importacion LOCAL requiere una base de datos vacia")
        .hasMessageContaining("MAYORES")
        .hasMessageContaining("1 entries existentes");
  }

  @Test
  void filtered_import_requires_nifs_or_postal_codes() {
    OpenDataMalagaLocal openData = new OpenDataMalagaLocal();

    assertThatThrownBy(
            () ->
                invokeValidateRequiredTargetFilter(
                    openData, new local.jarios.core.pipeline.context.OpenDataExecutionContext()))
        .isInstanceOf(PropertiesManagerException.class)
        .hasMessageContaining("requiere informar filter.nifs o filter.codigosPostales");
  }

  @Test
  void filtered_import_rejects_filters_without_effective_targets() {
    OpenDataMalagaLocal openData = new OpenDataMalagaLocal();
    local.jarios.core.pipeline.context.OpenDataExecutionContext context =
        new local.jarios.core.pipeline.context.OpenDataExecutionContext();
    context.setFiltroCodigosPostales("29");

    assertThatThrownBy(() -> invokeValidateRequiredTargetFilter(openData, context))
        .isInstanceOf(PropertiesManagerException.class)
        .hasMessageContaining("no ha encontrado ningun organo/NIF efectivo");
  }

  @Test
  void filtered_import_accepts_effective_nif_filter() throws Exception {
    OpenDataMalagaLocal openData = new OpenDataMalagaLocal();
    local.jarios.core.pipeline.context.OpenDataExecutionContext context =
        new local.jarios.core.pipeline.context.OpenDataExecutionContext();
    context.setFiltroNifs("P2900000G");
    context.setConjuntoNifsEnFiltro(Set.of("P2900000G"));

    invokeValidateRequiredTargetFilter(openData, context);
  }

  private static void injectServicePrincipal(
      AbstractOpenDataBase openData, ServicePrincipal servicePrincipal) throws Exception {
    Field field = AbstractOpenDataBase.class.getDeclaredField("servicePrincipal");
    field.setAccessible(true);
    field.set(openData, servicePrincipal);
  }

  private static void invokeValidateEmptyDatabase(
      OpenDataMalagaLocal openData, TipoSindicacion tipoSindicacion) throws Exception {
    var method =
        OpenDataMalagaLocal.class.getDeclaredMethod(
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

  private static void invokeValidateRequiredTargetFilter(
      OpenDataMalagaLocal openData,
      local.jarios.core.pipeline.context.OpenDataExecutionContext context)
      throws Exception {
    var method =
        AbstractOpenDataMalaga.class.getDeclaredMethod(
            "validateRequiredTargetFilter",
            local.jarios.core.pipeline.context.OpenDataExecutionContext.class);
    method.setAccessible(true);
    try {
      method.invoke(openData, context);
    } catch (java.lang.reflect.InvocationTargetException ex) {
      Throwable cause = ex.getCause();
      if (cause instanceof Exception exception) {
        throw exception;
      }
      throw ex;
    }
  }

  private static final class CountingServicePrincipal implements ServicePrincipal {
    private final long entriesCount;

    private CountingServicePrincipal(long entriesCount) {
      this.entriesCount = entriesCount;
    }

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
    public void persistirImportacion(ImportPersistencePlan plan) throws MiServiceException {}

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
