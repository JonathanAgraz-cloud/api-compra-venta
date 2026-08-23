package com.jonypacheco.marketplace.persistence.repository;

import com.jonypacheco.marketplace.persistence.domain.entity.AlertSent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlertSentRepository extends JpaRepository<AlertSent, Long> {

    // Metodo clave de dedupe: el servicio de alertas debe consultarlo antes
    // de intentar enviar, aunque el UNIQUE de la tabla es la garantia real.
    boolean existsByListing_Id(Long listingId);

    Optional<AlertSent> findByListing_Id(Long listingId);
}
