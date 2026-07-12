/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.app;

import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacManGameVariant;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GamePlay;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameFlow;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.game.Cartridge;
import de.amr.pacmanfx.game.GameExtension;

import java.util.Set;

public class ArcadeMsPacMan_Cartridge {

    public static final Cartridge CARTRIDGE = new Cartridge(
        GameVariantID.ARCADE_MS_PACMAN,
        ArcadeMsPacMan_GamePlay::new,
        Arcade_GameFlow::new,
        ArcadeMsPacMan_GameModel::new,
        ArcadeMsPacManGameVariant::new,
        Set.of(new GameExtension(Arcade_GameExtensions.ACTIONS, Arcade_Actions::new))
    );
}
