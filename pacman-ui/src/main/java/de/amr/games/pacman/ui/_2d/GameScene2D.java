/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.ui.GameAction;
import de.amr.games.pacman.ui.GameScene;
import javafx.beans.property.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.ui.Globals.THE_UI;

/**
 * Base class of all 2D scenes.
 *
 * @author Armin Reichert
 */
public abstract class GameScene2D implements GameScene {

    protected final ObjectProperty<Color> backgroundColorPy = new SimpleObjectProperty<>(Color.BLACK);
    protected final BooleanProperty debugInfoVisiblePy = new SimpleBooleanProperty(false);
    protected final FloatProperty scalingPy = new SimpleFloatProperty(1.0f);

    protected final Map<KeyCodeCombination, GameAction> actionBindings = new HashMap<>();

    protected GameRenderer gr;
    protected Canvas canvas;

    @Override
    public final void init() {
        doInit();
        bindGameActions();
        enableActionBindings();
    }

    @Override
    public final void end() {
        doEnd();
        disableActionBindings();
    }

    @Override
    public Map<KeyCodeCombination, GameAction> actionBindings() {
        return actionBindings;
    }

    public FloatProperty scalingProperty() { return scalingPy; }
    public void setScaling(double scaling) { scalingPy.set((float) scaling); }
    public float scaling() { return scalingPy.get(); }
    public float scaled(double value) {
        return (float) value * scaling();
    }

    public ObjectProperty<Color> backgroundColorProperty() { return backgroundColorPy; }
    public void setBackgroundColor(Color color) { backgroundColorPy.set(color); }
    public Color backgroundColor() { return backgroundColorPy.get(); }

    public BooleanProperty debugInfoVisibleProperty() { return debugInfoVisiblePy; }

    protected void doInit() {}

    protected void doEnd() {}

    public void setGameRenderer(GameRenderer renderer) {
        gr = assertNotNull(renderer);
    }

    public GameRenderer renderer() {
        return gr;
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public Canvas canvas() { return canvas; }

    protected abstract void drawSceneContent();

    protected void drawDebugInfo() {
        gr.drawTileGrid(sizeInPx().x(), sizeInPx().y());
    }

    public void draw() {
        gr.setScaling(scaling());
        gr.clearCanvas(backgroundColor());
        if (THE_UI.isScoreVisible()) {
            gr.drawScores(Color.web(Arcade.Palette.WHITE), gr.scaledArcadeFont(TS));
        }
        drawSceneContent();
        if (debugInfoVisiblePy.get()) {
            drawDebugInfo();
        }
    }

    /**
     * @param defaultSize size returned if level is not existing yet
     * @return level size in tiles
     */
    protected Vector2i levelSizeInTilesOrElse(Vector2i defaultSize) {
        return game().level().map(level -> vec_2i(level.worldMap().numCols(), level.worldMap().numRows())).orElse(defaultSize);
    }
}