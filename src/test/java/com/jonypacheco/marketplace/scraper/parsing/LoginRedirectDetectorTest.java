package com.jonypacheco.marketplace.scraper.parsing;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRedirectDetectorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.facebook.com/marketplace/merida/search?query=iphone",
            "https://www.facebook.com/marketplace/item/123456789/"
    })
    void noDetectaLoginEnUrlsNormalesDeMarketplace(String url) {
        assertThat(LoginRedirectDetector.isLoginOrCheckpoint(url)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.facebook.com/login/?next=%2Fmarketplace%2F",
            "https://www.facebook.com/checkpoint/1234567/",
            "https://www.facebook.com/login/two_step_verification/"
    })
    void detectaLoginOCheckpoint(String url) {
        assertThat(LoginRedirectDetector.isLoginOrCheckpoint(url)).isTrue();
    }

    @ParameterizedTest
    @NullSource
    @CsvSource({"''"})
    void noFallaConUrlNulaOVacia(String url) {
        assertThat(LoginRedirectDetector.isLoginOrCheckpoint(url)).isFalse();
    }
}
