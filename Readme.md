# Importador de Datos Abiertos de la PLACSP

## 📦 Descripción General

Este proyecto es una solución robusta para la importación, transformación y almacenamiento de datos abiertos publicados por la Plataforma de Contratación del Sector Público (PLACSP), dependiente del Ministerio de Hacienda de España.

Su principal objetivo es automatizar la ingesta de información relativa a licitaciones, adjudicaciones y contrataciones públicas, garantizando su correcta validación, normalización y persistencia en una base de datos relacional.

🔗 [Portal de Datos Abiertos - Ministerio de Hacienda](https://www.hacienda.gob.es/es-ES/GobiernoAbierto/Datos%20Abiertos/Paginas/licitaciones_plataforma_contratacion.aspx)

---

## 🛠️ Tecnologías y Herramientas

| Tecnología             | Versión         | Descripción                                          |
|------------------------|-----------------| ---------------------------------------------------- |
| Java                   | 21.0.11         | Lenguaje principal del proyecto                      |
| MariaDB                | 11.8            | Base de datos relacional con uso de esquemas         |
| Hibernate Core         | 7.3.0.Final     | Framework ORM para persistencia en base de datos     |
| HikariCP               | 7.0.2           | Pool de conexiones eficiente para la base de datos   |
| Lombok                 | 1.18.44         | Reducción de código boilerplate mediante anotaciones |
| SLF4J + Log4J          | 2.0.17 / 2.25.4 | Sistema de registro de logs centralizado y flexible  |
| JAXB                   | 2.3.x - 2.4.x   | Procesamiento y parseo de documentos XML             |
| Jakarta Mail           | 2.1.3           | Envío de notificaciones por correo electrónico       |

---

## 📊 Información Procesada

El sistema procesa archivos XML publicados por la PLACSP que contienen:

- Datos de licitaciones públicas.
- Información sobre adjudicaciones.
- Detalles de procedimientos administrativos.

Los formatos admitidos incluyen:

- Ficheros XML conforme a los esquemas oficiales.
- Archivos comprimidos (zip) que contienen múltiples XML.

---

## 📂 Base de Datos

Se utiliza MariaDB como motor principal de base de datos, con esquemas separados para las variantes de importación y scripts SQL de índices para optimizar consultas e importaciones incrementales.

---

## 📚 Estructura del Proyecto

```
placsp-importador/
├── README.md               # Documentación del proyecto
├── pom.xml                 # Configuración de dependencias Maven
├── logs/                   # Directorio para archivos de log
└── src/
    ├── main/
    │   ├── java/           # Código fuente Java
    │   └── resources/      # Recursos y configuración del proyecto
```

---

## ⚙️ Funcionamiento General

1. Descarga o recepción de los ficheros XML desde el portal de Datos Abiertos.
2. Validación y parseo de los documentos XML mediante JAXB.
3. Transformación a objetos Java.
4. Persistencia en MariaDB usando Hibernate.
5. Registro de eventos, advertencias y errores mediante Log4J.
6. Envío automático de notificaciones por correo en caso de errores o finalización de procesos.

---

## 🔧 Requisitos Previos

- Java JDK 21 o superior. En desarrollo local se usa `C:\java\software\jdk-21.0.11`.
- Maven 3.9.16. En desarrollo local se usa `C:\java\software\apache-maven-3.9.16`.
- Base de datos configurada según los properties externos del entorno.
- Acceso a internet para la resolución de dependencias Maven.

---

## 📑 Instrucciones de Uso

1. Clona el repositorio o descarga el código fuente.
2. Configura el acceso a la base de datos en `application.properties`.
3. Prepara la sesión con Java 21.0.11 y Maven 3.9.16:

```powershell
.\scripts\use-java21-maven3916.ps1
```

4. Ejecuta las pruebas:

```powershell
.\scripts\use-java21-maven3916.ps1 test
```

5. Compila y empaqueta el proyecto:

```powershell
.\scripts\use-java21-maven3916.ps1 clean package
```

6. Ejecuta la aplicación siguiendo las instrucciones internas del proyecto.

---


## GitHub Actions

El CI ejecutable está en `.github/workflows/maven-ci.yml` y lanza:

```powershell
mvn -B test
mvn -B spotless:check
mvn -B spotbugs:check
```

Para resolver `jarios-parent` y los helpers privados en GitHub Packages, configura el secret `PACKAGES_TOKEN` con permisos de lectura sobre paquetes. Si no existe, el workflow intentará usar `GITHUB_TOKEN`.

El fichero antiguo `.github/workflow/maven-ci.yml` está en una carpeta no reconocida por GitHub Actions y queda obsoleto.

---
## Seguridad de dependencias

OWASP Dependency Check se ejecuta de forma explicita, igual que en el resto de proyectos. Define `NVD_API_KEY` en la sesion o en CI y lanza el goal Maven:

```powershell
$env:NVD_API_KEY='<clave-nvd>'
.\scripts\use-java21-maven3916.ps1 org.owasp:dependency-check-maven:check
```

El umbral configurado para fallar el build es CVSS 8.0 y `ossIndexAnalyzerEnabled` queda desactivado.

---

## 🔄 Configuración

La configuración operativa se carga desde el directorio externo `properties` indicado al ejecutar los JAR mediante `--configDir`. Los ficheros principales son:

| Fichero | Uso |
|---------|-----|
| `properties/app.properties` | Parámetros funcionales de importación y orígenes de datos. |
| `properties/filter.properties` | Filtros por NIF o código postal para importaciones con filtros. |
| `properties/hibernate.properties` | Parámetros base de Hibernate y HikariCP. |
| `properties/bd.properties` | Conexión JDBC principal a la base de datos. |
| `properties/jakarta_filtro.properties` | Conexión JDBC usada para filtros SQL, cuando aplique. |
| `properties/mail.properties` | Parámetros de correo y notificaciones. |

`bd.properties` debe conservar las claves Jakarta JDBC existentes:

```properties
jakarta.persistence.jdbc.url=
jakarta.persistence.jdbc.driver=
jakarta.persistence.jdbc.user=
jakarta.persistence.jdbc.password=
```

---

## 📚 Licencia

Este proyecto está distribuido bajo los términos de la licencia MIT. Consulte el archivo `LICENSE` para más detalles.

---

## 📧 Contacto

Para consultas, soporte o sugerencias, puede contactar a:

- **Autor:** Juan Antonio
- **Correo:** [introducir correo aquí]

---

*Desarrollado con el objetivo de facilitar el acceso y análisis de la información pública de forma eficiente y automatizada.*
