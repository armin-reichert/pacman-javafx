package de.amr.games.pacman.arcade.pacman;

import de.amr.games.pacman.arcade.Resources;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.assets.ResourceManager;
import de.amr.games.pacman.ui.lib.Flyer;

public class ArcadePacManStartPage {

    private final Flyer flyer;

    public ArcadePacManStartPage() {
        ResourceManager rm = () -> Resources.class;
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