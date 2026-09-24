package com.bazaar.catalogo.categoria;


import com.bazaar.catalogo.categoria.dto.CategoriaArbolResponse;
import com.bazaar.catalogo.categoria.dto.CategoriaRequest;
import com.bazaar.catalogo.categoria.dto.CategoriaResponse;
import com.bazaar.common.exception.BusinessException;
import com.bazaar.common.exception.ResourceNotFoundException;
import com.bazaar.common.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        String slug = resolverSlug(request.slug(), request.nombre());

        if (categoriaRepository.existsBySlug(slug)) {
            throw new BusinessException("Ya existe una categoría con el slug '" + slug + "'");
        }

        Categoria categoriaPadre = resolverPadre(request.categoriaPadreId(), null);

        Categoria categoria = Categoria.builder()
                .nombre(request.nombre())
                .slug(slug)
                .categoriaPadre(categoriaPadre)
                .build();

        return CategoriaMapper.toResponse(categoriaRepository.save(categoria));
    }

    @Override
    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        Categoria categoria = buscarOFallar(id);

        String slug = resolverSlug(request.slug(), request.nombre());
        if (categoriaRepository.existsBySlugAndIdNot(slug, id)) {
            throw new BusinessException("Ya existe una categoría con el slug '" + slug + "'");
        }

        Categoria categoriaPadre = resolverPadre(request.categoriaPadreId(), id);

        categoria.setNombre(request.nombre());
        categoria.setSlug(slug);
        categoria.setCategoriaPadre(categoriaPadre);

        return CategoriaMapper.toResponse(categoria);
    }

    @Override
    public CategoriaResponse obtenerPorId(Long id) {
        return CategoriaMapper.toResponse(buscarOFallar(id));
    }

    @Override
    public Page<CategoriaResponse> listar(Pageable pageable) {
        return categoriaRepository.findAll(pageable).map(CategoriaMapper::toResponse);
    }

    @Override
    public List<CategoriaResponse> listarRaiz() {
        return categoriaRepository.findByCategoriaPadreIsNull().stream()
                .map(CategoriaMapper::toResponse)
                .toList();
    }

    @Override
    public List<CategoriaResponse> listarPorPadre(Long categoriaPadreId) {
        buscarOFallar(categoriaPadreId);
        return categoriaRepository.findByCategoriaPadre_Id(categoriaPadreId).stream()
                .map(CategoriaMapper::toResponse)
                .toList();
    }

    @Override
    public List<CategoriaArbolResponse> obtenerArbol() {
        List<Categoria> todas = categoriaRepository.findAll();
        return todas.stream()
                .filter(c -> c.getCategoriaPadre() == null)
                .map(raiz -> construirNodo(raiz, todas))
                .toList();
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = buscarOFallar(id);

        if (categoriaRepository.countByCategoriaPadre_Id(id) > 0) {
            throw new BusinessException("No se puede eliminar: la categoría tiene subcategorías asociadas");
        }

        try {
            categoriaRepository.delete(categoria);
            categoriaRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            // La FK productos.categoria_id (u otra) impide el borrado
            throw new BusinessException("No se puede eliminar: la categoría está siendo usada por productos u otros registros");
        }
    }

    private CategoriaArbolResponse construirNodo(Categoria categoria, List<Categoria> todas) {
        List<CategoriaArbolResponse> hijos = todas.stream()
                .filter(c -> c.getCategoriaPadre() != null && c.getCategoriaPadre().getId().equals(categoria.getId()))
                .map(hijo -> construirNodo(hijo, todas))
                .toList();

        return new CategoriaArbolResponse(categoria.getId(), categoria.getNombre(), categoria.getSlug(), hijos);
    }

    private Categoria resolverPadre(Long categoriaPadreId, Long idPropia) {
        if (categoriaPadreId == null) {
            return null;
        }
        if (categoriaPadreId.equals(idPropia)) {
            throw new BusinessException("Una categoría no puede ser padre de sí misma");
        }

        Categoria padre = buscarOFallar(categoriaPadreId);

        if (idPropia != null && esDescendiente(padre, idPropia)) {
            throw new BusinessException(
                    "No se puede asignar como padre a una subcategoría propia (referencia circular)");
        }
        return padre;
    }

    private boolean esDescendiente(Categoria posibleDescendiente, Long idAncestro) {
        Categoria actual = posibleDescendiente;
        while (actual != null) {
            if (actual.getId().equals(idAncestro)) {
                return true;
            }
            actual = actual.getCategoriaPadre();
        }
        return false;
    }

    private Categoria buscarOFallar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id " + id));
    }

    private String resolverSlug(String slugSolicitado, String nombre) {
        String base = (slugSolicitado != null && !slugSolicitado.isBlank()) ? slugSolicitado : nombre;
        return SlugUtil.generar(base);
    }
}
