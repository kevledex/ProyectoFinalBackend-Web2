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
import java.util.Optional;

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Optional<Movimiento> movimientoOpt = movimientoService.obtenerPorId(id);
        if (movimientoOpt.isEmpty() || !tieneAcceso(movimientoOpt.get(), authentication)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Movimiento con id " + id + " no encontrado"));
        }

        return ResponseEntity.ok(movimientoOpt.get());
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

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Optional<Movimiento> existenteOpt = movimientoService.obtenerPorId(id);
        if (existenteOpt.isEmpty() || !tieneAcceso(existenteOpt.get(), authentication)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Movimiento con id " + id + " no encontrado"));
        }

        return movimientoService.actualizar(id, movimiento)
                .map(error -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", error)))
                .orElse(ResponseEntity.ok(Map.of("mensaje", "Movimiento actualizado correctamente")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Optional<Movimiento> existenteOpt = movimientoService.obtenerPorId(id);
        if (existenteOpt.isEmpty() || !tieneAcceso(existenteOpt.get(), authentication)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Movimiento con id " + id + " no encontrado"));
        }

        movimientoService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Movimiento eliminado correctamente"));
    }

    private boolean esAdministrador(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMINISTRADOR"));
    }

    private boolean tieneAcceso(Movimiento movimiento, Authentication authentication) {
        if (esAdministrador(authentication)) {
            return true;
        }

        Usuario usuario = usuarioService.obtenerPorEmail(authentication.getName()).orElseThrow();
        return movimiento.getUsuario().getId().equals(usuario.getId());
    }

    @PostMapping("/transferencia")
    public ResponseEntity<?> transferir(
            @RequestBody Map<String, Object> datos) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Usuario usuarioOrigen = usuarioService.obtenerPorEmail(authentication.getName()).orElseThrow();

        Long usuarioDestinoId = Long.valueOf(datos.get("usuarioDestinoId").toString());

        Double monto = Double.valueOf(datos.get("monto").toString());

        String descripcion = datos.get("descripcion").toString();

        Optional<String> error = movimientoService.transferir(usuarioOrigen, usuarioDestinoId, monto, descripcion);

        if (error.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", error.get()));
        }

        return ResponseEntity.ok(
                Map.of("mensaje", "Transferencia realizada correctamente")
        );
    }
}
