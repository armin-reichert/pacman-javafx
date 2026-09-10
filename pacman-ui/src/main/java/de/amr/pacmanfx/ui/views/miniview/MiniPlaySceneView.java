/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.miniview;

import de.amr.basics.math.Vector2i;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.vm.GameViewModel;
import de.amr.pacmanfx.ui.vm.MiniViewSettingsVM;
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
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.stream.Stream;

import static de.amr.pacmanfx.uilib.rendering.RenderableWrapper.reassignLayer;
import static java.util.Objects.requireNonNull;

public class MiniPlaySceneView extends HBox implements Renderable {

    public static final Insets PADDING = new Insets(0, 10, 0, 10);

    private final DoubleProperty scaling = new SimpleDoubleProperty(1.0);

    private final ObjectProperty<Vector2i> worldSize = new SimpleObjectProperty<>(WorldMap.ARCADE_MAP_SIZE_IN_PIXELS);

    private final Canvas canvas;

    private TranslateTransition slideInAnimation;
    private TranslateTransition slideOutAnimation;

    private MiniViewSettingsVM settingsViewModel;

    private GameAppContext app;
    private GameLevel level;

    private boolean expanded;

    public MiniPlaySceneView() {
        canvas = new Canvas();
        getChildren().add(canvas);
        setBorder(Border.stroke(Color.grayRgb(66)));
        setPadding(PADDING);

        // Canvas size determines mini view size
        maxWidthProperty().bind(canvas.widthProperty().add(PADDING.getLeft() + PADDING.getRight()));
        maxHeightProperty().bind(canvas.heightProperty());
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.OVERLAY;
    }

    public Stream<Renderable> renderables() {
        if (level == null) return Stream.empty();
        return Ufx.streamOf(
            reassignLayer(level, RenderingLayer.OVERLAY, -100),
            level.renderableEntities().map(r -> reassignLayer(r, RenderingLayer.OVERLAY, r.z()))
        );
    }

    public void setLevel(GameLevel level) {
        this.level = requireNonNull(level);
        worldSize.set(level.worldMap().terrainLayer().sizeInPixel());
    }

    public Canvas canvas() {
        return canvas;
    }

    public DoubleProperty scalingProperty() {
        return scaling;
    }

    public void clearCanvas() {
        final var ctx = canvas.getGraphicsContext2D();
        final GameViewModel viewModel = app.ui().viewModel();
        ctx.setFill(viewModel.common2DSettings().canvasBackgroundColorProperty().get());
        ctx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public MiniViewRenderer createRenderer() {
        final GameViewModel viewModel = app.ui().viewModel();
        final ActorSpriteAnimController animController = app.game().variant().systems().actorSpriteAnimController();
        final GameVariantRenderConfig renderConfig = app.currentGameVariantUIConfig().renderConfig();

        final var miniViewRenderer = new MiniViewRenderer(canvas, animController, renderConfig, viewModel);
        miniViewRenderer.backgroundColorProperty().bind(viewModel.common2DSettings().canvasBackgroundColorProperty());
        miniViewRenderer.scalingProperty().bind(scalingProperty());

        return miniViewRenderer;
    }

    public void setGameApp(GameAppContext app) {
        this.app = requireNonNull(app);

        final GameViewModel viewModel = app.ui().viewModel();
        settingsViewModel = viewModel.miniViewSettings();

        backgroundProperty().bind(viewModel.common2DSettings().canvasBackgroundColorProperty().map(Background::fill));
        opacityProperty()   .bind(settingsViewModel.opacityPercentageProperty.divide(100.0));

        canvas.heightProperty().bind(settingsViewModel.heightProperty);
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

        setTranslateY(-canvas.getHeight());
    }

    public void update() {
        final boolean shouldBeActive = app.ui().viewModel().miniViewSettings().activeProperty.get();
        if (shouldBeActive && !expanded && !isMoving()) {
            setVisible(true);
            slideOut();
        } else if (!shouldBeActive && expanded && !isMoving()) {
            slideIn();
        }
    }

    private void slideIn() {
        final Duration duration = Duration.seconds(settingsViewModel.slideInSecondsProperty.get());
        slideInAnimation = new TranslateTransition(duration, this);
        slideInAnimation.setOnFinished(_ -> expanded = false);
        slideInAnimation.setToY(0);
        slideInAnimation.setByY(10);
        slideInAnimation.setDelay(Duration.seconds(0.5));
        slideInAnimation.setInterpolator(Interpolator.EASE_OUT);
        slideInAnimation.play();
    }

    private void slideOut() {
        final Duration duration = Duration.seconds(settingsViewModel.slideOutSecondsProperty.get());
        slideOutAnimation = new TranslateTransition(duration, this);
        slideOutAnimation.setOnFinished(_ -> expanded = true);
        slideOutAnimation.setToY(-getHeight());
        slideOutAnimation.setByY(10);
        slideOutAnimation.setDelay(Duration.seconds(0.5));
        slideOutAnimation.setInterpolator(Interpolator.EASE_IN);
        slideOutAnimation.play();
    }

    public boolean isMoving() {
        return slideInAnimation != null && slideInAnimation.getStatus() == Animation.Status.RUNNING
            || slideOutAnimation != null && slideOutAnimation.getStatus() == Animation.Status.RUNNING;
    }
}