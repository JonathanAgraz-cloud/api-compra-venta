package com.jonypacheco.marketplace.alert.telegram;

import com.jonypacheco.marketplace.alert.config.TelegramProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Envia un mensaje de texto plano a la Bot API de Telegram usando
 * {@link HttpClient} del JDK (sin agregar spring-boot-starter-web ni ningun
 * cliente HTTP nuevo -- ver plan del modulo de alertas). Nunca loggea el
 * token ni la URL completa de la request, solo un mensaje fijo y, en caso
 * de error, el status code y el body que devuelve Telegram.
 */
@Component
public class TelegramClient {

    private static final Logger log = LoggerFactory.getLogger(TelegramClient.class);

    private final TelegramProperties properties;
    private final HttpClient httpClient;

    public TelegramClient(TelegramProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newHttpClient();
    }

    public boolean sendMessage(String texto) {
        if (properties.getBotToken() == null || properties.getBotToken().isBlank()
                || properties.getChatId() == null || properties.getChatId().isBlank()) {
            log.error("No se puede enviar alerta a Telegram: falta configurar TELEGRAM_BOT_TOKEN/TELEGRAM_CHAT_ID");
            return false;
        }

        String json = "{\"chat_id\":\"" + JsonEscaper.escape(properties.getChatId())
                + "\",\"text\":\"" + JsonEscaper.escape(texto) + "\"}";
        URI uri = URI.create(properties.getBaseUrl() + "/bot" + properties.getBotToken() + "/sendMessage");
        HttpRequest request = HttpRequest.newBuilder(uri)
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            log.info("Enviando alerta a Telegram...");
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return true;
            }
            log.error("Telegram respondio con status {}: {}", response.statusCode(), response.body());
            return false;
        } catch (IOException e) {
            log.error("Error de red enviando alerta a Telegram: {}", e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Envio de alerta a Telegram interrumpido: {}", e.getMessage());
            return false;
        }
    }
}
