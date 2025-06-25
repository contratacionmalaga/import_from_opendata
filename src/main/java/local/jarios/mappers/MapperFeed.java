package local.jarios.mappers;

import local.jarios.entity.Log;
import local.jarios.entity.atom.Feed;
import local.jarios.helpers.ComunHelper;
import local.jarios.mappers.auxiliares.LinkInfo;
import local.jarios.common.util.Constantes;
import lombok.extern.slf4j.Slf4j;
import org.w3._2005.atom.FeedType;

import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice
 * Author: juan
 * Date: 11/04/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperFeed {

    private MapperFeed() { }

    public static Feed getFeed(Log miLog, FeedType feedType) {
        var feed = new Feed();
        feed.setMiLog(miLog);

        Optional.ofNullable(feedType.getUpdated())
                .map(updated -> updated.getValue().toGregorianCalendar().toZonedDateTime().toLocalDateTime())
                .ifPresent(feed::setUpdated);

        var linkInfo = LinkInfo.getLinkInfoFromFeedType(feedType);

        feed.setLinkNext(limitarLink(linkInfo.getLinkNext()));
        feed.setLinkFirst(limitarLink(linkInfo.getLinkFirst()));
        feed.setLinkPrev(limitarLink(linkInfo.getLinkPrev()));
        feed.setLinkSelf(limitarLink(linkInfo.getLinkSelf()));

        feed.setListEntry(MapperEntry.getListEntryFromEntryType(feed, feedType));

        return feed;
    }

    private static String limitarLink(String link) {

        return ComunHelper.limitarRegistro(link, Constantes.TAMANO_MAXIMO_CAMPO_250);
    }
}
