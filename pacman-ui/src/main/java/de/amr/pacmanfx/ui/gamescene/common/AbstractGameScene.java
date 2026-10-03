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
import de.amr.pacmanfx.ui.action.core.GameApp;
import de.amr.pacmanfx.ui.action.core.QuitHandler;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneView2D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

/**
 * Abstract base class for all game scenes (2D and 3D).
 */
public abstract class AbstractGameScene extends Composition<Object> implements GameScene, QuitHandler, Disposable {

    private GameApp app;

    public AbstractGameScene() {
        final var r2d = new GameSceneView2D();
        r2d.setUnscaledWidth(WorldMap.ARCADE_MAP_SIZE_IN_PIXELS.x());
        r2d.setUnscaledHeight(WorldMap.ARCADE_MAP_SIZE_IN_PIXELS.y());
        setComp(GameSceneView2D.class, r2d);

        setComp(ActionBindingsComp.class, new ActionBindingsComp(this));
    }

    // Typed game scene component access

    public GameSceneView2D view2D() {
        return reqComp(GameSceneView2D.class);
    }

    public ActionBindingsComp actionBindings() {
        return reqComp(ActionBindingsComp.class);
    }

    // Events

    protected void onAppConnected() {}

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
        final Vector2i terrainSize = level.worldMap().terrainLayer().sizeInPixel();
        view2D().unscaledWidthProperty().set(terrainSize.x());
        view2D().unscaledHeightProperty().set(terrainSize.y());
    }

    // Interface GameScene

    @Override
    public final void setApp(GameApp app) {
        requireNonNull(app);
        if (this.app != null) {
            return;
        }
        this.app = app;
        onAppConnected();
        Logger.info("Game scene {} connected with app", getClass().getSimpleName());
    }

    @Override
    public GameApp app() {
        return requireNonNull(app);
    }

    @Override
    public void activate() {
        onActivate();
    }

    @Override
    public final void deactivate() {
        onDeactivate();
        optSoundEffects().ifPresent(GameSoundEffects::stopAll);
    }

    @Override
    public void onInput() {
        optComp(ActionBindingsComp.class)
            .map(ActionBindingsComp::registry)
            .ifPresent(registry -> registry.executeMatchingAction(app()));
    }

    // --- QuitHandler

    @Override
    public void onQuit() {
        deactivate();
    }
}
