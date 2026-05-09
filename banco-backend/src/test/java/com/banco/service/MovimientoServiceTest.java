package com.banco.service;

import com.banco.dto.MovimientoRequest;
import com.banco.dto.MovimientoResponse;
import com.banco.entity.Cliente;
import com.banco.entity.Cuenta;
import com.banco.entity.Movimiento;
import com.banco.entity.TipoMovimiento;
import com.banco.exception.BusinessException;
import com.banco.exception.NotFoundException;
import com.banco.repository.CuentaRepository;
import com.banco.repository.MovimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceTest {

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    @InjectMocks
    private MovimientoService movimientoService;

    private Cuenta cuenta;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(movimientoService, "limiteRetiroDiario", new BigDecimal("1000"));

        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Juan Perez");
        cliente.setClienteId("jperez");

        cuenta = new Cuenta();
        cuenta.setId(10L);
        cuenta.setNumeroCuenta("123456");
        cuenta.setTipoCuenta("Ahorros");
        cuenta.setSaldoInicial(new BigDecimal("500.00"));
        cuenta.setEstado(true);
        cuenta.setCliente(cliente);
    }

    @Test
    void deposito_aumentaSaldo() {
        MovimientoRequest req = new MovimientoRequest();
        req.setNumeroCuenta("123456");
        req.setTipoMovimiento(TipoMovimiento.DEPOSITO);
        req.setValor(new BigDecimal("200"));

        when(cuentaRepository.findByNumeroCuenta("123456")).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.findByCuentaAndFechas(eq(10L), any(), any()))
                .thenReturn(Collections.emptyList());
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> {
            Movimiento m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });

        MovimientoResponse res = movimientoService.create(req);

        assertEquals(new BigDecimal("700.00"), res.getSaldo());
        assertEquals(new BigDecimal("200"), res.getValor());
    }

    @Test
    void retiro_descuentaSaldo() {
        MovimientoRequest req = new MovimientoRequest();
        req.setNumeroCuenta("123456");
        req.setTipoMovimiento(TipoMovimiento.RETIRO);
        req.setValor(new BigDecimal("100"));

        when(cuentaRepository.findByNumeroCuenta("123456")).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.findByCuentaAndFechas(eq(10L), any(), any()))
                .thenReturn(Collections.emptyList());
        when(movimientoRepository.sumValorByTipoYFecha(eq(10L), eq(TipoMovimiento.RETIRO), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        MovimientoResponse res = movimientoService.create(req);

        assertEquals(new BigDecimal("400.00"), res.getSaldo());
        assertEquals(new BigDecimal("-100"), res.getValor());
    }

    @Test
    void retiro_sinSaldoLanzaError() {
        MovimientoRequest req = new MovimientoRequest();
        req.setNumeroCuenta("123456");
        req.setTipoMovimiento(TipoMovimiento.RETIRO);
        req.setValor(new BigDecimal("600"));

        when(cuentaRepository.findByNumeroCuenta("123456")).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.findByCuentaAndFechas(eq(10L), any(), any()))
                .thenReturn(Collections.emptyList());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> movimientoService.create(req));
        assertEquals("Saldo no disponible", ex.getMessage());
    }

    @Test
    void retiro_excedeCupoDiarioLanzaError() {
        MovimientoRequest req = new MovimientoRequest();
        req.setNumeroCuenta("123456");
        req.setTipoMovimiento(TipoMovimiento.RETIRO);
        req.setValor(new BigDecimal("200"));

        // saldo suficiente, pero ya retiro 900 hoy
        cuenta.setSaldoInicial(new BigDecimal("5000"));

        Movimiento previo = new Movimiento();
        previo.setValor(new BigDecimal("-900"));
        previo.setSaldo(new BigDecimal("4100"));

        when(cuentaRepository.findByNumeroCuenta("123456")).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.findByCuentaAndFechas(eq(10L), any(), any()))
                .thenReturn(List.of(previo));
        when(movimientoRepository.sumValorByTipoYFecha(eq(10L), eq(TipoMovimiento.RETIRO), any(), any()))
                .thenReturn(new BigDecimal("900"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> movimientoService.create(req));
        assertEquals("Cupo diario excedido", ex.getMessage());
    }

    @Test
    void cuentaNoEncontradaLanzaError() {
        MovimientoRequest req = new MovimientoRequest();
        req.setNumeroCuenta("999999");
        req.setTipoMovimiento(TipoMovimiento.DEPOSITO);
        req.setValor(new BigDecimal("100"));

        when(cuentaRepository.findByNumeroCuenta("999999")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> movimientoService.create(req));
    }

    @Test
    void valorCeroLanzaError() {
        MovimientoRequest req = new MovimientoRequest();
        req.setNumeroCuenta("123456");
        req.setTipoMovimiento(TipoMovimiento.DEPOSITO);
        req.setValor(BigDecimal.ZERO);

        when(cuentaRepository.findByNumeroCuenta("123456")).thenReturn(Optional.of(cuenta));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> movimientoService.create(req));
        assertTrue(ex.getMessage().contains("distinto de cero"));
    }

    @Test
    void update_recalculaSaldosPosteriores() {
        // Saldo inicial 500
        // Mov1: +200 -> 700
        // Mov2: -100 -> 600
        // Si Mov1 cambia a +500 -> Mov1 saldo 1000, Mov2 saldo 900
        Movimiento mov1 = new Movimiento();
        mov1.setId(1L);
        mov1.setTipoMovimiento(TipoMovimiento.DEPOSITO);
        mov1.setValor(new BigDecimal("200"));
        mov1.setSaldo(new BigDecimal("700"));
        mov1.setCuenta(cuenta);

        Movimiento mov2 = new Movimiento();
        mov2.setId(2L);
        mov2.setTipoMovimiento(TipoMovimiento.RETIRO);
        mov2.setValor(new BigDecimal("-100"));
        mov2.setSaldo(new BigDecimal("600"));
        mov2.setCuenta(cuenta);

        when(movimientoRepository.findById(1L)).thenReturn(Optional.of(mov1));
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoRepository.findByCuentaOrdered(10L)).thenReturn(List.of(mov1, mov2));

        MovimientoRequest req = new MovimientoRequest();
        req.setNumeroCuenta("123456");
        req.setTipoMovimiento(TipoMovimiento.DEPOSITO);
        req.setValor(new BigDecimal("500"));

        movimientoService.update(1L, req);

        assertEquals(new BigDecimal("500"), mov1.getValor());
        assertEquals(new BigDecimal("1000.00"), mov1.getSaldo());
        assertEquals(new BigDecimal("900.00"), mov2.getSaldo());
    }

    @Test
    void delete_recalculaSaldosCuenta() {
        // Saldo inicial 500
        // Mov1: +200 -> 700
        // Mov2: -100 -> 600
        // Eliminar Mov1 -> Mov2 saldo 400
        Movimiento mov1 = new Movimiento();
        mov1.setId(1L);
        mov1.setValor(new BigDecimal("200"));
        mov1.setCuenta(cuenta);

        Movimiento mov2 = new Movimiento();
        mov2.setId(2L);
        mov2.setValor(new BigDecimal("-100"));
        mov2.setCuenta(cuenta);

        when(movimientoRepository.findById(1L)).thenReturn(Optional.of(mov1));
        when(movimientoRepository.findByCuentaOrdered(10L)).thenReturn(List.of(mov2));
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        movimientoService.delete(1L);

        verify(movimientoRepository).delete(mov1);
        assertEquals(new BigDecimal("400.00"), mov2.getSaldo());
    }
}
