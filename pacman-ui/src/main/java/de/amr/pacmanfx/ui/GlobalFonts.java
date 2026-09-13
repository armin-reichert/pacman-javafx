/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.text.Font;

public enum GlobalFonts {
    ARCADE        ("/de/amr/pacmanfx/ui/fonts/emulogic.ttf", 8),
    HANDWRITING   ("/de/amr/pacmanfx/ui/fonts/Molle-Italic.ttf", 9),
    MONOSPACED    ("/de/amr/pacmanfx/ui/fonts/fantasquesansmono-bold.otf", 12),
    PAC_FONT_GOOD ("/de/amr/pacmanfx/ui/fonts/PacfontGood.ttf", 8);

    GlobalFonts(String path, float defaultSize) {
        this.path = path;
        this.defaultSize = defaultSize;
    }

    private void loadFont() {
        final ResourceManager resourceManager = this::getClass;
        font = resourceManager.loadFont(path, defaultSize);
    }

    public Font font() {
        if (font == null) {
            loadFont();
        }
        return font;
    }

    public Font font(double size) {
        return Font.font(font().getFamily(), size);
    }

    private final String path;
    private final float defaultSize;

    private Font font;
}
