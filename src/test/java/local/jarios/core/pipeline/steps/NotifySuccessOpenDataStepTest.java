package local.jarios.core.pipeline.steps;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Map;
import local.jarios.core.abstracts.AbstractOpenDataBase;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.email.exception.EmailException;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.auxiliares.Estadistica;
import local.jarios.exceptions.MiParseException;
import local.jarios.exceptions.MiServiceException;
import org.junit.jupiter.api.Test;

class NotifySuccessOpenDataStepTest {

  @Test
  void does_not_fail_a_confirmed_import_when_the_success_email_fails() {
    NotificationFailingOpenData openData = new NotificationFailingOpenData();
    OpenDataExecutionContext context = new OpenDataExecutionContext();

    assertThatCode(() -> new NotifySuccessOpenDataStep(openData).execute(context))
        .doesNotThrowAnyException();
    org.assertj.core.api.Assertions.assertThat(openData.notificationFailure).isNotNull();
  }

  private static final class NotificationFailingOpenData extends AbstractOpenDataBase {
    private Throwable notificationFailure;

    @Override
    public void sendSuccessEmail(OpenDataExecutionContext context) throws EmailException {
      throw new EmailException("fallo SMTP simulado");
    }

    @Override
    public void handleSuccessNotificationFailure(OpenDataExecutionContext context, Throwable ex) {
      notificationFailure = ex;
    }

    @Override
    protected void initVariantContext(OpenDataExecutionContext context) {}

    @Override
    protected String parsearAtomsFeeds(OpenDataExecutionContext context) throws MiParseException {
      return "0ms";
    }

    @Override
    public Map<String, Entry> resolveEntriesToPersist(OpenDataExecutionContext context) {
      return Map.of();
    }

    @Override
    public void previewPersistData(OpenDataExecutionContext context) {}

    @Override
    public Estadistica persistAll(OpenDataExecutionContext context) throws MiServiceException {
      return null;
    }

    @Override
    protected String getDefaultAppName() {
      return "test";
    }
  }
}
