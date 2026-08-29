package com.jonypacheco.marketplace.alert.telegram;

/**
 * Escapa un {@code String} para poder insertarlo como valor de un campo JSON
 * sin depender de Jackson (el payload de la Bot API de Telegram que este
 * modulo arma tiene nada mas dos campos, {@code chat_id} y {@code text}).
 * Cubre comillas, backslash, saltos de linea/tab y caracteres de control.
 */
public final class JsonEscaper {

    private JsonEscaper() {
    }

    public static String escape(String texto) {
        if (texto == null) {
            return "";
        }

        StringBuilder resultado = new StringBuilder(texto.length());
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            switch (c) {
                case '"' -> resultado.append("\\\"");
                case '\\' -> resultado.append("\\\\");
                case '\n' -> resultado.append("\\n");
                case '\r' -> resultado.append("\\r");
                case '\t' -> resultado.append("\\t");
                default -> {
                    if (c < 0x20) {
                        resultado.append(String.format("\\u%04x", (int) c));
                    } else {
                        resultado.append(c);
                    }
                }
            }
        }
        return resultado.toString();
    }
}
