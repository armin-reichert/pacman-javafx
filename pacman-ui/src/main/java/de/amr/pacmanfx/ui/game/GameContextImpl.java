/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.simulation.HuntingStepResult;

import static java.util.Objects.requireNonNull;

public class GameContextImpl implements GameContext {

    private final PacManGamesCollection game;
    private final GameVariantRuntime gameVariantRuntime;

    private HuntingStepResult huntingStepResult;

    public GameContextImpl(PacManGamesCollection game, GameVariantRuntime gameVariantRuntime) {
        this.game = requireNonNull(game);
        this.gameVariantRuntime = requireNonNull(gameVariantRuntime);
    }

    @Override
    public CoinMechanism coinMechanism() {
        return game.coinMechanism();
    }

    @Override
    public GameModel model() {
        return gameVariantRuntime.gameModel();
    }

    @Override
    public GameRules rules() {
        return gameVariantRuntime.gameRules();
    }

    @Override
    public GameFlow flow() {
        return gameVariantRuntime.gameFlow();
    }

    @Override
    public void startNewHuntingStep() {
        huntingStepResult = new HuntingStepResult();
    }

    @Override
    public HuntingStepResult huntingResult() {
        return huntingStepResult;
    }
}
