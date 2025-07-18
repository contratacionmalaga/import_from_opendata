package local.jarios.mappers;

import local.jarios.entity.placsp.AuctionTerms;
import local.jarios.entity.placsp.TenderingProcess;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.AuctionTermsType;

/**
 * Mapper para convertir objetos de tipo {@link AuctionTermsType} del modelo Codice
 * a la entidad {@link AuctionTerms} del modelo local.
 * <p>
 * Esta clase proporciona métodos estáticos para realizar la conversión.
 * </p>
 *
 * @author juan
 * @since 2024-04-11
 * @version 1.0
 */
@Slf4j
public final class MapperAuctionTerms {

    /**
     * Constructor privado para evitar la instanciación de esta clase utilitaria.
     */
    private MapperAuctionTerms() {
    }

    /**
     * Convierte un objeto {@link AuctionTermsType} y un {@link TenderingProcess} asociado
     * en una entidad {@link AuctionTerms} del modelo local.
     *
     * @param tenderingProcess el proceso de licitación al que pertenece el AuctionTerms
     * @param auctionTermsType el objeto del modelo Codice a mapear
     * @return una instancia de {@link AuctionTerms} con los datos mapeados
     */
    public static AuctionTerms getAuctionTerms(
            TenderingProcess tenderingProcess,
            AuctionTermsType auctionTermsType) {

        AuctionTerms auctionTerms = new AuctionTerms();

        auctionTerms.setTenderingProcess(tenderingProcess);

        auctionTerms.setAuctionConstraintIndicator(
                auctionTermsType.getAuctionConstraintIndicator().isValue());

        return auctionTerms;
    }
}
