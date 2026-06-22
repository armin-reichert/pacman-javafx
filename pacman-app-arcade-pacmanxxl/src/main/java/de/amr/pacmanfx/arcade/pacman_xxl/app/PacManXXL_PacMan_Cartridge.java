package de.amr.pacmanfx.arcade.pacman_xxl.app;

import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameFlow;
import de.amr.pacmanfx.arcade.pacman_xxl.pacman.PacManXXL_PacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman_xxl.pacman.PacManXXL_PacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman_xxl.pacman.PacManXXL_PacManConfig;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.game.Cartridge;

public class PacManXXL_PacMan_Cartridge {

    public static final Cartridge CARTRIDGE = new Cartridge(
        GameVariantID.ARCADE_PACMAN_XXL,
        Arcade_GameFlow::new,
        PacManXXL_PacMan_GameModel::new,
        PacManXXL_PacMan_GameRules::new,
        PacManXXL_PacManConfig::new
    );
}
