package com.jonypacheco.marketplace.analysis.comparable;

import com.jonypacheco.marketplace.persistence.domain.entity.ComparableListing;
import com.jonypacheco.marketplace.persistence.domain.entity.Listing;
import com.jonypacheco.marketplace.persistence.domain.enums.ZonaMerida;
import com.jonypacheco.marketplace.persistence.repository.ComparableListingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComparableSyncServiceTest {

    @Mock
    private ComparableListingRepository comparableListingRepository;

    private ComparableSyncService syncService;

    @Test
    void creaUnComparableNuevoCuandoNoExisteUnoParaEseListing() {
        syncService = new ComparableSyncService(comparableListingRepository);
        Listing listing = new Listing("111", "iPhone 12 128GB", new BigDecimal("6500"), ZonaMerida.ALTABRISA,
                "https://www.facebook.com/marketplace/item/111/");
        listing.setCategoria("iPhone 12 128GB");
        ReflectionTestUtils.setField(listing, "id", 1L);
        when(comparableListingRepository.findBySourceListing_Id(1L)).thenReturn(List.of());

        syncService.sync(listing);

        ArgumentCaptor<ComparableListing> captor = ArgumentCaptor.forClass(ComparableListing.class);
        verify(comparableListingRepository).save(captor.capture());
        ComparableListing guardado = captor.getValue();
        assertThat(guardado.getProductoNormalizado()).isEqualTo("iphone 12 128gb");
        assertThat(guardado.getPrecio()).isEqualByComparingTo(new BigDecimal("6500"));
        assertThat(guardado.getSourceListing()).isEqualTo(listing);
        assertThat(guardado.isConfiable()).isTrue();
    }

    @Test
    void actualizaElComparableExistenteEnVezDeCrearUnoNuevo() {
        syncService = new ComparableSyncService(comparableListingRepository);
        Listing listing = new Listing("222", "iPhone 11 (precio bajado)", new BigDecimal("4500"), ZonaMerida.CHOLUL,
                "https://www.facebook.com/marketplace/item/222/");
        listing.setCategoria("iPhone 11");
        ReflectionTestUtils.setField(listing, "id", 2L);
        ComparableListing existente = new ComparableListing("iphone 11", "iPhone 11", new BigDecimal("5000"));
        existente.setConfiable(false);
        when(comparableListingRepository.findBySourceListing_Id(2L)).thenReturn(List.of(existente));

        syncService.sync(listing);

        verify(comparableListingRepository).save(existente);
        assertThat(existente.getPrecio()).isEqualByComparingTo(new BigDecimal("4500"));
        assertThat(existente.isConfiable()).isTrue();
    }

    @Test
    void noSincronizaCuandoElListingNoTieneCategoriaValida() {
        syncService = new ComparableSyncService(comparableListingRepository);
        Listing listing = new Listing("333", "Articulo sin categoria", new BigDecimal("1000"), ZonaMerida.OTRA_ZONA,
                "https://www.facebook.com/marketplace/item/333/");

        syncService.sync(listing);

        verify(comparableListingRepository, never()).findBySourceListing_Id(any());
        verify(comparableListingRepository, never()).save(any());
    }
}
