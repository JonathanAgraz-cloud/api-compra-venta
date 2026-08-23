package com.jonypacheco.marketplace.scraper;

/**
 * La sesion de Facebook guardada (storageState) no existe, no es valida, o
 * Facebook nos redirigio a login/checkpoint durante una corrida. Debe
 * abortar la corrida completa de inmediato -- reintentar insistentemente
 * con una sesion invalida se parece mas a trafico de bot.
 */
public class ScraperSessionException extends RuntimeException {

    public ScraperSessionException(String message) {
        super(message);
    }

    public ScraperSessionException(String message, Throwable cause) {
        super(message, cause);
    }
}
