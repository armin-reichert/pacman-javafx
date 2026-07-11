/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.views.startpages.FlyerStartPage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;

public class TengenMsPacMan_StartPage extends FlyerStartPage {

    private static final String RESOURCE_ROOT = "/de/amr/pacmanfx/tengenmspacman/";
    private static final ResourceManager RESOURCE_MANAGER = () -> TengenMsPacMan_StartPage.class;

    public TengenMsPacMan_StartPage() {
        super(
            GameVariantID.TENGEN_MS_PACMAN.name(),
            "Ms. Pac-Man (Tengen)",
            RESOURCE_MANAGER.loadMedia(RESOURCE_ROOT + "sound/flyer-text.mp3"),
            RESOURCE_MANAGER.loadImage(RESOURCE_ROOT + "graphics/flyer-page-1.png"),
            RESOURCE_MANAGER.loadImage(RESOURCE_ROOT + "graphics/flyer-page-2.png")
        );
    }
}