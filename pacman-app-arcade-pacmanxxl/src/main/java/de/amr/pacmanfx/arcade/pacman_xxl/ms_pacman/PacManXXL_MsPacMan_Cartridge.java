package de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman;

import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameFlow;
import de.amr.pacmanfx.ui.game.GameVariantCartridge;

public class PacManXXL_MsPacMan_Cartridge {

    public static final GameVariantCartridge CARTRIDGE = new GameVariantCartridge(
        Arcade_GameFlow::new,
        PacManXXL_MsPacMan_GameModel::new,
        PacManXXL_MsPacMan_GameRules::new,
        PacManXXL_MsPacMan_UIConfig::new
    );
}
