/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.miniview;

import de.amr.basics.math.Vector2i;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneManager;
import de.amr.pacmanfx.ui.vm.GameViewModel;
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

    public static final Border BORDER = Border.stroke(Color.grayRgb(66));

    private final DoubleProperty scaling = new SimpleDoubleProperty(1.0);

    private final ObjectProperty<Vector2i> worldSize = new SimpleObjectProperty<>(WorldMap.ARCADE_MAP_SIZE_IN_PIXELS);

    private final Canvas canvas = new Canvas();

    private TranslateTransition slidingInAnimation;

    private TranslateTransition slidingOutAnimation;

    private GameLevel level;

    private GameViewModel viewModel;

    public MiniPlaySceneView() {
        setPadding(PADDING);
        setBorder(BORDER);
        getChildren().add(canvas);
        setVisible(false);
    }

    public void setViewModel(GameViewModel viewModel) {
        this.viewModel = requireNonNull(viewModel);

        backgroundProperty().bind(viewModel.common2DSettings().canvasBackgroundColorProperty().map(Background::fill));
        opacityProperty()   .bind(viewModel.miniViewSettings().opacityPercentageProperty.divide(100.0));

        canvas.heightProperty().bind(viewModel.miniViewSettings().heightProperty);
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

        // Canvas size determines mini view size
        maxWidthProperty().bind(canvas.widthProperty().add(PADDING.getLeft() + PADDING.getRight()));
        maxHeightProperty().bind(canvas.heightProperty().add(PADDING.getTop() + PADDING.getBottom()));

        setTranslateY(-canvas.getHeight());
    }

    public void update(GameSceneManager gameSceneManager) {
        final boolean is3DPlaySceneActive = gameSceneManager.currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D);
        final boolean shouldBeVisible = is3DPlaySceneActive && viewModel.miniViewSettings().activeProperty.get();
        if (shouldBeVisible) {
            if (!expanded()) {
                slideIntoView();
            }
        } else {
            if (expanded()) {
                slideOutOfView();
            }
        }
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.OVERLAY;
    }

    public Stream<Renderable> renderables() {
        if (!isVisible() || level == null) return Stream.empty();
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

    public GameViewModel viewModel() {
        return viewModel;
    }

    public DoubleProperty scalingProperty() {
        return scaling;
    }

    public void clearCanvas() {
        final var ctx = canvas.getGraphicsContext2D();
        ctx.setFill(viewModel.common2DSettings().canvasBackgroundColorProperty().get());
        ctx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private boolean expanded() {
        return getTranslateY() == 0;
    }

    private void slideIntoView() {
        if (slidingInAnimation != null && slidingInAnimation.getStatus() == Animation.Status.RUNNING) {
            return;
        }
        final Duration duration = Duration.seconds(viewModel.miniViewSettings().slideInSecondsProperty.get());
        slidingInAnimation = new TranslateTransition(duration, this);
        slidingInAnimation.setToY(0);
        slidingInAnimation.setByY(10);
        slidingInAnimation.setInterpolator(Interpolator.EASE_OUT);
        slidingInAnimation.play();
        setVisible(true);
    }

    private void slideOutOfView() {
        if (slidingOutAnimation != null && slidingOutAnimation.getStatus() == Animation.Status.RUNNING) {
            return;
        }
        final Duration duration = Duration.seconds(viewModel.miniViewSettings().slideOutSecondsProperty.get());
        slidingOutAnimation = new TranslateTransition(duration, this);
        slidingOutAnimation.setToY(-getHeight());
        slidingOutAnimation.setByY(10);
        slidingOutAnimation.setInterpolator(Interpolator.EASE_IN);
        slidingOutAnimation.setOnFinished(_ -> setVisible(false));
        slidingOutAnimation.play();
    }

    public boolean isMoving() {
        return slidingInAnimation != null && slidingInAnimation.getStatus() == Animation.Status.RUNNING
            || slidingOutAnimation != null && slidingOutAnimation.getStatus() == Animation.Status.RUNNING;
    }
}