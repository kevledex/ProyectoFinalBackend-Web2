package com.itsqmet.proyectofinalbackedweb2.controller;

import com.itsqmet.proyectofinalbackedweb2.model.Usuario;
import com.itsqmet.proyectofinalbackedweb2.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<Usuario>> obtenerTodos() {
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        return usuarioService.obtenerPorId(id)
                .map(usuario -> ResponseEntity.ok((Object) usuario))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Usuario con id " + id + " no encontrado")));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Usuario usuario) {
        Optional<String> error = usuarioService.registrar(usuario);
        if (error.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", error.get()));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Usuario creado correctamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody Usuario usuario) {

        Optional<String> error =
                usuarioService.actualizar(id, usuario);

        if (error.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", error.get()));
        }

        return ResponseEntity.ok(
                Map.of("mensaje", "Usuario actualizado correctamente")
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (usuarioService.eliminar(id)) {
            return ResponseEntity.ok(Map.of("mensaje", "Usuario eliminado correctamente"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Usuario con id " + id + " no encontrado"));
    }
}
