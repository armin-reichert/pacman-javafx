/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.game.PacManGamesCollection;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.model.GameViewModel;
import javafx.scene.SubScene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.shape.DrawMode;

/**
 * Infobox with 3D related settings.
 */
public class DS_3DSettings extends GameDashboardSection {

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

    public DS_3DSettings() {
        super(DashboardID.SETTINGS_3D);
    }

    @Override
    public void connect(PacManGamesCollection game) {
        final GameViewModel viewModel = game.ui().viewModel();

        cbUsePlayScene3D = checkBox("3D Play Scene");
        comboPerspectives = choiceBox("Perspective", PerspectiveID.values());
        colorPicker("Light Color", viewModel.maze3D.lightColorProperty);
        colorPicker("Floor Color", viewModel.maze3D.floorColorProperty);
        addDynamicInfo("Camera",         () -> subSceneCameraInfo(game));
        addDynamicInfo("Sub-scene Size", () -> subSceneSizeInfo(game));
        addDynamicInfo("Scene Size",     () -> sceneSizeInfo(game));

        cbMiniViewVisible = checkBox("Mini View", viewModel.miniView.activeProperty);

        sliderMiniViewSceneHeight = slider(
            " - Height",
            MINI_VIEW_MIN_HEIGHT, MINI_VIEW_MAX_HEIGHT,
            viewModel.miniView.heightProperty.get(),
            false, false);

        sliderMiniViewOpacityPercentage = slider(
            " - Opacity",
            0, 100,
            viewModel.miniView.opacityPercentageProperty.get(),
            false, false);

        sliderWallHeight = slider(
            "Wall Height",
            0, 16,
            viewModel.maze3D.wallHeightProperty.get(),
            false, false);

        sliderWallOpacity = slider(
            "Wall Opacity",
            0, 1,
            viewModel.maze3D.wallOpacityProperty.get(),
            false, false);

        cbAxesVisible = checkBox("Show Axes", viewModel.common3D.axesVisibleProperty);
        cbWireframeMode = checkBox("Wireframe Mode");

        setTooltip(sliderMiniViewSceneHeight, sliderMiniViewSceneHeight.valueProperty(), "%.0f px");
        setTooltip(sliderMiniViewOpacityPercentage, sliderMiniViewOpacityPercentage.valueProperty(), "%.0f %%");

        setTooltip(sliderWallHeight, sliderWallHeight.valueProperty(), "%.0f px");
        setTooltip(sliderWallOpacity, sliderWallOpacity.valueProperty().multiply(100), "%.0f %%");

        editPropertyWithSlider(sliderMiniViewSceneHeight,       viewModel.miniView.heightProperty);
        editPropertyWithSlider(sliderMiniViewOpacityPercentage, viewModel.miniView.opacityPercentageProperty);
        editPropertyWithSlider(sliderWallHeight,                viewModel.maze3D.wallHeightProperty);
        editPropertyWithSlider(sliderWallOpacity,               viewModel.maze3D.wallOpacityProperty);
        editPropertyWithChoiceBox(comboPerspectives,               viewModel.common3D.cameraPerspectiveIdProperty);

        cbUsePlayScene3D.setOnAction(_ -> game.commonActions().uiSettingsActions().actionTogglePlayScene2D3D().execute());
        cbWireframeMode.setOnAction(_ -> game.commonActions().camera3DActions().actionToggleDrawMode().execute());
    }

    @Override
    public void update(PacManGamesCollection game) {
        super.update(game);

        final GameViewModel viewModel = game.ui().viewModel();

        comboPerspectives.setValue(viewModel.common3D.cameraPerspectiveIdProperty.get());

        sliderMiniViewSceneHeight.setDisable(game.ui().views().gamePlayView().miniPlaySceneView().isMoving());

        cbUsePlayScene3D.setSelected(viewModel.common3D.view3DEnabledProperty.get());

        cbMiniViewVisible.setSelected(viewModel.miniView.activeProperty.getValue());

        comboPerspectives.setValue(viewModel.common3D.cameraPerspectiveIdProperty.get());

        cbAxesVisible.setSelected(viewModel.common3D.axesVisibleProperty.get());

        cbWireframeMode.setSelected(viewModel.common3D.drawModeProperty.get() == DrawMode.LINE);
    }

    private String subSceneSizeInfo(PacManGamesCollection game) {
        return game.ui().gameScenes().optCurrentGameScene()
            .flatMap(GameScene::optSubSceneFX)
            .map(subScene -> "%.0fx%.0f".formatted(subScene.getWidth(), subScene.getHeight()))
            .orElse(NO_INFO);
    }

    private String subSceneCameraInfo(PacManGamesCollection game) {
        final GameScene gameScene = game.ui().gameScenes().optCurrentGameScene().orElse(null);
        if (gameScene == null) return NO_INFO;
        return gameScene.optSubSceneFX().map(SubScene::getCamera)
            .map(camera -> "rot=%.0f x=%.0f y=%.0f z=%.0f".formatted(
                camera.getRotate(),
                camera.getTranslateX(),
                camera.getTranslateY(),
                camera.getTranslateZ()))
            .orElse(NO_INFO);
    }

    private String sceneSizeInfo(PacManGamesCollection game) {
        final GameModel gameModel = game.gameContext().model();
        final GameScene gameScene = game.ui().gameScenes().optCurrentGameScene().orElse(null);
        if (gameScene == null) return NO_INFO;

        if (gameScene instanceof AbstractGameScene2D gameScene2D) {
            return "%dx%d (scaled: %.0fx%.0f)".formatted(
                gameScene2D.unscaledWidth(), gameScene2D.unscaledHeight(),
                gameScene2D.width(), gameScene2D.height());
        }

        if (gameModel.optLevel().isPresent()) {
            final WorldMap worldMap = gameModel.optLevel().get().worldMap();
            return "%dx%d (map size px)".formatted(worldMap.numCols() * WorldMap.TS, worldMap.numRows() * WorldMap.TS);
        }

        return NO_INFO;
    }
}