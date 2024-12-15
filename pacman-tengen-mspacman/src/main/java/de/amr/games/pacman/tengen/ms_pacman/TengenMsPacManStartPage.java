package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.assets.ResourceManager;
import de.amr.games.pacman.ui.lib.Flyer;

public class TengenMsPacManStartPage {

    private final Flyer flyer;

    public TengenMsPacManStartPage() {
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
