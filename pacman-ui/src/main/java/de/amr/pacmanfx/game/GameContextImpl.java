/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.GameEventManager;
import de.amr.pacmanfx.core.event.GameEventManagerImpl;
import de.amr.pacmanfx.core.flow.GameFlow;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.simulation.GamePlay;

import static java.util.Objects.requireNonNull;

/**
 * Context for the currently running game variant.
 */
public class GameContextImpl implements GameContext {

    private final CoinMechanism coinMechanism;

    private final GameVariant gameVariant;

    private final GameEventManager eventManager;

    public GameContextImpl(CoinMechanism coinMechanism, GameVariant gameVariant) {
        this.coinMechanism = requireNonNull(coinMechanism);
        this.gameVariant = requireNonNull(gameVariant);
        this.eventManager = new GameEventManagerImpl();
        //TODO rethink this
        model().hudState().creditProperty().bind(coinMechanism().numCoinsProperty());
    }

    @Override
    public GameCheats cheats() {
        return gameVariant.cheats();
    }

    @Override
    public CoinMechanism coinMechanism() {
        return coinMechanism;
    }

    @Override
    public GameEventManager eventManager() {
        return eventManager;
    }

    @Override
    public GameModel model() {
        return gameVariant.gameModel();
    }

    @Override
    public GameLevel level() {
        return model().optLevel().orElse(null);
    }

    @Override
    public GameFlow flow() {
        return gameVariant.gameFlow();
    }

    @Override
    public GamePlay gamePlay() {
        return gameVariant.gamePlay();
    }
}
