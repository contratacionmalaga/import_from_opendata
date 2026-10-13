package local.jarios.repositories;

import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import local.jarios.entity.atom.Entry;
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
          Map.entry("NoticeInfo", List.of("noticeTypeCode")),
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
    existing.setFeed(incoming.getFeed());
    synchronize(session, existing, incoming);
    return existing;
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
    Collection<?> replacements = (Collection<?>) field.get(incoming);
    if (managed == null) {
      managed = new ArrayList<>();
      field.set(existing, managed);
    }
    List<Object> unmatched = new ArrayList<>(managed);
    for (Object replacement : replacements == null ? List.of() : replacements) {
      Object match = findMatch(unmatched, replacement);
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
