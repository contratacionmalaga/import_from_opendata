package local.jarios.entity.auxiliares;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.Column;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import local.jarios.core.enums.LugarImportacion;
import local.jarios.core.enums.TipoSindicacion;
import org.junit.jupiter.api.Test;

class LogCreatedAtTest {

  @Test
  void assigns_created_at_when_the_log_is_persisted() {
    Log log = new Log(LugarImportacion.LOCAL, TipoSindicacion.MAYORES);

    log.onCreate();

    assertThat(log.getCreatedAt()).isNotNull();
  }

  @Test
  void keeps_an_explicit_created_at_value() {
    LocalDateTime expected = LocalDateTime.of(2026, 9, 27, 10, 15);
    Log log = new Log(LugarImportacion.LOCAL, TipoSindicacion.MAYORES);
    log.setCreatedAt(expected);

    log.onCreate();

    assertThat(log.getCreatedAt()).isEqualTo(expected);
  }

  @Test
  void maps_created_at_as_an_immutable_required_column() throws NoSuchFieldException {
    Field field = Log.class.getDeclaredField("createdAt");
    Column column = field.getAnnotation(Column.class);

    assertThat(column).isNotNull();
    assertThat(column.name()).isEqualTo("created_at");
    assertThat(column.nullable()).isFalse();
    assertThat(column.updatable()).isFalse();
  }
}
