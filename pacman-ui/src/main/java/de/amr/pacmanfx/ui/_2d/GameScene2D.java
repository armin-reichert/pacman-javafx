/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.ui.action.DefaultActionBindingsManager;
import de.amr.pacmanfx.ui.api.ActionBindingsManager;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.rendering.HUDRenderer;
import javafx.beans.property.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.tinylog.Logger;

import static de.amr.pacmanfx.ui._2d.GameScene2DRenderer.configureRendererForGameScene;
import static java.util.Objects.requireNonNull;

/**
 * Base class of all 2D scenes.
 */
public abstract class GameScene2D implements GameScene {

    public static final Vector2i DEFAULT_SIZE_PX = Globals.ARCADE_MAP_SIZE_IN_PIXELS;

    protected final BooleanProperty debugInfoVisible = new SimpleBooleanProperty(false);
    protected final DoubleProperty scaling = new SimpleDoubleProperty(1.0f);
    protected final ObjectProperty<Paint> background = new SimpleObjectProperty<>(Color.BLACK);

    protected final GameUI ui;
    protected final ActionBindingsManager actionBindings;
    protected final AnimationRegistry animationRegistry;

    protected Canvas canvas;
    protected BaseDebugInfoRenderer debugInfoRenderer;

    protected GameScene2D(GameUI ui) {
        this.ui = requireNonNull(ui);
        actionBindings = new DefaultActionBindingsManager();
        animationRegistry = new AnimationRegistry();
    }

    @Override
    public GameUI ui() {
        return ui;
    }

    @Override
    public GameContext context() {
        return ui.gameContext();
    }

    @Override
    public final void init() {
        doInit();
        actionBindings.assignBindingsToKeyboard(ui.keyboard());
    }

    @Override
    public final void end() {
        doEnd();
        ui.soundManager().stopAll();
        Logger.info("{} ends", getClass().getSimpleName());
    }

    @Override
    public void handleKeyboardInput() {
        actionBindings.matchingAction(ui.keyboard()).ifPresent(gameAction -> gameAction.executeIfEnabled(ui));
    }

    protected abstract void doInit();

    protected abstract void doEnd();

    protected abstract HUDRenderer hudRenderer();

    public void setCanvas(Canvas canvas) {
        this.canvas = requireNonNull(canvas);
        createRenderers(canvas);
    }

    public Canvas canvas() {
        return canvas;
    }

    protected void createRenderers(Canvas canvas) {
        debugInfoRenderer = configureRendererForGameScene(
            new BaseDebugInfoRenderer(this, canvas, ui.currentConfig().spriteSheet()), this);
    }

    @Override
    public ActionBindingsManager actionBindings() { return actionBindings; }

    @Override
    public void onStopAllSounds(GameEvent event) { ui.soundManager().stopAll(); }

    @Override
    public void onUnspecifiedChange(GameEvent event) {
        // TODO: remove (this is only used by game state GameState.TESTING_CUT_SCENES)
        ui.updateGameScene(true);
    }

    public ObjectProperty<Paint> backgroundProperty() {
        return background;
    }

    public Paint background() {
        return background.get();
    }

    public void setBackground(Paint paint) {
        background.set(paint);
    }

    public DoubleProperty scalingProperty() {
        return scaling;
    }

    public void setScaling(double value) {
        scaling.set(value);
    }

    public double scaling() { return
        scaling.get();
    }

    public double scaled(double value) {
        return value * scaling();
    }

    public BooleanProperty debugInfoVisibleProperty() {
        return debugInfoVisible;
    }

    public boolean debugInfoVisible() {
        return debugInfoVisible.get();
    }

    /**
     * @return (unscaled) scene size in pixels e.g. 224x288 for Arcade scenes
     */
    public Vector2i sizeInPx() {
        return DEFAULT_SIZE_PX;
    }

    /**
     * Draws this scene into the canvas.
     * <p>
     * Default implementation scales the renderer to the current scene scaling,
     * clears the canvas and draws the scores (if on), scene content and debug information (if on).
     * </p>
     */
    public void draw() {
        canvas.getGraphicsContext2D().setFill(background());
        canvas.getGraphicsContext2D().fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawSceneContent();
        if (debugInfoVisible()) {
            debugInfoRenderer.drawDebugInfo();
        }
        drawHUD();
    }

    protected void drawHUD() {
        if (hudRenderer() != null) {
            hudRenderer().drawHUD(context().game(), context().game().hud(), sizeInPx());
        }
    }

    /**
     * Draws the scene content using the already scaled game renderer.
     */
    public abstract void drawSceneContent();
}