package com.jonypacheco.marketplace.scraper.browser;

import com.jonypacheco.marketplace.scraper.ScraperSessionException;
import com.jonypacheco.marketplace.scraper.config.ScraperProperties;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Abre una {@link BrowserSession} (Playwright + Chromium + contexto con la
 * sesion de Facebook cargada) para una corrida del scraper. No mantiene un
 * {@code Browser} residente entre corridas -- se abre y se cierra cada vez,
 * para no dejar Chromium ocupando memoria todo el tiempo en el t3.micro.
 */
@Component
public class PlaywrightBrowserManager {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightBrowserManager.class);

    private final ScraperProperties properties;

    public PlaywrightBrowserManager(ScraperProperties properties) {
        this.properties = properties;
    }

    public BrowserSession open() {
        Path sessionFilePath = properties.getSessionFilePath();
        if (sessionFilePath == null || !Files.isRegularFile(sessionFilePath)) {
            throw new ScraperSessionException(
                    "No existe el archivo de sesion de Facebook en '" + sessionFilePath + "'. "
                            + "Corre FacebookSessionBootstrap primero para generarlo (login manual, una sola vez).");
        }

        log.info("Abriendo Chromium (headless={}) con sesion '{}'", properties.isHeadless(), sessionFilePath);

        Playwright playwright = Playwright.create();
        try {
            Browser browser = playwright.chromium()
                    .launch(new BrowserType.LaunchOptions().setHeadless(properties.isHeadless()));
            BrowserContext context = browser.newContext(
                    new Browser.NewContextOptions().setStorageStatePath(sessionFilePath));
            return new BrowserSession(playwright, browser, context);
        } catch (RuntimeException e) {
            // Si algo falla despues de crear Playwright pero antes de terminar
            // de armar la sesion, cerramos para no dejar el proceso huerfano.
            playwright.close();
            throw e;
        }
    }
}
