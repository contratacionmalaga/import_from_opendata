package local.jarios.core.pipeline.steps;

import java.util.List;
import java.util.Map;
import local.jarios.core.abstracts.AbstractOpenDataBase;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;

/** Resuelve los entries a persistir y los agrupa por feed. */
public class ResolveEntriesOpenDataStep extends AbstractOpenDataStep {

  public ResolveEntriesOpenDataStep(AbstractOpenDataBase openData) {
    super("resolve-entries-open-data", openData);
  }

  @Override
  protected void doExecute(OpenDataExecutionContext context) {
    Map<String, Entry> mapEntriesToBaseDatos = getOpenData().resolveEntriesToPersist(context);

    Map<Feed, List<Entry>> mapFeedsToBaseDatos =
        getOpenData().groupEntriesByFeed(mapEntriesToBaseDatos);

    context.setMapEntriesToBaseDatos(mapEntriesToBaseDatos);
    context.setMapFeedsToBaseDatos(mapFeedsToBaseDatos);
  }
}
