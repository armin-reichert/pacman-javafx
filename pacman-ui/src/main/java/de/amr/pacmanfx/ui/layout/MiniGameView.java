/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui._2d.GameRenderer;
import de.amr.pacmanfx.ui.api.GameUI;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.ui.api.GameUI_Config.SCENE_ID_PLAY_SCENE_3D;
import static de.amr.pacmanfx.ui.api.GameUI_Properties.*;
import static de.amr.pacmanfx.uilib.rendering.BaseRenderer.fillCanvas;
import static java.util.Objects.requireNonNull;

public class MiniGameView extends VBox {

    public static final Vector2f ARCADE_SIZE = Vector2f.of(28, 36);
    public static final Duration SLIDE_IN_DURATION = Duration.seconds(1);
    public static final Duration SLIDE_OUT_DURATION = Duration.seconds(2);

    private final FloatProperty aspectProperty                  = new SimpleFloatProperty(ARCADE_SIZE.x() / ARCADE_SIZE.y());
    private final ObjectProperty<Color> backgroundColorProperty = new SimpleObjectProperty<>(Color.BLACK);
    private final BooleanProperty debugProperty                 = new SimpleBooleanProperty(false);
    private final DoubleProperty canvasHeightProperty           = new SimpleDoubleProperty(400);
    private final FloatProperty scalingProperty                 = new SimpleFloatProperty(1.0f);
    private final ObjectProperty<Vector2f> worldSizeProperty    = new SimpleObjectProperty<>(ARCADE_SIZE.scaled(TS));

    private final Canvas canvas;
    private final HBox canvasContainer;

    private GameUI ui;
    private GameRenderer gameRenderer;
    private GameLevel gameLevel;
    private long drawCallCount;

    private final TranslateTransition slideInAnimation;
    private final TranslateTransition slideOutAnimation;

    public MiniGameView() {
        canvas = new Canvas();
        canvas.heightProperty().bind(canvasHeightProperty());
        canvas.widthProperty().bind(Bindings.createDoubleBinding(
            () -> aspectProperty.get() * canvas.getHeight(),
            aspectProperty, canvas.heightProperty()
        ));

        // The VBox fills the complete parent container height (why?), so we put the canvas
        // into an HBox that does not grow in height and provides some padding around the canvas
        canvasContainer = new HBox(canvas);
        canvasContainer.setPadding(new Insets(0, 10, 0, 10));
        canvasContainer.backgroundProperty().bind(backgroundColorProperty().map(Background::fill));
        VBox.setVgrow(canvasContainer, Priority.NEVER);
        getChildren().add(canvasContainer);

        scalingProperty.bind(Bindings.createFloatBinding(
            () -> (float) canvas.getHeight() / worldSizeProperty.get().y(),
            canvas.heightProperty(), worldSizeProperty
        ));
        aspectProperty.bind(worldSizeProperty.map(size -> size.x() / size.y()));

        slideInAnimation = new TranslateTransition(SLIDE_IN_DURATION, this);
        slideInAnimation.setToY(0);
        slideInAnimation.setByY(10);
        slideInAnimation.setDelay(Duration.seconds(1));
        slideInAnimation.setInterpolator(Interpolator.EASE_OUT);

        slideOutAnimation = new TranslateTransition(SLIDE_OUT_DURATION, this);
        slideOutAnimation.setToY(-canvasContainer.getHeight());
        slideOutAnimation.setByY(10);
        slideOutAnimation.setDelay(Duration.seconds(2));
        slideOutAnimation.setInterpolator(Interpolator.EASE_IN);
    }

    public ObjectProperty<Color> backgroundColorProperty() {
        return backgroundColorProperty;
    }

    public BooleanProperty debugProperty() {
        return debugProperty;
    }

    public DoubleProperty canvasHeightProperty() {
        return canvasHeightProperty;
    }

    public void setGameUI(GameUI ui) {
        this.ui = requireNonNull(ui);
        backgroundColorProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR);
        debugProperty().bind(PROPERTY_DEBUG_INFO_VISIBLE);
        canvasHeightProperty().bind(PROPERTY_MINI_VIEW_HEIGHT);
        opacityProperty().bind(PROPERTY_MINI_VIEW_OPACITY_PERCENT.divide(100.0));
        visibleProperty().bind(Bindings.createObjectBinding(
            () -> PROPERTY_MINI_VIEW_ON.get() && ui.isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_3D),
            PROPERTY_MINI_VIEW_ON, PROPERTY_CURRENT_GAME_SCENE
        ));
    }

    public void setGameLevel(GameLevel gameLevel) {
        /* TODO: The game renderer cannot yet be created in setGameUI because at the time, setGameUI is called, the
                 game controller has not yet selected a game variant and therefore the current UI config is null! */
        gameRenderer = ui.currentConfig().createGameRenderer(canvas);
        gameRenderer.setScaling(scalingProperty.floatValue());
        this.gameLevel = requireNonNull(gameLevel);
        worldSizeProperty.set(gameLevel.worldSizePx());
        gameRenderer.applyRenderingHints(gameLevel);
    }

    public void slideIn() {
        slideInAnimation.play();
    }

    public void slideOut() {
        slideOutAnimation.setToY(-canvasContainer.getHeight());
        slideOutAnimation.play();
    }

    public void draw() {
        drawCallCount += 1;

        if (!isVisible() || gameRenderer == null) {
            return;
        }

        float scaling = scalingProperty.get();
        gameRenderer.setScaling(scaling);

        fillCanvas(canvas, backgroundColorProperty.get());

        if (gameLevel != null) {
            gameRenderer.drawLevel(ui.gameContext(),
                gameLevel,
                backgroundColorProperty().get(),
                false,
                gameLevel.blinking().isOn(),
                ui.clock().tickCount());
            gameLevel.bonus().ifPresent(gameRenderer::drawActor);
            gameRenderer.drawActor(gameLevel.pac());
            Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW)
                .map(gameLevel::ghost)
                .forEach(gameRenderer::drawActor);
        }

        if (debugProperty().get()) {
            gameRenderer.ctx().save();
            gameRenderer.ctx().setTextAlign(TextAlignment.CENTER);
            gameRenderer.ctx().setFill(Color.WHITE);
            gameRenderer.ctx().setFont(Font.font(14 * scaling));
            gameRenderer.ctx().fillText("scaling: %.2f, draw calls: %d".formatted(scaling, drawCallCount), canvas.getWidth() * 0.5, 16 * scaling);
            gameRenderer.ctx().restore();
        }
    }
}