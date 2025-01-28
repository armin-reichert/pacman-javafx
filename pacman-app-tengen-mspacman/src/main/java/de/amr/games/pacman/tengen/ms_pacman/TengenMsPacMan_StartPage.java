/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.assets.ResourceManager;
import de.amr.games.pacman.ui2d.lib.Flyer;

public class TengenMsPacMan_StartPage {

    private final Flyer flyer;

    public TengenMsPacMan_StartPage() {
        ResourceManager rm = this::getClass;
        flyer = new Flyer(
            rm.loadImage("graphics/f1.png"),
            rm.loadImage("graphics/f2.png")
        );
        flyer.setUserData(GameVariant.MS_PACMAN_TENGEN);
        flyer.selectFlyerPage(0);
    }

    public Flyer root() {
        return flyer;
    }
}
