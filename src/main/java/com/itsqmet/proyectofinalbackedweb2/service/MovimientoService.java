package com.itsqmet.proyectofinalbackedweb2.service;

import com.itsqmet.proyectofinalbackedweb2.model.Movimiento;
import com.itsqmet.proyectofinalbackedweb2.model.Usuario;
import com.itsqmet.proyectofinalbackedweb2.repository.MovimientoRepository;
import com.itsqmet.proyectofinalbackedweb2.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MovimientoService {

    @Autowired
    private MovimientoRepository movimientoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Movimiento> obtenerTodos() {
        return movimientoRepository.findAll();
    }

    public List<Movimiento> obtenerPorUsuario(Long usuarioId) {
        return movimientoRepository.findByUsuarioId(usuarioId);
    }

    public Optional<Movimiento> obtenerPorId(Long id) {
        return movimientoRepository.findById(id);
    }

    public Optional<String> registrar(Movimiento movimiento) {
        if (movimiento.getUsuario() == null || movimiento.getUsuario().getId() == null) {
            return Optional.of("El usuario del movimiento es obligatorio");
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(movimiento.getUsuario().getId());
        if (usuarioOpt.isEmpty()) {
            return Optional.of("El usuario no existe");
        }

        Usuario usuario = usuarioOpt.get();

        if (movimiento.getTipo().equalsIgnoreCase("RETIRO") && usuario.getSaldo() < movimiento.getMonto()) {
            return Optional.of("El saldo es insuficiente para realizar el retiro");
        }

        double nuevoSaldo;
        if (movimiento.getTipo().equalsIgnoreCase("DEPOSITO")) {
            nuevoSaldo = usuario.getSaldo() + movimiento.getMonto();
        } else {
            nuevoSaldo = usuario.getSaldo() - movimiento.getMonto();
        }

        usuario.setSaldo(nuevoSaldo);
        usuarioRepository.save(usuario);

        movimiento.setUsuario(usuario);
        movimiento.setFecha(LocalDateTime.now());
        movimientoRepository.save(movimiento);

        return Optional.empty();
    }

    public Optional<String> actualizar(Long id, Movimiento datosActualizados) {
        Optional<Movimiento> movimientoOpt = movimientoRepository.findById(id);
        if (movimientoOpt.isEmpty()) {
            return Optional.of("El movimiento no existe");
        }

        Movimiento movimiento = movimientoOpt.get();
        Usuario usuario = movimiento.getUsuario();

        double saldoSinMovimiento;
        if (movimiento.getTipo().equalsIgnoreCase("DEPOSITO")) {
            saldoSinMovimiento = usuario.getSaldo() - movimiento.getMonto();
        } else {
            saldoSinMovimiento = usuario.getSaldo() + movimiento.getMonto();
        }

        if (datosActualizados.getTipo().equalsIgnoreCase("RETIRO") && saldoSinMovimiento < datosActualizados.getMonto()) {
            return Optional.of("El saldo es insuficiente para realizar el retiro");
        }

        double nuevoSaldo;
        if (datosActualizados.getTipo().equalsIgnoreCase("DEPOSITO")) {
            nuevoSaldo = saldoSinMovimiento + datosActualizados.getMonto();
        } else {
            nuevoSaldo = saldoSinMovimiento - datosActualizados.getMonto();
        }

        usuario.setSaldo(nuevoSaldo);
        usuarioRepository.save(usuario);

        movimiento.setTipo(datosActualizados.getTipo());
        movimiento.setMonto(datosActualizados.getMonto());
        movimiento.setDescripcion(datosActualizados.getDescripcion());
        movimientoRepository.save(movimiento);

        return Optional.empty();
    }

    public boolean eliminar(Long id) {
        Optional<Movimiento> movimientoOpt = movimientoRepository.findById(id);
        if (movimientoOpt.isEmpty()) {
            return false;
        }

        Movimiento movimiento = movimientoOpt.get();
        Usuario usuario = movimiento.getUsuario();

        double saldoRevertido;
        if (movimiento.getTipo().equalsIgnoreCase("DEPOSITO")) {
            saldoRevertido = usuario.getSaldo() - movimiento.getMonto();
        } else {
            saldoRevertido = usuario.getSaldo() + movimiento.getMonto();
        }

        usuario.setSaldo(saldoRevertido);
        usuarioRepository.save(usuario);

        movimientoRepository.deleteById(id);
        return true;
    }
}
