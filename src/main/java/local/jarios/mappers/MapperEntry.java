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
 * Mapper para convertir objetos del modelo Atom {@link FeedType} y {@link EntryType}
 * a las entidades internas {@link Feed} y {@link Entry} usadas en el proyecto.
 * <p>
 * Proporciona métodos para transformar listas de entradas Atom en listas de
 * objetos {@link Entry} vinculados a un {@link Feed} dado.
 * <p>
 * Se aplican límites de tamaño a los campos para asegurar compatibilidad con
 * la base de datos y evitar errores por datos demasiado largos.
 *
 * @author juan
 */
@Slf4j
public final class MapperEntry {

    private MapperEntry() {
        // Constructor privado para evitar instanciación
    }

    /**
     * Convierte un objeto {@link FeedType} y su lista de {@link EntryType}
     * en una lista de objetos {@link Entry} vinculados al {@link Feed} proporcionado.
     *
     * @param feed    Entidad {@link Feed} a la que se asociarán las entradas.
     * @param feedType Objeto {@link FeedType} que contiene las entradas Atom.
     * @return Lista de objetos {@link Entry} convertidos desde {@link EntryType}.
     */
    public static List<Entry> getListEntryFromEntryType(Feed feed, FeedType feedType) {
        List<Entry> listEntry = new ArrayList<>();

        for (EntryType entryType : feedType.getEntry()) {
            listEntry.add(getEntryFromEntryType(feed, entryType));
        }

        return listEntry;
    }

    /**
     * Convierte un objeto {@link EntryType} en una entidad {@link Entry}
     * vinculada a un {@link Feed} dado.
     *
     * @param feed      Entidad {@link Feed} asociada.
     * @param entryType Objeto {@link EntryType} a convertir.
     * @return Objeto {@link Entry} construido a partir de {@code entryType}.
     */
    private static Entry getEntryFromEntryType(Feed feed, EntryType entryType) {
        var entry = new Entry();
        entry.setFeed(feed);

        Optional.ofNullable(entryType.getId())
                .map(id -> ComunHelper.limitarRegistro(id.getValue(), Constantes.TAMANO_MAXIMO_CAMPO_500))
                .ifPresent(entry::setIdEntry);

        entry.setLink(ComunHelper.limitarRegistro(
                MapperStringFromList.getStringFromListLinkType(entryType.getLink()),
                Constantes.TAMANO_MAXIMO_CAMPO_500));

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

        entry.setListContractFolderStatus(
                MapperContractFolderStatus.getListContractFolderStatusFromListType(entry, entryType));

        return entry;
    }
}
