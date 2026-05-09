package com.banco.dto;

import lombok.Data;

@Data
public class ClienteResponse {

    private Long id;
    private String nombre;
    private String genero;
    private Integer edad;
    private String identificacion;
    private String direccion;
    private String telefono;
    private String clienteId;
    private Boolean estado;
}
