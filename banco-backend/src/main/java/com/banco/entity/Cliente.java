package com.banco.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cliente")
@Getter
@Setter
public class Cliente extends Persona {

    @Column(name = "cliente_id", nullable = false, unique = true, length = 30)
    private String clienteId;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false)
    private Boolean estado = true;
}
