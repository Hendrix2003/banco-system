package com.banco.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CuentaRequest {

    @NotBlank(message = "El numero de cuenta es obligatorio")
    @Size(max = 20)
    private String numeroCuenta;

    @NotBlank(message = "El tipo de cuenta es obligatorio")
    private String tipoCuenta;

    @NotNull(message = "El saldo inicial es obligatorio")
    private BigDecimal saldoInicial;

    private Boolean estado = true;

    @NotBlank(message = "El clienteId es obligatorio")
    private String clienteId;
}
