package com.banco.controller;

import com.banco.dto.ClienteRequest;
import com.banco.dto.ClienteResponse;
import com.banco.exception.NotFoundException;
import com.banco.service.ClienteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClienteService clienteService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void listarClientes_retornaLista() throws Exception {
        ClienteResponse c = new ClienteResponse();
        c.setId(1L);
        c.setNombre("Juan Perez");
        c.setClienteId("jperez");

        when(clienteService.findAll()).thenReturn(List.of(c));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Juan Perez"))
                .andExpect(jsonPath("$[0].clienteId").value("jperez"));
    }

    @Test
    void crearCliente_retornaCreated() throws Exception {
        ClienteRequest req = new ClienteRequest();
        req.setNombre("Maria Lopez");
        req.setIdentificacion("0102030405");
        req.setClienteId("mlopez");
        req.setPassword("1234");
        req.setEdad(30);

        ClienteResponse res = new ClienteResponse();
        res.setId(2L);
        res.setNombre("Maria Lopez");
        res.setClienteId("mlopez");

        when(clienteService.create(any(ClienteRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.nombre").value("Maria Lopez"));
    }

    @Test
    void crearCliente_sinNombreRetorna400() throws Exception {
        ClienteRequest req = new ClienteRequest();
        req.setIdentificacion("0102030405");
        req.setClienteId("mlopez");
        req.setPassword("1234");

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerCliente_inexistenteRetorna404() throws Exception {
        when(clienteService.findById(eq(99L))).thenThrow(new NotFoundException("Cliente no encontrado: 99"));

        mockMvc.perform(get("/api/clientes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarCliente_retorna204() throws Exception {
        doNothing().when(clienteService).delete(1L);

        mockMvc.perform(delete("/api/clientes/1"))
                .andExpect(status().isNoContent());
    }
}
