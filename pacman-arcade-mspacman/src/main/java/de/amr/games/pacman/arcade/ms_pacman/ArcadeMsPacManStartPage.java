package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.assets.ResourceManager;
import de.amr.games.pacman.ui.lib.Flyer;

public class ArcadeMsPacManStartPage {

    private final Flyer flyer;

    public ArcadeMsPacManStartPage() {
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
