/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.event.DefaultGameEventListener;
import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.event.StopAllSoundsEvent;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_ServicesAccess;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.action.ActionBindingsSet;
import de.amr.pacmanfx.ui.action.GameActionBindingsSet;
import de.amr.pacmanfx.ui.input.Input;
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

        public GameUI_ServicesAccess services() {
            return gameScene.services();
        }

        public GameScene gameScene() {
            return gameScene;
        }

        public Optional<GameLevel> optGameLevel() {
            return services().currentGame().optGameLevel();
        }

        @Override
        public void onStopAllSounds(StopAllSoundsEvent event) {
            gameScene.services().currentSoundEffects().ifPresent(GameSoundEffects::stopAll);
        }
    }

    protected final ActionBindingsSet actionBindings = new GameActionBindingsSet("Action Bindings for " + getClass().getSimpleName());

    protected final GameUI ui;

    private GameEventListener gameEventHandler;

    public GameScene(GameUI ui) {
        this.ui = requireNonNull(ui);
        setGameEventHandler(new DefaultGameEventHandler(this));
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

    public GameUI_ServicesAccess services() {
        return ui.access();
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
        final String gameVariantName = services().gameContext().gameVariantName();
        onActivate(services().configurations().getOrCreateUIConfig(gameVariantName));
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
        services().currentSoundEffects().ifPresent(GameSoundEffects::stopAll);
        Logger.trace("Game scene {} deactivated", getClass().getSimpleName());
    }

    /** Called when the scene becomes active. */
    public void onActivate(UIConfig uiConfig) {}

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
    public void onInput(GameUI ui) {
        actionBindings().actionMatchingKeyboardState(Input.instance().keyboard).ifPresent(action -> action.executeIfEnabled(ui));
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
