package com.itsqmet.proyectofinalbackedweb2.repository;

import com.itsqmet.proyectofinalbackedweb2.model.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByUsuarioId(Long usuarioId);
}
