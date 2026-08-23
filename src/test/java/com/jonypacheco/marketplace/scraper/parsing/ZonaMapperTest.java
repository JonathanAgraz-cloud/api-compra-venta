package com.jonypacheco.marketplace.scraper.parsing;

import com.jonypacheco.marketplace.persistence.domain.enums.ZonaMerida;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.assertThat;

class ZonaMapperTest {

    @ParameterizedTest
    @CsvSource({
            "'Altabrisa, Merida', ALTABRISA",
            "'altabrisa', ALTABRISA",
            "'Temozon Norte, Yuc.', TEMOZON_NORTE",
            "'temozon norte', TEMOZON_NORTE",
            "'Cholul, Yucatan', CHOLUL",
            "'Dzitya, Merida', DZITYA",
            "'Yucatan Country Club', YUCATAN_COUNTRY_CLUB",
            "'Country Club, Merida', YUCATAN_COUNTRY_CLUB",
            "'Francisco de Montejo', FRANCISCO_DE_MONTEJO",
            "'Montejo, Merida', FRANCISCO_DE_MONTEJO"
    })
    void mapeaZonasConocidasSinImportarAcentosOMayusculas(String ubicacionRaw, ZonaMerida esperada) {
        assertThat(ZonaMapper.map(ubicacionRaw)).isEqualTo(esperada);
    }

    @ParameterizedTest
    @CsvSource({
            "'Cancun, Quintana Roo'",
            "'Centro, Merida'",
            "'Temozon Sur, Merida'"
    })
    void devuelveOtraZonaParaTextoQueNoMatchea(String ubicacionRaw) {
        assertThat(ZonaMapper.map(ubicacionRaw)).isEqualTo(ZonaMerida.OTRA_ZONA);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void devuelveOtraZonaParaTextoVacioONulo(String ubicacionRaw) {
        assertThat(ZonaMapper.map(ubicacionRaw)).isEqualTo(ZonaMerida.OTRA_ZONA);
    }
}
