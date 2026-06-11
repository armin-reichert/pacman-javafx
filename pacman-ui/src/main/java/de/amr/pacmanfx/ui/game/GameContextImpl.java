/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.simulation.HuntingStepResult;

import static java.util.Objects.requireNonNull;

public class GameContextImpl implements GameContext {

    private final GameImpl game;
    private final GameVariant gameVariant;

    private HuntingStepResult huntingStepResult;

    public GameContextImpl(GameImpl game, GameVariant gameVariant) {
        this.game = requireNonNull(game);
        this.gameVariant = requireNonNull(gameVariant);
    }

    private GameVariant currentGameVariant() {
        return gameVariant;
    }

    @Override
    public CoinMechanism coinMechanism() {
        return game.coinMechanism();
    }

    @Override
    public GameModel model() {
        return gameVariant.gameModel();
    }

    @Override
    public GameRules rules() {
        return gameVariant.gameRules();
    }

    @Override
    public GameFlow flow() {
        return gameVariant.gameFlow();
    }

    @Override
    public CollisionStrategy collisionStrategy() {
        return game.collisionStrategy();
    }

    @Override
    public boolean isCollisionDoubleChecked() {
        return game.isCollisionDoubleChecked();
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
