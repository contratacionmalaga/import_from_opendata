package local.jarios.mappers;

import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.helpers.ComunHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import local.jarios.common.util.Constantes;
import lombok.extern.slf4j.Slf4j;
import org.w3._2005.atom.EntryType;
import org.w3._2005.atom.FeedType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Description: Subtipos del modelo Codice
 * Author: juan
 * Date: 11/04/2024
 * Team: Juan Antonio
 */
@Slf4j
public final class MapperEntry {

    private MapperEntry() { }

    public static List<Entry> getListEntryFromEntryType(Feed feed, FeedType feedType) {

        // Creo la lista para no devolver NULL
        List<Entry> listEntry = new ArrayList<>();

        // feedType.getEntry() nunca es NULL puede ser VACÍA
        for (EntryType entryType : feedType.getEntry()) {

            // Añado coda entry a la lista que voy a devolver
            listEntry.add(getEntryFromEntryType(feed, entryType));
        }

        // Valor devuelto
        return listEntry;
    }

    //
    private static Entry getEntryFromEntryType(
            Feed feed, EntryType entryType) {

        //
        var entry = new Entry();
        entry.setFeed(feed);

        Optional.ofNullable(entryType.getId())
                .map(id -> ComunHelper.limitarRegistro(id.getValue(), Constantes.TAMANO_MAXIMO_CAMPO_250))
                .ifPresent(entry::setIdEntry);

        entry.setLink(ComunHelper.limitarRegistro(
                MapperStringFromList.getStringFromListLinkType(entryType.getLink()),
                Constantes.TAMANO_MAXIMO_CAMPO_250));

        Optional.ofNullable(entryType.getTitle())
                .map(title -> MapperStringFromList.getStringFromListObject(title.getContent()))
                .map(s -> ComunHelper.limitarRegistro(s, Constantes.TAMANO_MAXIMO_CAMPO_2500))
                .ifPresent(entry::setTitle);

        Optional.ofNullable(entryType.getSummary())
                .map(summary -> MapperStringFromList.getStringFromListObject(summary.getContent()))
                .map(s -> ComunHelper.limitarRegistro(s, Constantes.TAMANO_MAXIMO_CAMPO_2500))
                .ifPresent(entry::setSummary);

        Optional.ofNullable(entryType.getUpdated())
                .map(updated -> updated.getValue().toGregorianCalendar().toZonedDateTime().toLocalDateTime())
                .ifPresent(entry::setUpdated);

        //
        log.debug(entry.toString());

        entry.setListContractFolderStatus(
                MapperContractFolderStatus.getListContractFolderStatusFromListType(entry, entryType));

        return entry;
    }
}
