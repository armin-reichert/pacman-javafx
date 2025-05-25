/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
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

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.ui.PacManGamesEnv.*;
import static java.util.Objects.requireNonNull;

/**
 * Base class of all 2D scenes.
 */
public abstract class GameScene2D implements GameScene, GameActionBindingManager {

    protected final Map<KeyCodeCombination, GameAction> actionBindings = new HashMap<>();

    protected final ObjectProperty<Font>  arcadeFontScaledTS = new SimpleObjectProperty<>();
    protected final ObjectProperty<Color> backgroundColorPy = new SimpleObjectProperty<>(Color.BLACK);
    protected final BooleanProperty       debugInfoVisiblePy = new SimpleBooleanProperty(false);
    protected final FloatProperty         scalingPy = new SimpleFloatProperty(1.0f);

    private GameRenderer gameRenderer;
    private Canvas canvas;

    @Override
    public final void init() {
        arcadeFontScaledTS.bind(scalingPy.map(scaling -> theAssets().arcadeFontAtSize((float) scaling * TS)));
        doInit();
        updateActionBindings();
        theKeyboard().logCurrentBindings();
    }

    @Override
    public final void end() {
        doEnd();
        deleteActionBindings();
        theSound().stopAll();
    }

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

    public FloatProperty scalingProperty() { return scalingPy; }
    public void setScaling(double scaling) { scalingPy.set((float) scaling); }
    public float scaling() { return scalingPy.get(); }
    public float scaled(double value) {
        return (float) value * scaling();
    }

    public Font arcadeFontScaledTS() { return arcadeFontScaledTS.get(); }

    public Color backgroundColor() { return backgroundColorPy.get(); }
    public void setBackgroundColor(Color color) { backgroundColorPy.set(color); }
    public ObjectProperty<Color> backgroundColorProperty() { return backgroundColorPy; }

    protected void doInit() {}
    protected void doEnd() {}

    public GameRenderer gr() { return gameRenderer; }
    public void setGameRenderer(GameRenderer renderer) {
        gameRenderer = requireNonNull(renderer);
    }

    public Canvas canvas() { return canvas; }
    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    /**
     * @param defaultSize size in tiles (sizeX, sizeY) = (numCols, numRows) if level is not existing
     * @return level size in tiles (sizeX, sizeY) = (numCols, numRows)
     */
    public final Vector2i levelSizeInTilesOrElse(Vector2i defaultSize) {
        return optGameLevel()
            .map(GameLevel::worldMap)
            .map(worldMap -> Vector2i.of(worldMap.numCols(), worldMap.numRows()))
            .orElse(defaultSize);
    }

    public void draw() {
        gameRenderer.fillCanvas(backgroundColor());
        gameRenderer.setScaling(scaling());
        drawSceneContent();
        if (debugInfoVisiblePy.get()) {
            drawDebugInfo();
        }
    }

    /**
     * Draws the scene content using the already scaled game renderer.
     */
    protected abstract void drawSceneContent();

    public BooleanProperty debugInfoVisibleProperty() { return debugInfoVisiblePy; }

    protected void drawDebugInfo() {
        gameRenderer.drawTileGrid(sizeInPx().x(), sizeInPx().y(), Color.LIGHTGRAY);
        gameRenderer.ctx().setFill(Color.YELLOW);
        gameRenderer.ctx().setFont(DEBUG_TEXT_FONT);
        gameRenderer.ctx().fillText("%s %d".formatted(theGameState(), theGameState().timer().tickCount()), 0, scaled(3 * TS));
    }
}