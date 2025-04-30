/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.ui.GameScene;
import de.amr.games.pacman.uilib.Action;
import javafx.beans.property.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.HashMap;
import java.util.Map;

import static de.amr.games.pacman.Globals.TS;
import static de.amr.games.pacman.ui.Globals.*;
import static java.util.Objects.requireNonNull;

/**
 * Base class of all 2D scenes.
 *
 * @author Armin Reichert
 */
public abstract class GameScene2D implements GameScene {

    protected final ObjectProperty<Color> backgroundColorPy = new SimpleObjectProperty<>(Color.BLACK);
    protected final BooleanProperty debugInfoVisiblePy = new SimpleBooleanProperty(false);
    protected final FloatProperty scalingPy = new SimpleFloatProperty(1.0f);
    protected final ObjectProperty<Font> arcadeFontScaledTS = new SimpleObjectProperty<>();

    protected final Map<KeyCodeCombination, Action> actionBindings = new HashMap<>();

    protected GameRenderer gr;
    protected Canvas canvas;

    @Override
    public final void init() {
        arcadeFontScaledTS.bind(scalingPy.map(scaling -> THE_ASSETS.arcadeFontAtSize((float) scaling * TS)));
        doInit();
        bindActions();
        enableActionBindings(THE_KEYBOARD);
        THE_KEYBOARD.logCurrentBindings();
    }

    @Override
    public final void end() {
        doEnd();
        disableActionBindings(THE_KEYBOARD);
    }

    @Override
    public Map<KeyCodeCombination, Action> actionBindings() {
        return actionBindings;
    }

    @Override
    public void bindActions() {}

    public FloatProperty scalingProperty() { return scalingPy; }
    public void setScaling(double scaling) { scalingPy.set((float) scaling); }
    public float scaling() { return scalingPy.get(); }
    public float scaled(double value) {
        return (float) value * scaling();
    }

    /**
     * @return Arcade font at scaled tile size.
     */
    public Font arcadeFontScaledTS() { return arcadeFontScaledTS.get(); }
    public ObjectProperty<Color> backgroundColorProperty() { return backgroundColorPy; }
    public void setBackgroundColor(Color color) { backgroundColorPy.set(color); }
    public Color backgroundColor() { return backgroundColorPy.get(); }

    public BooleanProperty debugInfoVisibleProperty() { return debugInfoVisiblePy; }

    protected void doInit() {}

    protected void doEnd() {}

    public void setGameRenderer(GameRenderer renderer) {
        gr = requireNonNull(renderer);
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public Canvas canvas() { return canvas; }

    /**
     * @param defaultSize size in tiles (sizeX, sizeY) = (numCols, numRows) if level is not existing
     * @return level size in tiles (sizeX, sizeY) = (numCols, numRows)
     */
    public final Vector2i levelSizeInTilesOrElse(Vector2i defaultSize) {
        return game().level()
            .map(GameLevel::worldMap)
            .map(worldMap -> Vector2i.of(worldMap.numCols(), worldMap.numRows()))
            .orElse(defaultSize);
    }

    public void draw() {
        drawSceneContent();
        if (debugInfoVisiblePy.get()) {
            drawDebugInfo();
        }
    }

    protected abstract void drawSceneContent();

    protected void drawDebugInfo() {
        gr.drawTileGrid(sizeInPx().x(), sizeInPx().y(), Color.LIGHTGRAY);
        gr.ctx().setFill(Color.YELLOW);
        gr.ctx().setFont(DEBUG_TEXT_FONT);
        gr.ctx().fillText("%s %d".formatted(gameState(), gameState().timer().tickCount()), 0, scaled(3 * TS));
    }
}