/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman;

import de.amr.games.pacman.arcade.ResourceRoot;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.uilib.Flyer;
import de.amr.games.pacman.uilib.ResourceManager;

public class ArcadePacMan_StartPage {

    private final Flyer flyer;

    public ArcadePacMan_StartPage() {
        ResourceManager rm = () -> ResourceRoot.class;
        flyer = new Flyer(
            rm.loadImage("graphics/f1.jpg"),
            rm.loadImage("graphics/f2.jpg"),
            rm.loadImage("graphics/f3.jpg")
        );
        flyer.setUserData(GameVariant.PACMAN);
        flyer.selectFlyerPage(0);
    }

    public Flyer root() {
        return flyer;
    }

}
