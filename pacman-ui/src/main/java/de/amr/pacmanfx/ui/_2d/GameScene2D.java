/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.ui.DefaultActionBindingsManager;
import de.amr.pacmanfx.ui.api.ActionBindingsManager;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.rendering.GameRenderer;
import de.amr.pacmanfx.uilib.rendering.HUDRenderer;
import javafx.beans.property.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.uilib.rendering.GameRenderer.fillCanvas;
import static java.util.Objects.requireNonNull;

/**
 * Base class of all 2D scenes.
 */
public abstract class GameScene2D implements GameScene {

    protected final GameUI ui;

    protected final Color debugTextFill;
    protected final Color debugTextStroke;
    protected final Font debugTextFont;

    protected final ObjectProperty<Font> arcadeFont8 = new SimpleObjectProperty<>();
    protected final ObjectProperty<Font> arcadeFont6 = new SimpleObjectProperty<>();
    protected final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.BLACK);
    protected final BooleanProperty debugInfoVisible = new SimpleBooleanProperty(false);
    protected final DoubleProperty scaling = new SimpleDoubleProperty(1.0f);

    protected final ActionBindingsManager actionBindings;
    protected final AnimationRegistry animationRegistry = new AnimationRegistry();
    protected GameRenderer gameRenderer;
    protected HUDRenderer hudRenderer;
    protected Canvas canvas;
    protected final List<Actor> actorsInZOrder = new ArrayList<>();

    protected GameScene2D(GameUI ui) {
        this.ui = requireNonNull(ui);
        actionBindings = new DefaultActionBindingsManager();
        debugTextFill   = ui.uiPreferences().getColor("debug_text.fill");
        debugTextStroke = ui.uiPreferences().getColor("debug_text.stroke");
        debugTextFont   = ui.uiPreferences().getFont("debug_text.font");
    }

    @Override
    public GameContext gameContext() {
        return ui.gameContext();
    }

    @Override
    public final void init() {
        arcadeFont8.bind(scaling.map(s -> ui.assets().arcadeFont(s.floatValue() * 8)));
        arcadeFont6.bind(scaling.map(s -> ui.assets().arcadeFont(s.floatValue() * 6)));
        doInit();
        actionBindings.installBindings(ui.keyboard());
        ui.keyboard().logCurrentBindings();
    }

    @Override
    public final void end() {
        ui.soundManager().stopAll();
        doEnd();
    }

    @Override
    public void handleKeyboardInput() {
        actionBindings.matchingAction(ui.keyboard()).ifPresent(gameAction -> gameAction.executeIfEnabled(ui));
    }

    protected abstract void doInit();
    protected abstract void doEnd();

    @Override
    public ActionBindingsManager actionBindings() { return actionBindings; }

    @Override
    public void onStopAllSounds(GameEvent event) { ui.soundManager().stopAll(); }

    @Override
    public void onUnspecifiedChange(GameEvent event) {
        // TODO: remove (this is only used by game state GameState.TESTING_CUT_SCENES)
        ui.updateGameScene(true);
    }

    public void  setScaling(double scaling) { this.scaling.set((float) scaling); }
    public double scaling() { return scaling.get(); }
    public double scaled(double value) { return (float) value * scaling(); }

    public Font scaledArcadeFont8() { return arcadeFont8.get(); }
    public Font scaledArcadeFont6() { return arcadeFont6.get(); }

    public Color backgroundColor() { return backgroundColor.get(); }
    public void setBackgroundColor(Color color) { backgroundColor.set(color); }

    @SuppressWarnings("unchecked")
    public <T extends GameRenderer & DebugInfoRenderer> T renderer() { return (T) gameRenderer; }

    public void setGameRenderer(GameRenderer renderer) { gameRenderer = requireNonNull(renderer); }

    public void setHudRenderer(HUDRenderer hudRenderer) {
        this.hudRenderer = requireNonNull(hudRenderer);
    }

    public Canvas canvas() { return canvas; }
    public void setCanvas(Canvas canvas) { this.canvas = canvas; }

    public ObjectProperty<Color> backgroundColorProperty() { return backgroundColor; }

    public BooleanProperty debugInfoVisibleProperty() { return debugInfoVisible; }
    public boolean isDebugInfoVisible() { return debugInfoVisible.get(); }

    public DoubleProperty scalingProperty() { return scaling; }

    /**
     * @return (unscaled) scene size in pixels e.g. 224x288
     */
    public abstract Vector2f sizeInPx();

    public void clear() {
        if (canvas != null) {
            fillCanvas(canvas, backgroundColor());
        } else {
            Logger.error("Cannot clear scene, canvas not available");
        }
    }

    /**
     * Default implementation: scales the renderer to the current scene scaling,
     * clears the canvas and draws the scores (if on), scene content and debug information (if on).
     */
    public void draw() {
        if (gameRenderer == null) {
            gameRenderer = ui.currentConfig().createGameRenderer(canvas);
        }
        clear();
        gameRenderer.setScaling(scaling());
        drawSceneContent();
        if (debugInfoVisible.get()) {
            drawDebugInfo();
        }
        if (hudRenderer != null) {
            hudRenderer.drawHUD(gameContext(), gameContext().game().hudData(), sizeInPx());
        }
    }

    /**
     * Draws the scene content using the already scaled game renderer.
     */
    public abstract void drawSceneContent();

    /**
     * Default implementation: Draws a grid indicating the tiles, the game state and the state timer.
     */
    protected void drawDebugInfo() {
        Vector2f sizePx = sizeInPx();
        gameRenderer.drawTileGrid(sizePx.x(), sizePx.y(), Color.LIGHTGRAY);
        gameRenderer.ctx().setFill(debugTextFill);
        gameRenderer.ctx().setStroke(debugTextStroke);
        gameRenderer.ctx().setFont(debugTextFont);
        TickTimer stateTimer = gameContext().gameState().timer();
        String stateText = "Game State: '%s' (Tick %d of %s)".formatted(
            gameContext().gameState().name(),
            stateTimer.tickCount(),
            stateTimer.durationTicks() == TickTimer.INDEFINITE ? "∞" : String.valueOf(stateTimer.tickCount())
        );
        gameRenderer.ctx().fillText(stateText, 0, scaled(3 * TS));
    }
}