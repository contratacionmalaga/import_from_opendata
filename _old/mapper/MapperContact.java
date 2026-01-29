package local.jarios.mappers.codice;

import local.jarios.common.util.Constantes;
import local.jarios.entity.codice.Contact;
import local.jarios.entity.codice.Party;
import local.jarios.helpers.ComunHelper;
import lombok.extern.slf4j.Slf4j;
import org.dgpe.codice.common.caclib.ContactType;
import org.dgpe.codice.common.cbclib.ElectronicMailType;
import org.dgpe.codice.common.cbclib.NameType;
import org.dgpe.codice.common.cbclib.TelefaxType;
import org.dgpe.codice.common.cbclib.TelephoneType;

import java.util.Optional;

/**
 * Description: Juan Antonio Author: juan Date: 06/07/2024 Team: Juan Antonio.
 */
@Slf4j
public final class MapperContact {

  private MapperContact() {
  }

  public static Contact getContact(
      Party party,
      WinningParty winningParty,
      ContactType contactType) {

    //
    Contact contact = new Contact();
    contact.setParty(party);
    contact.setWinningParty(winningParty);

    Optional.ofNullable(contactType.getName())
        .map(NameType::getValue)
        .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_500))
        .ifPresent(contact::setName);

    Optional.ofNullable(contactType.getElectronicMail())
        .map(ElectronicMailType::getValue)
        .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_500))
        .ifPresent(contact::setElectronicMail);

    Optional.ofNullable(contactType.getTelephone())
        .map(TelephoneType::getValue)
        .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50))
        .ifPresent(contact::setTelephone);

    Optional.ofNullable(contactType.getTelefax())
        .map(TelefaxType::getValue)
        .map(value -> ComunHelper.limitarRegistro(value, Constantes.TAMANO_MAXIMO_CAMPO_50))
        .ifPresent(contact::setTelefax);

    //
    return contact;
  }
}
