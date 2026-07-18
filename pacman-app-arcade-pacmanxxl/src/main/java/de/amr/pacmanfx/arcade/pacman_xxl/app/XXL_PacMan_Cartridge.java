package de.amr.pacmanfx.arcade.pacman_xxl.app;

import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman.XXL_MsPacMan_GameVariantConfig;
import de.amr.pacmanfx.arcade.pacman_xxl.pacman.XXL_PacMan_GameVariantConfig;
import de.amr.pacmanfx.arcade.pacman_xxl.pacman.XXL_PacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman_xxl.pacman.XXL_PacMan_GamePlay;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.game.Cartridge;
import de.amr.pacmanfx.game.GameExtension;

import java.util.Set;

public class XXL_PacMan_Cartridge {

    public static final Cartridge CARTRIDGE = new Cartridge(
        GameVariantID.ARCADE_PACMAN_XXL,
        XXL_PacMan_GamePlay::new,
        XXL_MsPacMan_GameVariantConfig::createGameFlow,
        XXL_PacMan_GameModel::new,
        XXL_PacMan_GameVariantConfig::new,
        Set.of(new GameExtension(Arcade_GameExtensions.ACTIONS, Arcade_Actions::new))
    );
}
