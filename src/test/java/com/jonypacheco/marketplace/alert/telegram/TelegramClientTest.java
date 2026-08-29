package com.jonypacheco.marketplace.alert.telegram;

import com.jonypacheco.marketplace.alert.config.TelegramProperties;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica {@link TelegramClient} contra un servidor HTTP local
 * ({@code com.sun.net.httpserver.HttpServer}, incluido en el JDK) en vez de
 * la API real de Telegram -- a diferencia del scraper, aqui si se puede
 * testear la capa con efectos porque nosotros controlamos ambos lados de la
 * llamada HTTP.
 */
class TelegramClientTest {

    private HttpServer server;

    @AfterEach
    void detenerServidor() {
        if (server != null) {
            server.stop(0);
        }
    }

    private HttpServer iniciarServidor(int statusCode, String respuesta, StringBuilder pathCapturado,
                                        StringBuilder bodyCapturado, AtomicInteger llamadas) throws IOException {
        HttpServer s = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        s.createContext("/", exchange -> {
            llamadas.incrementAndGet();
            pathCapturado.append(exchange.getRequestURI().getPath());
            try (InputStream is = exchange.getRequestBody()) {
                bodyCapturado.append(new String(is.readAllBytes(), StandardCharsets.UTF_8));
            }
            byte[] bytes = respuesta.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        s.start();
        return s;
    }

    private TelegramProperties propiedadesApuntandoAlServidor(int puerto) {
        TelegramProperties properties = new TelegramProperties();
        properties.setBotToken("TOKEN123");
        properties.setChatId("999888");
        properties.setBaseUrl("http://localhost:" + puerto);
        return properties;
    }

    @Test
    void devuelveTrueYMandaElBodyCorrectoCuandoTelegramRespondeConStatus200() throws IOException {
        StringBuilder path = new StringBuilder();
        StringBuilder body = new StringBuilder();
        AtomicInteger llamadas = new AtomicInteger();
        server = iniciarServidor(200, "{\"ok\":true}", path, body, llamadas);
        TelegramClient client = new TelegramClient(propiedadesApuntandoAlServidor(server.getAddress().getPort()));

        boolean resultado = client.sendMessage("Hola mundo");

        assertThat(resultado).isTrue();
        assertThat(llamadas.get()).isEqualTo(1);
        assertThat(path.toString()).isEqualTo("/botTOKEN123/sendMessage");
        assertThat(body.toString()).contains("\"chat_id\":\"999888\"");
        assertThat(body.toString()).contains("\"text\":\"Hola mundo\"");
    }

    @Test
    void devuelveFalseCuandoTelegramRespondeConStatus400() throws IOException {
        StringBuilder path = new StringBuilder();
        StringBuilder body = new StringBuilder();
        AtomicInteger llamadas = new AtomicInteger();
        server = iniciarServidor(400, "{\"ok\":false,\"description\":\"Bad Request\"}", path, body, llamadas);
        TelegramClient client = new TelegramClient(propiedadesApuntandoAlServidor(server.getAddress().getPort()));

        boolean resultado = client.sendMessage("Hola mundo");

        assertThat(resultado).isFalse();
        assertThat(llamadas.get()).isEqualTo(1);
    }

    @Test
    void devuelveFalseCuandoTelegramRespondeConStatus500() throws IOException {
        StringBuilder path = new StringBuilder();
        StringBuilder body = new StringBuilder();
        AtomicInteger llamadas = new AtomicInteger();
        server = iniciarServidor(500, "Internal Server Error", path, body, llamadas);
        TelegramClient client = new TelegramClient(propiedadesApuntandoAlServidor(server.getAddress().getPort()));

        boolean resultado = client.sendMessage("Hola mundo");

        assertThat(resultado).isFalse();
    }

    @Test
    void noLlamaARedSiFaltaElBotToken() {
        TelegramProperties properties = new TelegramProperties();
        properties.setBotToken("");
        properties.setChatId("999888");
        properties.setBaseUrl("http://localhost:1");
        TelegramClient client = new TelegramClient(properties);

        boolean resultado = client.sendMessage("Hola mundo");

        assertThat(resultado).isFalse();
    }

    @Test
    void noLlamaARedSiFaltaElChatId() {
        TelegramProperties properties = new TelegramProperties();
        properties.setBotToken("TOKEN123");
        properties.setChatId("");
        properties.setBaseUrl("http://localhost:1");
        TelegramClient client = new TelegramClient(properties);

        boolean resultado = client.sendMessage("Hola mundo");

        assertThat(resultado).isFalse();
    }
}
