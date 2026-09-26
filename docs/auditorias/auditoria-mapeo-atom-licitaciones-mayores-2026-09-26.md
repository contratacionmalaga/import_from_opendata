# Auditoría de mapeo ATOM a persistencia

**Fecha:** 26 de septiembre de 2026  
**Fichero analizado:** `D:/placsp/may/licitacionesPerfilesContratanteCompleto3.atom`  
**Alcance:** entradas ATOM de contratos mayores y su mapeo al modelo JPA. No cubre la estructura del `feed` fuera de `entry` ni los ficheros de consultas preliminares.

## Conclusión

No todos los datos presentes en los `entry` se incorporan al sistema. El fichero contiene **493 entradas**, **194 rutas terminales** y **102 nombres de campo terminales**. Seis campos terminales no se consultan en ningún mapper, varios bloques compuestos se omiten de forma total o parcial, y hay un defecto que guarda un plazo incorrecto.

La mayor parte del contenido principal sí se persiste: identificadores y metadatos ATOM, estado e identificadores del expediente, documentos, proyecto de contratación, CPV, lotes, criterios, condiciones, garantías, resultados y UUID. La revisión se realizó sobre el fichero completo, no únicamente sobre su primera entrada.

## Método

1. Se recorrió el XML completo con `XmlReader`, sin cargar su árbol completo en memoria.
2. Se inventariaron rutas de elementos, atributos, número de apariciones y ejemplos. El inventario técnico queda en `target/atom-audit/` para reproducir el análisis local.
3. Se contrastó cada ruta con el flujo real `FeedHelper -> MapperFeed -> MapperEntry -> mappers.codice` y con las entidades JPA de destino.
4. Se distinguieron: campos no leídos, campos leídos pero aplicados a un destino equivocado, datos que se descartan por diseño y metadatos de atributos no persistidos.

Una aparición indica que el campo está presente en ese número de nodos del fichero; no equivale necesariamente a un expediente distinto, ya que hay colecciones como documentos, criterios y CPV.

## Datos correctamente cubiertos

| Bloque | Cobertura comprobada |
|---|---|
| ATOM `Entry` | `id`, `link/@href`, `title`, `summary` y `updated` se transforman en `Entry`. |
| Identificación del expediente | `ContractFolderStatusCode`, `ContractFolderID`, UUID y los identificadores directos de la entidad contratante se procesan. |
| Documentos | Técnicos, jurídicos, adicionales, generales y de publicaciones: ID, tipo, hash, fichero y URI cuando existen. |
| Proyecto y lotes | Nombre, tipo, subtipo, importes, periodos, ubicación principal, CPV, datos propios de lote y criterios/solvencia de lote. |
| Proceso de licitación | Procedimiento, sistema de contratación, urgencia, presentación, límites de lotes, subasta, umbral y lista corta. |
| Condiciones y resultado | Criterios de adjudicación, garantías, requisitos, financiación, recursos, adjudicación, licitador ganador, contrato e importes. |

Los cuatro campos de sistema de contratación original añadidos recientemente ya están mapeados en `MapperTenderingProcess`. No aparecen en este fichero concreto, por lo que su presencia en origen no se ha podido validar aquí.

## Campos y bloques que no se incorporan

### Prioridad alta

| Ruta ATOM | Apariciones | Situación actual | Consecuencia |
|---|---:|---|---|
| `LocatedContractingParty/ParentLocatedParty` y toda su jerarquía | 493 entradas; profundidad de hasta 8 padres | No existe ninguna llamada a `getParentLocatedParty()` ni entidad relacional que la reciba. | Se pierde la cadena de organismos superiores, sus nombres e identificadores. |
| `TenderingProcess/DocumentAvailabilityPeriod` | 277 | Existe una columna de destino, pero `MapperTenderingProcess` invoca su mapeo con `getTenderSubmissionDeadlinePeriod()` en lugar de `getDocumentAvailabilityPeriod()`. | `document_availability_period` se llena con el plazo de presentación de ofertas; el plazo real no se guarda. |
| `TenderingProcess/ParticipationRequestReceptionPeriod` | 16 | No se lee ni existe un destino en `TenderingProcess`. | Se pierde el plazo de recepción de solicitudes de participación. |

### Prioridad media

