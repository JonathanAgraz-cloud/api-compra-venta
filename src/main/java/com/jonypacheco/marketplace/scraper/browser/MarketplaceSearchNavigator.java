package com.jonypacheco.marketplace.scraper.browser;

import com.jonypacheco.marketplace.scraper.ScraperSessionException;
import com.jonypacheco.marketplace.scraper.config.ScraperProperties;
import com.jonypacheco.marketplace.scraper.parsing.LoginRedirectDetector;
import com.jonypacheco.marketplace.scraper.parsing.RawListingCard;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Navega a una URL de busqueda de Marketplace y extrae tarjetas de
 * resultado crudas. Cero logica de negocio aqui -- solo strings crudos
 * hacia {@link RawListingCard}; el parseo vive en {@code scraper.parsing}.
 *
 * <p><b>Advertencia:</b> los selectores usados aqui (link de item, texto de
 * la tarjeta) son un placeholder razonable, NO verificado contra una sesion
 * real de Facebook -- hay que ajustarlos la primera vez que se corra con
 * Playwright Inspector/{@code codegen} (ver riesgos del modulo scraper). En
 * particular, la heuristica de {@link #dividirTextoDeTarjeta(String)} asume
 * que el texto visible de cada tarjeta viene en lineas separadas
 * aproximadamente en el orden precio/titulo/ubicacion, que es el layout
 * habitual de Marketplace pero puede variar.
 */
@Component
public class MarketplaceSearchNavigator {

    private static final Logger log = LoggerFactory.getLogger(MarketplaceSearchNavigator.class);

    private static final String ITEM_LINK_SELECTOR = "a[href*='/marketplace/item/']";
    private static final int MAX_SCROLL_ATTEMPTS = 8;
    private static final int SCROLL_STEP_PX = 1800;
    private static final long WAIT_FOR_RESULTS_TIMEOUT_MS = 15_000;

    private final ScraperProperties properties;

    public MarketplaceSearchNavigator(ScraperProperties properties) {
        this.properties = properties;
    }

    public List<RawListingCard> search(Page page, String searchUrl, int maxResults) {
        page.navigate(searchUrl);
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);

        if (LoginRedirectDetector.isLoginOrCheckpoint(page.url())) {
            throw new ScraperSessionException(
                    "Facebook redirigio a login/checkpoint en vez de mostrar resultados de Marketplace "
                            + "(URL actual: " + page.url() + "). La sesion guardada ya no es valida.");
        }

        page.waitForSelector(ITEM_LINK_SELECTOR, new Page.WaitForSelectorOptions()
                .setTimeout(WAIT_FOR_RESULTS_TIMEOUT_MS));

        Locator itemLinks = page.locator(ITEM_LINK_SELECTOR);
        cargarResultadosConScroll(page, itemLinks, maxResults);

        int total = Math.min(itemLinks.count(), maxResults);
        List<RawListingCard> resultado = new ArrayList<>(total);
        for (int i = 0; i < total; i++) {
            RawListingCard card = extraerTarjeta(itemLinks.nth(i));
            if (card != null) {
                resultado.add(card);
            }
        }
        return resultado;
    }

    private void cargarResultadosConScroll(Page page, Locator itemLinks, int maxResults) {
        int intentos = 0;
        while (itemLinks.count() < maxResults && intentos < MAX_SCROLL_ATTEMPTS) {
            page.mouse().wheel(0, SCROLL_STEP_PX);
            sleepBreve();
            intentos++;
        }
    }

    private RawListingCard extraerTarjeta(Locator link) {
        try {
            String href = link.getAttribute("href");
            if (href == null) {
                return null;
            }

            String innerText = link.innerText();
            String[] partes = dividirTextoDeTarjeta(innerText);

            String imagenUrl = null;
            try {
                imagenUrl = link.locator("img").first().getAttribute("src");
            } catch (RuntimeException e) {
                // Best-effort: si la tarjeta no trae imagen extraible, seguimos sin ella.
                log.debug("No se pudo extraer imagen de una tarjeta de resultado", e);
            }

            return new RawListingCard(href, partes[1], partes[0], partes[2], imagenUrl);
        } catch (RuntimeException e) {
            log.warn("No se pudo extraer una tarjeta de resultado, se omite: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Heuristica: separa el texto visible de una tarjeta en
     * {@code [precio, titulo, ubicacion]}. Asume que la primera linea con
     * pinta de precio (contiene '$' seguido de un digito) es el precio, la
     * siguiente linea no vacia es el titulo, y la ultima linea restante es
     * la ubicacion. Placeholder a validar con una tarjeta real.
     */
    private String[] dividirTextoDeTarjeta(String innerText) {
        List<String> lineas = new ArrayList<>();
        if (innerText != null) {
            for (String linea : innerText.split("\\R")) {
                String limpia = linea.trim();
                if (!limpia.isEmpty()) {
                    lineas.add(limpia);
                }
            }
        }

        String precio = lineas.stream()
                .filter(linea -> linea.matches(".*\\$\\s?[0-9].*"))
                .findFirst()
                .orElse("");
        lineas.remove(precio);

        String titulo = lineas.isEmpty() ? "" : lineas.get(0);
        String ubicacion = lineas.size() > 1 ? lineas.get(lineas.size() - 1) : "";

        return new String[] {precio, titulo, ubicacion};
    }

    private void sleepBreve() {
        long minMs = Math.max(300, properties.getDelayMinMs() / 10);
        long maxMs = Math.max(minMs + 100, properties.getDelayMaxMs() / 10);
        sleep(ThreadLocalRandom.current().nextLong(minMs, maxMs));
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrumpido durante el delay del scraper", e);
        }
    }
}
