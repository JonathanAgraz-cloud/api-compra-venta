package com.jonypacheco.marketplace.scraper.parsing;

/**
 * Detecta si, tras navegar, Facebook nos redirigio a una pantalla de login o
 * verificacion (checkpoint) en vez de mostrar resultados de Marketplace --
 * senal de que la sesion guardada ya no es valida. Recibe la URL como
 * {@code String} (no un {@code Page} de Playwright) para que sea testeable
 * sin navegador.
 */
public final class LoginRedirectDetector {

    private LoginRedirectDetector() {
    }

    public static boolean isLoginOrCheckpoint(String currentUrl) {
        if (currentUrl == null) {
            return false;
        }

        String lower = currentUrl.toLowerCase();
        return lower.contains("/login")
                || lower.contains("checkpoint")
                || lower.contains("two_step_verification")
                || lower.contains("recover");
    }
}
