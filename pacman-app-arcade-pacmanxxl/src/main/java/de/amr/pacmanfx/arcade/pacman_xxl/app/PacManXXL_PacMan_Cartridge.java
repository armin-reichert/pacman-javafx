package de.amr.pacmanfx.arcade.pacman_xxl.app;

import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameFlow;
import de.amr.pacmanfx.arcade.pacman_xxl.pacman.PacManXXLGameVariant;
import de.amr.pacmanfx.arcade.pacman_xxl.pacman.PacManXXL_PacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman_xxl.pacman.PacManXXL_PacMan_GameRules;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.game.Cartridge;
import de.amr.pacmanfx.ui.game.GameExtension;

import java.util.Set;

public class PacManXXL_PacMan_Cartridge {

    public static final Cartridge CARTRIDGE = new Cartridge(
        GameVariantID.ARCADE_PACMAN_XXL,
        Arcade_GameFlow::new,
        PacManXXL_PacMan_GameModel::new,
        PacManXXL_PacMan_GameRules::new,
        PacManXXLGameVariant::new,
        Set.of(
            new GameExtension(Arcade_GameExtensions.ACTIONS, Arcade_Actions::new)
        )
    );
}
