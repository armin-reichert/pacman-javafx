/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.Composition;
import de.amr.basics.Disposable;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.engine.gamescene.GameScene;
import de.amr.pacmanfx.engine.runtime.action.ActionBindingsRegistry;
import de.amr.pacmanfx.engine.runtime.action.GameActionBindingsRegistry;
import de.amr.pacmanfx.engine.runtime.action.GameActionContext;
import de.amr.pacmanfx.engine.QuitHandler;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneView2D;
import de.amr.pacmanfx.ui.sound.PacManGameSoundEffects;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Abstract base class for all game scenes (2D and 3D).
 */
public abstract class AbstractGameScene extends Composition<Object> implements GameScene, QuitHandler, Disposable {

    private GameActionContext actionContext;

    public AbstractGameScene() {
        final var view2D = new GameSceneView2D();
        view2D.setUnscaledWidth(WorldMap.ARCADE_MAP_SIZE_IN_PIXELS.x());
        view2D.setUnscaledHeight(WorldMap.ARCADE_MAP_SIZE_IN_PIXELS.y());
        setComponent(GameSceneView2D.class, view2D);

        setComponent(ActionBindingsRegistry.class, new GameActionBindingsRegistry("Action Bindings for '%s'".formatted(this)));
    }

    // Typed game scene component access

    public GameSceneView2D view2D() {
        return assertComponent(GameSceneView2D.class);
    }

    public ActionBindingsRegistry actionBindingsRegistry() {
        return assertComponent(ActionBindingsRegistry.class);
    }

    public Optional<PacManGameSoundEffects> optSoundEffects() {
        return actionContext().gameVariantManager().currentRuntime().uiConfig().optSoundEffects();
    }

    // Events

    protected void onEngineConnected() {}

    /**
     * Hook method called when the game scene becomes active.
     */
    protected void onActivate() {}

    /**
     * Hook method called when the game scene becomes inactive.
     */
    protected void onDeactivate() {}

    /**
     * If a 3D-variant of this game scene is active when the game level gets created, this method has not yet been called,
     * but it gets called when the 3D->2D scene switch happens.
     */
    public void onAcceptGameLevel(GameSession session, GameLevel level) {
        final Vector2i terrainSizeInPixel = level.worldMap().terrainLayer().sizeInPixel();
        view2D().unscaledWidthProperty().set(terrainSizeInPixel.x());
        view2D().unscaledHeightProperty().set(terrainSizeInPixel.y());
    }

    // Convenience
    public GameActionContext actionContext() {
        return actionContext;
    }

    public GameUI ui() {
        return actionContext.ui();
    }

    public final void setActionContext(GameActionContext actionContext) {
        requireNonNull(actionContext);
        if (this.actionContext != null) {
            Logger.debug("Engine already assigned to game scene {}", this);
            return;
        }
        this.actionContext = actionContext;
        onEngineConnected();
        Logger.info("Game scene {} connected with app", getClass().getSimpleName());
    }

    // Interface GameScene

    @Override
    public final void activate() {
        onActivate();
    }

    @Override
    public final void deactivate() {
        onDeactivate();
        optSoundEffects().ifPresent(PacManGameSoundEffects::stopAll);
    }

    @Override
    public void onInput() {
        if (actionContext != null) {
            actionBindingsRegistry().executeMatchingAction(actionContext);
        }
    }

    // --- QuitHandler

    @Override
    public void onQuit() {
        deactivate();
    }
}
