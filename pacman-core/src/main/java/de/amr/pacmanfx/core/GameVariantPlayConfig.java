package de.amr.pacmanfx.core;

import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.gamestate.GameFlow;
import de.amr.pacmanfx.core.model.world.map.WorldMapManager;
import de.amr.pacmanfx.core.rules.GameRules;

public class GameVariantPlayConfig {

    private final GameSystems systems;
    private final GamePlay gamePlay;
    private final GameFlow gameFlow;
    private final GameRules gameRules;
    private final WorldMapManager worldMapManager;

    public GameVariantPlayConfig(
        GameSystems systems,
        GamePlay gamePlay,
        GameFlow gameFlow,
        GameRules gameRules,
        WorldMapManager worldMapManager)
    {
        this.systems = systems;
        this.gamePlay = gamePlay;
        this.gameFlow = gameFlow;
        this.gameRules = gameRules;
        this.worldMapManager = worldMapManager;
    }

    public GameSystems systems() {
        return systems;
    }

    public GamePlay gamePlay() {
        return gamePlay;
    }

    public GameFlow gameFlow() {
        return gameFlow;
    }

    public GameRules rules() {
        return gameRules;
    }

    public WorldMapManager worldMapManager() {
        return worldMapManager;
    }
}
