package com.bazaar.subastas.adaptadores.salida;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PujaJpaRepository extends JpaRepository<PujaEntity, Long> {
    List<PujaEntity> findBySubastaIdOrderByMontoDesc(Long subastaId);
}