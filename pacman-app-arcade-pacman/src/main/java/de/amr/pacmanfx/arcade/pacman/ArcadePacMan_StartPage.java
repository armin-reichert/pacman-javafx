/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.model.StandardGameVariant;
import de.amr.pacmanfx.ui.layout.FlyerStartPage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.widgets.Flyer;

public class ArcadePacMan_StartPage extends FlyerStartPage {

    private static final ResourceManager LOCAL_RESOURCES = () -> ArcadePacMan_StartPage.class;

    public ArcadePacMan_StartPage() {
        super(StandardGameVariant.PACMAN.name());
        title = "Pac-Man (Arcade)";
    }

    @Override
    protected Flyer createFlyer() {
        return new Flyer(
            LOCAL_RESOURCES.loadImage("graphics/flyer-page-1.jpg"),
            LOCAL_RESOURCES.loadImage("graphics/flyer-page-2.jpg"),
            LOCAL_RESOURCES.loadImage("graphics/flyer-page-3.jpg")
        );
    }
}