/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman;

import de.amr.games.pacman.arcade.ResourceRoot;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.assets.ResourceManager;
import de.amr.games.pacman.ui2d.lib.Flyer;

public class ArcadePacManStartPage {

    private final Flyer flyer;

    public ArcadePacManStartPage() {
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
