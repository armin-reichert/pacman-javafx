/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D;
import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.model.GameUIViewModel;
import javafx.scene.SubScene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.shape.DrawMode;

/**
 * Infobox with 3D related settings.
 */
public class DashboardSection3DSettings extends DashboardSection {

    private static final int MINI_VIEW_MIN_HEIGHT = 280;
    private static final int MINI_VIEW_MAX_HEIGHT = 600;

    private CheckBox cbUsePlayScene3D;
    private ChoiceBox<PerspectiveID> comboPerspectives;
    private CheckBox cbMiniViewVisible;
    private Slider sliderMiniViewSceneHeight;
    private Slider sliderMiniViewOpacityPercentage;
    private Slider sliderWallHeight;
    private Slider sliderWallOpacity;
    private CheckBox cbAxesVisible;
    private CheckBox cbWireframeMode;

    public DashboardSection3DSettings(Dashboard dashboard) {
        super(dashboard);
    }

    @Override
    public void connect(Game game) {
        final GameUIViewModel viewModel = game.ui().viewModel();

        cbUsePlayScene3D = addCheckBox("3D Play Scene");
        comboPerspectives = addChoiceBox("Perspective", PerspectiveID.values());
        addColorPicker("Light Color", viewModel.maze3D.lightColorProperty);
        addColorPicker("Floor Color", viewModel.maze3D.floorColorProperty);
        addDynamicLabeledValue("Camera",         () -> subSceneCameraInfo(game));
        addDynamicLabeledValue("Sub-scene Size", () -> subSceneSizeInfo(game));
        addDynamicLabeledValue("Scene Size",     () -> sceneSizeInfo(game));

        cbMiniViewVisible = addCheckBox("Mini View", game.ui().viewModel().miniView.activeProperty);

        sliderMiniViewSceneHeight = addSlider(
            " - Height",
            MINI_VIEW_MIN_HEIGHT, MINI_VIEW_MAX_HEIGHT,
            game.ui().viewModel().miniView.heightProperty.get(),
            false, false);

        sliderMiniViewOpacityPercentage = addSlider(
            " - Opacity",
            0, 100,
            game.ui().viewModel().miniView.opacityPercentageProperty.get(),
            false, false);

        sliderWallHeight = addSlider(
            "Wall Height",
            0, 16,
            viewModel.maze3D.wallHeightProperty.get(),
            false, false);

        sliderWallOpacity = addSlider(
            "Wall Opacity",
            0, 1,
            viewModel.maze3D.wallOpacityProperty.get(),
            false, false);

        cbAxesVisible = addCheckBox("Show Axes", viewModel.common3D.axesVisibleProperty);
        cbWireframeMode = addCheckBox("Wireframe Mode");

        setTooltip(sliderMiniViewSceneHeight, sliderMiniViewSceneHeight.valueProperty(), "%.0f px");
        setTooltip(sliderMiniViewOpacityPercentage, sliderMiniViewOpacityPercentage.valueProperty(), "%.0f %%");

        setTooltip(sliderWallHeight, sliderWallHeight.valueProperty(), "%.0f px");
        setTooltip(sliderWallOpacity, sliderWallOpacity.valueProperty().multiply(100), "%.0f %%");

        setEditor(sliderMiniViewSceneHeight, viewModel.miniView.heightProperty);
        setEditor(sliderMiniViewOpacityPercentage, viewModel.miniView.opacityPercentageProperty);
        setEditor(sliderWallHeight,  viewModel.maze3D.wallHeightProperty);
        setEditor(sliderWallOpacity, viewModel.maze3D.wallOpacityProperty);
        setEditor(comboPerspectives, viewModel.common3D.cameraPerspectiveIdProperty);

        cbUsePlayScene3D.setOnAction(_ -> game.actions().uiSettingsActions().actionTogglePlayScene2D3D().execute());
        cbWireframeMode.setOnAction(_ -> game.actions().camera3DActions().actionToggleDrawMode().execute());
    }

    @Override
    public void update(Game game) {
        super.update(game);

        final GameUIViewModel viewModel = game.ui().viewModel();

        comboPerspectives.setValue(viewModel.common3D.cameraPerspectiveIdProperty.get());
        sliderMiniViewSceneHeight.setValue(viewModel.miniView.heightProperty.get());
        sliderMiniViewSceneHeight.setDisable(game.ui().views().gamePlayView().miniPlaySceneView().isMoving());
        sliderMiniViewOpacityPercentage.setValue(viewModel.miniView.opacityPercentageProperty.get());
        sliderWallHeight.setValue(viewModel.maze3D.wallHeightProperty.get());
        sliderWallOpacity.setValue(viewModel.maze3D.wallOpacityProperty.get());
        cbUsePlayScene3D.setSelected(viewModel.common3D.view3DEnabledProperty.get());
        cbMiniViewVisible.setSelected(viewModel.miniView.activeProperty.getValue());
        comboPerspectives.setValue(viewModel.common3D.cameraPerspectiveIdProperty.get());
        cbAxesVisible.setSelected(viewModel.common3D.axesVisibleProperty.get());
        cbWireframeMode.setSelected(viewModel.common3D.drawModeProperty.get() == DrawMode.LINE);
    }

    private String subSceneSizeInfo(Game game) {
        return game.ui().gameScenes().optCurrentGameScene()
            .flatMap(AbstractGameScene::optSubSceneFX)
            .map(subScene -> "%.0fx%.0f".formatted(subScene.getWidth(), subScene.getHeight()))
            .orElse(NO_INFO);
    }

    private String subSceneCameraInfo(Game game) {
        final AbstractGameScene gameScene = game.ui().gameScenes().optCurrentGameScene().orElse(null);
        if (gameScene == null) return NO_INFO;
        return gameScene.optSubSceneFX().map(SubScene::getCamera)
            .map(camera -> "rot=%.0f x=%.0f y=%.0f z=%.0f".formatted(
                camera.getRotate(),
                camera.getTranslateX(),
                camera.getTranslateY(),
                camera.getTranslateZ()))
            .orElse(NO_INFO);
    }

    private String sceneSizeInfo(Game game) {
        final GameModel gameModel = game.currentGameContext().model();
        final AbstractGameScene gameScene = game.ui().gameScenes().optCurrentGameScene().orElse(null);
        if (gameScene == null) return NO_INFO;

        if (gameScene instanceof GameScene2D gameScene2D) {
            return "%dx%d (scaled: %.0fx%.0f)".formatted(
                gameScene2D.unscaledWidth(), gameScene2D.unscaledHeight(),
                gameScene2D.width(), gameScene2D.height());
        }

        if (gameModel.optGameLevel().isPresent()) {
            final WorldMap worldMap = gameModel.optGameLevel().get().worldMap();
            return "%dx%d (map size px)".formatted(worldMap.numCols() * WorldMap.TS, worldMap.numRows() * WorldMap.TS);
        }

        return NO_INFO;
    }
}