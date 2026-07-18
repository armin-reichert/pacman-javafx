package de.amr.pacmanfx.arcade.pacman_xxl.app;

import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman.XXL_MsPacMan_GameVariantConfig;
import de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman.XXL_MsPacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman.XXL_MsPacMan_GamePlay;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.game.Cartridge;
import de.amr.pacmanfx.game.GameExtension;

import java.util.Set;

public class XXL_MsPacMan_Cartridge {

    public static final Cartridge CARTRIDGE = new Cartridge(
        GameVariantID.ARCADE_MS_PACMAN_XXL,
        XXL_MsPacMan_GamePlay::new,
        XXL_MsPacMan_GameVariantConfig::createGameFlow,
        XXL_MsPacMan_GameModel::new,
        XXL_MsPacMan_GameVariantConfig::new,
        Set.of(new GameExtension(Arcade_GameExtensions.ACTIONS, Arcade_Actions::new))
    );
}
