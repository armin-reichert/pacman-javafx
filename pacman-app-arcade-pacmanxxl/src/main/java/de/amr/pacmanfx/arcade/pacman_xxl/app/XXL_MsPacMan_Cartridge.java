package de.amr.pacmanfx.arcade.pacman_xxl.app;

import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameSystems;
import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_WorldMapManager;
import de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman.XXL_MsPacMan_GameFlow;
import de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman.XXL_MsPacMan_GamePlay;
import de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman.XXL_MsPacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman.XXL_MsPacMan_GameVariantUIConfig;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.game.Cartridge;

/**
 * Extension of Arcade Ms. Pac-Man with
 * <ul>
 * <li>8 new builtin Arcade-like mazes (shamelessly stole from to the one and only
 * <a href="https://github.com/masonicGIT/pacman">Shaun Williams</a>)</li>
 * <li>the possibility to play custom maps.</li>
 * </ul>
 */
public class XXL_MsPacMan_Cartridge {

    public static final Cartridge CARTRIDGE = new Cartridge(
        GameVariantID.ARCADE_MS_PACMAN_XXL,
        ArcadeMsPacMan_GameSystems::new,
        XXL_MsPacMan_GamePlay::new,
        XXL_MsPacMan_GameFlow::new,
        XXL_MsPacMan_GameRules::new,
        XXL_WorldMapManager::instance,
        XXL_MsPacMan_GameVariantUIConfig::new
    );
}
