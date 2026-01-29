package local.jarios.mappers.atom;

import local.jarios.common.util.Constantes;
import local.jarios.entity.atom.DeletedEntry;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.helpers.ComunHelper;
import local.jarios.helpers.LocalDateTimeHelper;
import local.jarios.mappers.auxiliares.LinkInfo;
import lombok.extern.slf4j.Slf4j;
import org.w3._2005.atom.FeedType;

import java.util.List;

/**
 * Description: Subtipos del modelo Codice Author: juan Date: 11/04/2024 Team: Juan Antonio
 */
@Slf4j
public final class MapperFeed {

  private MapperFeed() {
  }

  public static Feed getFeed(FeedType feedType) {

    // Creación del objeto
    var feed = new Feed();

    // Asignación de valores
    feed.setUpdated(
        LocalDateTimeHelper.getLocalDateTimeFromXmlGregorianCalendar(
            feedType.getUpdated().getValue()
        )
    );

    // Creación del objeto LinkInfo y asignación de valores
    LinkInfo linkInfo = LinkInfo.getLinkInfoFromFeedType(feedType);
    feed.setLinkNext(limitarLink(linkInfo.getLinkNext()));
    feed.setLinkFirst(limitarLink(linkInfo.getLinkFirst()));
    feed.setLinkPrev(limitarLink(linkInfo.getLinkPrev()));
    feed.setLinkSelf(limitarLink(linkInfo.getLinkSelf()));

    // Asignación de la lista de Entry
    List<Entry> entryList = MapperEntry.getListEntryFromFeedType(feed, feedType);
    feed.setEntryList(entryList);

    // Asignación de la lista de DeletedEntry
    List<DeletedEntry> deletedEntryList = MapperDeletedEntry.getListDeletedEntryFromFeedType(feed,
                                                                                             feedType);
    feed.setDeletedEntryList(deletedEntryList);

    return feed;
  }

  private static String limitarLink(String link) {

    return ComunHelper.limitarRegistro(link, Constantes.TAMANO_MAXIMO_CAMPO_500);
  }
}
