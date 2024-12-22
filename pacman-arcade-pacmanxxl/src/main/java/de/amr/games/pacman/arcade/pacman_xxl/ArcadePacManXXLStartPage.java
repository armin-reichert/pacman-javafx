package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.assets.ResourceManager;
import de.amr.games.pacman.ui2d.lib.Flyer;

public class ArcadePacManXXLStartPage {

    private final Flyer flyer;

    public ArcadePacManXXLStartPage() {
        ResourceManager rm = this::getClass;
        flyer = new Flyer(rm.loadImage("graphics/pacman_xxl_logo.jpg"));
        flyer.setUserData(GameVariant.PACMAN_XXL);
        flyer.selectFlyerPage(0);
        flyer.setLayoutMode(0, Flyer.LayoutMode.FILL);
    }

    public Flyer root() {
        return flyer;
    }
}
