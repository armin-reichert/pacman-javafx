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
import de.amr.pacmanfx.engine.QuitHandler;
import de.amr.pacmanfx.engine.gamescene.GameScene;
import de.amr.pacmanfx.app.runtime.DefaultGameVariantRuntime;
import de.amr.pacmanfx.engine.action.ActionBindingsRegistry;
import de.amr.pacmanfx.engine.action.DefaultActionBindingsRegistry;
import de.amr.pacmanfx.ui.EngineUIContext;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneView2D;
import de.amr.pacmanfx.ui.sound.PacManGameSoundEffects;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.engine.action.ActionExecutor.executeMatchingAction;
import static java.util.Objects.requireNonNull;

/**
 * Abstract base class for all game scenes (2D and 3D).
 */
public abstract class AbstractGameScene extends Composition<Object> implements GameScene, QuitHandler, Disposable {

    protected EngineUIContext engineContext;

    public AbstractGameScene() {
        final var view2D = new GameSceneView2D();
        view2D.setUnscaledWidth(WorldMap.ARCADE_MAP_SIZE_IN_PIXELS.x());
        view2D.setUnscaledHeight(WorldMap.ARCADE_MAP_SIZE_IN_PIXELS.y());
        setComponent(GameSceneView2D.class, view2D);

        setComponent(ActionBindingsRegistry.class, new DefaultActionBindingsRegistry<>("Action Bindings for '%s'".formatted(this)));
    }

    // Typed game scene component access

    public GameSceneView2D view2D() {
        return assertComponent(GameSceneView2D.class);
    }

    @SuppressWarnings("unchecked")
    public ActionBindingsRegistry<EngineUIContext> actionBindingsRegistry() {
        return assertComponent(ActionBindingsRegistry.class);
    }

    public Optional<PacManGameSoundEffects> optSoundEffects() {
        return runtime().uiConfig().optSoundEffects();
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

    public EngineUIContext engineContext() {
        return engineContext;
    }

    public GameUI ui() {
        return engineContext.ui();
    }

    public final void setEngineContext(EngineUIContext engineContext) {
        requireNonNull(engineContext);
        if (this.engineContext != null) {
            Logger.debug("Engine context already assigned to game scene {}", this);
            return;
        }
        this.engineContext = engineContext;
        onEngineConnected();
        Logger.info("Game scene {} connected with engine", getClass().getSimpleName());
    }

    public DefaultGameVariantRuntime runtime() {
        return (DefaultGameVariantRuntime) engineContext.gameVariantManager().currentRuntime();
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
        if (engineContext != null) {
            executeMatchingAction(actionBindingsRegistry(), engineContext);
        }
    }

    // --- QuitHandler

    @Override
    public void onQuit() {
        deactivate();
    }
}
