/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.GameEventListener;
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

    protected final ActionBindingsSet actionBindings = new GameActionBindingsSet("Action Bindings for " + getClass().getSimpleName());

    protected final AppContext appContext;

    private GameEventListener gameEventHandler;

    public GameScene(AppContext appContext) {
        this.appContext = requireNonNull(appContext);
        setGameEventHandler(new BaseGameSceneHandler(appContext));
    }

    public AppContext appContext() {
        return appContext;
    }

    public GameContext gameContext() {
        return appContext.currentGameContext();
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
        onActivate(appContext);
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
        appContext().currentSoundEffects().ifPresent(GameSoundEffects::stopAll);
        Logger.trace("Game scene {} deactivated", getClass().getSimpleName());
    }

    /** Called when the scene becomes active. */
    public void onActivate(AppContext context) {}

    /** Called when the scene is deactivated. */
    public void onDeactivate() {}

    /** Called every game tick. */
    public void onTick(long tick) {}

    /** Called when the scene is embedded into the UI. */
    public void onEmbedded() {}

    /**
     * Called when a key combination is pressed inside this scene.
     * Executes the first matching action.
     */
    public void onInput(AppContext context) {
        actionBindings().actionMatchingKeyboardState(context.input().keyboard())
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
