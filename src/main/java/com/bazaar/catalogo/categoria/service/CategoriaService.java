package com.bazaar.catalogo.categoria.service;

import com.bazaar.catalogo.categoria.dto.CategoriaArbolResponse;
import com.bazaar.catalogo.categoria.dto.CategoriaRequest;
import com.bazaar.catalogo.categoria.dto.CategoriaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoriaService {

    CategoriaResponse crear(CategoriaRequest request);

    CategoriaResponse actualizar(Long id, CategoriaRequest request);

    CategoriaResponse obtenerPorId(Long id);

    Page<CategoriaResponse> listar(Pageable pageable);

    List<CategoriaResponse> listarRaiz();

    List<CategoriaResponse> listarPorPadre(Long categoriaPadreId);

    List<CategoriaArbolResponse> obtenerArbol();

    void eliminar(Long id);
}
