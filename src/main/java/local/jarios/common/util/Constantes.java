package local.jarios.common.util;

/**
 *
 * @author Home
 */

public final class Constantes {

    /** Nombre sin extensión del fichero app.properties */
    public static final String APP_PROPERTIES = "app";

    /** Nombre sin extensión del fichero app.properties */
    public static final String EMAIL_PROPERTIES = "email";

    /** Nombre sin extensión del fichero app.properties */
    public static final String FILTER_PROPERTIES = "filter";

    /** Nombre sin extensión del fichero app.properties */
    public static final String HIBERNATE_PROPERTIES = "hibernate";

    /** Nombre sin extensión del fichero app.properties */
    public static final String VALIDATION_PROPERTIES = "validation";

    /** clave utilizada para la encriptación / desencriptación de las value con formato ENC(encrypt_value) */
    public static final String KEY_EMAIL_FROM = "mail.from";

    /** clave utilizada para la encriptación / desencriptación de las value con formato ENC(encrypt_value) */
    public static final String KEY_EMAIL_TO = "mail.to";

    /** clave utilizada para la encriptación / desencriptación de las value con formato ENC(encrypt_value) */
    public static final String KEY_APP_NAME = "app.name";

    /** Ruta del directorio con los ficheros properties */
    public static final String CONFIG_DIR = "config";

    /** clave utilizada para la encriptación / desencriptación de las value con formato ENC(encrypt_value) */
    public static final String ENCRYPT_PASSWORD = "Malaga$2025";

    public static final String CADENA_VACIA = "";

    public static final String RETORNO_CARRO = "\r";
    public static final String SALTO_LINEA = "\n";
    public static final String RETORNO_CARRO_HTML = "&#xD;";

    public static final String TABULADOR_1 = "    ";
    public static final String TABULADOR_2 = "        ";
    public static final String TABULADOR_3 = "            ";

    //Constantes
    public static final String URL_FINAL_MAYORES = "licitacionesPerfilesContratanteCompleto3.atom";
    public static final String URL_FINAL_MENORES = "contratosMenoresPerfilesContratantes.atom";
    public static final String URL_FINAL_AGREGADAS = "PlataformasAgregadasSinMenores.atom";
    public static final String URL_FINAL_ENCARGOSMEDIOSPROPIOS = "EMP_SectorPublico.atom";
    public static final String URL_FINAL_CONSULTASPRELIMINARESMERCADO = "CPM_SectorPublico.atom";
    public static final String URL_FINAL_PRUEBAS = "prueba.atom";

    public static final String SI = "SI";
    public static final String NO = "NO";
    public static final String ANNIO_INICIAL = "2018";
    public static final String ANNIO_FINAL = "2035";

    /* Tipos de LINKs */
    public static final String LINK_FIRST = "first";
    public static final String LINK_NEXT = "next";
    public static final String LINK_SELF = "self";
    public static final String LINK_PREV = "prev";    

    /* DELIMITADORES UTILIZADOS EN LOS MÉTODOS toString() */
    public static final String CR = "\n";

    public static final String FORMATO_FECHA = "yyyy-MM-dd";
    public static final String FECHA_FINAL_LECTURA = "2018-01-01";


    /* INFORMACIÓN PARA EL PARSEO MEDIANTE JAXB */
    public static final String JAXB_ATOM = "org.w3._2005.atom";
    public static final String JAXB_ORG_DGPE_CODICE_COMMON_CACLIB = "org.dgpe.codice.common.caclib";
    public static final String JAXB_ORG_DGPE_CODICE_COMMON_CBCLIB = "org.dgpe.codice.common.cbclib";
    public static final String JAXB_EXT_PLACE_CODICE_COMMON_CACLIB = "ext.place.codice.common.caclib";
    public static final String JAXB_EXT_PLACE_CODICE_COMMON_CBCLIB = "ext.place.codice.common.cbclib";
    public static final String JAXB_TOMBSTONES = "org.purl.atompub.tombstones._1";

    /* VARIABLES PARA EL DEBUG DE LAS INSERCCIONES */

   //Validación de los tamaños máximos de los campos antes de enviarse a la base de datos
    public static final int TAMANO_MAXIMO_CAMPO_5 = 5;
    public static final int TAMANO_MAXIMO_CAMPO_15 = 15;
    public static final int TAMANO_MAXIMO_CAMPO_50 = 50;
    public static final int TAMANO_MAXIMO_CAMPO_250 = 250;
    public static final int TAMANO_MAXIMO_CAMPO_450 = 450;
    public static final int TAMANO_MAXIMO_CAMPO_2500 = 2500;

    // Identificadores utilizados por el componente PARTY
    public static final String DIR3 = "DIR3";
    public static final String IDPLATAFORMA = "ID_PLATAFORMA";
    public static final String IDOCPLAT = "ID_OC_PLAT";
    public static final String NIF = "NIF";
    public static final String OTROS = "OTROS";

    private Constantes() {}

}





