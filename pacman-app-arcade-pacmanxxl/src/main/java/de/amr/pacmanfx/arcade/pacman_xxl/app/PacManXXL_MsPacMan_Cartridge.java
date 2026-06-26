package de.amr.pacmanfx.arcade.pacman_xxl.app;

import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameFlow;
import de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman.PacManXXL_MsPacManConfig;
import de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman.PacManXXL_MsPacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman.PacManXXL_MsPacMan_GameRules;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.game.Cartridge;

import java.util.Map;

public class PacManXXL_MsPacMan_Cartridge {

    public static final Cartridge CARTRIDGE = new Cartridge(
        GameVariantID.ARCADE_MS_PACMAN_XXL,
        Arcade_GameFlow::new,
        PacManXXL_MsPacMan_GameModel::new,
        PacManXXL_MsPacMan_GameRules::new,
        PacManXXL_MsPacManConfig::new,
        Map.of(Arcade_GameExtensions.ACTIONS, Arcade_Actions::new)
    );
}
