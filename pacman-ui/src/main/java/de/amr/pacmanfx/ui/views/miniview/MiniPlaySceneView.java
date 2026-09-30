/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.miniview;

import de.amr.basics.InfoMap;
import de.amr.basics.ecs.GameEntity;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.basics.ui.rendering.GameEntityView;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.entities.world.Energizer;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneManager;
import de.amr.pacmanfx.ui.gamescene.d2.GameLevelView;
import de.amr.pacmanfx.ui.gamescene.d2.GenericLevelRenderer;
import de.amr.pacmanfx.ui.rendering.GameEntityViewBuilder;
import de.amr.pacmanfx.ui.rendering.RenderingUtil;
import de.amr.pacmanfx.ui.viewmodel.GameViewModel;
import de.amr.pacmanfx.uilib.view2d.LevelRenderInfoKey;
import de.amr.pacmanfx.uilib.view2d.RenderingSurface;
import de.amr.pacmanfx.uilib.view2d.TerrainMapColoring;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class MiniPlaySceneView {

    public static final Insets PADDING = new Insets(0, 10, 0, 10);

    public static final Border BORDER = Border.stroke(Color.grayRgb(66));

    private TranslateTransition slidingInAnimation;
    private TranslateTransition slidingOutAnimation;

    private GameLevel level;
    private GameViewModel viewModel;

    private final HBox root = new HBox();
    private final RenderingSurface renderingSurface;

    public MiniPlaySceneView() {
        root.setPadding(PADDING);
        root.setBorder(BORDER);
        root.setVisible(false);

        renderingSurface = new RenderingSurface();
        renderingSurface.heightProperty().addListener((_, _, _) -> updateViewSize(level));

        root.getChildren().add(renderingSurface.canvas());
    }

    public HBox root() {
        return root;
    }

    public RenderingSurface renderingSurface() {
        return renderingSurface;
    }

    public void setViewModel(GameViewModel viewModel) {
        this.viewModel = requireNonNull(viewModel);

        renderingSurface.heightProperty().bind(viewModel.miniViewSettings().heightProperty);
        renderingSurface.backgroundColorProperty().bind(viewModel.common2DSettings().canvasBackgroundColorProperty());

        root.backgroundProperty().bind(viewModel.common2DSettings().canvasBackgroundColorProperty().map(Background::fill));
        root.opacityProperty()   .bind(viewModel.miniViewSettings().opacityPercentageProperty.divide(100.0));
        root.maxWidthProperty() .bind(renderingSurface.widthProperty().add(PADDING.getLeft() + PADDING.getRight()));
        root.maxHeightProperty().bind(renderingSurface.heightProperty().add(PADDING.getTop() + PADDING.getBottom()));

        // Move out of view
        root.setTranslateY(outOfViewY());
    }

    private double outOfViewY() {
        return -(renderingSurface.height() + PADDING.getBottom());
    }

    public void update(GameSceneManager gameSceneManager) {
        final boolean is3DPlaySceneActive = gameSceneManager.currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D);
        final boolean shouldBeVisible = is3DPlaySceneActive && viewModel.miniViewSettings().activeProperty.get();
        if (shouldBeVisible) {
            if (!isInsideView()) {
                slideIn();
            }
        } else {
            if (isInsideView()) {
                slideOut();
            }
        }
    }

    public Stream<Renderable> renderables() {
        if (!root.isVisible() || level == null) return Stream.empty();
        return Ufx.streamOf(createLevelView(level), createEntityViews());
    }

    public void setLevel(GameLevel level) {
        this.level = requireNonNull(level);
        updateViewSize(level);
    }

    public boolean isSliding() {
        return slidingInAnimation != null && slidingInAnimation.getStatus() == Animation.Status.RUNNING
            || slidingOutAnimation != null && slidingOutAnimation.getStatus() == Animation.Status.RUNNING;
    }

    // --- private ---

    private boolean isInsideView() {
        return root.getTranslateY() == 0;
    }

    private void updateViewSize(GameLevel level) {
        if (level == null) return;

        final Vector2i worldSize = level.worldMap().terrainLayer().sizeInPixel();
        final double aspect = (double) worldSize.x() / (double) worldSize.y();
        renderingSurface.setWidth(aspect * renderingSurface.height());
        renderingSurface.setScaling(renderingSurface.height() / worldSize.y());
    }

    private void slideIn() {
        if (isSliding()) return;
        final Duration duration = Duration.seconds(viewModel.miniViewSettings().slideInSecondsProperty.get());
        slidingInAnimation = new TranslateTransition(duration, root);
        slidingInAnimation.setToY(0);
        slidingInAnimation.setInterpolator(Interpolator.EASE_OUT);
        slidingInAnimation.play();

        root.setVisible(true);
    }

    private void slideOut() {
        if (isSliding()) return;
        final Duration duration = Duration.seconds(viewModel.miniViewSettings().slideOutSecondsProperty.get());
        slidingOutAnimation = new TranslateTransition(duration, root);
        slidingOutAnimation.setToY(outOfViewY());
        slidingOutAnimation.setInterpolator(Interpolator.EASE_IN);
        slidingOutAnimation.setOnFinished(_ -> root.setVisible(false));
        slidingOutAnimation.play();
    }

    private GameLevelView createLevelView(GameLevel level) {
        final InfoMap renderInfo = InfoMap.create();
        renderInfo.put(LevelRenderInfoKey.ENERGIZERS_SHOWN, level.heartbeat().state() == Pulse.State.ON);
        renderInfo.put(LevelRenderInfoKey.SHOW_BRIGHT_MAZE, false);
        renderInfo.put(LevelRenderInfoKey.SHOW_EMPTY_MAZE, level.food().remainingFoodCount() == 0);
        renderInfo.put(LevelRenderInfoKey.MAZE_IS_FLASHING, false);
        final TerrainMapColoring terrainMapColoring = RenderingUtil.findMapColoring(viewModel, level.worldMap());
        if (terrainMapColoring != null) {
            // Only available for generic level renderer in XXL game variants
            renderInfo.put(GenericLevelRenderer.RenderInfoKey.TERRAIN_MAP_COLORING, terrainMapColoring);
        }

        return new GameLevelView(level, renderInfo, RenderingLayer.MINI_VIEW_OVERLAY, 0, Vector2f.ZERO);
    }

    private Stream<GameEntityView> createEntityViews() {
        final InfoMap energizerRenderInfo = InfoMap.create();
        energizerRenderInfo.put(GenericLevelRenderer.RenderInfoKey.PELLET_COLOR, RenderingUtil.findPelletColor(level.worldMap()));
        return level.entitySet().entities().all()
            .map(e -> createEntityView(e, e instanceof Energizer ? energizerRenderInfo : InfoMap.EMPTY_IMMUTABLE_MAP));
    }

    private GameEntityView createEntityView(GameEntity entity, InfoMap renderInfo) {
        return GameEntityViewBuilder.builder()
            .entity(entity)
            .layer(RenderingLayer.MINI_VIEW_OVERLAY)
            .renderInfo(renderInfo)
            .build();
    }
}