/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneController;
import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.views.playview.MiniPlaySceneView;
import de.amr.pacmanfx.ui.vm.GameViewModel;
import javafx.scene.Camera;
import javafx.scene.SubScene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.shape.DrawMode;

/**
 * Infobox with 3D related settings.
 */
public class DS_3DSettings extends GameDashboardSection {

    private CheckBox cbUsePlayScene3D;
    private ChoiceBox<PerspectiveID> comboPerspectives;
    private CheckBox cbMiniViewVisible;
    private Slider sliderMiniViewHeight;
    private Slider sliderMiniViewOpacityPercentage;
    private Slider sliderWallHeight;
    private Slider sliderWallOpacity;
    private CheckBox cbAxesVisible;
    private CheckBox cbWireframeMode;

    public DS_3DSettings() {
        super(DashboardID.SETTINGS_3D);
    }

    @Override
    public void setGameApp(GameAppContext app) {
        final GameViewModel vm = app.ui().viewModel();

        cbUsePlayScene3D = checkBox("3D Play Scene");

        comboPerspectives = choiceBox("Perspective", PerspectiveID.values());

        colorPicker("Light Color", vm.maze3DSettings().lightColorProperty());

        colorPicker("Floor Color", vm.maze3DSettings().floorColorProperty());

        addDynamicInfo("Camera", () -> subSceneCameraInfo(currentSubSceneFX(app)));

        addDynamicInfo("Sub-scene Size", () -> subSceneSizeInfo(currentSubSceneFX(app)));

        addDynamicInfo("Scene Size", () -> sceneSizeInfo(
            app.ui().gameScenes().optCurrentGameScene().orElse(null),
            app.game().session().optLevel().orElse(null)
        ));

        cbMiniViewVisible = checkBox("Mini View", vm.miniViewSettings().activeProperty);

        sliderMiniViewHeight = slider(
            " - Height",
            vm.miniViewSettings().minHeightProperty.get(),
            vm.miniViewSettings().maxHeightProperty.get(),
            vm.miniViewSettings().heightProperty.get(),
            false, false);

        sliderMiniViewOpacityPercentage = slider(
            " - Opacity",
            0, 100,
            vm.miniViewSettings().opacityPercentageProperty.get(),
            false, false);

        sliderWallHeight = slider(
            "Wall Height",
            0, 16,
            vm.maze3DSettings().wallHeightProperty().get(),
            false, false);

        sliderWallOpacity = slider(
            "Wall Opacity",
            0, 1,
            vm.maze3DSettings().wallOpacityProperty().get(),
            false, false);

        cbAxesVisible = checkBox("Show Axes", vm.common3DSettings().axesVisibleProperty());

        cbWireframeMode = checkBox("Wireframe Mode");

        setTooltip(sliderMiniViewHeight, sliderMiniViewHeight.valueProperty(), "%.0f px");
        setTooltip(sliderMiniViewOpacityPercentage, sliderMiniViewOpacityPercentage.valueProperty(), "%.0f %%");

        setTooltip(sliderWallHeight, sliderWallHeight.valueProperty(), "%.0f px");
        setTooltip(sliderWallOpacity, sliderWallOpacity.valueProperty().multiply(100), "%.0f %%");

        editPropertyWithSlider(sliderMiniViewHeight,            vm.miniViewSettings().heightProperty);
        editPropertyWithSlider(sliderMiniViewOpacityPercentage, vm.miniViewSettings().opacityPercentageProperty);
        editPropertyWithSlider(sliderWallHeight,                vm.maze3DSettings().wallHeightProperty());
        editPropertyWithSlider(sliderWallOpacity,               vm.maze3DSettings().wallOpacityProperty());
        editPropertyWithChoiceBox(comboPerspectives,            vm.common3DSettings().cameraPerspectiveIDProperty());

        cbUsePlayScene3D.setOnAction(_ -> app.runAction(app.commonActions().uiSettingsActions().actionTogglePlayScene2D3D()));
        cbWireframeMode .setOnAction(_ -> app.runAction(app.commonActions().camera3DActions().actionToggleDrawMode()));
    }

    @Override
    public void update(GameAppContext app) {
        super.update(app);

        final GameViewModel vm = app.ui().viewModel();
        final MiniPlaySceneView miniView = app.ui().views().gamePlayView().miniPlaySceneView();

        comboPerspectives.setValue(vm.common3DSettings().cameraPerspectiveIDProperty().get());

        cbUsePlayScene3D.setSelected(vm.common3DSettings().view3DEnabledProperty().get());
        cbAxesVisible   .setSelected(vm.common3DSettings().axesVisibleProperty().get());
        cbWireframeMode .setSelected(vm.common3DSettings().drawModeProperty().get() == DrawMode.LINE);

        // Mini view
        cbMiniViewVisible.setSelected(vm.miniViewSettings().activeProperty.getValue());
        sliderMiniViewHeight.setDisable(miniView.isMoving());
    }

    private static SubScene currentSubSceneFX(GameAppContext app) {
        return app.ui().gameScenes().optCurrentGameScene().flatMap(GameSceneController::optSubSceneFX).orElse(null);
    }

    private static String subSceneSizeInfo(SubScene subScene) {
        return subScene != null
            ? "%.0fx%.0f".formatted(subScene.getWidth(), subScene.getHeight())
            : NO_INFO;
    }

    private static String subSceneCameraInfo(SubScene subScene) {
        if (subScene == null) {
            return NO_INFO;
        }
        final Camera camera = subScene.getCamera();
        return "rot=%.0f x=%.0f y=%.0f z=%.0f".formatted(
            camera.getRotate(),
            camera.getTranslateX(),
            camera.getTranslateY(),
            camera.getTranslateZ());
    }

    private static String sceneSizeInfo(GameScene gameScene, GameLevel level) {
        if (gameScene == null) return NO_INFO;

        if (gameScene.optCanvasRendering().isPresent()) {
            final var canvasRendering = gameScene.optCanvasRendering().get();
            return "%dx%d (scaled: %.0fx%.0f)".formatted(
                canvasRendering.unscaledWidth(), canvasRendering.unscaledHeight(),
                canvasRendering.scaledWidth(),   canvasRendering.scaledHeight());
        }

        if (level != null) {
            final WorldMap worldMap = level.worldMap();
            return "%dx%d (map size px)".formatted(worldMap.numCols() * WorldMap.TS, worldMap.numRows() * WorldMap.TS);
        }

        return NO_INFO;
    }
}