package com.bazaar.common.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

/** Genera slugs URL-friendly: quita tildes, caracteres especiales y espacios. */
public final class SlugUtil {

    private static final Pattern NO_LATIN = Pattern.compile("[^a-zA-Z0-9\\s-]");
    private static final Pattern ESPACIOS_GUIONES = Pattern.compile("[\\s-]+");

    private SlugUtil() {}

    public static String generar(String texto) {
        String normalizado = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String sinCaracteresRaros = NO_LATIN.matcher(normalizado).replaceAll("");
        String conGuiones = ESPACIOS_GUIONES.matcher(sinCaracteresRaros).replaceAll("-");
        return conGuiones.toLowerCase().replaceAll("^-+|-+$", "");
    }
}
