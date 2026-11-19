package local.jarios.repositories;

import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.codice.NoticeInfo;
import org.hibernate.Hibernate;
import org.hibernate.Session;

/** Actualiza el grafo importado conservando las claves de las entidades que siguen presentes. */
final class EntryGraphUpdater {

  // Claves del proveedor dentro de cada relación padre; nunca se usa el orden de una lista.
  private static final Map<String, List<String>> COLLECTION_KEYS =
      Map.ofEntries(
          Map.entry("ContractFolderStatus", List.of("contractFolderId")),
          Map.entry(
              "PreliminaryMarketConsultationStatus", List.of("preliminaryMarketConsultationID")),
          Map.entry("DocumentReference", List.of("documentReferenceId")),
          Map.entry("Uuid", List.of("schemeName", "uuid")),
          Map.entry("ProcurementProjectLot", List.of("lote")),
          Map.entry("TenderResult", List.of("idContract", "procurementProjectLotId", "nif")),
          Map.entry("ContractModification", List.of("contractId", "contractModificationId")),
          Map.entry("AdditionalPublicationStatus", List.of("publicationMediaName")),
          Map.entry("AdditionalPublicationRequest", List.of("agencyId", "sendDateTime")),
          Map.entry("AdditionalPublicationDocumentReference", List.of("uri")),
          Map.entry("CommodityClassification", List.of("itemClassificationCode")),
          Map.entry(
              "AwardingCriteria",
              List.of("awardingCriteriaTypeCode", "awardingCriteriaSubTypeCode")),
          Map.entry("ContractExecutionRequirement", List.of("executionRequirementCode")),
          Map.entry("FinancialGuarantee", List.of("guaranteeTypeCode")),
          Map.entry("EvaluationCriteria", List.of("evaluationCriteriaTypeCode", "tipoSolvencia")),
          Map.entry("ClassificationScheme", List.of("classificationSchemeId")),
          Map.entry("ClassificationCategory", List.of("codeValue")),
          Map.entry("TendererRequirement", List.of("requirementTypeCode")));

  private EntryGraphUpdater() {}

  static Entry update(Session session, Entry existing, Entry incoming) {
    Entry managed = Hibernate.unproxy(existing, Entry.class);
    managed.setFeed(incoming.getFeed());
    synchronize(session, managed, incoming);
    return managed;
  }

  private static void synchronize(Session session, Object existing, Object incoming) {
    try {
      for (Field field : Hibernate.getClass(existing).getDeclaredFields()) {
        Column column = field.getAnnotation(Column.class);
        if (column != null && column.updatable()) {
          field.setAccessible(true);
          field.set(existing, field.get(incoming));
        }
        OneToMany many = field.getAnnotation(OneToMany.class);
        OneToOne one = field.getAnnotation(OneToOne.class);
        if (many != null && !many.mappedBy().isBlank()) {
          synchronizeMany(session, existing, incoming, field, many.mappedBy());
        } else if (one != null && !one.mappedBy().isBlank()) {
          synchronizeOne(session, existing, incoming, field, one.mappedBy());
        }
      }
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(
          "No se pudo actualizar el grafo de " + Hibernate.getClass(existing).getName(), e);
    }
  }

  private static void synchronizeMany(
      Session session, Object existing, Object incoming, Field field, String mappedBy)
      throws ReflectiveOperationException {
    field.setAccessible(true);
    @SuppressWarnings("unchecked")
    Collection<Object> managed = (Collection<Object>) field.get(existing);
    Collection<?> incomingChildren = (Collection<?>) field.get(incoming);
    Collection<?> replacements = incomingChildren == null ? List.of() : incomingChildren;
    if (managed == null) {
      managed = new ArrayList<>();
      field.set(existing, managed);
    }
    List<Object> unmatched = new ArrayList<>(managed);
    for (Object replacement : replacements) {
      Object match =
          replacement instanceof NoticeInfo notice
              ? findNoticeMatch(unmatched, notice, managed, replacements)
              : findMatch(unmatched, replacement);
      if (match == null) {
        bindParent(replacement, mappedBy, existing);
        bindChildren(replacement);
        managed.add(replacement);
        session.persist(replacement);
      } else {
        unmatched.remove(match);
        synchronize(session, match, replacement);
      }
    }
    // Se muta la colección gestionada para que orphanRemoval elimine solo los hijos retirados.
    managed.removeAll(unmatched);
  }

