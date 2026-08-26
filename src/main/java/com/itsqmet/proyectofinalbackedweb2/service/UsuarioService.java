package com.itsqmet.proyectofinalbackedweb2.service;

import com.itsqmet.proyectofinalbackedweb2.model.Usuario;
import com.itsqmet.proyectofinalbackedweb2.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Optional<String> registrar(Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            return Optional.of("El email ya está registrado");
        }

        if (usuario.getSaldo() == null) {
            usuario.setSaldo(0.0);
        }

        String passwordHasheado = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(passwordHasheado);

        usuarioRepository.save(usuario);
        return Optional.empty();
    }

    public Optional<String> actualizar(Long id, Usuario datos) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) {
            return Optional.of("El usuario no existe");
        }

        Usuario usuario = usuarioOpt.get();

        usuario.setNombre(datos.getNombre());
        usuario.setEmail(datos.getEmail());
        usuario.setRol(datos.getRol());
        usuario.setSaldo(datos.getSaldo());

        usuarioRepository.save(usuario);
        return Optional.empty();
    }

    public boolean eliminar(Long id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