| Ruta ATOM | Apariciones | Situación actual | Consecuencia |
|---|---:|---|---|
| `LocatedContractingParty/ActivityCode` | 701 | No se lee. | Se pierde la clasificación de actividad del poder adjudicador. |
| `LocatedContractingParty/Party/PostalAddress/Country/IdentificationCode` y `Name` | 493 cada uno | El mapper de la entidad contratante solo conserva línea, municipio y código postal. | Se pierde país de la dirección del órgano de contratación. |
| `LocatedContractingParty/Party/Contact/Telefax` | 208 | No se lee. | Se pierde el fax de contacto. |
| `ProcurementProject[/Lot]/RealizedLocation/Address/StreetName` | 13 | No se lee. | Se pierde la vía concreta del lugar de ejecución. |
| `ProcurementProject/ContractExtension/OptionValidityPeriod/Description` | 157 | Solo se mapea `OptionsDescription`; este subbloque no se procesa. | Se pierde la descripción de la duración o vigencia de las opciones. |
| `TenderingProcess/TenderSubmissionDeadlinePeriod/Description` | 121 | Solo se guarda fecha y hora. | Se pierde la explicación textual del plazo. |
| `TenderingTerms/Language/ID` | 493 | No se lee ni tiene columna de destino. | Se pierde el idioma de la licitación. |
| `TenderingTerms/TenderRecipientParty/EndpointID` | 431 | No se lee ni tiene columna de destino. | Se pierde el punto de entrada o identificador del destinatario de ofertas. |
| `TendererQualificationRequest/OperatingYearsQuantity` | 3 | No se lee. | Se pierde el mínimo de años de actividad requerido. |
| `TendererQualificationRequest/OperatingYearsDescription` | 3 | No se lee. | Se pierde la descripción del requisito de años de actividad. |

### Observaciones de cardinalidad

- `AllowedSubcontractTerms` se reduce al primer elemento cuando hubiese más de uno.
- `TenderResult/SubcontractTerms` también conserva únicamente el primer elemento.
- Los identificadores de una misma clase (`NIF`, `DIR3`, etc.) se asignan a columnas escalares. Si el origen proporcionase dos identificadores del mismo esquema, el último sobrescribiría al anterior.

En este fichero no se ha detectado una segunda instancia conflictiva de los dos primeros casos; son límites del modelo que deben protegerse con pruebas antes de ampliar el alcance funcional.

## Atributos de origen no persistidos

Los atributos siguientes aportan semántica y actualmente no tienen destino persistente. No impiden utilizar el valor principal del elemento, pero el sistema no puede reconstruir toda la información original.

| Atributo | Apariciones | Situación y relevancia |
|---|---:|---|
| `@currencyID` | 3.293 | No se conserva. En la muestra todos los valores son `EUR`; aun así se pierde la divisa explícita. |
| `@listURI` | 24.210 | No se conserva. Identifica el catálogo o versión de cada código (CPV, NUTS, procedimiento, estado, etc.). |
| `@name` en códigos | 1.246 | No se conserva. Contiene la etiqueta publicada para determinados códigos, por ejemplo programas de financiación o presentación por lotes. |
| `@languageID` de `ContractFolderStatusCode` | 493 | No se conserva; en esta muestra vale `es`. |
| `summary/@type` | 493 | No se conserva; en esta muestra vale `text`. |
| `ProcurementProjectLot/ID/@schemeName` | 358 | El valor de lote se guarda, pero no el esquema `ID_LOTE`. |

`link/@href` sí se guarda. `UUID/@schemeName` sí se guarda. Los `PartyIdentification/ID/@schemeName` se utilizan para decidir en qué columna se almacena el identificador directo. Sin embargo, los esquemas de los organismos de la jerarquía `ParentLocatedParty` también se pierden porque esa jerarquía no se procesa.

## Causa del defecto de plazo

En `MapperTenderingProcess`, el método correcto existe, pero la llamada utiliza el origen equivocado:

```java
mapDocumentAvailabilityPeriod(
    tenderingProcess, tenderingProcessType.getTenderSubmissionDeadlinePeriod());
```

Debe recibir `tenderingProcessType.getDocumentAvailabilityPeriod()`. Este cambio es de bajo riesgo y debe acompañarse de una prueba con ambos plazos distintos para evitar una regresión.

## Propuesta de corrección por fases

1. **Corregir el plazo de disponibilidad** y añadir una prueba unitaria con dos periodos distintos. Es un error de integridad de datos ya existentes.
2. **Añadir los campos escalares de alta utilidad**: actividad, fax, país de órgano contratante, vía de ejecución, idioma, endpoint, plazo de solicitudes y los dos datos de años de actividad. Incluir migración SQL versionada y pruebas de mapper.
3. **Modelar `ParentLocatedParty` como entidad jerárquica** asociada a `ContractFolderStatus`, con nombre, identificadores, padre y orden. No debe comprimirse en columnas fijas porque el fichero llega a ocho niveles.
4. **Decidir la retención de atributos semánticos**. Para importación analítica, como mínimo conviene conservar `currencyID`; para trazabilidad completa, guardar también catálogo (`listURI`) y etiqueta (`name`) en las entidades de código o en una tabla de metadatos.
5. **Añadir una prueba de cobertura de muestra**: cargar este ATOM en un test de integración y afirmar la presencia de cada ruta soportada y la ausencia explícitamente aceptada de las no soportadas. El inventario generado debe actualizarse cuando cambie la versión CÓDICE.

## Resultado de la auditoría

El mapeo actual es funcional para el núcleo del expediente, pero **no es una copia completa de la estructura de cada `entry`**. Antes de afirmar cobertura total deben corregirse, como mínimo, el error de `DocumentAvailabilityPeriod`, la jerarquía `ParentLocatedParty` y los campos de prioridad media enumerados.
