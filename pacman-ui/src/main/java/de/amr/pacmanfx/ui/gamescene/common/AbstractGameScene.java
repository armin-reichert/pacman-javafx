/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameActionBindingsMap;
import de.amr.pacmanfx.ui.action.core.QuitHandler;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameFacade;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.ScrollEvent;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Abstract base class for all game scenes (2D and 3D).
 */
public abstract class AbstractGameScene implements GameFacade, QuitHandler, Disposable {

    private final ActionBindingsRegistry actionBindings = new GameActionBindingsMap("Action Bindings for " + getClass().getSimpleName());

    private final Game game;

    private GameEventListener gameEventHandler;

    public AbstractGameScene(Game game) {
        this.game = requireNonNull(game);
        gameEventHandler = new BaseGameEventHandler(game);
    }

    public Game game() {
        return game;
    }

    public void setGameEventHandler(GameEventListener delegate) {
        gameEventHandler = requireNonNull(delegate);
    }

    public GameEventListener gameEventHandler() {
        return gameEventHandler;
    }

    /**
     * @return action bindings for this scene
     */
    public ActionBindingsRegistry actionBindings() {
        return actionBindings;
    }

    /**
     * Activates the scene and assigns keyboard bindings.
     */
    public final void activate() {
        onActivate();
        Logger.trace("Game scene {} activated", getClass().getSimpleName());
        Logger.info(actionBindings);
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
        optSoundEffects().ifPresent(GameSoundEffects::stopAll);
        Logger.trace("Game scene {} deactivated", getClass().getSimpleName());
    }

    /** Called when the scene becomes active. */
    public void onActivate() {}

    /** Called when the scene is deactivated. */
    public void onDeactivate() {}

    @Override
    public void handleQuit(Game game) {
        onDeactivate();
    }

    /**
     * Called every game tick.
     *
     * @param tick the tick count of the global game clock. Note that each game state has its own timer!
     */
    public void onTick(long tick) {}

    /** Called when the scene is embedded into the UI. */
    public void onEmbedded() {}

    /**
     * Called when a key combination is pressed inside this scene.
     * Executes the first matching action.
     */
    public void onInput() {
        actionBindings().findActionMatchingPressedKeys(game.input().keyboard()).ifPresent(GameAction::execute);
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
