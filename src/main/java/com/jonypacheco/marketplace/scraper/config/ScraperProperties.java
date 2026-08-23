package com.jonypacheco.marketplace.scraper.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;

/**
 * Configuracion del scraper de Facebook Marketplace, bindeada desde
 * {@code application*.yml} bajo el prefijo {@code scraper}. Ver
 * {@code .env.example} para las variables de entorno correspondientes.
 */
@Component
@ConfigurationProperties(prefix = "scraper")
@Validated
public class ScraperProperties {

    /**
     * Ruta del archivo de storageState (sesion de Facebook) generado por
     * {@code FacebookSessionBootstrap}. Sin default en produccion a
     * proposito: debe fallar al arrancar si falta configurar la variable
     * de entorno, igual que las credenciales de BD.
     */
    @NotNull
    private Path sessionFilePath;

    /** false solo para depurar selectores localmente con el navegador visible. */
    private boolean headless = true;

    @Positive
    private long delayMinMs = 3000;

    @Positive
    private long delayMaxMs = 8000;

    /** Informativa/para logs; la ubicacion real la determina la sesion de Facebook. */
    private String baseLocationQuery = "Mérida, Yucatán, México";

    @Positive
    private int maxResultsPerSearch = 20;

    public Path getSessionFilePath() {
        return sessionFilePath;
    }

    public void setSessionFilePath(Path sessionFilePath) {
        this.sessionFilePath = sessionFilePath;
    }

    public boolean isHeadless() {
        return headless;
    }

    public void setHeadless(boolean headless) {
        this.headless = headless;
    }

    public long getDelayMinMs() {
        return delayMinMs;
    }

    public void setDelayMinMs(long delayMinMs) {
        this.delayMinMs = delayMinMs;
    }

    public long getDelayMaxMs() {
        return delayMaxMs;
    }

    public void setDelayMaxMs(long delayMaxMs) {
        this.delayMaxMs = delayMaxMs;
    }

    public String getBaseLocationQuery() {
        return baseLocationQuery;
    }

    public void setBaseLocationQuery(String baseLocationQuery) {
        this.baseLocationQuery = baseLocationQuery;
    }

    public int getMaxResultsPerSearch() {
        return maxResultsPerSearch;
    }

    public void setMaxResultsPerSearch(int maxResultsPerSearch) {
        this.maxResultsPerSearch = maxResultsPerSearch;
    }
}
