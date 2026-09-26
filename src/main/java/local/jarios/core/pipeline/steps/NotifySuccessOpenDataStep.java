package local.jarios.core.pipeline.steps;

import local.jarios.core.abstracts.AbstractOpenDataBase;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;

/** Envío de notificación final de éxito. */
public class NotifySuccessOpenDataStep extends AbstractOpenDataStep {

  public NotifySuccessOpenDataStep(AbstractOpenDataBase openData) {
    super("notify-success-open-data", openData);
  }

  @Override
  protected void doExecute(OpenDataExecutionContext context) {
    try {
      getOpenData().sendSuccessEmail(context);
    } catch (Exception ex) {
      getOpenData().handleSuccessNotificationFailure(context, ex);
    }
  }
}
