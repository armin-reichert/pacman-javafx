/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.assets;

import de.amr.basics.ui.assets.TranslationManager;

import java.util.ResourceBundle;

public class DefaultTranslationManager implements TranslationManager {

    static final ResourceBundle TEXT_BUNDLE = ResourceBundle.getBundle(
        "de.amr.pacmanfx.ui.localized_texts",
        DefaultTranslationManager.class.getModule());

    @Override
    public ResourceBundle textBundle() {
        return TEXT_BUNDLE;
    }
}
