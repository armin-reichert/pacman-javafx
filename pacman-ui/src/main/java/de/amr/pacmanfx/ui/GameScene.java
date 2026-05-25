/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.DefaultGameEventListener;
import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.event.StopAllSoundsEvent;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.action.ActionBindingsManager;
import de.amr.pacmanfx.ui.action.GameActionBindingsManager;
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
 * <p>
 * A {@code GameScene} provides:
 * <ul>
 *   <li>Access to the global {@link GameContext}</li>
 *   <li>Scene lifecycle hooks ({@link #onSceneStart()}, {@link #onTick(long)}, {@link #onSceneEnd()})</li>
 *   <li>Keyboard action binding via {@link ActionBindingsManager}</li>
 *   <li>Optional sound effects</li>
 *   <li>Optional JavaFX {@link SubScene} for 3D scenes</li>
 *   <li>Optional context menu contribution</li>
 *   <li>A pluggable {@link GameEventListener}</li>
 * </ul>
 *
 * <h2>Lifecycle</h2>
 * <ul>
 *   <li>{@link #init()} — activates the scene and assigns input bindings</li>
 *   <li>{@link #update()} — called once per game tick</li>
 *   <li>{@link #end()} — deactivates the scene, removes bindings, stops sounds</li>
 * </ul>
 *
 * <h2>Input</h2>
 * Keyboard input is resolved through {@link ActionBindingsManager}.
 * {@link #onUserInput()} executes the first matching action.
 *
 * <h2>Event Handling</h2>
 * A scene owns a {@link GameEventListener}.
 * The default handler processes generic UI updates and sound‑stop events.
 *
 * <h2>Subscenes</h2>
 * 3D scenes override {@link #optSubSceneFX()}.
 * 2D scenes typically return {@code Optional.empty()}.
 *
 * <h2>Context Menu</h2>
 * Scenes may contribute menu items via {@link #supplyContextMenu()}.
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

        public GameScene gameScene() {
            return gameScene;
        }

        public GameUI ui() {
            return gameScene.ui();
        }

        public GameContext gameContext() {
            return gameScene.gameContext();
        }

        public Game game() {
            return gameContext().game();
        }

        public Optional<GameLevel> optGameLevel() {
            return gameContext().game().optGameLevel();
        }

        public Optional<GameSoundEffects> soundEffects() {
            return gameScene.soundEffects();
        }

        @Override
        public void onStopAllSounds(StopAllSoundsEvent event) {
            soundEffects().ifPresent(GameSoundEffects::stopAll);
        }
    }

    protected final ActionBindingsManager actionBindings = new GameActionBindingsManager(Input.instance().keyboard);

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

    /** @return the game UI */
    public GameUI ui() {
        return ui;
    }

    /**
     * @return optional JavaFX subscene for this scene (3D scenes override)
     */
    public Optional<SubScene> optSubSceneFX() {
        return Optional.empty();
    }

    /**
     * @return optional sound effects for this scene
     */
    public Optional<GameSoundEffects> soundEffects() {
        return ui().currentConfig().optSoundEffects();
    }

    /**
     * @return global game context
     */
    public GameContext gameContext() {
        return ui().gameContext();
    }

    /**
     * @return action bindings for this scene
     */
    public ActionBindingsManager actionBindings() {
        return actionBindings;
    }

    /**
     * Activates the scene and assigns keyboard bindings.
     */
    public final void init() {
        onSceneStart();
        actionBindings.assignToKeyboard();
        Logger.trace("Game scene {} initialized", getClass().getSimpleName());
    }

    /**
     * Deactivates the scene, removes bindings, stops sounds.
     */
    public final void end() {
        onSceneEnd();
        actionBindings.removeFromKeyboard();
        actionBindings.dispose();
        soundEffects().ifPresent(GameSoundEffects::stopAll);
        Logger.trace("Game scene {} ends", getClass().getSimpleName());
    }

    /**
     * Called once per game tick.
     */
    public final void update() {
        final long tick = gameContext().clock().tickCount();
        onTick(tick);
    }

    /** Called when the scene becomes active. */
    public void onSceneStart() {}

    /** Called every game tick. */
    public void onTick(long tick) {}

    /** Called when the scene is deactivated. */
    public void onSceneEnd() {}

    /** Called when the scene is embedded into the UI. */
    public void onEmbeddedIntoUI() {}

    /**
     * Called when a key combination is pressed inside this scene.
     * Executes the first matching action.
     */
    public void onUserInput() {
        actionBindings().matchingAction().ifPresent(action -> action.executeIfEnabled(ui()));
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
