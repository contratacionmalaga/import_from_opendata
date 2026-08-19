package local.jarios.mappers.atom;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import local.jarios.common.util.TamanoCampos;
import local.jarios.entity.atom.DeletedEntry;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.helpers.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.purl.atompub.tombstones._1.DeletedEntryType;
import org.w3._2005.atom.EntryType;
import org.w3._2005.atom.FeedType;

/**
 * Mapper para convertir objetos del modelo Atom {@link FeedType} y {@link EntryType} a las
 * entidades internas {@link Feed} y {@link Entry} usadas en el proyecto.
 *
 * <p>Proporciona métodos para transformar listas de entradas Atom en listas de objetos {@link
 * Entry} vinculados a un {@link Feed} dado.
 *
 * <p>Se aplican límites de tamaño a los campos para asegurar compatibilidad con la base de datos y
 * evitar errores por datos demasiado largos.
 *
 * @author juan
 */
@Slf4j
public final class MapperDeletedEntry {

  /** Constructor privado */
  private MapperDeletedEntry() {
    //
  }

  /**
   * Convierte un objeto {@link FeedType} y su lista de {@link EntryType} en una lista de objetos
   * {@link Entry} vinculados al {@link Feed} proporcionado.
   *
   * @param feed Entidad {@link Feed} a la que se asociarán las entradas.
   * @param feedType Objeto {@link FeedType} que contiene las entradas Atom.
   * @return Lista de objetos {@link DeletedEntry} convertidos desde {@link EntryType}.
   */
  public static List<DeletedEntry> getListDeletedEntryFromFeedType(Feed feed, FeedType feedType) {

    List<DeletedEntry> listDeletedEntry = new ArrayList<>();

    if (feedType.getAny() != null) {
      for (int indice = 0; indice < feedType.getAny().size(); indice++) {
        @SuppressWarnings("unchecked")
        DeletedEntryType deletedEntryType =
            ((JAXBElement<DeletedEntryType>) feedType.getAny().get(indice)).getValue();
        DeletedEntry deletedEntry = getDeletedEntryFromFeedType(feed, deletedEntryType);
        listDeletedEntry.add(deletedEntry);
      }
    }

    return listDeletedEntry;
  }

  /**
   * Convierte un objeto {@link EntryType} en una entidad {@link Entry} vinculada a un {@link Feed}
   * dado.
   *
   * @param feed Entidad {@link Feed} asociada.
   * @param deletedEntryType Objeto {@link DeletedEntryType} a convertir.
   * @return Objeto {@link DeletedEntryType} construido a partir de {@code entryType}.
   */
  private static DeletedEntry getDeletedEntryFromFeedType(
      Feed feed, DeletedEntryType deletedEntryType) {

    //
    var deleltedEntry = new DeletedEntry();
    deleltedEntry.setFeed(feed);

    // Verifica que no sea null
    if (deletedEntryType != null && deletedEntryType.getRef() != null) {
      String ref = deletedEntryType.getRef();
      deleltedEntry.setRef(StringHelper.limit(ref, TamanoCampos.TAMANO_500));
      deleltedEntry.setRefCorto(
          StringHelper.limit(StringHelper.obtenerIdCorto(ref), TamanoCampos.TAMANO_50));
    }

    // Verifica que no sea null
    if (deletedEntryType != null && deletedEntryType.getWhen() != null) {
      deleltedEntry.setUpdated(
          deletedEntryType.getWhen().toGregorianCalendar().toZonedDateTime().toLocalDateTime());
    }

    // Verifica que no sea null
    if (deletedEntryType != null
        && deletedEntryType.getComment() != null
        && deletedEntryType.getComment().getType() != null) {
      deleltedEntry.setComment(deletedEntryType.getComment().getType());
    }

    // Devuelvo el valor
    return deleltedEntry;
  }
}
