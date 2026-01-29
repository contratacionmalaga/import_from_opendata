package local.jarios.managers;

import local.jarios.common.util.Constantes;
import local.jarios.common.util.ConstantesExcel;
import local.jarios.entity.auxiliares.OrganoContratacion;
import local.jarios.exceptions.MiIoException;
import local.jarios.helpers.HttpClientHelper;
import local.jarios.imports.ImportResult;
import local.jarios.mappers.excel.MapperOrganoContratacion;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

@Getter
@Slf4j
public class ManagerSpreeadSheet {

  private static final DataFormatter DATA_FORMATTER = new DataFormatter(Locale.getDefault());
  private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  private ManagerSpreeadSheet() {
    /* no instances */
  }

  /**
   * Lee una fila de Excel y devuelve un array con el valor "visible" de cada celda. Usa
   * DataFormatter para respetar el formato del Excel (ej: ceros a la izquierda).
   */
  public static String[] readRow(Row row, int ncolumnas) {
    String[] values = new String[ncolumnas];

    for (int i = 0; i < ncolumnas; i++) {
      Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
      values[i] = (cell == null) ? Constantes.CADENA_VACIA : DATA_FORMATTER.formatCellValue(cell);
      if (values[i] == null) values[i] = Constantes.CADENA_VACIA;
    }

    log.debug("[readRow] rowNum={} values={}", row.getRowNum(), Arrays.toString(values));
    return values;
  }

  private static <T> List<T> getListaEntitysFromSheet(Sheet sheet, Function<String[], T> mapperFunction) {
    List<T> entityList = new ArrayList<>();

    int startRow = ConstantesExcel.EXCEL_OC_FILA_INICIODATOS;
    int lastRow = sheet.getLastRowNum();

    for (int rowIndex = startRow; rowIndex <= lastRow; rowIndex++) {
      Row row = sheet.getRow(rowIndex);

      // Si la fila es null o vacía, asumimos fin de datos (tu lógica original)
      if (row == null || isRowEmpty(row)) {
        break;
      }

      String[] fila = readRow(row, ConstantesExcel.EXCEL_OC_NCOLUMNAS);
      T entity = mapperFunction.apply(fila);
      entityList.add(entity);
      log.debug("[getListaEntitysFromSheet] rowIndex={} entity={}", rowIndex, entity);
    }

    return entityList;
  }

  /**
   * Fila vacía = todas las celdas (hasta lastCellNum) son BLANK o null.
   */
  private static boolean isRowEmpty(Row row) {
    short last = row.getLastCellNum(); // puede ser -1 si no hay celdas
    if (last <= 0) return true;

    for (int c = 0; c < last; c++) {
      Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
      if (cell == null) continue;

      // Si la celda tiene contenido "visible", no es vacía
      String value = DATA_FORMATTER.formatCellValue(cell);
      if (value != null && !value.isBlank()) {
        return false;
      }
    }
    return true;
  }

  public static List<OrganoContratacion> getListaOcsFromSheet(Sheet sheet) {
    return getListaEntitysFromSheet(sheet, MapperOrganoContratacion::getOc);
  }

  public static <T> ImportResult<T> importarDesdeExcel(
      String url,
      Function<Sheet, List<T>> sheetProcessor
  ) throws MiIoException {

    try (InputStream is = HttpClientHelper.downloadExcel(url);
         Workbook workbook = openWorkbook(url, is)) {

      Sheet sheet = workbook.getSheetAt(0);

      Date fechaGeneracion = extractFechaGeneracion(sheet); // puede ser null
      List<T> lista = sheetProcessor.apply(sheet);

      return new ImportResult<>(lista, fechaGeneracion);

    } catch (Exception ex) {
      log.error("[importarDesdeExcel] Error durante la importación desde URL={}", url, ex);
      throw new MiIoException("Error durante la importación desde Excel: " + url, ex);
    }
  }

  private static Workbook openWorkbook(String url, InputStream is) throws Exception {
    if (url != null && url.toLowerCase(Locale.ROOT).endsWith(".xls")) {
      return new HSSFWorkbook(is);
    }
    return new XSSFWorkbook(is);
  }

  /**
   * Lee la fecha de generación desde C2 (fila 1, columna 2) y la devuelve como java.sql.Date. - Si
   * la celda es fecha Excel: la convierte. - Si es texto: intenta parsear dd/MM/yyyy. - Si no
   * puede: devuelve null (o lanza si prefieres).
   */
  private static Date extractFechaGeneracion(Sheet sheet) {
    // C2 => row=1, col=2
    Row row = sheet.getRow(1);
    if (row == null) return null;

    Cell cell = row.getCell(2, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
    if (cell == null) return null;

    // Caso 1: Excel date (numérica con formato de fecha)
    if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
      java.util.Date utilDate = cell.getDateCellValue();
      LocalDate localDate = utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      return Date.valueOf(localDate);
    }

    // Caso 2: texto con dd/MM/yyyy (u otros formatos visibles)
    String raw = DATA_FORMATTER.formatCellValue(cell);
    if (raw == null || raw.isBlank()) return null;

    // Normaliza espacios
    raw = raw.trim();

    try {
      LocalDate localDate = LocalDate.parse(raw, DMY);
      return Date.valueOf(localDate);
    } catch (DateTimeParseException e) {
      log.warn(
          "[extractFechaGeneracion] No se pudo parsear fechaGeneracion='{}' (esperado dd/MM/yyyy)",
          raw);
      return null; // si prefieres, lanza excepción
    }
  }
}
