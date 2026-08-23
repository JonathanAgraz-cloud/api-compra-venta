package com.jonypacheco.marketplace.persistence.repository;

import com.jonypacheco.marketplace.persistence.domain.entity.ComparableListing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComparableListingRepository extends JpaRepository<ComparableListing, Long> {

    List<ComparableListing> findByProductoNormalizadoAndConfiableTrue(String productoNormalizado);

    // Usado por el motor de analisis para validar la regla de negocio de
    // minimo 5 comparables confiables antes de calcular precio de mercado.
    long countByProductoNormalizadoAndConfiableTrue(String productoNormalizado);

    List<ComparableListing> findBySourceListing_Id(Long listingId);
}
