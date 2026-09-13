package de.amr.pacmanfx.core;

import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.gamestate.GameFlowController;
import de.amr.pacmanfx.core.model.world.map.WorldMapManager;
import de.amr.pacmanfx.core.rules.GameRules;
import org.tinylog.Logger;

public class GameVariantPlayConfig {

    private final CoinMechanism coinMechanism;
    private final GameSystems systems;
    private final GamePlay gamePlay;
    private final GameFlowController gameFlow;
    private final GameRules gameRules;
    private final WorldMapManager worldMapManager;

    private int initialLifeCount = 3;

    public GameVariantPlayConfig(
        CoinMechanism coinMechanism,
        GameSystems systems,
        GamePlay gamePlay,
        GameFlowController gameFlow,
        GameRules gameRules,
        WorldMapManager worldMapManager)
    {
        this.coinMechanism = coinMechanism;
        this.systems = systems;
        this.gamePlay = gamePlay;
        this.gameFlow = gameFlow;
        this.gameRules = gameRules;
        this.worldMapManager = worldMapManager;
    }

    public int initialLifeCount() {
        return initialLifeCount;
    }

    public void setInitialLifeCount(int initialLifeCount) {
        this.initialLifeCount = initialLifeCount;
        Logger.info("Initial life count: {}", initialLifeCount);
    }

    public CoinMechanism coinMechanism() {
        return coinMechanism;
    }

    public GameSystems systems() {
        return systems;
    }

    public GamePlay gamePlay() {
        return gamePlay;
    }

    public GameFlowController gameFlow() {
        return gameFlow;
    }

    public GameRules rules() {
        return gameRules;
    }

    public WorldMapManager worldMapManager() {
        return worldMapManager;
    }
}
