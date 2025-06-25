# Importación de los Datos Abiertos proporcionados por la PLACSP

## Información General del Aplicativo
Este aplicativo se encarga de importar la información publicada por el Ministerio de Hacienda en el portal de Datos Abiertos (https://www.hacienda.gob.es/es-ES/GobiernoAbierto/Datos%20Abiertos/Paginas/licitaciones_plataforma_contratacion.aspx)

Los datos que se pueden obtener son los siguentes


## Tecnología utilizada para el desarrollo del proyecto
- __Lenguaje de Programación__: Java en su versión `v21.0.5`.
- __Base de datos__: PostgreSQL en su versión `17.2` haciendo uso de esquemas.
- __Versión del framework Hibernate__: HikariCP en su versión `6.6.9` para core y `6.6.9` para HikariCP
- __Framework acceso a la base de datos__: HikariCP en su versión `6.2.1`.
- __Libreria PostgreSQL__: PostgreSQL Connector for Java en su versión `42.7.5`.
- __Biblioteca Lombok__: Biblioteca que facilita la programación mediante la inyección de código mediante etiquetas. Utilizamos la versión `1.18.34`.
- __Gestión de Logs__: Utilizamos `slf4j` como fachada para los logs de los componentes y como elemento generador de logs utilizamos `log4j`. Las versiones de los productos utilizadas son las siguientes:
  - __Fachada__: `slf4j` versión `2.0.16` junto con la implementación `log4j-slf4j2-impl` para `log4j` en su versión `2.24.3`.
  - __Gestor de Logs__: `log4j` en su versión `2.24.3`.
- Parseo de los ficheros GC con
  - __jaxb-core__: `2.3.0.1`
  - __jaxb-imp__: `2.3.0.1`
  - __jaxb-api__: `2.4.0-b180830.0359`
  - __javax.activation-api__: `1.2.0`
- __Envío de email__: Jakarta Mail con los siguientes paquetes:
  - __jakarta.mail-api__: `2.1.3`
  - __jakarta.activation-api__: `2.1.3`
  - __angus-mail__: `2.0.3`
 

## Información a procesar


## Importaciones


## Base de datos que almacenará la información


## Estructura de las carpetas que componen el aplicativo

A continuación se detalla el funcionamiento de los ficheros y carpetas que componen este proyecto

- `Readme.md`- Este fichero.
- `pom.xml`  - Fichero con la configuración de MAVEN.

- __logs__. Carpeta donde se almacenan los logs durante la ejecución del programa. Para los logs se hace uso de `log4j`. 
- __scr__. Carpeta que contiene el código fuente del aplicativo.
  - __main__ Se subdivide en dos carpetas
    - __java__ Codigo fuente de Java
    - 

## Funcionamiento del Aplicativo


  
***

<p>
Juan Antonio Ríos Peláez
</p>

`jarios@malaga.es`
