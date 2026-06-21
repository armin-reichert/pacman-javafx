/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.window;

import de.amr.pacmanfx.uilib.assets.TranslationManager;

import java.util.ResourceBundle;

public class GameTranslationManager implements TranslationManager {

    public static final ResourceBundle TEXT_BUNDLE = ResourceBundle.getBundle("de.amr.pacmanfx.ui.localized_texts",
        GameTranslationManager.class.getModule());

    @Override
    public ResourceBundle textBundle() {
        return TEXT_BUNDLE;
    }
}
