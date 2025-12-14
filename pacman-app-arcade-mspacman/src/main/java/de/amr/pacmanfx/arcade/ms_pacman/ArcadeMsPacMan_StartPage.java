/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.model.StandardGameVariant;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.layout.FlyerStartPage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.widgets.Flyer;

public class ArcadeMsPacMan_StartPage extends FlyerStartPage {

    private static final ResourceManager LOCAL_RESOURCES = () -> ArcadeMsPacMan_StartPage.class;

    public ArcadeMsPacMan_StartPage(GameUI ui) {
        super(ui, StandardGameVariant.MS_PACMAN.name());
    }

    @Override
    protected Flyer createFlyer() {
        return new Flyer(
            LOCAL_RESOURCES.loadImage("graphics/flyer-page-1.jpg"),
            LOCAL_RESOURCES.loadImage("graphics/flyer-page-2.jpg")
        );
    }

    @Override
    public String title() {
        return "Arcade Ms. Pac-Man"; //TODO localize
    }
}