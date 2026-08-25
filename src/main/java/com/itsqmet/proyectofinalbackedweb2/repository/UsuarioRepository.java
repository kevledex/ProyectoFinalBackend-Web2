package com.itsqmet.proyectofinalbackedweb2.repository;

import com.itsqmet.proyectofinalbackedweb2.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
}
