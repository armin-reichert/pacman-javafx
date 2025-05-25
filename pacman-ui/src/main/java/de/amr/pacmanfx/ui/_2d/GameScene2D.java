/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.ui.GameActionBindingManager;
import de.amr.pacmanfx.uilib.GameAction;
import de.amr.pacmanfx.uilib.GameScene;
import de.amr.pacmanfx.uilib.input.Keyboard;
import javafx.beans.property.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.HashMap;
import java.util.Map;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.Globals.theGameState;
import static de.amr.pacmanfx.ui.PacManGamesEnv.*;
import static java.util.Objects.requireNonNull;

/**
 * Base class of all 2D scenes.
 */
public abstract class GameScene2D implements GameScene, GameActionBindingManager {

    protected final Map<KeyCodeCombination, GameAction> actionBindings = new HashMap<>();

    // The Arcade font at size scaled(tile_size), adapts to current scaling
    private final ObjectProperty<Font>  arcadeFontOneTileScaled = new SimpleObjectProperty<>();
    private final ObjectProperty<Color> backgroundColorPy = new SimpleObjectProperty<>(Color.BLACK);
    private final BooleanProperty       debugInfoVisiblePy = new SimpleBooleanProperty(false);
    private final FloatProperty         scalingPy = new SimpleFloatProperty(1.0f);

    private GameRenderer gameRenderer;
    private Canvas canvas;

    protected GameScene2D() {
        arcadeFontOneTileScaled.bind(scalingPy.map(s -> theAssets().arcadeFontAtSize(s.floatValue() * TS)));
    }

    @Override
    public final void init() {
        doInit();
        updateActionBindings();
        theKeyboard().logCurrentBindings();
    }

    protected void doInit() {}

    @Override
    public final void end() {
        doEnd();
        deleteActionBindings();
        theSound().stopAll();
    }

    protected void doEnd() {}

    @Override
    public Map<KeyCodeCombination, GameAction> actionBindings() {
        return actionBindings;
    }

    @Override
    public void onStopAllSounds(GameEvent event) {
        theSound().stopAll();
    }

    @Override
    public void onUnspecifiedChange(GameEvent event) {
        // TODO: remove (this is only used by game state GameState.TESTING_CUT_SCENES)
        theUI().updateGameScene(true);
    }

    @Override
    public Keyboard keyboard() { return theKeyboard(); }

    public void setScaling(double scaling) { scalingPy.set((float) scaling); }
    public float scaling() { return scalingPy.get(); }
    public float scaled(double value) {
        return (float) value * scaling();
    }

    public Font defaultSceneFont() { return arcadeFontOneTileScaled.get(); }

    public Color backgroundColor() { return backgroundColorPy.get(); }
    public void setBackgroundColor(Color color) { backgroundColorPy.set(color); }

    public GameRenderer gr() { return gameRenderer; }
    public void setGameRenderer(GameRenderer renderer) {
        gameRenderer = requireNonNull(renderer);
    }

    public Canvas canvas() { return canvas; }
    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public ObjectProperty<Color> backgroundColorProperty() { return backgroundColorPy; }
    public BooleanProperty debugInfoVisibleProperty() { return debugInfoVisiblePy; }
    public FloatProperty scalingProperty() { return scalingPy; }

    /**
     * Default implementation: scales the renderer to the current scene scaling,
     * clears the canvas and draws the scene content and optionally the debug information.
     */
    public void draw() {
        gameRenderer.fillCanvas(backgroundColor());
        gameRenderer.setScaling(scaling());
        drawSceneContent();
        if (debugInfoVisiblePy.get()) {
            drawDebugInfo();
        }
    }

    /**
     * Default implementation: Draws a grid indicating the tiles, the game state and the state timer.
     */
    protected void drawDebugInfo() {
        gameRenderer.drawTileGrid(sizeInPx().x(), sizeInPx().y(), Color.LIGHTGRAY);
        gameRenderer.ctx().setFill(Color.YELLOW);
        gameRenderer.ctx().setFont(DEBUG_TEXT_FONT);
        gameRenderer.ctx().fillText("%s %d".formatted(theGameState(), theGameState().timer().tickCount()), 0, scaled(3 * TS));
    }

    /**
     * Draws the scene content using the already scaled game renderer.
     */
    protected abstract void drawSceneContent();

    protected Color scoreColor() {
        return theAssets().color(theUIConfig().current().assetNamespace() + ".color.score");
    }
}