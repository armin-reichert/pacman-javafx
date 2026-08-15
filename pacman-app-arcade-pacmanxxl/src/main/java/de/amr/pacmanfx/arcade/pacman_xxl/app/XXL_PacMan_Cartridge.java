package de.amr.pacmanfx.arcade.pacman_xxl.app;

import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_WorldMapManager;
import de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman.XXL_MsPacMan_GameVariantUIConfig;
import de.amr.pacmanfx.arcade.pacman_xxl.pacman.XXL_PacMan_GamePlay;
import de.amr.pacmanfx.arcade.pacman_xxl.pacman.XXL_PacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman_xxl.pacman.XXL_PacMan_GameVariantUIConfig;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.core.ecs.systems.DefaultGameSystems;
import de.amr.pacmanfx.game.Cartridge;

/**
 * Extension of Arcade Pac-Man with
 * <ul>
 * <li>8 new builtin Arcade-like mazes (shamelessly stole from to the one and only
 * <a href="https://github.com/masonicGIT/pacman">Shaun Williams</a>)</li>
 * <li>the possibility to play custom maps.</li>
 * </ul>
 */
public class XXL_PacMan_Cartridge {

    public static final Cartridge CARTRIDGE = new Cartridge(
        GameVariantID.ARCADE_PACMAN_XXL,
        DefaultGameSystems::new,
        XXL_PacMan_GamePlay::new,
        XXL_PacMan_GameVariantUIConfig::createGameFlow,
        XXL_PacMan_GameRules::new,
        XXL_WorldMapManager::instance,
        XXL_PacMan_GameVariantUIConfig::new
    );
}
