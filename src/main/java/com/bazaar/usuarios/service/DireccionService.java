package com.bazaar.usuarios.service;

import com.bazaar.common.ResourceNotFoundException;
import com.bazaar.usuarios.dto.DireccionRequest;
import com.bazaar.usuarios.entity.Direccion;
import com.bazaar.usuarios.entity.Usuario;
import com.bazaar.usuarios.repository.DireccionRepository;
import com.bazaar.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DireccionService {

    private final DireccionRepository direccionRepository;
    private final UsuarioRepository usuarioRepository;

    public DireccionService(DireccionRepository direccionRepository, UsuarioRepository usuarioRepository) {
        this.direccionRepository = direccionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Direccion crear(Long usuarioId, DireccionRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + usuarioId));

        // Si es la primera dirección del usuario, queda como principal aunque no se pida explícitamente.
        boolean esPrimeraDireccion = direccionRepository.findByUsuarioId(usuarioId).isEmpty();
        boolean esPrincipal = Boolean.TRUE.equals(request.getEsPrincipal()) || esPrimeraDireccion;

        if (esPrincipal) {
            desmarcarPrincipalActual(usuarioId);
        }

        Direccion direccion = new Direccion();
        direccion.setUsuario(usuario);
        direccion.setAlias(request.getAlias());
        direccion.setCalle(request.getCalle());
        direccion.setCiudad(request.getCiudad());
        direccion.setDepartamento(request.getDepartamento());
        direccion.setPais(request.getPais());
        direccion.setCodigoPostal(request.getCodigoPostal());
        direccion.setEsPrincipal(esPrincipal);

        return direccionRepository.save(direccion);
    }

    @Transactional(readOnly = true)
    public List<Direccion> listarPorUsuario(Long usuarioId) {
        validarUsuarioExiste(usuarioId);
        return direccionRepository.findByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public Direccion obtener(Long usuarioId, Long direccionId) {
        return direccionRepository.findByIdAndUsuarioId(direccionId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dirección no encontrada: " + direccionId + " para el usuario " + usuarioId));
    }

    @Transactional
    public Direccion actualizar(Long usuarioId, Long direccionId, DireccionRequest request) {
        Direccion direccion = obtener(usuarioId, direccionId);

        direccion.setAlias(request.getAlias());
        direccion.setCalle(request.getCalle());
        direccion.setCiudad(request.getCiudad());
        direccion.setDepartamento(request.getDepartamento());
        direccion.setPais(request.getPais());
        direccion.setCodigoPostal(request.getCodigoPostal());

        if (Boolean.TRUE.equals(request.getEsPrincipal()) && !direccion.isEsPrincipal()) {
            desmarcarPrincipalActual(usuarioId);
            direccion.setEsPrincipal(true);
        }

        return direccionRepository.save(direccion);
    }

    @Transactional
    public void marcarComoPrincipal(Long usuarioId, Long direccionId) {
        Direccion direccion = obtener(usuarioId, direccionId);
        desmarcarPrincipalActual(usuarioId);
        direccion.setEsPrincipal(true);
        direccionRepository.save(direccion);
    }

    @Transactional
    public void eliminar(Long usuarioId, Long direccionId) {
        Direccion direccion = obtener(usuarioId, direccionId);
        direccionRepository.delete(direccion);
    }

    private void validarUsuarioExiste(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario no encontrado: " + usuarioId);
        }
    }

    private void desmarcarPrincipalActual(Long usuarioId) {
        direccionRepository.findByUsuarioIdAndEsPrincipalTrue(usuarioId)
                .forEach(d -> {
                    d.setEsPrincipal(false);
                    direccionRepository.save(d);
                });
    }
}
