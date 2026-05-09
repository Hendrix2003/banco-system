package com.banco.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReporteResponse {

    private String formato;
    private List<ReporteItem> movimientos;

    // Solo se llena cuando el formato es PDF
    private String pdfBase64;
}
