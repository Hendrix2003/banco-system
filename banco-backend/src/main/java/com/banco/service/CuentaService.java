package com.banco.service;

import com.banco.dto.CuentaRequest;
import com.banco.dto.CuentaResponse;
import com.banco.entity.Cliente;
import com.banco.entity.Cuenta;
import com.banco.exception.BusinessException;
import com.banco.exception.NotFoundException;
import com.banco.repository.ClienteRepository;
import com.banco.repository.CuentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final ClienteRepository clienteRepository;

    public CuentaService(CuentaRepository cuentaRepository, ClienteRepository clienteRepository) {
        this.cuentaRepository = cuentaRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public List<CuentaResponse> findAll() {
        return cuentaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CuentaResponse findById(Long id) {
        Cuenta cuenta = cuentaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada: " + id));
        return toResponse(cuenta);
    }

    // Crea cuenta asociada a un cliente
    @Transactional
    public CuentaResponse create(CuentaRequest request) {
        if (cuentaRepository.existsByNumeroCuenta(request.getNumeroCuenta())) {
            throw new BusinessException("Ya existe una cuenta con ese numero");
        }

        Cliente cliente = clienteRepository.findByClienteId(request.getClienteId())
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado: " + request.getClienteId()));

        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(request.getNumeroCuenta());
        cuenta.setTipoCuenta(request.getTipoCuenta());
        cuenta.setSaldoInicial(request.getSaldoInicial());
        cuenta.setEstado(request.getEstado() != null ? request.getEstado() : true);
        cuenta.setCliente(cliente);

        return toResponse(cuentaRepository.save(cuenta));
    }

    @Transactional
    public CuentaResponse update(Long id, CuentaRequest request) {
        Cuenta cuenta = cuentaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada: " + id));

        // Validamos numero de cuenta duplicado solo si cambia
        if (!cuenta.getNumeroCuenta().equals(request.getNumeroCuenta())
                && cuentaRepository.existsByNumeroCuenta(request.getNumeroCuenta())) {
            throw new BusinessException("Numero de cuenta ya existe");
        }

        // Si se cambia el cliente, validamos que exista
        if (request.getClienteId() != null
                && !request.getClienteId().equals(cuenta.getCliente().getClienteId())) {
            Cliente cliente = clienteRepository.findByClienteId(request.getClienteId())
                    .orElseThrow(() -> new NotFoundException("Cliente no encontrado: " + request.getClienteId()));
            cuenta.setCliente(cliente);
        }

        cuenta.setNumeroCuenta(request.getNumeroCuenta());
        cuenta.setTipoCuenta(request.getTipoCuenta());
        cuenta.setSaldoInicial(request.getSaldoInicial());
        if (request.getEstado() != null) {
            cuenta.setEstado(request.getEstado());
        }

        return toResponse(cuentaRepository.save(cuenta));
    }

    @Transactional
    public void delete(Long id) {
        if (!cuentaRepository.existsById(id)) {
            throw new NotFoundException("Cuenta no encontrada: " + id);
        }
        cuentaRepository.deleteById(id);
    }

    private CuentaResponse toResponse(Cuenta cuenta) {
        CuentaResponse res = new CuentaResponse();
        res.setId(cuenta.getId());
        res.setNumeroCuenta(cuenta.getNumeroCuenta());
        res.setTipoCuenta(cuenta.getTipoCuenta());
        res.setSaldoInicial(cuenta.getSaldoInicial());
        res.setEstado(cuenta.getEstado());
        if (cuenta.getCliente() != null) {
            res.setClienteId(cuenta.getCliente().getClienteId());
            res.setNombreCliente(cuenta.getCliente().getNombre());
        }
        return res;
    }
}
