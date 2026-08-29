package com.jonypacheco.marketplace.persistence.repository;

import com.jonypacheco.marketplace.persistence.domain.entity.ComparableListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComparableListingRepository extends JpaRepository<ComparableListing, Long> {

    List<ComparableListing> findByProductoNormalizadoAndConfiableTrue(String productoNormalizado);

    // Usado por el motor de analisis para validar la regla de negocio de
    // minimo 5 comparables confiables antes de calcular precio de mercado.
    long countByProductoNormalizadoAndConfiableTrue(String productoNormalizado);

    // Invariante: a lo sumo un comparable por listing origen (lo mantiene
    // ComparableSyncService). Se usa List en vez de Optional por convencion
    // de Spring Data con derived queries sobre relaciones, pero en la
    // practica nunca hay mas de un elemento.
    List<ComparableListing> findBySourceListing_Id(Long listingId);

    // Usado por el motor de analisis para calcular precio de mercado,
    // excluyendo el propio listing evaluado de su propio set de comparables.
    // Se usa @Query explicito (no "...SourceListingIdNot") porque en SQL
    // "NULL <> :id" es UNKNOWN, no true: una derived query excluiria de mas
    // los comparables sin listing origen (sourceListing null).
    @Query("SELECT c FROM ComparableListing c WHERE c.productoNormalizado = :productoNormalizado "
            + "AND c.confiable = true "
            + "AND (c.sourceListing IS NULL OR c.sourceListing.id <> :listingId)")
    List<ComparableListing> findComparablesForAnalysis(@Param("productoNormalizado") String productoNormalizado,
                                                         @Param("listingId") Long listingId);
}
