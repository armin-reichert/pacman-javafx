package de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman;

import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameFlow;
import de.amr.pacmanfx.ui.game.Cartridge;

public class PacManXXL_MsPacMan_Cartridge {

    public static final Cartridge CARTRIDGE = new Cartridge(
        Arcade_GameFlow::new,
        PacManXXL_MsPacMan_GameModel::new,
        PacManXXL_MsPacMan_GameRules::new,
        PacManXXL_MsPacMan_UIConfig::new
    );
}
