/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.assets;

import org.tinylog.Logger;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;

public interface Translator {

    ResourceBundle localizedTexts();

    default String translate(String keyOrPattern, Object... args) {
        requireNonNull(keyOrPattern);
        requireNonNull(args);
        if (localizedTexts() == null) {
            Logger.error("No localized text resources available");
            return "???";
        }
        if (localizedTexts().containsKey(keyOrPattern)) {
            return MessageFormat.format(localizedTexts().getString(keyOrPattern), args);
        }
        Logger.error("Missing localized text for key {}", keyOrPattern);
        return "[" + keyOrPattern + "]";
    }
}