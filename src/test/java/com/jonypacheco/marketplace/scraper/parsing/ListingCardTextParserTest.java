package com.jonypacheco.marketplace.scraper.parsing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.assertThat;

class ListingCardTextParserTest {

    @Test
    void separaUnaTarjetaNormalEnPrecioTituloUbicacion() {
        String innerText = "MX$3,699\niPhone 13\nMérida, Yuc";

        ListingCardTextParser.CardText resultado = ListingCardTextParser.parse(innerText);

        assertThat(resultado.precio()).isEqualTo("MX$3,699");
        assertThat(resultado.titulo()).isEqualTo("iPhone 13");
        assertThat(resultado.ubicacion()).isEqualTo("Mérida, Yuc");
    }

    @Test
    void quitaElBadgeJustListedAntesDelPrecio() {
        // Caso real: Facebook antepone "Just listed" cuando el anuncio es reciente.
        String innerText = "Just listed\nMX$7,900\niPhone 14 Pro Max de 128gb\nMérida, Yuc";

        ListingCardTextParser.CardText resultado = ListingCardTextParser.parse(innerText);

        assertThat(resultado.precio()).isEqualTo("MX$7,900");
        assertThat(resultado.titulo()).isEqualTo("iPhone 14 Pro Max de 128gb");
        assertThat(resultado.ubicacion()).isEqualTo("Mérida, Yuc");
    }

    @Test
    void usaElPrimerPrecioYQuitaElPrecioOriginalTachadoDelTitulo() {
        // Caso real: anuncio con descuento -- Facebook muestra el precio actual
        // y luego el precio "reduced from" original, ambos con pinta de precio.
        String innerText = "MX$12,800\nMX$13,500\niPhone 17 at&t\nMérida, Yuc";

        ListingCardTextParser.CardText resultado = ListingCardTextParser.parse(innerText);

        assertThat(resultado.precio()).isEqualTo("MX$12,800");
        assertThat(resultado.titulo()).isEqualTo("iPhone 17 at&t");
        assertThat(resultado.ubicacion()).isEqualTo("Mérida, Yuc");
    }

    @Test
    void combinaBadgeYPrecioOriginalEnLaMismaTarjeta() {
        String innerText = "Just listed\nMX$5,000\nMX$5,500\niPhone 12\nCholul, Yuc";

        ListingCardTextParser.CardText resultado = ListingCardTextParser.parse(innerText);

        assertThat(resultado.precio()).isEqualTo("MX$5,000");
        assertThat(resultado.titulo()).isEqualTo("iPhone 12");
        assertThat(resultado.ubicacion()).isEqualTo("Cholul, Yuc");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void devuelveCamposVaciosParaTextoNuloOVacio(String innerText) {
        ListingCardTextParser.CardText resultado = ListingCardTextParser.parse(innerText);

        assertThat(resultado.precio()).isEmpty();
        assertThat(resultado.titulo()).isEmpty();
        assertThat(resultado.ubicacion()).isEmpty();
    }

    @Test
    void devuelveSoloUbicacionSiNoHayPrecioNiTitulo() {
        ListingCardTextParser.CardText resultado = ListingCardTextParser.parse("Mérida, Yuc");

        assertThat(resultado.precio()).isEmpty();
        assertThat(resultado.titulo()).isEmpty();
        assertThat(resultado.ubicacion()).isEqualTo("Mérida, Yuc");
    }
}
