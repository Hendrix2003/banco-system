package com.banco.controller;

import com.banco.dto.ReporteResponse;
import com.banco.service.ReporteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    // /api/reportes?cliente=jdoe&fechaInicio=2024-01-01&fechaFin=2024-01-31&formato=json|pdf
    @GetMapping
    public ReporteResponse generar(
            @RequestParam("cliente") String cliente,
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(value = "formato", defaultValue = "json") String formato) {
        return reporteService.generar(cliente, fechaInicio, fechaFin, formato);
    }
}
