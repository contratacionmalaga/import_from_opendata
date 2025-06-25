# Importador de Datos Abiertos de la PLACSP

## 📦 Descripción General

Este proyecto es una solución robusta para la importación, transformación y almacenamiento de datos abiertos publicados por la Plataforma de Contratación del Sector Público (PLACSP), dependiente del Ministerio de Hacienda de España.

Su principal objetivo es automatizar la ingesta de información relativa a licitaciones, adjudicaciones y contrataciones públicas, garantizando su correcta validación, normalización y persistencia en una base de datos relacional.

🔗 [Portal de Datos Abiertos - Ministerio de Hacienda](https://www.hacienda.gob.es/es-ES/GobiernoAbierto/Datos%20Abiertos/Paginas/licitaciones_plataforma_contratacion.aspx)

---

## 🛠️ Tecnologías y Herramientas

| Tecnología             | Versión         | Descripción                                          |
|------------------------|-----------------| ---------------------------------------------------- |
| Java                   | 21.0.7          | Lenguaje principal del proyecto                      |
| MariaDB                | 11.8            | Base de datos relacional con uso de esquemas         |
| Hibernate Core         | 7.0.3           | Framework ORM para persistencia en base de datos     |
| HikariCP               | 6.3.0           | Pool de conexiones eficiente para la base de datos   |
| Lombok                 | 1.18.38         | Reducción de código boilerplate mediante anotaciones |
| SLF4J + Log4J          | 2.0.16 / 2.24.3 | Sistema de registro de logs centralizado y flexible  |
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

Se utiliza PostgreSQL como motor de base de datos, con organización en esquemas lógicos para separar y estructurar la información importada, facilitando su posterior consulta y análisis.

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
4. Persistencia en PostgreSQL usando Hibernate.
5. Registro de eventos, advertencias y errores mediante Log4J.
6. Envío automático de notificaciones por correo en caso de errores o finalización de procesos.

---

## 🔧 Requisitos Previos

- Java JDK 21 o superior.
- Maven instalado.
- PostgreSQL 17.2 configurado.
- Acceso a internet para la resolución de dependencias Maven.

---

## 📑 Instrucciones de Uso

1. Clona el repositorio o descarga el código fuente.
2. Configura el acceso a la base de datos en `application.properties`.
3. Compila y empaqueta el proyecto:

```bash
mvn clean install
```

4. Ejecuta la aplicación siguiendo las instrucciones internas del proyecto.

---

## 🔄 Configuración

El archivo de configuración principal se encuentra en:

```
src/main/resources/application.properties
```

Allí deberá especificar los parámetros de conexión a la base de datos, configuraciones de logs y direcciones de correo si se desea habilitar las notificaciones.

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

