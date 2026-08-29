package com.jonypacheco.marketplace.alert.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuracion de la Bot API de Telegram. A proposito SIN {@code @Validated}
 * ni {@code @NotBlank} en {@code botToken}/{@code chatId} (a diferencia de
 * {@code ScraperProperties.sessionFilePath}) -- asi {@code mvn spring-boot:run}
 * local no exige tener un bot de Telegram configurado; {@link com.jonypacheco.marketplace.alert.telegram.TelegramClient}
 * valida en el momento del envio y loggea un error claro si faltan.
 */
@Component
@ConfigurationProperties(prefix = "telegram")
public class TelegramProperties {

    private String botToken = "";
    private String chatId = "";
    private String baseUrl = "https://api.telegram.org";

    public String getBotToken() {
        return botToken;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
