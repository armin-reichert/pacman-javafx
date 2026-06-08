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

    private final GameImplementation appContextImpl;
    private GameFlow gameFlow;
    private HuntingStepResult huntingStepResult;

    public GameContextImpl(GameImplementation appContextImpl) {
        this.appContextImpl = requireNonNull(appContextImpl);
    }

    public void setGameFlow(GameFlow gameFlow) {
        this.gameFlow = requireNonNull(gameFlow);
    }

    private GameVariantSpecification currentGame() {
        return appContextImpl.gameForVariant(appContextImpl.currentGameVariantName());
    }

    @Override
    public CoinMechanism coinMechanism() {
        return appContextImpl.coinMechanism();
    }

    @Override
    public GameModel model() {
        return currentGame().gameModel();
    }

    @Override
    public GameRules rules() {
        return currentGame().gameRules();
    }

    @Override
    public GameFlow flow() {
        return gameFlow;
    }

    @Override
    public CollisionStrategy collisionStrategy() {
        return appContextImpl.collisionStrategy();
    }

    @Override
    public boolean isCollisionDoubleChecked() {
        return appContextImpl.isCollisionDoubleChecked();
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
