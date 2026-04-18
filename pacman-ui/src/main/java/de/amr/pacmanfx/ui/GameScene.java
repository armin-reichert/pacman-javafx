/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.event.StopAllSoundsEvent;
import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui.action.ActionBindingsManager;
import de.amr.pacmanfx.ui.action.GameActionBindingsManager;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.layout.GameUI_ContextMenu;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import javafx.scene.SubScene;
import javafx.scene.input.ScrollEvent;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Common base class of all game scenes (2D and 3D).
 */
public abstract class GameScene implements GameEventListener, Disposable {

    protected final ActionBindingsManager actionBindings = new GameActionBindingsManager(Input.instance().keyboard);
    protected GameUI ui;

    /**
     * @return the game UI
     */
    public final GameUI ui() {
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
        return ui().currentConfig().soundEffects();

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
        actionBindings.pluginKeyboard();
        Logger.info("Game scene {} initialized", getClass().getSimpleName());
    }

    public final void end() {
        onSceneEnd();
        actionBindings.unplugKeyboard();
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

    public void onEmbeddedIntoUI(GameUI ui) {
        this.ui = requireNonNull(ui);
    }

    /**
     * Called when a key combination has been pressed inside this game scene. By public, the first matching action
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

    @Override
    public void onStopAllSounds(StopAllSoundsEvent e) {
        soundEffects().ifPresent(GameSoundEffects::stopAll);
    }

    /**
     * @param game the current game
     * @return context menu provided by this game scene which is merged into the view's context menu
     */
    public Optional<GameUI_ContextMenu> supplyContextMenu(Game game) {
        return Optional.empty();
    }
}