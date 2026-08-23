package com.jonypacheco.marketplace.scraper.browser;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Envuelve {@link Playwright} + {@link Browser} + {@link BrowserContext}
 * abiertos para UNA corrida completa del scraper (todas las
 * {@code SearchConfig} activas comparten esta misma sesion/contexto -- no
 * se abre un browser por config). Pensado para {@code try-with-resources}
 * desde {@code MarketplaceScraperService}; {@link #close()} garantiza el
 * cierre aunque la corrida haya abortado por sesion invalida.
 */
public class BrowserSession implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(BrowserSession.class);

    private final Playwright playwright;
    private final Browser browser;
    private final BrowserContext context;

    BrowserSession(Playwright playwright, Browser browser, BrowserContext context) {
        this.playwright = playwright;
        this.browser = browser;
        this.context = context;
    }

    public Page newPage() {
        return context.newPage();
    }

    @Override
    public void close() {
        try {
            context.close();
        } catch (Exception e) {
            log.warn("Error cerrando BrowserContext de Playwright", e);
        }
        try {
            browser.close();
        } catch (Exception e) {
            log.warn("Error cerrando Browser de Playwright", e);
        }
        try {
            playwright.close();
        } catch (Exception e) {
            log.warn("Error cerrando Playwright", e);
        }
    }
}
