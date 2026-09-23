/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.Composition;
import de.amr.basics.Disposable;
import de.amr.basics.math.Vector2i;
import de.amr.basics.ui.entities.props.textdisplay.TextView;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.ui.action.core.GameApp;
import de.amr.pacmanfx.ui.action.core.QuitHandler;
import de.amr.pacmanfx.ui.assets.GlobalFonts;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneCanvasRenderingComp;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static java.util.Objects.requireNonNull;

//TODO Should a game scene really be a renderable itself or only produce renderables?

/**
 * Abstract base class for all game scenes (2D and 3D).
 */
public abstract class AbstractGameScene
    extends Composition<GameSceneComponent>
    implements GameScene, QuitHandler, Disposable, Renderable
{
    public static TextView createText(String text, Color color, int fontSize, float tileX, float tileY) {
        final var textDisplay = new TextView();
        textDisplay.data().setFillColor(color);
        textDisplay.data().setFont(GlobalFonts.ARCADE.font(fontSize));
        textDisplay.data().setText(text);
        textDisplay.pos().set(tilesPx(tileX), tilesPx(tileY));
        textDisplay.show();
        return textDisplay;
    }

    private GameApp app;

    // Game scene components

    public Optional<GameSceneCanvasRenderingComp> optCanvasRendering() {
        return optComp(GameSceneCanvasRenderingComp.class);
    }

    public GameSceneCanvasRenderingComp reqCanvasRendering() {
        return reqComp(GameSceneCanvasRenderingComp.class);
    }

    public boolean wantsClearCanvas() {
        return true;
    }

    public ActionBindingsComp actionBindings() {
        ActionBindingsComp actionBindings = optComp(ActionBindingsComp.class).orElse(null);
        if (actionBindings == null) {
            setComp(ActionBindingsComp.class, new ActionBindingsComp(this));
        }
        return reqComp(ActionBindingsComp.class);
    }

    // GameScene

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
    public void acceptGameLevel(GameSession session, GameLevel level) {
        optCanvasRendering().ifPresent(canvasRendering -> {
            final Vector2i terrainSize = level.worldMap().terrainLayer().sizeInPixel();
            canvasRendering.unscaledWidthProperty().set(terrainSize.x());
            canvasRendering.unscaledHeightProperty().set(terrainSize.y());
        });
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
        optComp(ActionBindingsComp.class).ifPresent(comp -> comp.registry().dispose());
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

    // --- Renderable

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.SCENE;
    }

}
