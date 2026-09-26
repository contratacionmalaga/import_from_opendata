package local.jarios.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.BufferedReader;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.Map;
import local.jarios.core.pipeline.context.OpenDataExecutionContext;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.exceptions.MiParseException;
import local.jarios.interfaces.FeedSource;
import org.junit.jupiter.api.Test;

class FeedHelperNavigationTest {

  @Test
  void accepts_a_normal_end_when_the_feed_does_not_include_next() throws Exception {
    OpenDataExecutionContext context = new OpenDataExecutionContext();

    FeedHelper.parsearFeeds(
        new InMemoryFeedSource("first", Map.of("first", atomFeed(null))), context);

    assertThat(context.getConjuntoFeedsFromAtoms()).hasSize(1);
  }

  @Test
  void rejects_an_informed_next_link_that_cannot_be_opened() {
    assertThatThrownBy(
            () ->
                FeedHelper.parsearFeeds(
                    new InMemoryFeedSource("first", Map.of("first", atomFeed("missing"))),
                    new OpenDataExecutionContext()))
        .isInstanceOf(MiParseException.class);
  }

  @Test
  void rejects_a_cycle_in_the_feed_chain() {
    assertThatThrownBy(
            () ->
                FeedHelper.parsearFeeds(
                    new InMemoryFeedSource(
                        "first", Map.of("first", atomFeed("second"), "second", atomFeed("first"))),
                    new OpenDataExecutionContext()))
        .isInstanceOf(MiParseException.class)
        .hasMessageContaining("Ciclo detectado");
  }

  @Test
  void rejects_an_invalid_initial_link() {
    assertThatThrownBy(
            () ->
                FeedHelper.parsearFeeds(
                    new InMemoryFeedSource("missing", Map.of()), new OpenDataExecutionContext()))
        .isInstanceOf(MiParseException.class);
  }

  @Test
  void rejects_a_feed_with_a_doctype_or_external_entity() {
    String unsafeAtom =
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <!DOCTYPE feed [<!ENTITY injected SYSTEM "file:///not-allowed">]>
        <feed xmlns="http://www.w3.org/2005/Atom">
          <id>urn:test:feed</id>
          <title>&injected;</title>
          <updated>2026-09-26T10:00:00Z</updated>
          <link rel="self" href="self"/>
        </feed>
        """;

    assertThatThrownBy(
            () ->
                FeedHelper.parsearFeeds(
                    new InMemoryFeedSource("first", Map.of("first", unsafeAtom)),
                    new OpenDataExecutionContext()))
        .isInstanceOf(MiParseException.class);
  }

  @Test
  void continues_after_incremental_cutoff_to_collect_deleted_entries_from_later_pages()
      throws Exception {
    OpenDataExecutionContext context = new OpenDataExecutionContext();
    context.setCompararConExistentes(true);
    context.setNewestEntry(entry("cursor", "2026-09-26T10:00:00"));

    FeedHelper.parsearFeeds(
        new InMemoryFeedSource(
            "first",
            Map.of(
                "first", atomFeedWithEntry("second", "old-entry", "2026-09-26T09:59:59"),
                "second", atomFeedWithDeletedEntry("deleted-entry-1"))),
        context);

    assertThat(context.getConjuntoFeedsFromAtoms()).hasSize(2);
    assertThat(context.getMapEntriesFromAtoms()).isEmpty();
    assertThat(context.getMapDeletedEntriesFromAtoms()).containsKey("deleted-entry-1");
  }

  private static String atomFeed(String next) {
    String nextLink = next == null ? "" : "<link rel=\"next\" href=\"" + next + "\"/>";
    return """
        <?xml version="1.0" encoding="UTF-8"?>
        <feed xmlns="http://www.w3.org/2005/Atom">
          <id>urn:test:feed</id>
          <title>Feed de prueba</title>
          <updated>2026-09-26T10:00:00Z</updated>
          <link rel="self" href="self"/>
          %s
        </feed>
        """
        .formatted(nextLink);
  }

  private static String atomFeedWithEntry(String next, String entryId, String updated) {
    return atomFeed(next)
        .replace(
            "</feed>",
            """
            <entry>
              <id>%s</id>
              <title>Entrada de prueba</title>
              <summary>Resumen de prueba</summary>
              <updated>%sZ</updated>
            </entry>
            </feed>
            """
                .formatted(entryId, updated));
  }

  private static String atomFeedWithDeletedEntry(String ref) {
    return """
        <?xml version="1.0" encoding="UTF-8"?>
        <feed xmlns="http://www.w3.org/2005/Atom"
              xmlns:at="http://purl.org/atompub/tombstones/1.0">
          <id>urn:test:feed</id>
          <title>Feed de prueba</title>
          <updated>2026-09-26T10:00:00Z</updated>
          <link rel="self" href="self"/>
          <at:deleted-entry ref="%s" when="2026-09-26T10:00:00Z"/>
        </feed>
        """
        .formatted(ref);
  }

  private static Entry entry(String entryId, String updated) {
    Entry entry = new Entry();
    entry.setEntryId(entryId);
    entry.setUpdated(LocalDateTime.parse(updated));
    return entry;
  }

  private record InMemoryFeedSource(String initialLink, Map<String, String> feeds)
      implements FeedSource {

    @Override
    public String getInitialLink() {
      return initialLink;
    }

    @Override
    public boolean isNextLinkValid(String link) {
      return feeds.containsKey(link);
    }

    @Override
    public String getNextLink(Feed feed) {
      return feed.getLinkNext();
    }

    @Override
    public BufferedReader openBufferedReader(String path) {
      return new BufferedReader(new StringReader(feeds.get(path)));
    }
  }
}
