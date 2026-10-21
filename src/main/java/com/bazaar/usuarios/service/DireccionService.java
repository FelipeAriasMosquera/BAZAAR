package com.bazaar.usuarios.service;

import com.bazaar.common.exception.ResourceNotFoundException;
import com.bazaar.usuarios.dto.DireccionRequest;
import com.bazaar.usuarios.entity.Direccion;
import com.bazaar.usuarios.model.Usuario;
import com.bazaar.usuarios.repository.DireccionRepository;
import com.bazaar.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Objects;

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
    public Direccion crear(DireccionRequest request) {
        Usuario usuario = usuarioAutenticado();
        Long usuarioId = usuario.getId();

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
    public List<Direccion> listar() {
        return direccionRepository.findByUsuarioId(usuarioAutenticado().getId());
    }

    @Transactional(readOnly = true)
    public Direccion obtener(Long direccionId) {
        return buscarDelUsuario(usuarioAutenticado().getId(), direccionId);
    }

    @Transactional
    public Direccion actualizar(Long direccionId, DireccionRequest request) {
        Long usuarioId = usuarioAutenticado().getId();
        Direccion direccion = buscarDelUsuario(usuarioId, direccionId);

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
    public void marcarComoPrincipal(Long direccionId) {
        Long usuarioId = usuarioAutenticado().getId();
        Direccion direccion = buscarDelUsuario(usuarioId, direccionId);
        desmarcarPrincipalActual(usuarioId);
        direccion.setEsPrincipal(true);
        direccionRepository.save(direccion);
    }

    @Transactional
    public void eliminar(Long direccionId) {
        direccionRepository.delete(buscarDelUsuario(usuarioAutenticado().getId(), direccionId));
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

    private Direccion buscarDelUsuario(Long usuarioId, Long direccionId) {
        return direccionRepository.findByIdAndUsuarioId(direccionId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dirección no encontrada: " + direccionId));
    }

    private Usuario usuarioAutenticado() {
        Authentication auth = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication());
        String email = auth.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado: " + email));
    }
}
