package com.jonypacheco.marketplace.scraper.ingest;

import com.jonypacheco.marketplace.persistence.domain.entity.Listing;
import com.jonypacheco.marketplace.persistence.domain.entity.SearchConfig;
import com.jonypacheco.marketplace.persistence.domain.enums.ListingStatus;
import com.jonypacheco.marketplace.persistence.domain.enums.ZonaMerida;
import com.jonypacheco.marketplace.persistence.repository.ListingRepository;
import com.jonypacheco.marketplace.scraper.parsing.ListingIdExtractor;
import com.jonypacheco.marketplace.scraper.parsing.PriceParser;
import com.jonypacheco.marketplace.scraper.parsing.RawListingCard;
import com.jonypacheco.marketplace.scraper.parsing.ZonaMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Convierte una {@link RawListingCard} cruda en un {@link Listing} persistido
 * (nuevo o actualizado). Si el {@code facebook_id} ya existe, actualiza en
 * vez de ignorar (regla de negocio confirmada: precio/estado se refrescan
 * cada vez que el anuncio reaparece en resultados de busqueda). La
 * {@code categoria} siempre viene del {@link SearchConfig} que disparo la
 * busqueda, nunca del texto scrapeado.
 */
@Service
public class ListingIngestService {

    private static final Logger log = LoggerFactory.getLogger(ListingIngestService.class);

    private final ListingRepository listingRepository;

    public ListingIngestService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    public IngestOutcome ingest(RawListingCard raw, SearchConfig config) {
        Optional<String> facebookId = ListingIdExtractor.extract(raw.rawUrl());
        if (facebookId.isEmpty()) {
            log.warn("Tarjeta omitida: no se pudo extraer facebook_id de la URL '{}'", raw.rawUrl());
            return IngestOutcome.SKIPPED_INVALID_ID;
        }

        Optional<BigDecimal> precio = PriceParser.parse(raw.rawPrice());
        if (precio.isEmpty()) {
            log.warn("Tarjeta omitida: no se pudo parsear el precio del anuncio '{}'", raw.rawUrl());
            return IngestOutcome.SKIPPED_INVALID_PRICE;
        }

        ZonaMerida zona = ZonaMapper.map(raw.rawLocationText());

        Optional<Listing> existente = listingRepository.findByFacebookId(facebookId.get());
        if (existente.isPresent()) {
            actualizar(existente.get(), raw, precio.get(), zona);
            return IngestOutcome.UPDATED;
        }

        crear(facebookId.get(), raw, precio.get(), zona, config);
        return IngestOutcome.NEW;
    }

    private void actualizar(Listing listing, RawListingCard raw, BigDecimal precio, ZonaMerida zona) {
        listing.setTitulo(raw.rawTitle());
        listing.setPrecio(precio);
        listing.setZona(zona);
        listing.setUbicacionRaw(raw.rawLocationText());
        listing.setUrl(raw.rawUrl());
        listing.setImagenUrl(raw.rawImageUrl());
        listing.setEstado(ListingStatus.ACTIVE);
        listingRepository.save(listing);
    }

    private void crear(String facebookId, RawListingCard raw, BigDecimal precio, ZonaMerida zona, SearchConfig config) {
        Listing listing = new Listing(facebookId, raw.rawTitle(), precio, zona, raw.rawUrl());
        listing.setCategoria(config.getCategoria());
        listing.setUbicacionRaw(raw.rawLocationText());
        listing.setImagenUrl(raw.rawImageUrl());
        listingRepository.save(listing);
    }
}
