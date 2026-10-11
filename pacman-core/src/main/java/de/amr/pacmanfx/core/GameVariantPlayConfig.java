package de.amr.pacmanfx.core;

import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.gamestate.GameFlow;
import de.amr.pacmanfx.core.model.world.map.WorldMapManager;
import de.amr.pacmanfx.core.rules.GameRules;

public record GameVariantPlayConfig(
    GameSystems systems,
    GamePlay gamePlay,
    GameFlow gameFlow,
    GameRules gameRules,
    WorldMapManager worldMapManager) {}
