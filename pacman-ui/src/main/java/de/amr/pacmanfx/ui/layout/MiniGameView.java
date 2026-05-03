/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.layout;

import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.GameUI;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.ui.GameUI.*;
import static java.util.Objects.requireNonNull;

public class MiniGameView {

    public static final Duration SLIDE_IN_DURATION  = Duration.seconds(1);
    public static final Duration SLIDE_OUT_DURATION = Duration.seconds(2);

    private final DoubleProperty scaling = new SimpleDoubleProperty(1.0);
    private final ObjectProperty<Vector2i> worldSize = new SimpleObjectProperty<>(ARCADE_MAP_SIZE_IN_PIXELS);

    private final Canvas canvas = new Canvas();
    private final VBox container = new VBox();
    private final HBox contentPane;

    private GameUI ui;

    private final BaseRenderer canvasRenderer;

    // Note: The renderers cannot be created in the constructor, because the game controller has not yet
    //       selected a game variant when the constructor is called, so no UI configuration is available!
    private GameLevelRenderer levelRenderer;
    private ActorRenderer actorRenderer;

    private TranslateTransition slideInAnimation;
    private TranslateTransition slideOutAnimation;

    // Used in debug draw mode
    private long drawCallCount;

    public MiniGameView() {
        canvasRenderer = new BaseRenderer(canvas);

        // The container fills the complete parent container height (why?), so we put the canvas
        // into an HBox that does not grow in height and provides some padding around the canvas.
        contentPane = new HBox(canvas);
        contentPane.backgroundProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR.map(Background::fill));
        contentPane.setPadding(new Insets(0, 10, 0, 10));
        VBox.setVgrow(contentPane, Priority.NEVER);

        container.getChildren().add(contentPane);

        container.opacityProperty().bind(PROPERTY_MINI_VIEW_OPACITY_PERCENT.divide(100.0));

        scaling.bind(Bindings.createDoubleBinding(
            () -> canvas.getHeight() / worldSize.get().y(),
            canvas.heightProperty(), worldSize
        ));
    }

    public Pane container() {
        return container;
    }

    public void setUI(GameUI ui) {
        this.ui = requireNonNull(ui);
        bindCanvasSize();
    }

    public void setGameLevel(GameLevel level) {
        worldSize.set(level.worldMap().terrainLayer().sizeInPixel());

        levelRenderer = ui.currentConfig().createGameLevelRenderer(canvas);
        levelRenderer.scalingProperty().bind(scaling);
        levelRenderer.backgroundColorProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR);

        actorRenderer = ui.currentConfig().createActorRenderer(canvas);
        actorRenderer.scalingProperty().bind(scaling);
        actorRenderer.backgroundColorProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR);
    }

    public void slideIn() {
        if (slideInAnimation != null) {
            slideInAnimation.stop();
        }
        slideInAnimation = new TranslateTransition(SLIDE_IN_DURATION, container);
        slideInAnimation.setToY(0);
        slideInAnimation.setByY(10);
        slideInAnimation.setDelay(Duration.seconds(1));
        slideInAnimation.setInterpolator(Interpolator.EASE_OUT);
        slideInAnimation.setOnFinished(_ -> bindCanvasSize());
        unbindCanvasSize();
        slideInAnimation.play();
    }

    public void slideOut() {
        if (slideOutAnimation != null) {
            slideOutAnimation.stop();
        }
        slideOutAnimation = new TranslateTransition(SLIDE_OUT_DURATION, container);
        slideOutAnimation.setToY(-contentPane.getHeight());
        slideOutAnimation.setByY(10);
        slideOutAnimation.setDelay(Duration.seconds(2));
        slideOutAnimation.setInterpolator(Interpolator.EASE_IN);
        slideOutAnimation.setOnFinished(_ -> bindCanvasSize());
        unbindCanvasSize();
        slideOutAnimation.play();
    }

    public boolean isMoving() {
        return slideInAnimation != null && slideInAnimation.getStatus() == Animation.Status.RUNNING
            || slideOutAnimation != null && slideOutAnimation.getStatus() == Animation.Status.RUNNING;
    }

    public void draw() {
        drawCallCount += 1;

        if (!container.isVisible() || levelRenderer == null) {
            return;
        }
        canvasRenderer.clearCanvas();

        final Game game = ui.gameContext().game();
        final Optional<GameLevel> optGameLevel = game.optGameLevel();
        if (optGameLevel.isPresent()) {
            final GameLevel level = optGameLevel.get();
            final var info = new RenderInfo();
            info.putAll(Map.of(
                CommonRenderInfoKey.ENERGIZER_VISIBLE, level.blinking().state() == Pulse.State.ON,
                CommonRenderInfoKey.MAP_BRIGHT, false,
                CommonRenderInfoKey.MAP_EMPTY, level.worldMap().foodLayer().remainingFoodCount() == 0,
                CommonRenderInfoKey.MAP_FLASHING, false,
                CommonRenderInfoKey.TICK, ui.gameContext().clock().tickCount()
            ));
            levelRenderer.applyLevelSettings(level, info);
            levelRenderer.drawLevel(level, info);

            level.optBonus().ifPresent(bonus -> actorRenderer.drawActor(bonus));
            actorRenderer.drawActor(level.pac());
            Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW)
                .map(level::ghost)
                .forEach(ghost -> actorRenderer.drawActor(ghost));
        }

        if (PROPERTY_DEBUG_INFO_VISIBLE.get()) {
            canvasRenderer.fillTextCentered(
                "scaling: %.2f, draw calls: %d".formatted(scaling.doubleValue(), drawCallCount),
                Color.WHITE, Font.font(14 * scaling.get()),
                0.5 * worldSize.get().x(), 16
            );
        }
    }

    // Private area

    private void bindCanvasSize() {
        canvas.heightProperty().bind(PROPERTY_MINI_VIEW_HEIGHT);
        canvas.widthProperty().bind(Bindings.createDoubleBinding(
            () -> {
                final double aspect = (double) worldSize.get().x() / worldSize.get().y();
                return aspect * canvas.getHeight();
            },
            worldSize, canvas.heightProperty()
        ));
    }

    private void unbindCanvasSize() {
        canvas.heightProperty().unbind();
        canvas.widthProperty().unbind();
    }


}