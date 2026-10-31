package pe.clinicasantaelena.citas.controller;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.clinicasantaelena.citas.dto.FilaReporte;
import pe.clinicasantaelena.citas.exception.RecursoNoEncontradoException;
import pe.clinicasantaelena.citas.report.ReporteExporter;
import pe.clinicasantaelena.citas.service.ReporteService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/** Pantalla P11 - panel de reportes y exportacion (RF10). */
@Controller
@RequestMapping("/admin/reportes")
public class ReporteController {

    private final ReporteService reporteService;
    private final ImmutableMap<String, ReporteExporter> exportadores;
    private final Clock clock;

    /**
     * Spring inyecta todas las implementaciones de {@link ReporteExporter}; se indexan por formato
     * con Guava {@link Maps#uniqueIndex}. Un formato nuevo no requiere tocar esta clase (OCP).
     *
     * @param reporteService servicio de reportes
     * @param exportadores   estrategias de exportacion disponibles
     * @param clock          reloj
     */
    public ReporteController(ReporteService reporteService, List<ReporteExporter> exportadores, Clock clock) {
        this.reporteService = reporteService;
        this.exportadores = Maps.uniqueIndex(exportadores, ReporteExporter::formato);
        this.clock = clock;
    }

    /**
     * @param desde fecha inicial (por defecto inicio de mes)
     * @param hasta fecha final (por defecto fin de mes)
     * @param model modelo
     * @return vista
     */
    @GetMapping
    public String panel(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                        Model model) {
        LocalDate hoy = LocalDate.now(clock);
        LocalDate d = desde == null ? hoy.with(TemporalAdjusters.firstDayOfMonth()).minusMonths(1) : desde;
        LocalDate h = hasta == null ? hoy.with(TemporalAdjusters.lastDayOfMonth()) : hasta;
        List<FilaReporte> filas = reporteService.citasPorEspecialidad(d, h);
        long atendidas = filas.stream().mapToLong(FilaReporte::getAtendidas).sum();
        long noAsistio = filas.stream().mapToLong(FilaReporte::getNoAsistio).sum();
        model.addAttribute("desde", d);
        model.addAttribute("hasta", h);
        model.addAttribute("filas", filas);
        model.addAttribute("total", filas.stream().mapToLong(FilaReporte::getTotal).sum());
        model.addAttribute("atendidas", atendidas);
        model.addAttribute("canceladas", filas.stream().mapToLong(FilaReporte::getCanceladas).sum());
        model.addAttribute("ausentismo", (atendidas + noAsistio) == 0 ? 0.0
                : Math.round(noAsistio * 1000.0 / (atendidas + noAsistio)) / 10.0);
        model.addAttribute("maximo", filas.stream().mapToLong(FilaReporte::getTotal).max().orElse(1));
        model.addAttribute("formatos", exportadores.keySet());
        return "admin/reportes";
    }

    /**
     * Descarga el reporte R01 en el formato pedido.
     *
     * @param formato {@code xlsx}
     * @param desde   fecha inicial
     * @param hasta   fecha final
     * @return archivo adjunto
     */
    @GetMapping("/r01")
    public ResponseEntity<byte[]> exportar(@RequestParam String formato,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        ReporteExporter exporter = exportadores.get(formato);
        if (exporter == null) {
            throw new RecursoNoEncontradoException("Formato no soportado: " + formato);
        }
        byte[] contenido = exporter.exportar(reporteService.citasPorEspecialidad(desde, hasta), desde, hasta);
        String nombre = "R01_citas_por_especialidad_" + desde + "_" + hasta + "." + exporter.formato();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nombre).build().toString())
                .contentType(MediaType.parseMediaType(exporter.contentType()))
                .body(contenido);
    }
}
