package com.jonypacheco.marketplace.analysis;

import com.jonypacheco.marketplace.analysis.comparable.ComparableSyncService;
import com.jonypacheco.marketplace.persistence.domain.entity.ComparableListing;
import com.jonypacheco.marketplace.persistence.domain.entity.Listing;
import com.jonypacheco.marketplace.persistence.domain.enums.GananciaClasificacion;
import com.jonypacheco.marketplace.persistence.domain.enums.ListingStatus;
import com.jonypacheco.marketplace.persistence.domain.enums.ZonaMerida;
import com.jonypacheco.marketplace.persistence.repository.AlertSentRepository;
import com.jonypacheco.marketplace.persistence.repository.ComparableListingRepository;
import com.jonypacheco.marketplace.persistence.repository.ListingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpportunityAnalysisServiceTest {

    @Mock
    private ListingRepository listingRepository;
    @Mock
    private ComparableListingRepository comparableListingRepository;
    @Mock
    private AlertSentRepository alertSentRepository;
    @Mock
    private ComparableSyncService comparableSyncService;

    private OpportunityAnalysisService analysisService;

    private Listing listingActivo(BigDecimal precio) {
        Listing listing = new Listing("999", "iPhone 12 128GB", precio, ZonaMerida.ALTABRISA,
                "https://www.facebook.com/marketplace/item/999/");
        listing.setCategoria("iPhone 12 128GB");
        ReflectionTestUtils.setField(listing, "id", 10L);
        return listing;
    }

    private List<ComparableListing> comparables(String... precios) {
        return java.util.Arrays.stream(precios)
                .map(p -> new ComparableListing("iphone 12 128gb", "iPhone 12 128GB", new BigDecimal(p)))
                .toList();
    }

    @Test
    void descartaListingsQueNoEstanActivos() {
        analysisService = new OpportunityAnalysisService(listingRepository, comparableListingRepository,
                alertSentRepository, comparableSyncService);
        Listing listing = listingActivo(new BigDecimal("1000"));
        listing.setEstado(ListingStatus.REMOVED);

        OpportunityResult resultado = analysisService.analyzeListing(listing);

        assertThat(resultado.outcome()).isEqualTo(AnalysisOutcome.LISTING_NOT_ACTIVE);
        verify(alertSentRepository, never()).existsByListing_Id(any());
    }

    @Test
    void descartaListingsQueYaTienenUnaAlertaEnviada() {
        analysisService = new OpportunityAnalysisService(listingRepository, comparableListingRepository,
                alertSentRepository, comparableSyncService);
        Listing listing = listingActivo(new BigDecimal("1000"));
        when(alertSentRepository.existsByListing_Id(10L)).thenReturn(true);

        OpportunityResult resultado = analysisService.analyzeListing(listing);

        assertThat(resultado.outcome()).isEqualTo(AnalysisOutcome.ALREADY_ALERTED);
        verify(comparableListingRepository, never()).findComparablesForAnalysis(any(), any());
    }

    @Test
    void descartaListingsSinCategoriaValida() {
        analysisService = new OpportunityAnalysisService(listingRepository, comparableListingRepository,
                alertSentRepository, comparableSyncService);
        Listing listing = listingActivo(new BigDecimal("1000"));
        listing.setCategoria(null);
        when(alertSentRepository.existsByListing_Id(10L)).thenReturn(false);

        OpportunityResult resultado = analysisService.analyzeListing(listing);

        assertThat(resultado.outcome()).isEqualTo(AnalysisOutcome.MISSING_CATEGORIA);
    }

    @Test
    void descartaCuandoHayMenosDeCincoComparablesConfiables() {
        analysisService = new OpportunityAnalysisService(listingRepository, comparableListingRepository,
                alertSentRepository, comparableSyncService);
        Listing listing = listingActivo(new BigDecimal("1000"));
        when(alertSentRepository.existsByListing_Id(10L)).thenReturn(false);
        when(comparableListingRepository.findComparablesForAnalysis("iphone 12 128gb", 10L))
                .thenReturn(comparables("2000", "2100", "2200", "2300"));

        OpportunityResult resultado = analysisService.analyzeListing(listing);

        assertThat(resultado.outcome()).isEqualTo(AnalysisOutcome.INSUFFICIENT_COMPARABLES);
        assertThat(resultado.comparablesUsados()).isEqualTo(4);
    }

    @Test
    void descartaCuandoLaGananciaEstimadaEsMenorAlMinimo() {
        analysisService = new OpportunityAnalysisService(listingRepository, comparableListingRepository,
                alertSentRepository, comparableSyncService);
        // comparable mas barato 1000 -> reventa 950, costos 142.50, ganancia = 950 - 900 - 142.50 = -92.50
        Listing listing = listingActivo(new BigDecimal("900"));
        when(alertSentRepository.existsByListing_Id(10L)).thenReturn(false);
        when(comparableListingRepository.findComparablesForAnalysis("iphone 12 128gb", 10L))
                .thenReturn(comparables("1000", "1100", "1200", "1300", "1400"));

        OpportunityResult resultado = analysisService.analyzeListing(listing);

        assertThat(resultado.outcome()).isEqualTo(AnalysisOutcome.PROFIT_TOO_LOW);
        assertThat(resultado.comparablesUsados()).isEqualTo(5);
    }

    @Test
    void encuentraUnaOportunidadYLaClasificaCorrectamente() {
        analysisService = new OpportunityAnalysisService(listingRepository, comparableListingRepository,
                alertSentRepository, comparableSyncService);
        // comparable mas barato 3000 -> reventa 2850.00, costos 427.50, ganancia = 2850 - 1000 - 427.50 = 1422.50 (MEDIA)
        Listing listing = listingActivo(new BigDecimal("1000"));
        when(alertSentRepository.existsByListing_Id(10L)).thenReturn(false);
        when(comparableListingRepository.findComparablesForAnalysis("iphone 12 128gb", 10L))
                .thenReturn(comparables("3000", "3100", "3200", "3300", "3400"));

        OpportunityResult resultado = analysisService.analyzeListing(listing);

        assertThat(resultado.outcome()).isEqualTo(AnalysisOutcome.OPPORTUNITY_FOUND);
        assertThat(resultado.clasificacion()).contains(GananciaClasificacion.MEDIA);
        assertThat(resultado.calculo()).isPresent();
        assertThat(resultado.calculo().get().gananciaEstimada()).isEqualByComparingTo(new BigDecimal("1422.50"));
        assertThat(resultado.comparablesUsados()).isEqualTo(5);
    }

    @Test
    void analyzeAllActiveListingsSincronizaTodosAntesDeAnalizarCualquiera() {
        analysisService = new OpportunityAnalysisService(listingRepository, comparableListingRepository,
                alertSentRepository, comparableSyncService);
        Listing listing1 = listingActivo(new BigDecimal("1000"));
        Listing listing2 = listingActivo(new BigDecimal("1200"));
        ReflectionTestUtils.setField(listing2, "id", 11L);
        when(listingRepository.findByEstado(ListingStatus.ACTIVE)).thenReturn(List.of(listing1, listing2));
        when(alertSentRepository.existsByListing_Id(any())).thenReturn(true); // atajo: ambos ALREADY_ALERTED

        List<OpportunityResult> resultados = analysisService.analyzeAllActiveListings();

        assertThat(resultados).hasSize(2);
        verify(comparableSyncService, times(1)).sync(listing1);
        verify(comparableSyncService, times(1)).sync(listing2);
    }
}
