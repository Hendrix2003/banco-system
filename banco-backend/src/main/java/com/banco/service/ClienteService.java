package com.banco.service;

import com.banco.dto.ClienteRequest;
import com.banco.dto.ClienteResponse;
import com.banco.entity.Cliente;
import com.banco.exception.BusinessException;
import com.banco.exception.NotFoundException;
import com.banco.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> findAll() {
        return clienteRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse findById(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado: " + id));
        return toResponse(cliente);
    }

    @Transactional
    public ClienteResponse create(ClienteRequest request) {
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BusinessException("El password es obligatorio");
        }
        if (clienteRepository.existsByClienteId(request.getClienteId())) {
            throw new BusinessException("Ya existe un cliente con clienteId: " + request.getClienteId());
        }
        if (clienteRepository.existsByIdentificacion(request.getIdentificacion())) {
            throw new BusinessException("Ya existe una persona con identificacion: " + request.getIdentificacion());
        }

        Cliente cliente = new Cliente();
        applyRequest(cliente, request);
        return toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponse update(Long id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado: " + id));

        // Validamos duplicados solo si el campo cambia
        if (!cliente.getClienteId().equals(request.getClienteId())
                && clienteRepository.existsByClienteId(request.getClienteId())) {
            throw new BusinessException("ClienteId ya existe");
        }
        if (!cliente.getIdentificacion().equals(request.getIdentificacion())
                && clienteRepository.existsByIdentificacion(request.getIdentificacion())) {
            throw new BusinessException("Identificacion ya existe");
        }

        // Si no envia password se mantiene el actual
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            request.setPassword(cliente.getPassword());
        }
        applyRequest(cliente, request);
        return toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public void delete(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new NotFoundException("Cliente no encontrado: " + id);
        }
        clienteRepository.deleteById(id);
    }

    private void applyRequest(Cliente cliente, ClienteRequest req) {
        cliente.setNombre(req.getNombre());
        cliente.setGenero(req.getGenero());
        cliente.setEdad(req.getEdad());
        cliente.setIdentificacion(req.getIdentificacion());
        cliente.setDireccion(req.getDireccion());
        cliente.setTelefono(req.getTelefono());
        cliente.setClienteId(req.getClienteId());
        cliente.setPassword(req.getPassword());
        cliente.setEstado(req.getEstado() != null ? req.getEstado() : true);
    }

    private ClienteResponse toResponse(Cliente cliente) {
        ClienteResponse res = new ClienteResponse();
        res.setId(cliente.getId());
        res.setNombre(cliente.getNombre());
        res.setGenero(cliente.getGenero());
        res.setEdad(cliente.getEdad());
        res.setIdentificacion(cliente.getIdentificacion());
        res.setDireccion(cliente.getDireccion());
        res.setTelefono(cliente.getTelefono());
        res.setClienteId(cliente.getClienteId());
        res.setEstado(cliente.getEstado());
        return res;
    }
}
