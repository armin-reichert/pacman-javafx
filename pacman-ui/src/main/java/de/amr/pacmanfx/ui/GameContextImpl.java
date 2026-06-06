package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.simulation.HuntingStepResult;
import de.amr.pacmanfx.ui.app.GameSpecification;

import static java.util.Objects.requireNonNull;

public class GameContextImpl implements GameContext {

    private final GamesApp gamesApp;
    private final GameFlow gameFlow;
    private HuntingStepResult huntingStepResult;

    public GameContextImpl(GamesApp gamesApp, GameFlow gameFlow) {
        this.gamesApp = requireNonNull(gamesApp);
        this.gameFlow = requireNonNull(gameFlow);
    }

    private GameSpecification currentGame() {
        return gamesApp.gameForVariant(gamesApp.currentGameVariantName());
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
        return gamesApp.collisionStrategy();
    }

    @Override
    public Boolean isCollisionDoubleChecked() {
        return gamesApp.isCollisionDoubleChecked();
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
