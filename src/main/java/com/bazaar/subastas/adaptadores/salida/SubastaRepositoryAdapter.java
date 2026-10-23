package com.bazaar.subastas.adaptadores.salida;

import com.bazaar.subastas.dominio.*;
import com.bazaar.subastas.puertos.SubastaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SubastaRepositoryAdapter implements SubastaRepositoryPort {

    private final SubastaJpaRepository subastaJpa;
    private final PujaJpaRepository pujaJpa;

    @Override
    public Optional<Subasta> buscarParaActualizar(Long id) {
        return subastaJpa.findByIdForUpdate(id).map(this::aDominio);
    }

    @Override
    public Optional<Subasta> buscarPorId(Long id) {
        return subastaJpa.findById(id).map(this::aDominio);
    }

    @Override
    public List<Subasta> listarPorEstado(EstadoSubasta estado) {
        return subastaJpa.findByEstado(aBd(estado)).stream().map(this::aDominio).toList();
    }

    @Override
    public List<Subasta> buscarPorActivarAntesDe(OffsetDateTime ahora) {
        return subastaJpa.findByEstadoAndFechaInicioLessThanEqual(aBd(EstadoSubasta.PROGRAMADA), ahora)
                .stream().map(this::aDominio).toList();
    }

    @Override
    public List<Subasta> buscarPorCerrarAntesDe(OffsetDateTime ahora) {
        return subastaJpa.findByEstadoAndFechaFinLessThanEqual(aBd(EstadoSubasta.ACTIVA), ahora)
                .stream().map(this::aDominio).toList();
    }

    @Override
    public Subasta guardar(Subasta s) {
        SubastaEntity e = (s.getId() != null)
                ? subastaJpa.findById(s.getId()).orElseGet(SubastaEntity::new)
                : new SubastaEntity();
        e.setProductoId(s.getProductoId());
        e.setPrecioInicial(s.getPrecioInicial());
        e.setPrecioActual(s.getPrecioActual());
        e.setIncrementoMinimo(s.getIncrementoMinimo());
        e.setPrecioReserva(s.getPrecioReserva());
        e.setFechaInicio(s.getFechaInicio());
        e.setFechaFin(s.getFechaFin());
        e.setEstado(aBd(s.getEstado()));
        e.setGanadorId(s.getGanadorId());
        return aDominio(subastaJpa.save(e));
    }

    @Override
    public Puja guardarPuja(Puja p) {
        PujaEntity e = new PujaEntity();
        e.setSubastaId(p.subastaId());
        e.setUsuarioId(p.usuarioId());
        e.setMonto(p.monto());
        e.setFecha(p.fecha());
        return aPuja(pujaJpa.save(e));
    }

    @Override
    public List<Puja> listarPujas(Long subastaId) {
        return pujaJpa.findBySubastaIdOrderByMontoDesc(subastaId).stream().map(this::aPuja).toList();
    }

    private String aBd(EstadoSubasta e) {
        return e.name().toLowerCase();
    }

    private Puja aPuja(PujaEntity g) {
        return new Puja(g.getId(), g.getSubastaId(), g.getUsuarioId(), g.getMonto(), g.getFecha());
    }

    private Subasta aDominio(SubastaEntity e) {
        return new Subasta(e.getId(), e.getProductoId(),
                subastaJpa.findVendedorIdByProductoId(e.getProductoId()),
                e.getPrecioInicial(), e.getPrecioActual(), e.getIncrementoMinimo(), e.getPrecioReserva(),
                e.getFechaInicio(), e.getFechaFin(),
                EstadoSubasta.valueOf(e.getEstado().toUpperCase()), e.getGanadorId());
    }
}