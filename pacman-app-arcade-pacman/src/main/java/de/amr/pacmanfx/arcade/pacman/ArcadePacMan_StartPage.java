/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.model.StandardGameVariant;
import de.amr.pacmanfx.ui.layout.FlyerStartPage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;

public class ArcadePacMan_StartPage extends FlyerStartPage {

    private static final ResourceManager LOCAL_RESOURCES = () -> ArcadePacMan_StartPage.class;

    public ArcadePacMan_StartPage() {
        super(
            StandardGameVariant.PACMAN.name(),
            "Pac-Man (Arcade)",
            LOCAL_RESOURCES.loadImage("graphics/flyer-page-1.jpg"),
            LOCAL_RESOURCES.loadImage("graphics/flyer-page-2.jpg"),
            LOCAL_RESOURCES.loadImage("graphics/flyer-page-3.jpg")
        );
    }
}