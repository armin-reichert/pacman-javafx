/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.assets;

import org.tinylog.Logger;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;

public interface TranslationManager {

    ResourceBundle textBundle();

    default String translate(String key, Object... args) {
        requireNonNull(key);
        if (textBundle().containsKey(key)) {
            return replaceEscapeSequences(MessageFormat.format(textBundle().getString(key), args));
        }
        Logger.error("Missing localized text for key {}", key);
        return "[" + key + "]";
    }

    private static String replaceEscapeSequences(String s) {
        return s.replace("\\n", "\n");
    }
}