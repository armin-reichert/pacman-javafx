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

    private final GameImplementation gameImpl;
    private final GameVariantRuntime variantRuntime;

    private HuntingStepResult huntingStepResult;

    public GameContextImpl(GameImplementation gameImpl, GameVariantRuntime variantRuntime) {
        this.gameImpl = requireNonNull(gameImpl);
        this.variantRuntime = requireNonNull(variantRuntime);
    }

    private GameVariantRuntime currentVariantRuntime() {
        return variantRuntime;
    }

    @Override
    public CoinMechanism coinMechanism() {
        return gameImpl.coinMechanism();
    }

    @Override
    public GameModel model() {
        return variantRuntime.gameModel();
    }

    @Override
    public GameRules rules() {
        return variantRuntime.gameRules();
    }

    @Override
    public GameFlow flow() {
        return variantRuntime.gameFlow();
    }

    @Override
    public CollisionStrategy collisionStrategy() {
        return gameImpl.collisionStrategy();
    }

    @Override
    public boolean isCollisionDoubleChecked() {
        return gameImpl.isCollisionDoubleChecked();
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
