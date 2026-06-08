package de.amr.pacmanfx.arcade.pacman_xxl.pacman;

import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameFlow;
import de.amr.pacmanfx.ui.game.GameVariantCartridge;

public class PacManXXL_PacMan_Cartridge {

    public static final GameVariantCartridge CARTRIDGE = new GameVariantCartridge(
        Arcade_GameFlow::new,
        PacManXXL_PacMan_GameModel::new,
        PacManXXL_PacMan_GameRules::new,
        PacManXXL_PacMan_UIConfig::new
    );
}
