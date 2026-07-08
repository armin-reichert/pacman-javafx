/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.views.playview;

import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.ui.GameVariantConfig;
import de.amr.pacmanfx.ui.game.Game;
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

import static java.util.Objects.requireNonNull;

public class MiniPlaySceneView {

    public static final Insets PADDING = new Insets(0, 10, 0, 10);

    private final DoubleProperty scaling = new SimpleDoubleProperty(1.0);
    private final ObjectProperty<Vector2i> worldSize = new SimpleObjectProperty<>(WorldMap.ARCADE_MAP_SIZE_IN_PIXELS);

    private final HBox rootPane;
    private final Canvas canvas;

    private Game game;

    // Note: The level and actor renderers cannot be created in the constructor, because the game controller has not yet
    //       selected a game variant when the constructor is called, so no variant configuration is available yet!
    private BaseRenderer canvasRenderer;
    private GameLevelRenderer levelRenderer;
    private ActorRenderer actorRenderer;

    private TranslateTransition slideInAnimation;
    private TranslateTransition slideOutAnimation;

    // Used in debug draw mode
    private long drawCallCount;

    public MiniPlaySceneView() {
        canvas = new Canvas();

        rootPane = new HBox(canvas);
        rootPane.setBorder(Border.stroke(Color.grayRgb(66)));
        rootPane.setPadding(PADDING);

        // Canvas size determines mini view size
        rootPane.maxWidthProperty().bind(canvas.widthProperty().add(PADDING.getLeft() + PADDING.getRight()));
        rootPane.maxHeightProperty().bind(canvas.heightProperty());
    }

    public Pane rootPane() {
        return rootPane;
    }

    public void setUI(Game game) {
        this.game = requireNonNull(game);

        rootPane.backgroundProperty().bind(game.ui().viewModel().common2D.canvasBackgroundColorProperty.map(Background::fill));
        rootPane.opacityProperty().bind(game.ui().viewModel().miniView.opacityPercentageProperty.divide(100.0));

        canvas.heightProperty().bind(game.ui().viewModel().miniView.heightProperty);
        canvas.widthProperty().bind(Bindings.createDoubleBinding(
            () -> {
                final double aspect = (double) worldSize.get().x() / worldSize.get().y();
                return aspect * canvas.getHeight();
            },
            worldSize, canvas.heightProperty()
        ));

        scaling.bind(Bindings.createDoubleBinding(
            () -> canvas.getHeight() / worldSize.get().y(),
            canvas.heightProperty(), worldSize
        ));
    }

    public void setWorldSizeInPixel(Vector2i size) {
        worldSize.set(size);
    }

    public void setVariantConfig(GameVariantConfig variant) {
        canvasRenderer = new BaseRenderer(canvas);

        levelRenderer = variant.createGameLevelRenderer(canvas);
        levelRenderer.scalingProperty().bind(scaling);
        levelRenderer.backgroundColorProperty().bind(game.ui().viewModel().common2D.canvasBackgroundColorProperty);

        actorRenderer = variant.createActorRenderer(canvas);
        actorRenderer.scalingProperty().bind(scaling);
        actorRenderer.backgroundColorProperty().bind(game.ui().viewModel().common2D.canvasBackgroundColorProperty);
    }

    public void slideIn() {
        if (slideInAnimation != null) {
            slideInAnimation.stop();
        }
        slideInAnimation = new TranslateTransition(
            Duration.seconds(game.ui().viewModel().miniView.slideInSecondsProperty.get()), rootPane);
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
        slideOutAnimation = new TranslateTransition(
            Duration.seconds(game.ui().viewModel().miniView.slideOutSecondsProperty.get()), rootPane);
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
        if (canvasRenderer == null) {
            return;
        }
        if (game != null) {
            game.context().model().optLevel().ifPresent(this::draw);
        }
    }
    
    private void draw(GameLevel level) {
        canvasRenderer.clearCanvas();

        if (levelRenderer != null && actorRenderer != null) {
            drawGameLevel(level);
        }

        if (game.ui().viewModel().debugModeOnProperty.get()) {
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
            CommonRenderInfoKey.ENERGIZER_VISIBLE, level.heartbeat().state() == Pulse.State.ON,
            CommonRenderInfoKey.MAP_BRIGHT, false,
            CommonRenderInfoKey.MAP_EMPTY, level.worldMap().foodLayer().remainingFoodCount() == 0,
            CommonRenderInfoKey.MAP_FLASHING, false,
            CommonRenderInfoKey.TICK, game.machine().clock().currentTick()
        ));
        levelRenderer.applyLevelSettings(level, info);
        levelRenderer.drawLevel(level, info);

        level.optBonus().ifPresent(bonus -> actorRenderer.drawActor(bonus));
        actorRenderer.drawActor(level.entities().pac());
        Stream.of(GameModel.ORANGE_GHOST_POKEY, GameModel.CYAN_GHOST_BASHFUL, GameModel.PINK_GHOST_SPEEDY, GameModel.RED_GHOST_SHADOW).map(level::ghost)
            .forEach(ghost -> actorRenderer.drawActor(ghost));
    }
}