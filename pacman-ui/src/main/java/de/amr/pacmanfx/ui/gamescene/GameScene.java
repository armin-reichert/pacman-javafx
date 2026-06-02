/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.event.DefaultGameEventListener;
import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.event.StopAllSoundsEvent;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.action.ActionBindingsSet;
import de.amr.pacmanfx.ui.action.GameActionBindingsSet;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import javafx.scene.SubScene;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.ScrollEvent;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Abstract base class for all game scenes (2D and 3D).
 */
public abstract class GameScene implements Disposable {

    /**
     * Default event handler used by scenes unless replaced.
     * Handles generic UI updates and global sound stop events.
     */
    public static class DefaultGameEventHandler extends DefaultGameEventListener {

        private final GameScene gameScene;

        public DefaultGameEventHandler(GameScene gameScene) {
            this.gameScene = requireNonNull(gameScene);
        }

        public AppContext context() {
            return gameScene.context();
        }

        public GameScene gameScene() {
            return gameScene;
        }

        public Optional<GameLevel> optGameLevel() {
            return context().currentGame().optGameLevel();
        }

        @Override
        public void onStopAllSounds(StopAllSoundsEvent event) {
            context().currentSoundEffects().ifPresent(GameSoundEffects::stopAll);
        }
    }

    protected final ActionBindingsSet actionBindings = new GameActionBindingsSet("Action Bindings for " + getClass().getSimpleName());

    protected final AppContext context;

    private GameEventListener gameEventHandler;

    public GameScene(AppContext context) {
        this.context = requireNonNull(context);
        setGameEventHandler(new DefaultGameEventHandler(this));
    }

    public AppContext context() {
        return context;
    }

    public void setGameEventHandler(GameEventListener delegate) {
        gameEventHandler = requireNonNull(delegate);
    }

    public GameEventListener gameEventHandler() {
        return gameEventHandler;
    }

    /**
     * @return optional JavaFX subscene for this scene (3D scenes override)
     */
    public Optional<SubScene> optSubSceneFX() {
        return Optional.empty();
    }

    /**
     * @return action bindings for this scene
     */
    public ActionBindingsSet actionBindings() {
        return actionBindings;
    }

    /**
     * Activates the scene and assigns keyboard bindings.
     */
    public final void activate() {
        onActivate(context);
        Logger.info(actionBindings);
        Logger.trace("Game scene {} activated", getClass().getSimpleName());
    }

    /**
     * Called when the scene is deactivated.
     * Subclasses must:<br/>
     * - unbind all properties<br/>
     * - remove all listeners<br/>
     * - stop all timers<br/>
     * - release all UI references (canvas, subscene, etc.)
     */
    public final void deactivate() {
        onDeactivate();
        actionBindings.dispose();
        context().currentSoundEffects().ifPresent(GameSoundEffects::stopAll);
        Logger.trace("Game scene {} deactivated", getClass().getSimpleName());
    }

    /** Called when the scene becomes active. */
    public void onActivate(AppContext context) {}

    /** Called when the scene is deactivated. */
    public void onDeactivate() {}

    /** Called every game tick. */
    public void onTick(GameClock clock) {}

    /** Called when the scene is embedded into the UI. */
    public void onEmbedded() {}

    /**
     * Called when a key combination is pressed inside this scene.
     * Executes the first matching action.
     */
    public void onInput(AppContext context) {
        actionBindings().actionMatchingKeyboardState(context.input().keyboard)
            .ifPresent(action -> action.executeIfEnabled(context));
    }

    /**
     * Called when a scroll event occurs inside this scene.
     */
    public void onScroll(ScrollEvent scrollEvent) {}

    /**
     * @return optional context menu contributed by this scene
     */
    public Optional<ContextMenu> supplyContextMenu() {
        return Optional.empty();
    }
}
