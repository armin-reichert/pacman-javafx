/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.assets;

import org.tinylog.Logger;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;

public interface Translator {

    ResourceBundle resources();

    default String translate(String keyOrPattern, Object... args) {
        requireNonNull(keyOrPattern);
        requireNonNull(args);
        if (resources() == null) {
            Logger.error("No localized text resources available");
            return "???";
        }
        if (resources().containsKey(keyOrPattern)) {
            return MessageFormat.format(resources().getString(keyOrPattern), args);
        }
        Logger.error("Missing localized text for key {}", keyOrPattern);
        return "[" + keyOrPattern + "]";
    }
}