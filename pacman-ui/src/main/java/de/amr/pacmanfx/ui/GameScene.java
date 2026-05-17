/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui;

import de.amr.basics.Disposable;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.DefaultGameEventListener;
import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.event.GenericChangeEvent;
import de.amr.pacmanfx.event.StopAllSoundsEvent;
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
 * Common base class of all game scenes (2D and 3D).
 */
public abstract class GameScene implements Disposable {

    public static class DefaultGameEventHandler extends DefaultGameEventListener {

        private final GameScene gameScene;

        public DefaultGameEventHandler(GameScene gameScene) {
            this.gameScene = gameScene;
        }

        public GameScene gameScene() {
            return gameScene;
        }

        public GameUI ui() {
            return gameScene().ui();
        }

        @Override
        public void onStopAllSounds(StopAllSoundsEvent e) {
            gameScene.soundEffects().ifPresent(GameSoundEffects::stopAll);
        }

        @Override
        public void onGenericChange(GenericChangeEvent event) {
            gameScene.ui().forceGameSceneUpdate();
        }

    }

    protected final ActionBindingsManager actionBindings = new GameActionBindingsManager(Input.instance().keyboard);
    protected final GameUI ui;

    private GameEventListener gameEventHandler = new DefaultGameEventListener();

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
     * @return the game UI
     */
    public GameUI ui() {
        return ui;
    }

    /**
     * @return (optional) JavaFX subscene associated with this game scene. 2D scenes without camera do not need one.
     */
    public Optional<SubScene> optSubScene() {
        return Optional.empty();
    }

    /**
     * @return the (optional) game sound effects
     */
    public Optional<GameSoundEffects> soundEffects() {
        return ui().currentConfig().optSoundEffects();

    }
    /**
     * @return the global context providing access to some global data as the currently selected game variant or the
     *         coin mechanism used by Arcade games
     */
    public GameContext gameContext() {
        return ui().gameContext();
    }

    /**
     * @return the action bindings defined for this game scene
     */
    public ActionBindingsManager actionBindings() {
        return actionBindings;
    }

    /**
     * Called when the scene becomes the current one.
     */
    public final void init() {
        onSceneStart();
        actionBindings.assignToKeyboard();
        Logger.info("Game scene {} initialized", getClass().getSimpleName());
    }

    public final void end() {
        onSceneEnd();
        actionBindings.removeFromKeyboard();
        actionBindings.dispose();
        soundEffects().ifPresent(GameSoundEffects::stopAll);
        Logger.info("Game scene {} ends", getClass().getSimpleName());
    }

    public void update() {
        final long tick = gameContext().clock().tickCount();
        if (Logger.isTraceEnabled()) {
            Logger.trace("{}: Tick {}", getClass().getSimpleName(), tick);
        }
        onTick(tick);
    }

    /**
     * Called when the scene is initialized.
     * Subclasses implement their setup logic here (loading assets, configuring
     * input, preparing animations, etc.).
     */
    public void onSceneStart() {}

    /**
     * Called on every tick of the game clock.
     *
     * @param tick the current game clock tick count
     */
    public void onTick(long tick) {}

    /**
     * Called when the scene ends.
     * Subclasses implement cleanup logic here (stopping animations, releasing
     * temporary resources, etc.).
     */
    public void onSceneEnd() {}

    public void onEmbeddedIntoUI() {}

    /**
     * Called when a key combination has been pressed inside this game scene. By default, the first matching action
     * defined in the action bindings is executed.
     */
    public void onUserInput() {
        actionBindings().matchingAction().ifPresent(action -> action.executeIfEnabled(ui()));
    }

    /**
     * Called when a scroll event (mouse wheel event) has been triggered inside this game scene
     * @param scrollEvent the scroll event
     */
    public void onScroll(ScrollEvent scrollEvent) {}

    /**
     * @return context menu provided by this game scene which is merged into the view's context menu
     */
    public Optional<ContextMenu> supplyContextMenu() {
        return Optional.empty();
    }
}