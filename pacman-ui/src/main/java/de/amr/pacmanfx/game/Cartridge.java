/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.basics.Named;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.gamestate.GameFlowController;
import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.model.rules.GameRules;
import de.amr.pacmanfx.core.model.world.map.WorldMapManager;

import java.util.function.Supplier;

public record Cartridge(
    Named id,
    Supplier<? extends GameSystems> systemsFactory,
    Supplier<? extends GamePlay> gamePlayFactory,
    Supplier<? extends GameFlowController> gameFlowFactory,
    Supplier<? extends GameRules> gameRulesFactory,
    Supplier<? extends WorldMapManager> worldMapManagerFactory,
    Supplier<? extends GameVariantUIConfig> uiConfigFactory)
{}