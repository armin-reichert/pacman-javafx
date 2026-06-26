/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.app;

import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacManConfig;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameFlow;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.game.Cartridge;
import de.amr.pacmanfx.ui.game.GameExtension;

import java.util.Set;

public class ArcadeMsPacMan_Cartridge {

    public static final Cartridge CARTRIDGE = new Cartridge(
        GameVariantID.ARCADE_MS_PACMAN,
        Arcade_GameFlow::new,
        ArcadeMsPacMan_GameModel::new,
        ArcadeMsPacMan_GameRules::new,
        ArcadeMsPacManConfig::new,
        Set.of(
            new GameExtension(Arcade_GameExtensions.ACTIONS, Arcade_Actions::new)
        )
    );
}
