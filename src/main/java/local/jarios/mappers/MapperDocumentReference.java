package local.jarios.mappers;

import local.jarios.common.util.Constantes;
import local.jarios.entity.placsp.*;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.DocumentReferenceType;
import org.dgpe.codice.common.cbclib.DocumentTypeCodeType;

import java.util.Optional;

/**
 * Mapper para convertir objetos {@link DocumentReferenceType} del modelo Codice
 * en la entidad interna {@link DocumentReference} usada en el proyecto.
 * <p>
 * Este mapper asocia referencias documentales a sus posibles categorías:
 * adicionales, generales, legales y técnicas, y mapea propiedades relevantes
 * como el código de tipo de documento y el attachment.
 * <p>
 * Se aplican valores por defecto para evitar valores nulos en campos clave.
 *
 * <p><b>Autor:</b> Juan Antonio</p>
 * <p><b>Fecha:</b> 11/04/2024</p>
 * <p><b>Equipo:</b> Juan Antonio</p>
 */
@Slf4j
public final class MapperDocumentReference {

    private MapperDocumentReference() {
        // Constructor privado para evitar instanciación
    }

    /**
     * Convierte un {@link DocumentReferenceType} en un objeto {@link DocumentReference}
     * y lo vincula con sus entidades asociadas si existen.
     *
     * @param additionalDocumentReference   Referencia documental adicional asociada.
     * @param generalDocumentDocumentReference Referencia documental general asociada.
     * @param legalDocumentReference         Referencia documental legal asociada.
     * @param technicalDocumentReference     Referencia documental técnica asociada.
     * @param documentReferenceType          Objeto Codice que contiene los datos de referencia documental.
     * @return Entidad {@link DocumentReference} construida a partir de la información proporcionada.
     */
    public static DocumentReference getDocumentReferenceFromType(
            AdditionalDocumentReference additionalDocumentReference,
            GeneralDocumentDocumentReference generalDocumentDocumentReference,
            LegalDocumentReference legalDocumentReference,
            TechnicalDocumentReference technicalDocumentReference,
            DocumentReferenceType documentReferenceType) {

        DocumentReference documentReference = new DocumentReference();

        documentReference.setAdditionalDocumentReference(additionalDocumentReference);
        documentReference.setGeneralDocumentDocumentReference(generalDocumentDocumentReference);
        documentReference.setLegalDocumentReference(legalDocumentReference);
        documentReference.setTechnicalDocumentReference(technicalDocumentReference);
        documentReference.setIdDocumentReference(documentReferenceType.getID().getValue());

        String docTypeCode = Optional.ofNullable(documentReferenceType.getDocumentTypeCode())
                .map(DocumentTypeCodeType::getValue)
                .orElse(Constantes.CADENA_VACIA);
        documentReference.setDocumentTypeCode(docTypeCode);

        Optional.ofNullable(documentReferenceType.getAttachment())
                .ifPresent(attachmentType -> documentReference.setAttachment(
                        MapperAttachment.getAttachmentFromType(
                                documentReference, null, null, attachmentType)));

        return documentReference;
    }
}
