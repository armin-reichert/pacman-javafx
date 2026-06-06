package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.simulation.HuntingStepResult;
import de.amr.pacmanfx.ui.app.GameSpecification;

import static java.util.Objects.requireNonNull;

public class GameContextImpl implements GameContext {

    private final AppContextImpl appContextImpl;
    private GameFlow gameFlow;
    private HuntingStepResult huntingStepResult;

    public GameContextImpl(AppContextImpl appContextImpl) {
        this.appContextImpl = requireNonNull(appContextImpl);
    }

    public void setGameFlow(GameFlow gameFlow) {
        this.gameFlow = requireNonNull(gameFlow);
    }

    private GameSpecification currentGame() {
        return appContextImpl.gameForVariant(appContextImpl.currentGameVariantName());
    }

    @Override
    public CoinMechanism coinMechanism() {
        return appContextImpl.coinMechanism();
    }

    @Override
    public GameModel gameModel() {
        return currentGame().gameModel();
    }

    @Override
    public GameRules gameRules() {
        return currentGame().gameRules();
    }

    @Override
    public GameFlow gameFlow() {
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
