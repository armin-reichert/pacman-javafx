/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.window;

import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.uilib.assets.TranslationManager;

import java.util.ResourceBundle;

/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
public class GameTranslationManager implements TranslationManager {

    @Override
    public ResourceBundle textBundle() {
        return GlobalAssets.LOCALIZED_TEXTS;
    }
}
