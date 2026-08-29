package com.jonypacheco.marketplace.analysis;

import com.jonypacheco.marketplace.analysis.comparable.ComparableSyncService;
import com.jonypacheco.marketplace.analysis.pricing.GananciaClasificador;
import com.jonypacheco.marketplace.analysis.pricing.ProductNormalizer;
import com.jonypacheco.marketplace.analysis.pricing.ProfitCalculation;
import com.jonypacheco.marketplace.analysis.pricing.ProfitCalculator;
import com.jonypacheco.marketplace.persistence.domain.entity.ComparableListing;
import com.jonypacheco.marketplace.persistence.domain.entity.Listing;
import com.jonypacheco.marketplace.persistence.domain.enums.GananciaClasificacion;
import com.jonypacheco.marketplace.persistence.domain.enums.ListingStatus;
import com.jonypacheco.marketplace.persistence.repository.AlertSentRepository;
import com.jonypacheco.marketplace.persistence.repository.ComparableListingRepository;
import com.jonypacheco.marketplace.persistence.repository.ListingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Orquestador del motor de analisis: decide si un {@link Listing} es una
 * oportunidad de reventa (ver seccion 3 de arquitectura-tecnica.md). No
 * envia alertas ni escribe en {@code alerts_sent} -- eso lo hace el futuro
 * modulo de alertas a partir del {@link OpportunityResult} que este
 * servicio produce. No tiene {@code @Scheduled}; un futuro scheduler
 * llamara a {@link #analyzeAllActiveListings()} despues de cada corrida
 * del scraper.
 */
@Service
public class OpportunityAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(OpportunityAnalysisService.class);
    private static final int MINIMO_COMPARABLES = 5;

    private final ListingRepository listingRepository;
    private final ComparableListingRepository comparableListingRepository;
    private final AlertSentRepository alertSentRepository;
    private final ComparableSyncService comparableSyncService;

    public OpportunityAnalysisService(ListingRepository listingRepository,
                                       ComparableListingRepository comparableListingRepository,
                                       AlertSentRepository alertSentRepository,
                                       ComparableSyncService comparableSyncService) {
        this.listingRepository = listingRepository;
        this.comparableListingRepository = comparableListingRepository;
        this.alertSentRepository = alertSentRepository;
        this.comparableSyncService = comparableSyncService;
    }

    /**
     * Sincroniza y analiza todos los listings activos. La sincronizacion de
     * TODOS los comparables ocurre antes de analizar a ninguno, para que
     * cada analisis vea el pool de comparables ya actualizado sin importar
     * el orden de la lista.
     */
    public List<OpportunityResult> analyzeAllActiveListings() {
        List<Listing> activos = listingRepository.findByEstado(ListingStatus.ACTIVE);

        activos.forEach(comparableSyncService::sync);

        List<OpportunityResult> resultados = activos.stream().map(this::analyzeListing).toList();

        long oportunidades = resultados.stream().filter(r -> r.outcome() == AnalysisOutcome.OPPORTUNITY_FOUND).count();
        log.info("Analisis completo: {} listings evaluados, {} oportunidades encontradas", activos.size(), oportunidades);
        return resultados;
    }

    public OpportunityResult analyzeListing(Listing listing) {
        if (listing.getEstado() != ListingStatus.ACTIVE) {
            return OpportunityResult.descartado(listing, AnalysisOutcome.LISTING_NOT_ACTIVE);
        }

        if (alertSentRepository.existsByListing_Id(listing.getId())) {
            return OpportunityResult.descartado(listing, AnalysisOutcome.ALREADY_ALERTED);
        }

        String productoNormalizado = ProductNormalizer.normalize(listing.getCategoria());
        if (productoNormalizado == null) {
            log.warn("Listing {} sin categoria valida, no se puede analizar", listing.getFacebookId());
            return OpportunityResult.descartado(listing, AnalysisOutcome.MISSING_CATEGORIA);
        }

        List<ComparableListing> comparables =
                comparableListingRepository.findComparablesForAnalysis(productoNormalizado, listing.getId());
        if (comparables.size() < MINIMO_COMPARABLES) {
            return OpportunityResult.descartado(listing, AnalysisOutcome.INSUFFICIENT_COMPARABLES, comparables.size());
        }

        List<BigDecimal> precios = comparables.stream().map(ComparableListing::getPrecio).toList();
        ProfitCalculation calculo = ProfitCalculator.calculate(listing.getPrecio(), precios);

        Optional<GananciaClasificacion> clasificacion = GananciaClasificador.clasificar(calculo.gananciaEstimada());
        if (clasificacion.isEmpty()) {
            return OpportunityResult.descartado(listing, AnalysisOutcome.PROFIT_TOO_LOW, comparables.size());
        }

        log.info("Oportunidad encontrada: listing {} ganancia estimada {} ({})",
                listing.getFacebookId(), calculo.gananciaEstimada(), clasificacion.get());
        return OpportunityResult.encontrada(listing, calculo, clasificacion.get(), comparables.size());
    }
}
