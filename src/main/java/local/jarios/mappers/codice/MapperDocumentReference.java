package local.jarios.mappers.codice;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import local.jarios.common.util.Constantes;
import local.jarios.common.util.TamanoCampos;
import local.jarios.entity.codice.ContractFolderStatus;
import local.jarios.entity.codice.DocumentReference;
import local.jarios.enums.TipoDocumento;
import local.jarios.helpers.StringHelper;
import org.dgpe.codice.common.caclib.AttachmentType;
import org.dgpe.codice.common.caclib.DocumentReferenceType;
import org.dgpe.codice.common.caclib.ExternalReferenceType;
import org.dgpe.codice.common.cbclib.DocumentTypeCodeType;
import org.dgpe.codice.common.cbclib.DocumentTypeType;
import un.unece.uncefact.data.specification.corecomponenttypeschemamodule._2.IdentifierType;
import un.unece.uncefact.data.specification.corecomponenttypeschemamodule._2.TextType;

/**
 * Mapper para convertir objetos {@link DocumentReferenceType} del modelo CÓDICE a la entidad {@link
 * DocumentReference}.
 *
 * <p>Contrato:
 *
 * <ul>
 *   <li>El {@code ID} del documento es obligatorio (la entidad lo requiere).
 *   <li>Los campos de texto se recortan a los tamaños máximos definidos en {@link Constantes}.
 *   <li>Los campos opcionales no presentes no se informan (se dejan {@code null} o cadena vacía
 *       según el campo).
 * </ul>
 */
public final class MapperDocumentReference {

  /** Constructor privado para evitar instanciación. */
  private MapperDocumentReference() {
    // Utility class.
  }

  /**
   * Convierte una lista de {@link DocumentReferenceType} en una lista de {@link DocumentReference}
   * y vincula cada referencia como "additional document reference" al {@link ContractFolderStatus}.
   *
   * @param contractFolderStatus entidad padre (no nula).
   * @param tipoDocumento tipo lógico del documento (no nulo).
   * @param documentReferenceTypeList lista de referencias CÓDICE (no nula; puede ser vacía).
   * @return lista inmutable de {@link DocumentReference}.
   * @throws NullPointerException si algún argumento obligatorio es {@code null}.
   * @throws IllegalArgumentException si algún {@link DocumentReferenceType} no tiene ID.
   */
  public static List<DocumentReference> getListDocumentReferenceFromType(
      ContractFolderStatus contractFolderStatus,
      TipoDocumento tipoDocumento,
      List<DocumentReferenceType> documentReferenceTypeList) {

    Objects.requireNonNull(contractFolderStatus, "contractFolderStatus no puede ser null");
    Objects.requireNonNull(tipoDocumento, "tipoDocumento no puede ser null");
    Objects.requireNonNull(
        documentReferenceTypeList, "documentReferenceTypeList no puede ser null");

    return documentReferenceTypeList.stream()
        .filter(Objects::nonNull)
        .map(type -> mapDocumentReference(type, tipoDocumento))
        .peek(doc -> doc.setAdditionalDocumentReference(contractFolderStatus))
        .toList();
  }

  /**
   * Convierte un {@link DocumentReferenceType} en una entidad {@link DocumentReference}.
   *
   * <p>Fail-fast: si el {@code ID} no existe, se lanza {@link IllegalArgumentException} porque la
   * entidad lo requiere ({@code nullable=false}).
   *
   * @param documentReferenceType objeto CÓDICE (no nulo).
   * @return entidad {@link DocumentReference} ya construida.
   * @throws IllegalArgumentException si el {@code ID} es {@code null} o vacío.
   */
  public static DocumentReference getDocumentReferenceFromType(
      DocumentReferenceType documentReferenceType) {
    Objects.requireNonNull(documentReferenceType, "documentReferenceType no puede ser null");
    return mapDocumentReference(documentReferenceType, null);
  }

  private static DocumentReference mapDocumentReference(
      DocumentReferenceType type, TipoDocumento tipoDocumento) {

    DocumentReference entity = new DocumentReference();

    entity.setDocumentReferenceId(readRequiredLimited(type.getID(), IdentifierType::getValue));

    // En tu entidad: estos campos permiten null, pero tú usabas CADENA_VACIA.
    // Mantengo tu intención: si falta, guardamos "".
    entity.setDocumentTypeCode(
        readOptionalLimitedOrEmpty(
            type.getDocumentTypeCode(), DocumentTypeCodeType::getValue, TamanoCampos.TAMANO_50));

    entity.setDocumentType(
        readOptionalLimitedOrEmpty(
            type.getDocumentType(), DocumentTypeType::getValue, TamanoCampos.TAMANO_500));

    if (tipoDocumento != null) {
      entity.setTipoDocumento(tipoDocumento);
    }

    mapAttachment(entity, type.getAttachment());

    return entity;
  }

  private static void mapAttachment(DocumentReference entity, AttachmentType attachment) {
    if (attachment == null) {
      return;
    }

    ExternalReferenceType externalReference = attachment.getExternalReference();
    if (externalReference == null) {
      return;
    }

    setIfPresentLimited(
        entity::setDocumentHash, externalReference.getDocumentHash(), TextType::getValue);

    setIfPresentLimited(entity::setFilename, externalReference.getFileName(), TextType::getValue);

    setIfPresentLimited(entity::setUri, externalReference.getURI(), IdentifierType::getValue);
  }

  private static <T> void setIfPresentLimited(
      Consumer<String> setter, T source, Function<T, String> extractor) {

    if (source == null) {
      return;
    }
    String value = extractor.apply(source);
    if (value == null) {
      return;
    }
    setter.accept(StringHelper.limit(value, TamanoCampos.TAMANO_500));
  }

  private static <T> String readOptionalLimitedOrEmpty(
      T source, Function<T, String> extractor, int maxLength) {

    if (source == null) {
      return Constantes.CADENA_VACIA;
    }
    String value = extractor.apply(source);
    if (value == null) {
      return Constantes.CADENA_VACIA;
    }
    return StringHelper.limit(value, maxLength);
  }

  private static <T> String readRequiredLimited(T source, Function<T, String> extractor) {

    if (source == null) {
      throw new IllegalArgumentException("DocumentReferenceType.ID es obligatorio");
    }
    String value = extractor.apply(source);
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("DocumentReferenceType.ID es obligatorio");
    }
    return StringHelper.limit(value, TamanoCampos.TAMANO_500);
  }
}
