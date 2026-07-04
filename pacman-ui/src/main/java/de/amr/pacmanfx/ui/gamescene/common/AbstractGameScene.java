/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.GameActionBindingsMap;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import javafx.scene.SubScene;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Abstract base class for all game scenes (2D and 3D).
 */
public abstract class AbstractGameScene implements GameScene, Disposable {

    private final Game game;

    private final ActionBindingsRegistry actionBindings = new GameActionBindingsMap("Action Bindings for " + getClass().getSimpleName());

    private GameEventListener gameEventHandler;

    public AbstractGameScene(Game game) {
        this.game = requireNonNull(game);
        gameEventHandler = new BaseGameEventHandler(game);
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
    public Game game() {
        return game;
    }

    @Override
    public GameContext gameContext() {
        return game().context();
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
        return game().soundEffects();
    }

    @Override
    public GameEventListener gameEventHandler() {
        return gameEventHandler;
    }

    @Override
    public void onInput() {
        actionBindings().executeMatchingAction(game.input());
    }

    // --- Interface "QuitHandler"

    @Override
    public void handleQuit(Game game) {
        Logger.info("Game scene {} quitted", getClass().getSimpleName());
        onDeactivate();
    }
}
