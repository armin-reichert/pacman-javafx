/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.GameEventManager;
import de.amr.pacmanfx.core.flow.GameFlowController;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.GameActionBindingsMap;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import javafx.scene.SubScene;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Abstract base class for all game scenes (2D and 3D).
 */
public abstract class AbstractGameScene implements GameScene, GameSceneGameEventHandler, Disposable {

    private final GameAppContext appContext;

    private final ActionBindingsRegistry actionBindings = new GameActionBindingsMap("Action Bindings for " + getClass().getSimpleName());

    public AbstractGameScene(GameAppContext appContext) {
        this.appContext = requireNonNull(appContext);
    }

    /**
     * Hook method called when the game scene becomes active.
     */
    protected void onActivate() {}

    /**
     * Hook method called when the game scene becomes inactive.
     */
    protected void onDeactivate() {}

    public GameEventManager eventManager() {
        return gameContext().eventManager();
    }

    public GameFlowController gameFlow() {
        return gameContext().flow();
    }

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
    public GameAppContext appContext() {
        return appContext;
    }

    @Override
    public GameContext gameContext() {
        return appContext.currentGameContext();
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
    public Input input() {
        return appContext.input();
    }

    @Override
    public Optional<SubScene> optSubSceneFX() {
        return Optional.empty();
    }

    @Override
    public Optional<GameSoundEffects> optSoundEffects() {
        return appContext.variants().currentVariant().config().optSoundEffects();
    }

    @Override
    public void onInput() {
        actionBindings().executeMatchingAction(input());
    }

    // --- Interface "QuitHandler"

    @Override
    public void handleQuit(GameAppContext ac) {
        Logger.info("Game scene {} quit", getClass().getSimpleName());
        onDeactivate();
    }

    // --- Interface GameSceneGameEventHandler

    @Override
    public GameScene gameScene() {
        return this;
    }
}
