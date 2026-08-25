package com.itsqmet.proyectofinalbackedweb2.controller;

import com.itsqmet.proyectofinalbackedweb2.model.Movimiento;
import com.itsqmet.proyectofinalbackedweb2.model.Usuario;
import com.itsqmet.proyectofinalbackedweb2.service.MovimientoService;
import com.itsqmet.proyectofinalbackedweb2.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {

    @Autowired
    private MovimientoService movimientoService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<Movimiento>> obtenerTodo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (esAdministrador(authentication)) {
            return ResponseEntity.ok(movimientoService.obtenerTodos());
        }

        Usuario usuario = usuarioService.obtenerPorEmail(authentication.getName()).orElseThrow();
        return ResponseEntity.ok(movimientoService.obtenerPorUsuario(usuario.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        return movimientoService.obtenerPorId(id)
                .map(movimiento -> ResponseEntity.ok((Object) movimiento))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Movimiento con id " + id + " no encontrado")));
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Movimiento movimiento, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                    errores.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!esAdministrador(authentication)) {
            Usuario usuario = usuarioService.obtenerPorEmail(authentication.getName()).orElseThrow();
            movimiento.setUsuario(usuario);
        }

        return movimientoService.registrar(movimiento)
                .map(error -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", error)))
                .orElse(ResponseEntity.status(HttpStatus.CREATED)
                        .body(Map.of("mensaje", "Movimiento registrado correctamente")));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @Valid @RequestBody Movimiento movimiento,
                                        BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                    errores.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
        }

        return movimientoService.actualizar(id, movimiento)
                .map(error -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", error)))
                .orElse(ResponseEntity.ok(Map.of("mensaje", "Movimiento actualizado correctamente")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (movimientoService.eliminar(id)) {
            return ResponseEntity.ok(Map.of("mensaje", "Movimiento eliminado correctamente"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Movimiento con id " + id + " no encontrado"));
    }

    private boolean esAdministrador(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMINISTRADOR"));
    }
}
