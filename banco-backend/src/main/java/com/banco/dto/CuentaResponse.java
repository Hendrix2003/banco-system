package com.banco.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CuentaResponse {

    private Long id;
    private String numeroCuenta;
    private String tipoCuenta;
    private BigDecimal saldoInicial;
    private Boolean estado;
    private String clienteId;
    private String nombreCliente;
}
