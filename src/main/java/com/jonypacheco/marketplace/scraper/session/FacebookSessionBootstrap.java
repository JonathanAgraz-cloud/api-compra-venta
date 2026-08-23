package com.jonypacheco.marketplace.scraper.session;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Herramienta de un solo uso: abre Chromium VISIBLE para que el usuario
 * inicie sesion a mano con la cuenta secundaria de Facebook, y guarda la
 * sesion (cookies) en un archivo JSON que el scraper normal (headless)
 * reutiliza despues. NO es un bean de Spring -- corre standalone, sin
 * necesitar levantar todo el contexto de la app ni la base de datos.
 *
 * <p>Se corre con:
 * {@code mvnw.cmd exec:java -D exec.mainClass=com.jonypacheco.marketplace.scraper.session.FacebookSessionBootstrap}
 *
 * <p>Lee la ruta destino de la variable de entorno {@code SCRAPER_SESSION_FILE},
 * con el mismo default que {@code application-dev.yml}
 * ({@code <user.home>/.marketplace-scraper/facebook-session.json}) -- la
 * duplicacion es intencional, ya que esta clase corre fuera del contexto
 * Spring y no puede leer el {@code application.yml}.
 */
public final class FacebookSessionBootstrap {

    private FacebookSessionBootstrap() {
    }

    public static void main(String[] args) throws IOException {
        Path destino = resolverRutaDestino();
        if (destino.getParent() != null) {
            Files.createDirectories(destino.getParent());
        }

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            page.navigate("https://www.facebook.com");

            System.out.println("Se abrio Chromium. Inicia sesion con la cuenta SECUNDARIA de Facebook "
                    + "(nunca la personal).");
            System.out.println("Cuando termines de iniciar sesion, regresa aqui y presiona ENTER...");
            new BufferedReader(new InputStreamReader(System.in)).readLine();

            context.storageState(new BrowserContext.StorageStateOptions().setPath(destino));
            System.out.println("Sesion guardada en: " + destino.toAbsolutePath());

            browser.close();
        }
    }

    private static Path resolverRutaDestino() {
        String env = System.getenv("SCRAPER_SESSION_FILE");
        if (env != null && !env.isBlank()) {
            return Path.of(env);
        }
        return Path.of(System.getProperty("user.home"), ".marketplace-scraper", "facebook-session.json");
    }
}
