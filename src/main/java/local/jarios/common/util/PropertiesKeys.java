package local.jarios.common.util;

/**
 *
 * @author Home
 */

public final class PropertiesKeys {

    /* Nombre de las propiedades del fichero app.properties */
    public static final String APP_NAME = "app.name";
    public static final String APP_DESCRIPTION = "app.description";
    public static final String APP_URL = "app.url";
    public static final String APP_PATH = "app.path";
    public static final String APP_FILENAME = "app.filename";

    /* Nombre de las propiedades del fichero filter.properties */
    public static final String FILTRO_FECHAINICIALLECTURA = "filtro.fechaInicialLectura";
    public static final String FILTRO_FECHAFINALLECTURA = "filtro.fechaFinalLectura";
    public static final String FILTRO_SQL = "filtro.sql";
    public static final String FILTRO_OBJETO = "filtro.objeto";
    public static final String FILTRO_NUTS = "filtro.nuts";

    /* Nombre de las propiedades del fichero validation.properties */
    public static final String PARAMETRO_URI_SCHEME = "parametro.uri.scheme";
    public static final String PARAMETRO_URI_HOST = "parametro.uri.host";
    public static final String PARAMETRO_URI_PATH = "parametro.uri.path";

    /*  */
    public static final String JAKARTA_URL = "jakarta.persistence.jdbc.url";

    /* Nombre de las propiedades del fichero email.properties */
    public static final String EMAIL_USER = "mail.user";
    public static final String EMAIL_PASSWORD = "mail.password";
    public static final String EMAIL_FROM = "mail.from";
    public static final String EMAIL_TO = "mail.to";

    /* */
    public static final String LOCAL_URL = "jakarta.persistence.jdbc.url";
    public static final String BATCH_SIZE = "hibernate.jdbc.batch_size";

    /* Nombre de las propiedades del fichero hibernate.properties */
    public static final String HIBERNATE_FILTER_URL = "hibernate.filter.url";
    public static final String HIBERNATE_FILTER_DRIVER = "hibernate.filter.driver";
    public static final String HIBERNATE_FILTER_USER = "hibernate.filter.user";
    public static final String HIBERNATE_FILTER_PASSWORD = "hibernate.filter.password";

    /** Nombre sin extensión del fichero app.properties */
    public static final String APP_PROPERTIES = "app";

    /** Nombre sin extensión del fichero app.properties */
    public static final String EMAIL_PROPERTIES = "email";

    /** Nombre sin extensión del fichero app.properties */
    public static final String FILTER_PROPERTIES = "hibernate";

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

    /* INFORMACIÓN PARA EL PARSEO MEDIANTE JAXB */
    public static final String JAXB_ATOM = "org.w3._2005.atom";
    public static final String JAXB_ORG_DGPE_CODICE_COMMON_CACLIB = "org.dgpe.codice.common.caclib";
    public static final String JAXB_ORG_DGPE_CODICE_COMMON_CBCLIB = "org.dgpe.codice.common.cbclib";
    public static final String JAXB_EXT_PLACE_CODICE_COMMON_CACLIB = "ext.place.codice.common.caclib";
    public static final String JAXB_EXT_PLACE_CODICE_COMMON_CBCLIB = "ext.place.codice.common.cbclib";
    public static final String JAXB_TOMBSTONES = "org.purl.atompub.tombstones._1";


    private PropertiesKeys() {}

}





