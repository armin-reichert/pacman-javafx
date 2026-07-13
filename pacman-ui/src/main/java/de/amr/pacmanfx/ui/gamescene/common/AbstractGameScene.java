/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.GameEventListener;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.GameActionBindingsMap;
import de.amr.pacmanfx.ui.action.core.GameActionContext;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import javafx.scene.SubScene;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Abstract base class for all game scenes (2D and 3D).
 */
public abstract class AbstractGameScene implements GameScene, Disposable {

    private final GameActionContext actionContext;

    private final ActionBindingsRegistry actionBindings = new GameActionBindingsMap("Action Bindings for " + getClass().getSimpleName());

    private GameEventListener gameEventHandler;

    public AbstractGameScene(GameActionContext actionContext) {
        this.actionContext = requireNonNull(actionContext);
        gameEventHandler = new BaseGameEventHandler(actionContext);
    }

    public void setGameEventHandler(GameEventListener delegate) {
        gameEventHandler = requireNonNull(delegate);
    }

    /**
     * Hook method called when the game scene becomes active.
     */
    protected void onActivate() {}

    /**
     * Hook method called when the game scene becomes inactive.
     */
    protected void onDeactivate() {}

    // --- Interface "GameScene"

    @Override
    public final void activate() {
        onActivate();
        Logger.trace("Game scene {} activated", getClass().getSimpleName());
        Logger.info(actionBindings);
    }

    @Override
    public final void deactivate() {
        onDeactivate();
        actionBindings.dispose();
        optSoundEffects().ifPresent(GameSoundEffects::stopAll);
        Logger.trace("Game scene {} deactivated", getClass().getSimpleName());
    }

    @Override
    public ActionBindingsRegistry actionBindings() {
        return actionBindings;
    }

    @Override
    public GameActionContext actionContext() {
        return actionContext;
    }

    @Override
    public Input input() {
        return actionContext.input();
    }

    @Override
    public GameContext gameContext() {
        return actionContext.currentGameContext();
    }

    @Override
    public GameModel gameModel() {
        return gameContext().model();
    }

    @Override
    public GameState gameState() {
        return gameContext().state();
    }

    @Override
    public Optional<SubScene> optSubSceneFX() {
        return Optional.empty();
    }

    @Override
    public Optional<GameSoundEffects> optSoundEffects() {
        return actionContext.variants().currentVariant().config().optSoundEffects();
    }

    @Override
    public GameEventListener gameEventHandler() {
        return gameEventHandler;
    }

    @Override
    public void onInput() {
        actionBindings().executeMatchingAction(input());
    }

    // --- Interface "QuitHandler"

    @Override
    public void handleQuit(GameActionContext ac) {
        Logger.info("Game scene {} quitted", getClass().getSimpleName());
        onDeactivate();
    }
}
