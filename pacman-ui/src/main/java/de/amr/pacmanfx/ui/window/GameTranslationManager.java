/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.window;

import de.amr.pacmanfx.ui.GameUI_Constants;
import de.amr.pacmanfx.uilib.assets.TranslationManager;

import java.util.ResourceBundle;

/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
public class GameTranslationManager implements TranslationManager {

    @Override
    public ResourceBundle textBundle() {
        return GameUI_Constants.LOCALIZED_TEXTS;
    }
}
