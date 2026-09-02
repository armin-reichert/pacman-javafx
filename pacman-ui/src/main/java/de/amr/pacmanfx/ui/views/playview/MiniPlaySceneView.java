/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.views.playview;

import de.amr.basics.InfoMap;
import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelEntitySet;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.rules.GameRules;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.ActorAnimationManager;
import de.amr.pacmanfx.ui.vm.GameViewModel;
import de.amr.pacmanfx.ui.vm.MiniViewSettingsVM;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class MiniPlaySceneView {

    public static final List<GhostPersonality> GHOST_Z_ORDER = List.of(
        GhostPersonality.ORANGE_GHOST_POKEY,
        GhostPersonality.CYAN_GHOST_BASHFUL,
        GhostPersonality.PINK_GHOST_SPEEDY,
        GhostPersonality.RED_GHOST_SHADOW);

    public static final Insets PADDING = new Insets(0, 10, 0, 10);

    private final DoubleProperty scaling = new SimpleDoubleProperty(1.0);
    private final ObjectProperty<Vector2i> worldSize = new SimpleObjectProperty<>(WorldMap.ARCADE_MAP_SIZE_IN_PIXELS);

    private final HBox rootPane;
    private final Canvas canvas;

    private GameAppContext app;

    // Note: The level and actor renderers cannot be created in the constructor, because the game controller has not yet
    //       selected a game variant when the constructor is called, so no variant configuration is available yet!
    private BaseRenderer canvasRenderer;
    private GameLevelRenderer levelRenderer;
    private ActorRenderer actorRenderer;

    private TranslateTransition slideInAnimation;
    private TranslateTransition slideOutAnimation;

    // Used in debug draw mode
    private long drawCallCount;

    private final List<GameEntity> actorsInZOrder = new ArrayList<>();

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

    public void setGameApp(GameAppContext app) {
        this.app = requireNonNull(app);

        final GameViewModel vm = app.ui().viewModel();

        rootPane.backgroundProperty().bind(vm.common2DSettings().canvasBackgroundColorProperty().map(Background::fill));
        rootPane.opacityProperty()   .bind(vm.miniViewSettings().opacityPercentageProperty.divide(100.0));

        canvas.heightProperty().bind(vm.miniViewSettings().heightProperty);
        canvas.widthProperty() .bind(Bindings.createDoubleBinding(
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

    public void setRenderConfig(ActorSpriteAnimController animController, GameVariantRenderConfig renderConfig) {
        final GameViewModel vm = app.ui().viewModel();

        canvasRenderer = new BaseRenderer(canvas);

        levelRenderer = renderConfig.createGameLevelRenderer(animController, canvas);
        levelRenderer.scalingProperty().bind(scaling);
        levelRenderer.backgroundColorProperty().bind(vm.common2DSettings().canvasBackgroundColorProperty());

        actorRenderer = renderConfig.createActorRenderer(animController, canvas);
        actorRenderer.scalingProperty().bind(scaling);
        actorRenderer.backgroundColorProperty().bind(vm.common2DSettings().canvasBackgroundColorProperty());
    }

    public void slideIn(MiniViewSettingsVM settingsVM) {
        requireNonNull(settingsVM);

        if (slideInAnimation != null) {
            slideInAnimation.stop();
        }
        slideInAnimation = new TranslateTransition(Duration.seconds(settingsVM.slideInSecondsProperty.get()), rootPane);
        slideInAnimation.setToY(0);
        slideInAnimation.setByY(10);
        slideInAnimation.setDelay(Duration.seconds(1));
        slideInAnimation.setInterpolator(Interpolator.EASE_OUT);
        slideInAnimation.play();
    }

    public void slideOut(MiniViewSettingsVM settingsVM) {
        requireNonNull(settingsVM);

        if (slideOutAnimation != null) {
            slideOutAnimation.stop();
        }
        slideOutAnimation = new TranslateTransition(Duration.seconds(settingsVM.slideOutSecondsProperty.get()), rootPane);
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
        if (app != null) {
            final GameSession session = app.game().session();
            session.optLevel().ifPresent(level -> draw(app.game(), level));
        }
    }
    
    private void draw(GameContext game, GameLevel level) {
        canvasRenderer.clearCanvas();

        if (levelRenderer != null && actorRenderer != null) {
            ActorAnimationManager.ensureActorAnimationsCreated(app, level);
            drawGameLevel(game, level);
        }

        if (app.ui().viewModel().debugModeOnProperty().get()) {
            canvasRenderer.fillTextCentered(
                "scaling: %.2f, draw calls: %d".formatted(scaling.doubleValue(), drawCallCount),
                Color.WHITE, Font.font(12 * scaling.get()),
                0.5 * canvas.getWidth(), scaling.doubleValue() * 16
            );
        }

        drawCallCount += 1;
    }

    private void drawGameLevel(GameContext game, GameLevel level) {
        final GameRules rules = game.variant().rules();
        final var info = new InfoMap();
        info.putAll(Map.of(
            CommonRenderInfoKey.ENERGIZER_VISIBLE, level.heartbeat().state() == Pulse.State.ON,
            CommonRenderInfoKey.MAP_BRIGHT, false,
            CommonRenderInfoKey.MAP_EMPTY, level.food().remainingFoodCount() == 0,
            CommonRenderInfoKey.MAP_FLASHING, false,
            CommonRenderInfoKey.TICK, app.clock().currentTick()
        ));
        levelRenderer.applyLevelSettings(rules, level, info);
        levelRenderer.drawLevel(game, level, info);

        updateActorZOrder(level.entities());
        actorsInZOrder.forEach(actorRenderer::drawActor);
    }

    // Actor z-order: Bonus under Pac-Man under ghosts in z-order.
    private void updateActorZOrder(GameLevelEntitySet entities) {
        actorsInZOrder.clear();
        entities.optBonus().ifPresent(actorsInZOrder::add);
        actorsInZOrder.addAll(entities.theGhostPoints());
        actorsInZOrder.addAll(entities.theBonusPoints());
        actorsInZOrder.add(entities.pac());
        GHOST_Z_ORDER.stream().map(entities::ghost).forEach(actorsInZOrder::add);
    }
}