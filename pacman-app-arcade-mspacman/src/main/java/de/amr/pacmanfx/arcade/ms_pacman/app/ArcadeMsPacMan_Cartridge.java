/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.app;

import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GamePlay;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameVariantConfig;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameSystems;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_WorldMapManager;
import de.amr.pacmanfx.arcade.ms_pacman.rules.ArcadeMsPacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.game.Cartridge;
import de.amr.pacmanfx.game.GameExtension;

import java.util.Set;

/**
 * Ms. Pac-Man Arcade game.
 *
 * <p>There are slight differences to the original Arcade game.
 * <ul>
 *     <li>Attract mode is just a random hunting for at least 20 seconds.</li>
 *     <li>Timing of hunting phases unclear, just took all the information I had</li>
 *     <li>Bonus does not follow original "fruit paths" but randomly selects a portal to
 *     enter the maze, turns around the house and leaves the maze at a random portal on the other side</li>
 * </ul>
 * </p>
 */
public class ArcadeMsPacMan_Cartridge {

    public static final Cartridge CARTRIDGE = new Cartridge(
        GameVariantID.ARCADE_MS_PACMAN,
        ArcadeMsPacMan_GameSystems::new,
        ArcadeMsPacMan_GamePlay::new,
        ArcadeMsPacMan_GameVariantConfig::createGameFlow,
        ArcadeMsPacMan_GameRules::new,
        ArcadeMsPacMan_WorldMapManager::new,
        ArcadeMsPacMan_GameVariantConfig::new,
        Set.of(new GameExtension(Arcade_GameExtensions.ACTIONS, _ -> new Arcade_Actions()))
    );
}
