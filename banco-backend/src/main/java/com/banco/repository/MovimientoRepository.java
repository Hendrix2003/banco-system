package com.banco.repository;

import com.banco.entity.Movimiento;
import com.banco.entity.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    // Movimientos por cuenta y rango de fechas
    @Query("SELECT m FROM Movimiento m WHERE m.cuenta.id = :cuentaId " +
            "AND m.fecha BETWEEN :inicio AND :fin ORDER BY m.fecha ASC")
    List<Movimiento> findByCuentaAndFechas(@Param("cuentaId") Long cuentaId,
                                           @Param("inicio") LocalDateTime inicio,
                                           @Param("fin") LocalDateTime fin);

    // Todos los movimientos de la cuenta ordenados, para recalcular saldos
    @Query("SELECT m FROM Movimiento m WHERE m.cuenta.id = :cuentaId " +
            "ORDER BY m.fecha ASC, m.id ASC")
    List<Movimiento> findByCuentaOrdered(@Param("cuentaId") Long cuentaId);

    // Movimientos por cliente y rango de fechas (para reportes)
    @Query("SELECT m FROM Movimiento m WHERE m.cuenta.cliente.clienteId = :clienteId " +
            "AND m.fecha BETWEEN :inicio AND :fin ORDER BY m.fecha ASC")
    List<Movimiento> findByClienteAndFechas(@Param("clienteId") String clienteId,
                                            @Param("inicio") LocalDateTime inicio,
                                            @Param("fin") LocalDateTime fin);

    // Suma de retiros del dia para validar cupo diario
    @Query("SELECT COALESCE(SUM(ABS(m.valor)), 0) FROM Movimiento m " +
            "WHERE m.cuenta.id = :cuentaId AND m.tipoMovimiento = :tipo " +
            "AND m.fecha BETWEEN :inicio AND :fin")
    java.math.BigDecimal sumValorByTipoYFecha(@Param("cuentaId") Long cuentaId,
                                              @Param("tipo") TipoMovimiento tipo,
                                              @Param("inicio") LocalDateTime inicio,
                                              @Param("fin") LocalDateTime fin);
}
