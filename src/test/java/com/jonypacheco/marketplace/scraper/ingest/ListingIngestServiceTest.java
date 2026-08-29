package com.jonypacheco.marketplace.scraper.ingest;

import com.jonypacheco.marketplace.persistence.domain.entity.Listing;
import com.jonypacheco.marketplace.persistence.domain.entity.SearchConfig;
import com.jonypacheco.marketplace.persistence.domain.enums.ListingStatus;
import com.jonypacheco.marketplace.persistence.domain.enums.ZonaMerida;
import com.jonypacheco.marketplace.persistence.repository.ListingRepository;
import com.jonypacheco.marketplace.scraper.parsing.RawListingCard;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListingIngestServiceTest {

    @Mock
    private ListingRepository listingRepository;

    private ListingIngestService ingestService;

    private final SearchConfig config = new SearchConfig("Prueba", "Celulares", "iphone", ZonaMerida.ALTABRISA);

    @Test
    void insertaUnListingNuevoConCategoriaDelSearchConfig() {
        ingestService = new ListingIngestService(listingRepository);
        RawListingCard raw = new RawListingCard(
                "https://www.facebook.com/marketplace/item/111111111/",
                "iPhone 12 128GB",
                "MX$6,500",
                "Altabrisa, Merida",
                "https://scontent.example/img.jpg");
        when(listingRepository.findByFacebookId("111111111")).thenReturn(Optional.empty());

        IngestOutcome outcome = ingestService.ingest(raw, config);

        assertThat(outcome).isEqualTo(IngestOutcome.NEW);
        ArgumentCaptor<Listing> captor = ArgumentCaptor.forClass(Listing.class);
        verify(listingRepository).save(captor.capture());
        Listing guardado = captor.getValue();
        assertThat(guardado.getFacebookId()).isEqualTo("111111111");
        assertThat(guardado.getCategoria()).isEqualTo("Celulares");
        assertThat(guardado.getZona()).isEqualTo(ZonaMerida.ALTABRISA);
        assertThat(guardado.getPrecio()).isEqualByComparingTo(new BigDecimal("6500"));
    }

    @Test
    void actualizaUnListingExistenteEnVezDeCrearUnoNuevo() {
        ingestService = new ListingIngestService(listingRepository);
        Listing existente = new Listing("222222222", "iPhone 11 (viejo titulo)", new BigDecimal("5000"),
                ZonaMerida.OTRA_ZONA, "https://www.facebook.com/marketplace/item/222222222/");
        existente.setEstado(ListingStatus.REMOVED);
        // URL distinta a la del listing existente a proposito: cubre que
        // actualizar() refresque tambien la url (bug real encontrado al
        // ajustar selectores en vivo -- el update no la tocaba).
        RawListingCard raw = new RawListingCard(
                "https://www.facebook.com/marketplace/item/222222222/?ref=search&tracking=nuevo",
                "iPhone 11 64GB (precio bajado)",
                "MX$4,500",
                "Cholul, Yucatan",
                "https://scontent.example/img2.jpg");
        when(listingRepository.findByFacebookId("222222222")).thenReturn(Optional.of(existente));

        IngestOutcome outcome = ingestService.ingest(raw, config);

        assertThat(outcome).isEqualTo(IngestOutcome.UPDATED);
        verify(listingRepository).save(existente);
        assertThat(existente.getPrecio()).isEqualByComparingTo(new BigDecimal("4500"));
        assertThat(existente.getZona()).isEqualTo(ZonaMerida.CHOLUL);
        assertThat(existente.getEstado()).isEqualTo(ListingStatus.ACTIVE);
        assertThat(existente.getUrl()).isEqualTo("https://www.facebook.com/marketplace/item/222222222/?ref=search&tracking=nuevo");
    }

    @Test
    void omiteTarjetaConPrecioInvalidoSinTocarElRepositorio() {
        ingestService = new ListingIngestService(listingRepository);
        RawListingCard raw = new RawListingCard(
                "https://www.facebook.com/marketplace/item/333333333/",
                "Articulo gratis",
                "Gratis",
                "Dzitya, Merida",
                null);

        IngestOutcome outcome = ingestService.ingest(raw, config);

        assertThat(outcome).isEqualTo(IngestOutcome.SKIPPED_INVALID_PRICE);
        verify(listingRepository, never()).findByFacebookId(any());
        verify(listingRepository, never()).save(any());
    }

    @Test
    void omiteTarjetaSinIdDeAnuncioValidoSinTocarElRepositorio() {
        ingestService = new ListingIngestService(listingRepository);
        RawListingCard raw = new RawListingCard(
                "https://www.facebook.com/marketplace/merida/search?query=iphone",
                "iPhone 12",
                "MX$6,500",
                "Altabrisa, Merida",
                null);

        IngestOutcome outcome = ingestService.ingest(raw, config);

        assertThat(outcome).isEqualTo(IngestOutcome.SKIPPED_INVALID_ID);
        verify(listingRepository, never()).findByFacebookId(any());
        verify(listingRepository, never()).save(any());
    }
}
