/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.ui.GameAction;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import javafx.beans.property.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.HashMap;
import java.util.Map;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.ui.PacManGames.*;
import static de.amr.pacmanfx.ui.PacManGames_UI.DEBUG_TEXT_FILL;
import static de.amr.pacmanfx.ui.PacManGames_UI.DEBUG_TEXT_FONT;
import static java.util.Objects.requireNonNull;

/**
 * Base class of all 2D scenes.
 */
public abstract class GameScene2D implements GameScene {
    protected final ObjectProperty<Font> arcadeFont8Property = new SimpleObjectProperty<>();
    protected final ObjectProperty<Font> arcadeFont6Property = new SimpleObjectProperty<>();
    protected final ObjectProperty<Color> backgroundColorProperty = new SimpleObjectProperty<>(Color.BLACK);
    protected final BooleanProperty debugInfoVisibleProperty = new SimpleBooleanProperty(false);
    protected final FloatProperty scalingProperty = new SimpleFloatProperty(1.0f);

    protected final Map<KeyCombination, GameAction> actionBindings = new HashMap<>();
    protected GameRenderer gameRenderer;
    protected Canvas canvas;

    protected AnimationManager animationManager = new AnimationManager();

    @Override
    public final void init() {
        arcadeFont8Property.bind(scalingProperty.map(s -> theAssets().arcadeFont(s.floatValue() * 8)));
        arcadeFont6Property.bind(scalingProperty.map(s -> theAssets().arcadeFont(s.floatValue() * 6)));
        doInit();
        updateActionBindings();
        theKeyboard().logCurrentBindings();
    }

    @Override
    public final void end() {
        doEnd();
        clearActionBindings();
        theSound().stopAll();
    }

    protected abstract void doInit();
    protected abstract void doEnd();

    @Override
    public Map<KeyCombination, GameAction> actionBindings() { return actionBindings; }

    @Override
    public void onStopAllSounds(GameEvent event) { theSound().stopAll(); }

    @Override
    public void onUnspecifiedChange(GameEvent event) {
        // TODO: remove (this is only used by game state GameState.TESTING_CUT_SCENES)
        theUI().updateGameScene(true);
    }

    @Override
    public Keyboard keyboard() { return theKeyboard(); }

    public void  setScaling(double scaling) { scalingProperty.set((float) scaling); }
    public float scaling() { return scalingProperty.get(); }
    public float scaled(double value) { return (float) value * scaling(); }

    public Font scaledArcadeFont8() { return arcadeFont8Property.get(); }
    public Font scaledArcadeFont6() { return arcadeFont6Property.get(); }

    public Color backgroundColor() { return backgroundColorProperty.get(); }
    public void setBackgroundColor(Color color) { backgroundColorProperty.set(color); }

    public GameRenderer gr() { return gameRenderer; }
    public void setGameRenderer(SpriteGameRenderer renderer) { gameRenderer = requireNonNull(renderer); }
    public GraphicsContext ctx() { return gameRenderer.ctx(); }

    public Canvas canvas() { return canvas; }
    public void setCanvas(Canvas canvas) { this.canvas = canvas; }

    public ObjectProperty<Color> backgroundColorProperty() { return backgroundColorProperty; }
    public BooleanProperty debugInfoVisibleProperty() { return debugInfoVisibleProperty; }
    public FloatProperty scalingProperty() { return scalingProperty; }

    /**
     * @return (unscaled) scene size in pixels e.g. 224x288
     */
    public abstract Vector2f sizeInPx();

    /**
     * Default implementation: scales the renderer to the current scene scaling,
     * clears the canvas and draws the scores (if on), scene content and debug information (if on).
     */
    public void draw() {
        gameRenderer.fillCanvas(backgroundColor());
        gameRenderer.setScaling(scaling());
        drawSceneContent();
        if (debugInfoVisibleProperty.get()) {
            drawDebugInfo();
        }
        gameRenderer.drawHUD(theGame().hud());
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
        ctx().setFill(DEBUG_TEXT_FILL);
        ctx().setFont(DEBUG_TEXT_FONT);
        TickTimer stateTimer = theGameState().timer();
        String stateText = "Game State: '%s' (Tick %d of %s)".formatted(
            theGameState(),
            stateTimer.tickCount(),
            stateTimer.durationTicks() == TickTimer.INDEFINITE ? "∞" : String.valueOf(stateTimer.tickCount())
            );
        ctx().fillText(stateText, 0, scaled(3 * TS));
    }
}