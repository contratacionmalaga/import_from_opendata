package local.jarios.mappers.atom;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import local.jarios.common.util.TamanoCampos;
import local.jarios.entity.atom.DeletedEntry;
import local.jarios.entity.atom.Feed;
import org.junit.jupiter.api.Test;
import org.purl.atompub.tombstones._1.DeletedEntryType;
import org.w3._2005.atom.FeedType;

class MapperDeletedEntryTest {

  @Test
  void mapsRefCortoLikeEntryIdCortoWhenDeletedEntryIsAdded() {
    Feed feed = new Feed();
    FeedType feedType = new FeedType();
    DeletedEntryType deletedEntryType = new DeletedEntryType();
    deletedEntryType.setRef(
        "https://contrataciondelestado.es/sindicacion/licitacionesPerfilContratante/12345678901234567890123456789012345678901234567890-extra");

    feedType.getAny().add(deletedEntryElement(deletedEntryType));

    List<DeletedEntry> deletedEntries =
        MapperDeletedEntry.getListDeletedEntryFromFeedType(feed, feedType);

    assertThat(deletedEntries).hasSize(1);
    assertThat(deletedEntries.getFirst().getRef()).isEqualTo(deletedEntryType.getRef());
    assertThat(deletedEntries.getFirst().getRefCorto())
        .isEqualTo("12345678901234567890123456789012345678901234567890");
    assertThat(deletedEntries.getFirst().getRefCorto()).hasSize(TamanoCampos.TAMANO_50);
  }

  @Test
  void mapsRefCortoToWholeRefWhenRefHasNoSlash() {
    Feed feed = new Feed();
    FeedType feedType = new FeedType();
    DeletedEntryType deletedEntryType = new DeletedEntryType();
    deletedEntryType.setRef("licitacion-sin-url");

    feedType.getAny().add(deletedEntryElement(deletedEntryType));

    List<DeletedEntry> deletedEntries =
        MapperDeletedEntry.getListDeletedEntryFromFeedType(feed, feedType);

    assertThat(deletedEntries).hasSize(1);
    assertThat(deletedEntries.getFirst().getRefCorto()).isEqualTo("licitacion-sin-url");
  }

  private static JAXBElement<DeletedEntryType> deletedEntryElement(DeletedEntryType value) {
    return new JAXBElement<>(
        new QName("http://purl.org/atompub/tombstones/1.0", "deleted-entry"),
        DeletedEntryType.class,
        value);
  }
}
