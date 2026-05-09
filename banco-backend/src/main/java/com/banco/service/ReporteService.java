package com.banco.service;

import com.banco.dto.ReporteItem;
import com.banco.dto.ReporteResponse;
import com.banco.entity.Cuenta;
import com.banco.entity.Movimiento;
import com.banco.exception.NotFoundException;
import com.banco.repository.ClienteRepository;
import com.banco.repository.MovimientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReporteService {

    private final MovimientoRepository movimientoRepository;
    private final ClienteRepository clienteRepository;
    private final PdfService pdfService;

    public ReporteService(MovimientoRepository movimientoRepository,
                          ClienteRepository clienteRepository,
                          PdfService pdfService) {
        this.movimientoRepository = movimientoRepository;
        this.clienteRepository = clienteRepository;
        this.pdfService = pdfService;
    }

    // Genera reporte de movimientos por cliente y rango de fechas
    @Transactional(readOnly = true)
    public ReporteResponse generar(String clienteId, LocalDate fechaInicio, LocalDate fechaFin, String formato) {
        // Validamos que el cliente exista
        clienteRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado: " + clienteId));

        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);

        List<Movimiento> movimientos = movimientoRepository.findByClienteAndFechas(clienteId, inicio, fin);
        List<ReporteItem> items = new ArrayList<>();
        for (Movimiento m : movimientos) {
            items.add(toItem(m));
        }

        ReporteResponse response = new ReporteResponse();
        response.setFormato(formato);
        response.setMovimientos(items);

        if ("pdf".equalsIgnoreCase(formato)) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String pdf = pdfService.generarReportePdf(
                    items, clienteId, fechaInicio.format(fmt), fechaFin.format(fmt));
            response.setPdfBase64(pdf);
        }
        return response;
    }

    private ReporteItem toItem(Movimiento m) {
        ReporteItem item = new ReporteItem();
        item.setFecha(m.getFecha());
        Cuenta cuenta = m.getCuenta();
        item.setCliente(cuenta.getCliente().getNombre());
        item.setNumeroCuenta(cuenta.getNumeroCuenta());
        item.setTipo(cuenta.getTipoCuenta());
        item.setSaldoInicial(cuenta.getSaldoInicial());
        item.setEstado(cuenta.getEstado());
        item.setMovimiento(m.getValor());
        item.setSaldoDisponible(m.getSaldo());
        return item;
    }
}
