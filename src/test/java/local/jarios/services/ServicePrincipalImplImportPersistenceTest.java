package local.jarios.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import local.jarios.core.enums.LugarImportacion;
import local.jarios.core.enums.TipoSindicacion;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.entity.auxiliares.Log;
import local.jarios.repositories.Repository;
import org.junit.jupiter.api.Test;

class ServicePrincipalImplImportPersistenceTest {

  @Test
  void delegates_complete_import_plan_to_repository_once() throws Exception {
    AtomicReference<ImportPersistencePlan> capturedPlan = new AtomicReference<>();
    Repository repository =
        (Repository)
            Proxy.newProxyInstance(
                Repository.class.getClassLoader(),
                new Class<?>[] {Repository.class},
                (proxy, method, args) -> {
                  if ("persistirImportacion".equals(method.getName())) {
                    capturedPlan.set((ImportPersistencePlan) args[0]);
                  }
                  return null;
                });

    ServicePrincipalImpl service = new ServicePrincipalImpl(repository);
    Log log = new Log(LugarImportacion.INTERNET, TipoSindicacion.MAYORES);
    Estadistica estadistica = new Estadistica(log);
    ImportPersistencePlan plan =
        new ImportPersistencePlan(
            log, null, List.of(), List.of(), Set.of(), List.of(), estadistica);

    service.persistirImportacion(plan);

    assertThat(capturedPlan).hasValue(plan);
  }

  @Test
  void delegates_entry_count_to_repository() throws Exception {
    Repository repository =
        (Repository)
            Proxy.newProxyInstance(
                Repository.class.getClassLoader(),
                new Class<?>[] {Repository.class},
                (proxy, method, args) -> {
                  if ("countEntries".equals(method.getName())) {
                    assertThat(args).containsExactly(TipoSindicacion.MAYORES);
                    return 7L;
                  }
                  return null;
                });

    ServicePrincipalImpl service = new ServicePrincipalImpl(repository);

    assertThat(service.countEntries(TipoSindicacion.MAYORES)).isEqualTo(7L);
  }
}
