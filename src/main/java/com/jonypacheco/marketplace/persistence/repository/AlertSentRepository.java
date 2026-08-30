package com.jonypacheco.marketplace.persistence.repository;

import com.jonypacheco.marketplace.persistence.domain.entity.AlertSent;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertSentRepository extends JpaRepository<AlertSent, Long> {

    // Metodo clave de dedupe: el servicio de alertas debe consultarlo antes
    // de intentar enviar, aunque el UNIQUE de la tabla es la garantia real.
    boolean existsByListing_Id(Long listingId);

    Optional<AlertSent> findByListing_Id(Long listingId);

    // Usado por el dashboard (OpportunityController): trae el listing en la
    // misma consulta (EntityGraph) para evitar N+1 al mapear a OpportunityDto.
    // Orden por ganancia descendente para que la oportunidad mas importante
    // aparezca primero.
    @EntityGraph(attributePaths = "listing")
    List<AlertSent> findAllByOrderByGananciaEstimadaDesc();
}
