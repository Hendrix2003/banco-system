package com.banco.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ClienteRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String nombre;

    @Size(max = 20)
    private String genero;

    @Min(value = 0, message = "Edad invalida")
    @Max(value = 120)
    private Integer edad;

    @NotBlank(message = "La identificacion es obligatoria")
    @Size(max = 30)
    private String identificacion;

    @Size(max = 200)
    private String direccion;

    @Size(max = 30)
    private String telefono;

    @NotBlank(message = "El clienteId es obligatorio")
    @Size(max = 30)
    private String clienteId;

    @Size(min = 4, max = 100, message = "El password debe tener al menos 4 caracteres")
    private String password;

    private Boolean estado = true;
}
