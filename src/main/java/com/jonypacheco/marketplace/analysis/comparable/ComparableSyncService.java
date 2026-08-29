package com.jonypacheco.marketplace.analysis.comparable;

import com.jonypacheco.marketplace.analysis.pricing.ProductNormalizer;
import com.jonypacheco.marketplace.persistence.domain.entity.ComparableListing;
import com.jonypacheco.marketplace.persistence.domain.entity.Listing;
import com.jonypacheco.marketplace.persistence.repository.ComparableListingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Mantiene la tabla {@code comparables} sincronizada con los {@link Listing}
 * scrapeados: cada listing activo aporta un punto de referencia de precio
 * de mercado para su categoria. Sin esto, el motor de analisis nunca podria
 * juntar los "minimo 5 comparables confiables" que exige la regla de
 * negocio, porque el scraper solo escribe en {@code listings}.
 * <p>
 * Simplificacion documentada: no distingue entre {@code ACTIVE}/{@code SOLD}/
 * {@code REMOVED} porque el scraper hoy nunca marca un listing como vendido
 * o eliminado (esa deteccion es una mejora futura ya anotada en el modulo
 * scraper) -- todo listing sincronizado aqui queda {@code confiable = true}.
 */
@Service
public class ComparableSyncService {

    private static final Logger log = LoggerFactory.getLogger(ComparableSyncService.class);

    private final ComparableListingRepository comparableListingRepository;

    public ComparableSyncService(ComparableListingRepository comparableListingRepository) {
        this.comparableListingRepository = comparableListingRepository;
    }

    public void sync(Listing listing) {
        String productoNormalizado = ProductNormalizer.normalize(listing.getCategoria());
        if (productoNormalizado == null) {
            log.warn("Listing {} sin categoria valida, no se sincroniza como comparable", listing.getFacebookId());
            return;
        }

        Optional<ComparableListing> existente = comparableListingRepository.findBySourceListing_Id(listing.getId())
                .stream()
                .findFirst();

        if (existente.isPresent()) {
            actualizar(existente.get(), listing, productoNormalizado);
        } else {
            crear(listing, productoNormalizado);
        }
    }

    private void actualizar(ComparableListing comparable, Listing listing, String productoNormalizado) {
        comparable.setProductoNormalizado(productoNormalizado);
        comparable.setCategoria(listing.getCategoria());
        comparable.setPrecio(listing.getPrecio());
        comparable.setZona(listing.getZona());
        comparable.setUrl(listing.getUrl());
        comparable.setConfiable(true);
        comparableListingRepository.save(comparable);
    }

    private void crear(Listing listing, String productoNormalizado) {
        ComparableListing comparable = new ComparableListing(productoNormalizado, listing.getCategoria(), listing.getPrecio());
        comparable.setZona(listing.getZona());
        comparable.setSourceListing(listing);
        comparable.setUrl(listing.getUrl());
        comparableListingRepository.save(comparable);
    }
}
