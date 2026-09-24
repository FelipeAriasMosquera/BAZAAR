package com.bazaar.usuarios.repository;

import com.bazaar.usuarios.entity.Direccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DireccionRepository extends JpaRepository<Direccion, Long> {

    List<Direccion> findByUsuarioId(Long usuarioId);

    Optional<Direccion> findByIdAndUsuarioId(Long id, Long usuarioId);

    List<Direccion> findByUsuarioIdAndEsPrincipalTrue(Long usuarioId);
}
