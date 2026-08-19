package local.jarios.core.pipeline.steps;

import local.jarios.core.abstracts.AbstractOpenDataBase;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.helpers.DeletedEntryScopeResolver;

/** Ajusta los DeletedEntry al ambito real de la importacion filtrada. */
public class ResolveDeletedEntriesOpenDataStep extends AbstractOpenDataStep {

  public ResolveDeletedEntriesOpenDataStep(AbstractOpenDataBase openData) {
    super("resolve-deleted-entries-open-data", openData);
  }

  @Override
  protected void doExecute(OpenDataExecutionContext context) {
    DeletedEntryScopeResolver.retainDeletedEntriesInImportScope(context);
  }
}
