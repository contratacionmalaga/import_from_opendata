package local.jarios.mappers.atom;

import local.jarios.common.util.Constantes;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.helpers.ComunHelper;
import local.jarios.helpers.LocalDateTimeHelper;
import local.jarios.mappers.auxiliares.MapperStringFromList;
import local.jarios.mappers.codice.MapperContractFolderStatus;
import org.w3._2005.atom.EntryType;
import org.w3._2005.atom.FeedType;

import java.util.List;
import java.util.Objects;

/**
 * Mapper para convertir objetos del modelo Atom {@link FeedType} y {@link EntryType} a las
 * entidades internas {@link Feed} y {@link Entry} usadas en el proyecto.
 * <p>
 * Proporciona métodos para transformar listas de entradas Atom en listas de objetos {@link Entry}
 * vinculados a un {@link Feed} dado.
 * <p>
 * Se aplican límites de tamaño a los campos para asegurar compatibilidad con la base de datos y
 * evitar errores por datos demasiado largos.
 *
 * @author juan
 */
public final class MapperEntry {

  /**
   * Constructor privado
   */
  private MapperEntry() {
    // Creación del constructor
  }

  /**
   * Convierte un objeto {@link FeedType} y su lista de {@link EntryType} en una lista de objetos
   * {@link Entry} vinculados al {@link Feed} proporcionado.
   *
   * @param feed     Entidad {@link Feed} a la que se asociarán las entradas.
   * @param feedType Objeto {@link FeedType} que contiene las entradas Atom.
   * @return Lista de objetos {@link Entry} convertidos desde {@link EntryType}.
   */
  public static List<Entry> getListEntryFromFeedType(Feed feed, FeedType feedType) {

    return feedType.getEntry()
        .stream()
        .map(entryType -> getEntryFromFeedType(feed, entryType))
        .toList();
  }

  /**
   * Convierte un objeto {@link EntryType} en una entidad {@link Entry} vinculada a un {@link Feed}
   * dado.
   *
   * @param feed      Entidad {@link Feed} asociada.
   * @param entryType Objeto {@link EntryType} a convertir.
   * @return Objeto {@link Entry} construido a partir de {@code entryType}.
   */
  private static Entry getEntryFromFeedType(Feed feed, EntryType entryType) {

    var entry = new Entry();
    entry.setFeed(feed);

    String id = Objects.requireNonNull(
        entryType.getId().getValue(),
        "EntryType.getId().getValue() no puede ser null"
    );

    entry.setEntryId(
        ComunHelper.limitarRegistro(id, Constantes.TAMANO_MAXIMO_CAMPO_500)
    );

    entry.setEntryIdCorto(
        ComunHelper.limitarRegistro(
            obtenerIdCorto(id),
            Constantes.TAMANO_MAXIMO_CAMPO_50));

    entry.setLink(
        ComunHelper.limitarRegistro(
            MapperStringFromList.getStringFromListLinkType(entryType.getLink()),
            Constantes.TAMANO_MAXIMO_CAMPO_500
        )
    );

    entry.setTitle(
        ComunHelper.limitarRegistro(
            MapperStringFromList.getStringFromListObject(
                Objects.requireNonNull(entryType.getTitle()).getContent()
            ),
            Constantes.TAMANO_MAXIMO_CAMPO_500
        )
    );

    entry.setSummary(
        ComunHelper.limitarRegistro(
            MapperStringFromList.getStringFromListObject(
                Objects.requireNonNull(entryType.getSummary()).getContent()
            ),
            Constantes.TAMANO_MAXIMO_CAMPO_2500
        )
    );

    entry.setUpdated(
        LocalDateTimeHelper.getLocalDateTimeFromXmlGregorianCalendar(
            Objects.requireNonNull(entryType.getUpdated()).getValue()
        )
    );

    entry.setContractFolderStatusList(
        MapperContractFolderStatus.getListContractFolderStatusFromListType(entry, entryType));

    return entry;
  }

  /**
   * Extrae el identificador situado tras el último '/' de una URL.
   *
   * @param id largo del Entry
   * @return string con el id corto
   */
  private static String obtenerIdCorto(String id) {
    int idx = id.lastIndexOf('/');
    return idx >= 0 ? id.substring(idx + 1) : id;
  }
}
