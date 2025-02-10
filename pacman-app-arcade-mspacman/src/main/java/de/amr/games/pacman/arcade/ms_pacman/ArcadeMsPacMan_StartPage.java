/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.uilib.Flyer;
import de.amr.games.pacman.uilib.ResourceManager;

public class ArcadeMsPacMan_StartPage {

    private final Flyer flyer;

    public ArcadeMsPacMan_StartPage() {
        ResourceManager rm = this::getClass;
        flyer = new Flyer(
            rm.loadImage("graphics/f1.jpg"),
            rm.loadImage("graphics/f2.jpg")
        );
        flyer.setUserData(GameVariant.MS_PACMAN);
        flyer.selectFlyerPage(0);
    }

    public Flyer root() {
        return flyer;
    }
}