  private static void synchronizeOne(
      Session session, Object existing, Object incoming, Field field, String mappedBy)
      throws ReflectiveOperationException {
    field.setAccessible(true);
    Object replacement = field.get(incoming);
    Object managed = field.get(existing);
    if (replacement == null) {
      field.set(existing, null);
    } else if (managed == null) {
      bindParent(replacement, mappedBy, existing);
      bindChildren(replacement);
      field.set(existing, replacement);
      session.persist(replacement);
    } else {
      synchronize(session, Hibernate.unproxy(managed), replacement);
    }
  }

  private static Object findMatch(List<Object> candidates, Object incoming)
      throws ReflectiveOperationException {
    Class<?> type = Hibernate.getClass(incoming);
    List<String> keys = COLLECTION_KEYS.get(type.getSimpleName());
    if (keys == null) {
      throw new IllegalStateException("Falta clave de identidad para " + type.getName());
    }
    for (Object candidate : candidates) {
      boolean same = true;
      for (String key : keys) {
        Field field = type.getDeclaredField(key);
        field.setAccessible(true);
        if (!Objects.equals(field.get(Hibernate.unproxy(candidate)), field.get(incoming))) {
          same = false;
          break;
        }
      }
      if (same) {
        return Hibernate.unproxy(candidate);
      }
    }
    return null;
  }

  private static NoticeInfo findNoticeMatch(
      List<Object> candidates, NoticeInfo incoming, Collection<?> existing, Collection<?> replacements) {
    String type = incoming.getNoticeTypeCode();
    // Cuando el tipo se repite, el medio y la fecha distinguen las publicaciones.
    boolean repeated =
        hasRepeatedNoticeType(existing, type) || hasRepeatedNoticeType(replacements, type);
    Set<PublicationKey> publications = repeated ? publicationKeys(incoming) : Set.of();
    return candidates.stream()
        .map(NoticeInfo.class::cast)
        .filter(candidate -> Objects.equals(type, candidate.getNoticeTypeCode()))
        .filter(candidate -> !repeated || publications.equals(publicationKeys(candidate)))
        .findFirst()
        .map(candidate -> Hibernate.unproxy(candidate, NoticeInfo.class))
        .orElse(null);
  }

  private static boolean hasRepeatedNoticeType(Collection<?> notices, String type) {
    return notices.stream()
            .map(NoticeInfo.class::cast)
            .filter(notice -> Objects.equals(type, notice.getNoticeTypeCode()))
            .limit(2)
            .count()
        == 2;
  }

  private static Set<PublicationKey> publicationKeys(NoticeInfo notice) {
    Set<PublicationKey> keys = new HashSet<>();
    for (var publication : notice.getListAdditionalPublicationStatus()) {
      String medium = publication.getPublicationMediaName();
      var documents = publication.getAdditionalPublicationDocumentReferenceList();
      if (documents.isEmpty()) {
        keys.add(new PublicationKey(medium, null));
      }
      for (var document : documents) {
        keys.add(new PublicationKey(medium, document.getIssueDate()));
      }
    }
    return keys;
  }

  private record PublicationKey(String medium, LocalDate date) {}

  private static void bindParent(Object child, String mappedBy, Object parent)
      throws ReflectiveOperationException {
    Field backReference = Hibernate.getClass(child).getDeclaredField(mappedBy);
    backReference.setAccessible(true);
    backReference.set(child, parent);
  }

  private static void bindChildren(Object parent) throws ReflectiveOperationException {
    for (Field field : Hibernate.getClass(parent).getDeclaredFields()) {
      OneToMany many = field.getAnnotation(OneToMany.class);
      OneToOne one = field.getAnnotation(OneToOne.class);
      String mappedBy = many != null ? many.mappedBy() : one != null ? one.mappedBy() : "";
      if (mappedBy.isBlank()) {
        continue;
      }
      field.setAccessible(true);
      Object value = field.get(parent);
      Collection<?> children =
          value instanceof Collection<?> collection
              ? collection
              : value == null ? List.of() : List.of(value);
      for (Object child : children) {
        bindParent(child, mappedBy, parent);
        bindChildren(child);
      }
    }
  }
}
