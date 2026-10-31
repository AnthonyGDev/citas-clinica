package pe.clinicasantaelena.citas.report;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import pe.clinicasantaelena.citas.dto.FilaReporte;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exporta el reporte R01 a Excel (.xlsx) con Apache POI: titulo, encabezados con estilo,
 * filas de datos y una fila de totales con formulas {@code SUM} para que la administracion
 * pueda seguir calculando sobre el archivo.
 */
@Component
public class ExcelReporteExporter implements ReporteExporter {

    private static final String[] COLUMNAS = {
        "Especialidad", "Atendidas", "No asistió", "Canceladas", "Vigentes", "Total", "% Ausentismo"
    };
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public String formato() {
        return "xlsx";
    }

    @Override
    public String contentType() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    @Override
    public byte[] exportar(List<FilaReporte> filas, LocalDate desde, LocalDate hasta) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet hoja = wb.createSheet("R01 Citas por especialidad");
            CellStyle titulo = estiloTitulo(wb);
            CellStyle encabezado = estiloEncabezado(wb);
            CellStyle total = estiloTotal(wb);

            Row r0 = hoja.createRow(0);
            Cell c0 = r0.createCell(0);
            c0.setCellValue("Clínica Santa Elena — R01 Citas por especialidad y estado");
            c0.setCellStyle(titulo);
            hoja.createRow(1).createCell(0)
                .setCellValue("Periodo: " + desde.format(FMT) + " al " + hasta.format(FMT));

            Row cab = hoja.createRow(3);
            for (int i = 0; i < COLUMNAS.length; i++) {
                Cell c = cab.createCell(i);
                c.setCellValue(COLUMNAS[i]);
                c.setCellStyle(encabezado);
            }

            int fila = 4;
            for (FilaReporte f : filas) {
                Row r = hoja.createRow(fila++);
                r.createCell(0).setCellValue(f.getEspecialidad());
                r.createCell(1).setCellValue(f.getAtendidas());
                r.createCell(2).setCellValue(f.getNoAsistio());
                r.createCell(3).setCellValue(f.getCanceladas());
                r.createCell(4).setCellValue(f.getVigentes());
                r.createCell(5).setCellValue(f.getTotal());
                r.createCell(6).setCellValue(f.getPorcentajeAusentismo().doubleValue());
            }

            Row tot = hoja.createRow(fila);
            Cell etiqueta = tot.createCell(0);
            etiqueta.setCellValue("TOTAL");
            etiqueta.setCellStyle(total);
            for (int col = 1; col <= 5; col++) {
                String letra = String.valueOf((char) ('A' + col));
                Cell c = tot.createCell(col);
                c.setCellFormula("SUM(" + letra + "5:" + letra + fila + ")");
                c.setCellStyle(total);
            }
            Cell aus = tot.createCell(6);
            int n = fila + 1;
            aus.setCellFormula("IF((B" + n + "+C" + n + ")=0,0,ROUND(C" + n + "*100/(B" + n + "+C" + n + "),1))");
            aus.setCellStyle(total);

            hoja.createFreezePane(0, 4);
            for (int i = 0; i < COLUMNAS.length; i++) {
                hoja.autoSizeColumn(i);
            }
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el Excel", e);
        }
    }

    private static CellStyle estiloTitulo(Workbook wb) {
        Font f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 14);
        f.setColor(IndexedColors.DARK_TEAL.getIndex());
        CellStyle s = wb.createCellStyle();
        s.setFont(f);
        return s;
    }

    private static CellStyle estiloEncabezado(Workbook wb) {
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.WHITE.getIndex());
        CellStyle s = wb.createCellStyle();
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setBorderBottom(BorderStyle.THIN);
        return s;
    }

    private static CellStyle estiloTotal(Workbook wb) {
        Font f = wb.createFont();
        f.setBold(true);
        CellStyle s = wb.createCellStyle();
        s.setFont(f);
        s.setBorderTop(BorderStyle.THIN);
        return s;
    }
}
