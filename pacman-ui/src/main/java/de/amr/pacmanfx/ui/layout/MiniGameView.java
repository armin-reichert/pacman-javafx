/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.lib.Pulse;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.api.GameUI;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.Map;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.ui.api.GameUI.*;
import static java.util.Objects.requireNonNull;

public class MiniGameView extends VBox {

    public static final Duration SLIDE_IN_DURATION = Duration.seconds(1);
    public static final Duration SLIDE_OUT_DURATION = Duration.seconds(2);

    private final DoubleProperty scaling = new SimpleDoubleProperty(1.0f);
    private final ObjectProperty<Vector2i> worldSize = new SimpleObjectProperty<>(ARCADE_MAP_SIZE_IN_PIXELS);

    private final HBox layout;
    private final Canvas canvas;

    private GameUI ui;

    private final BaseRenderer canvasRenderer;
    private GameLevelRenderer gameLevelRenderer;
    private ActorRenderer actorRenderer;

    private long drawCallCount;

    private TranslateTransition slideInAnimation;
    private TranslateTransition slideOutAnimation;

    public MiniGameView() {
        canvas = new Canvas();
        bindCanvasSize();

        canvasRenderer = new BaseRenderer(canvas);

        // The VBox fills the complete parent container height (why?), so we put the canvas
        // into an HBox that does not grow in height and provides some padding around the canvas.
        layout = new HBox(canvas);
        layout.backgroundProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR.map(Background::fill));
        layout.setPadding(new Insets(0, 10, 0, 10));
        VBox.setVgrow(layout, Priority.NEVER);
        getChildren().add(layout);

        opacityProperty().bind(PROPERTY_MINI_VIEW_OPACITY_PERCENT.divide(100.0));
        scaling.bind(Bindings.createDoubleBinding(
            () -> canvas.getHeight() / worldSize.get().y(),
            canvas.heightProperty(), worldSize
        ));
    }

    private void bindCanvasSize() {
        canvas.heightProperty().bind(PROPERTY_MINI_VIEW_HEIGHT);
        canvas.widthProperty().bind(Bindings.createDoubleBinding(
            () -> {
                Vector2i size = worldSize.get();
                double aspect = (double) size.x() / size.y();
                return aspect * canvas.getHeight();
            },
            worldSize, canvas.heightProperty()
        ));
    }

    private void unbindCanvasSize() {
        canvas.heightProperty().unbind();
        canvas.widthProperty().unbind();
    }

    public void setUI(GameUI ui) {
        this.ui = requireNonNull(ui);
    }

    public void onLevelCreated(GameLevel gameLevel) {
        worldSize.set(gameLevel.worldMap().terrainLayer().sizeInPixel());
        /* TODO: The renderers cannot be created in the constructor because the game controller has not yet
            selected a game variant when the constructor is called, so no UI configuration is available! */

        gameLevelRenderer = ui.currentConfig().createGameLevelRenderer(canvas);
        gameLevelRenderer.scalingProperty().bind(scaling);
        gameLevelRenderer.backgroundColorProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR);

        actorRenderer = ui.currentConfig().createActorRenderer(canvas);
        actorRenderer.scalingProperty().bind(scaling);
        actorRenderer.backgroundColorProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR);
    }

    public void slideIn() {
        if (slideInAnimation != null) {
            slideInAnimation.stop();
        }
        slideInAnimation = new TranslateTransition(SLIDE_IN_DURATION, this);
        slideInAnimation.setToY(0);
        slideInAnimation.setByY(10);
        slideInAnimation.setDelay(Duration.seconds(1));
        slideInAnimation.setInterpolator(Interpolator.EASE_OUT);
        slideInAnimation.setOnFinished(e -> bindCanvasSize());
        unbindCanvasSize();
        slideInAnimation.play();
    }

    public void slideOut() {
        if (slideOutAnimation != null) {
            slideOutAnimation.stop();
        }
        slideOutAnimation = new TranslateTransition(SLIDE_OUT_DURATION, this);
        slideOutAnimation.setToY(-layout.getHeight());
        slideOutAnimation.setByY(10);
        slideOutAnimation.setDelay(Duration.seconds(2));
        slideOutAnimation.setInterpolator(Interpolator.EASE_IN);
        slideOutAnimation.setOnFinished(e -> bindCanvasSize());
        unbindCanvasSize();
        slideOutAnimation.play();
    }

    public boolean isMoving() {
        return slideInAnimation != null && slideInAnimation.getStatus() == Animation.Status.RUNNING
            || slideOutAnimation != null && slideOutAnimation.getStatus() == Animation.Status.RUNNING;
    }

    public void draw() {
        drawCallCount += 1;

        if (!isVisible() || gameLevelRenderer == null) {
            return;
        }
        canvasRenderer.clearCanvas();

        final Game game = ui.context().currentGame();
        if (game.level() != null) {
            var info = new RenderInfo();
            info.putAll(Map.of(
                CommonRenderInfoKey.ENERGIZER_ON, game.level().blinking().state() == Pulse.State.ON,
                CommonRenderInfoKey.MAP_BRIGHT, false,
                CommonRenderInfoKey.MAP_EMPTY, game.level().worldMap().foodLayer().uneatenFoodCount() == 0,
                CommonRenderInfoKey.MAP_FLASHING, false,
                CommonRenderInfoKey.TICK, ui.clock().tickCount()
            ));
            gameLevelRenderer.applyLevelSettings(game.level(), info);
            gameLevelRenderer.drawLevel(game.level(), info);

            game.level().optBonus().ifPresent(bonus -> actorRenderer.drawActor(bonus));
            actorRenderer.drawActor(game.level().pac());
            Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW)
                .map(game.level()::ghost)
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
}