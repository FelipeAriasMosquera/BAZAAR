package com.bazaar.ordenes.repository;

import com.bazaar.catalogo.entity.Producto;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Ajustes atómicos de stock. Viven aquí (y no en ProductoRepository del
 * módulo catálogo) para no tocar código ajeno.
 *
 * El descuento es un UPDATE condicional en una sola sentencia: si dos
 * checkouts compiten por la última unidad, la BD deja pasar a uno y al otro
 * le devuelve 0 filas afectadas. Con "leer stock, restar, guardar" ambos
 * podrían ver stock=1 y se sobrevendería.
 */
public interface ProductoStockRepository extends Repository<Producto, Long> {

    /** @return 1 si descontó, 0 si no había stock suficiente. */
    @Modifying
    @Query("update Producto p set p.stock = p.stock - :cantidad "
            + "where p.id = :productoId and p.stock >= :cantidad")
    int descontarStock(@Param("productoId") Long productoId, @Param("cantidad") int cantidad);

    @Modifying
    @Query("update Producto p set p.stock = p.stock + :cantidad where p.id = :productoId")
    int reponerStock(@Param("productoId") Long productoId, @Param("cantidad") int cantidad);
}
