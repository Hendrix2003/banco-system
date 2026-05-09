package com.banco.service;

import com.banco.dto.MovimientoRequest;
import com.banco.dto.MovimientoResponse;
import com.banco.entity.Cuenta;
import com.banco.entity.Movimiento;
import com.banco.entity.TipoMovimiento;
import com.banco.exception.BusinessException;
import com.banco.exception.NotFoundException;
import com.banco.repository.CuentaRepository;
import com.banco.repository.MovimientoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class MovimientoService {

    private static final Logger log = LoggerFactory.getLogger(MovimientoService.class);

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;

    // Limite diario (se puede cambiar en application.properties)
    @Value("${banco.limite-retiro-diario:1000}")
    private BigDecimal limiteRetiroDiario;

    public MovimientoService(MovimientoRepository movimientoRepository, CuentaRepository cuentaRepository) {
        this.movimientoRepository = movimientoRepository;
        this.cuentaRepository = cuentaRepository;
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponse> findAll() {
        return movimientoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MovimientoResponse findById(Long id) {
        Movimiento m = movimientoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Movimiento no encontrado: " + id));
        return toResponse(m);
    }

    // Crea movimiento aplicando las reglas de negocio
    @Transactional
    public MovimientoResponse create(MovimientoRequest request) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(request.getNumeroCuenta())
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada: " + request.getNumeroCuenta()));

        if (Boolean.FALSE.equals(cuenta.getEstado())) {
            throw new BusinessException("La cuenta esta inactiva");
        }

        BigDecimal valor = request.getValor();
        if (valor == null || valor.compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException("El valor debe ser distinto de cero");
        }

        // Calculamos saldo actual a partir del saldo inicial + movimientos previos
        BigDecimal saldoActual = calcularSaldoActual(cuenta);

        BigDecimal valorMovimiento;
        if (request.getTipoMovimiento() == TipoMovimiento.DEPOSITO) {
            valorMovimiento = valor.abs();
        } else {
            // Retiro
            valorMovimiento = valor.abs().negate();
            BigDecimal montoRetiro = valor.abs();

            if (saldoActual.compareTo(montoRetiro) < 0) {
                throw new BusinessException("Saldo no disponible");
            }
            validarCupoDiario(cuenta, montoRetiro);
        }

        BigDecimal nuevoSaldo = saldoActual.add(valorMovimiento);

        Movimiento movimiento = new Movimiento();
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setTipoMovimiento(request.getTipoMovimiento());
        movimiento.setValor(valorMovimiento);
        movimiento.setSaldo(nuevoSaldo);
        movimiento.setCuenta(cuenta);

        Movimiento guardado = movimientoRepository.save(movimiento);
        log.info("Movimiento registrado en cuenta {} valor {} saldo {}",
                cuenta.getNumeroCuenta(), valorMovimiento, nuevoSaldo);
        return toResponse(guardado);
    }

    @Transactional
    public MovimientoResponse update(Long id, MovimientoRequest request) {
        Movimiento movimiento = movimientoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Movimiento no encontrado: " + id));

        // Solo permitimos editar el valor y tipo, manteniendo la cuenta original
        BigDecimal valor = request.getValor();
        if (request.getTipoMovimiento() == TipoMovimiento.RETIRO) {
            valor = valor.abs().negate();
        } else {
            valor = valor.abs();
        }
        movimiento.setTipoMovimiento(request.getTipoMovimiento());
        movimiento.setValor(valor);
        movimientoRepository.save(movimiento);

        // Recalculamos saldos de la cuenta para mantener consistencia
        recalcularSaldosCuenta(movimiento.getCuenta());

        return toResponse(movimiento);
    }

    @Transactional
    public void delete(Long id) {
        Movimiento movimiento = movimientoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Movimiento no encontrado: " + id));
        Cuenta cuenta = movimiento.getCuenta();
        movimientoRepository.delete(movimiento);
        recalcularSaldosCuenta(cuenta);
    }

    // Recorre los movimientos de la cuenta en orden y reescribe el saldo
    private void recalcularSaldosCuenta(Cuenta cuenta) {
        List<Movimiento> movimientos = movimientoRepository.findByCuentaOrdered(cuenta.getId());
        BigDecimal saldo = cuenta.getSaldoInicial();
        for (Movimiento m : movimientos) {
            saldo = saldo.add(m.getValor());
            m.setSaldo(saldo);
            movimientoRepository.save(m);
        }
    }

    // Calcula saldo actual sumando saldo inicial mas movimientos previos
    private BigDecimal calcularSaldoActual(Cuenta cuenta) {
        BigDecimal saldo = cuenta.getSaldoInicial();
        LocalDateTime hace100Anios = LocalDateTime.now().minusYears(100);
        LocalDateTime ahora = LocalDateTime.now();
        List<Movimiento> movimientos = movimientoRepository.findByCuentaAndFechas(
                cuenta.getId(), hace100Anios, ahora);
        for (Movimiento m : movimientos) {
            saldo = saldo.add(m.getValor());
        }
        return saldo;
    }

    // Valida que no se exceda el cupo diario de retiro
    private void validarCupoDiario(Cuenta cuenta, BigDecimal montoRetiro) {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicio = hoy.atStartOfDay();
        LocalDateTime fin = hoy.atTime(LocalTime.MAX);

        BigDecimal totalRetiradoHoy = movimientoRepository.sumValorByTipoYFecha(
                cuenta.getId(), TipoMovimiento.RETIRO, inicio, fin);
        if (totalRetiradoHoy == null) {
            totalRetiradoHoy = BigDecimal.ZERO;
        }

        BigDecimal proyectado = totalRetiradoHoy.add(montoRetiro);
        if (proyectado.compareTo(limiteRetiroDiario) > 0) {
            throw new BusinessException("Cupo diario excedido");
        }
    }

    private MovimientoResponse toResponse(Movimiento m) {
        MovimientoResponse res = new MovimientoResponse();
        res.setId(m.getId());
        res.setFecha(m.getFecha());
        res.setTipoMovimiento(m.getTipoMovimiento());
        res.setValor(m.getValor());
        res.setSaldo(m.getSaldo());
        if (m.getCuenta() != null) {
            res.setNumeroCuenta(m.getCuenta().getNumeroCuenta());
        }
        return res;
    }
}
