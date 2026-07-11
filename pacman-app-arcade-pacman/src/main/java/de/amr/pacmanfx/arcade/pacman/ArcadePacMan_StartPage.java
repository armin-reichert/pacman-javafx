/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.views.startpages.FlyerStartPage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;

public class ArcadePacMan_StartPage extends FlyerStartPage {

    private static final String RESOURCE_ROOT = "/de/amr/pacmanfx/arcade/pacman/";
    private static final ResourceManager RESOURCE_MANAGER = () -> ArcadePacMan_StartPage.class;

    public ArcadePacMan_StartPage() {
        super(
            GameVariantID.ARCADE_PACMAN.name(),
            "Pac-Man (Arcade)",
            RESOURCE_MANAGER.loadMedia(RESOURCE_ROOT + "sound/flyer-text.mp3"),
            RESOURCE_MANAGER.loadImage(RESOURCE_ROOT + "graphics/flyer-page-1.jpg"),
            RESOURCE_MANAGER.loadImage(RESOURCE_ROOT + "graphics/flyer-page-2.jpg"),
            RESOURCE_MANAGER.loadImage(RESOURCE_ROOT + "graphics/flyer-page-3.jpg")
            );
    }
}