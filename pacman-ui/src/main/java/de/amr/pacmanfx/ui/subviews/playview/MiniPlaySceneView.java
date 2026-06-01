/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.subviews.playview;

import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.GameUI_Constants;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.uilib.rendering.*;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.Map;
import java.util.stream.Stream;

import static de.amr.pacmanfx.core.Globals.*;
import static java.util.Objects.requireNonNull;

public class MiniPlaySceneView {

    public static final Insets PADDING = new Insets(0, 10, 0, 10);

    public static final Duration SLIDE_IN_DURATION  = Duration.seconds(1);
    public static final Duration SLIDE_OUT_DURATION = Duration.seconds(2);

    private final DoubleProperty scaling = new SimpleDoubleProperty(1.0);
    private final ObjectProperty<Vector2i> worldSize = new SimpleObjectProperty<>(ARCADE_MAP_SIZE_IN_PIXELS);

    private final HBox rootPane;
    private final Canvas canvas;

    private AppContext context;

    // Note: The level and actor renderers cannot be created in the constructor, because the game controller has not yet
    //       selected a game variant when the constructor is called, so no UI configuration is available!
    private BaseRenderer canvasRenderer;
    private GameLevelRenderer levelRenderer;
    private ActorRenderer actorRenderer;

    private TranslateTransition slideInAnimation;
    private TranslateTransition slideOutAnimation;

    // Used in debug draw mode
    private long drawCallCount;

    public MiniPlaySceneView() {
        canvas = new Canvas();
        canvas.heightProperty().bind(GameUI_Constants.PROPERTY_MINI_VIEW_HEIGHT);
        canvas.widthProperty().bind(Bindings.createDoubleBinding(
            () -> {
                final double aspect = (double) worldSize.get().x() / worldSize.get().y();
                return aspect * canvas.getHeight();
            },
            worldSize, canvas.heightProperty()
        ));

        rootPane = new HBox(canvas);
        rootPane.setBorder(Border.stroke(Color.grayRgb(66)));
        rootPane.setPadding(PADDING);

        rootPane.backgroundProperty().bind(GameUI_Constants.PROPERTY_CANVAS_BACKGROUND_COLOR.map(Background::fill));
        rootPane.opacityProperty().bind(GameUI_Constants.PROPERTY_MINI_VIEW_OPACITY_PERCENT.divide(100.0));

        // Canvas size determines mini view size
        rootPane.maxWidthProperty().bind(canvas.widthProperty().add(PADDING.getLeft() + PADDING.getRight()));
        rootPane.maxHeightProperty().bind(canvas.heightProperty());

        scaling.bind(Bindings.createDoubleBinding(
            () -> canvas.getHeight() / worldSize.get().y(),
            canvas.heightProperty(), worldSize
        ));
    }

    public Pane rootPane() {
        return rootPane;
    }

    public void setUI(AppContext ui) {
        this.context = requireNonNull(ui);
    }

    public void setWorldSizeInPixel(Vector2i size) {
        worldSize.set(size);
    }

    public void setUIConfig(UIConfig uiConfig) {
        canvasRenderer = new BaseRenderer(canvas);

        levelRenderer = uiConfig.createGameLevelRenderer(canvas);
        levelRenderer.scalingProperty().bind(scaling);
        levelRenderer.backgroundColorProperty().bind(GameUI_Constants.PROPERTY_CANVAS_BACKGROUND_COLOR);

        actorRenderer = uiConfig.createActorRenderer(canvas);
        actorRenderer.scalingProperty().bind(scaling);
        actorRenderer.backgroundColorProperty().bind(GameUI_Constants.PROPERTY_CANVAS_BACKGROUND_COLOR);
    }

    public void slideIn() {
        if (slideInAnimation != null) {
            slideInAnimation.stop();
        }
        slideInAnimation = new TranslateTransition(SLIDE_IN_DURATION, rootPane);
        slideInAnimation.setToY(0);
        slideInAnimation.setByY(10);
        slideInAnimation.setDelay(Duration.seconds(1));
        slideInAnimation.setInterpolator(Interpolator.EASE_OUT);
        slideInAnimation.play();
    }

    public void slideOut() {
        if (slideOutAnimation != null) {
            slideOutAnimation.stop();
        }
        slideOutAnimation = new TranslateTransition(SLIDE_OUT_DURATION, rootPane);
        slideOutAnimation.setToY(-rootPane.getHeight());
        slideOutAnimation.setByY(10);
        slideOutAnimation.setDelay(Duration.seconds(2));
        slideOutAnimation.setInterpolator(Interpolator.EASE_IN);
        slideOutAnimation.play();
    }

    public boolean isMoving() {
        return slideInAnimation != null && slideInAnimation.getStatus() == Animation.Status.RUNNING
            || slideOutAnimation != null && slideOutAnimation.getStatus() == Animation.Status.RUNNING;
    }

    public void draw() {
        if (canvasRenderer != null) {
            drawCanvas();
        }
    }
    
    private void drawCanvas() {
        canvasRenderer.clearCanvas();

        if (levelRenderer != null && actorRenderer != null) {
            context.currentGame().optGameLevel().ifPresent(this::drawGameLevel);
        }

        if (GameUI_Constants.PROPERTY_DEBUG_INFO_VISIBLE.get()) {
            canvasRenderer.fillTextCentered(
                "scaling: %.2f, draw calls: %d".formatted(scaling.doubleValue(), drawCallCount),
                Color.WHITE, Font.font(12 * scaling.get()),
                0.5 * canvas.getWidth(), scaling.doubleValue() * 16
            );
        }

        drawCallCount += 1;
    }

    private void drawGameLevel(GameLevel level) {
        final var info = new RenderInfo();
        info.putAll(Map.of(
            CommonRenderInfoKey.ENERGIZER_VISIBLE, level.blinking().state() == Pulse.State.ON,
            CommonRenderInfoKey.MAP_BRIGHT, false,
            CommonRenderInfoKey.MAP_EMPTY, level.worldMap().foodLayer().remainingFoodCount() == 0,
            CommonRenderInfoKey.MAP_FLASHING, false,
            CommonRenderInfoKey.TICK, context.ui().gameClock().tickCount()
        ));
        levelRenderer.applyLevelSettings(level, info);
        levelRenderer.drawLevel(level, info);

        level.optBonus().ifPresent(bonus -> actorRenderer.drawActor(bonus));
        actorRenderer.drawActor(level.entities().pac());
        Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW).map(level::ghost)
            .forEach(ghost -> actorRenderer.drawActor(ghost));
    }
}