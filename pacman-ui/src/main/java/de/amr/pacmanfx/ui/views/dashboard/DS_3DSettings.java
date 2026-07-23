/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.model.GameUISettingsVM;
import de.amr.pacmanfx.ui.views.playview.MiniPlaySceneView;
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

    //TODO move to JSON settings file and get from view model
    private static final int MINI_VIEW_MIN_HEIGHT = 280;
    private static final int MINI_VIEW_MAX_HEIGHT = 600;

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
    public void setGameAppContext(GameAppContext app) {
        final GameUISettingsVM vm = app.ui().viewModel();

        cbUsePlayScene3D = checkBox("3D Play Scene");

        comboPerspectives = choiceBox("Perspective", PerspectiveID.values());

        colorPicker("Light Color", vm.maze3D.lightColorProperty);

        colorPicker("Floor Color", vm.maze3D.floorColorProperty);

        addDynamicInfo("Camera", () -> subSceneCameraInfo(currentSubSceneFX(app)));

        addDynamicInfo("Sub-scene Size", () -> subSceneSizeInfo(currentSubSceneFX(app)));

        addDynamicInfo("Scene Size", () -> sceneSizeInfo(
            app.optCurrentGameScene().orElse(null),
            app.currentGameContext().optLevel().orElse(null)
        ));

        cbMiniViewVisible = checkBox("Mini View", vm.miniView.activeProperty);

        sliderMiniViewHeight = slider(
            " - Height",
            MINI_VIEW_MIN_HEIGHT, MINI_VIEW_MAX_HEIGHT,
            vm.miniView.heightProperty.get(),
            false, false);

        sliderMiniViewOpacityPercentage = slider(
            " - Opacity",
            0, 100,
            vm.miniView.opacityPercentageProperty.get(),
            false, false);

        sliderWallHeight = slider(
            "Wall Height",
            0, 16,
            vm.maze3D.wallHeightProperty.get(),
            false, false);

        sliderWallOpacity = slider(
            "Wall Opacity",
            0, 1,
            vm.maze3D.wallOpacityProperty.get(),
            false, false);

        cbAxesVisible = checkBox("Show Axes", vm.common3D.axesVisibleProperty);

        cbWireframeMode = checkBox("Wireframe Mode");

        setTooltip(sliderMiniViewHeight, sliderMiniViewHeight.valueProperty(), "%.0f px");
        setTooltip(sliderMiniViewOpacityPercentage, sliderMiniViewOpacityPercentage.valueProperty(), "%.0f %%");

        setTooltip(sliderWallHeight, sliderWallHeight.valueProperty(), "%.0f px");
        setTooltip(sliderWallOpacity, sliderWallOpacity.valueProperty().multiply(100), "%.0f %%");

        editPropertyWithSlider(sliderMiniViewHeight,            vm.miniView.heightProperty);
        editPropertyWithSlider(sliderMiniViewOpacityPercentage, vm.miniView.opacityPercentageProperty);
        editPropertyWithSlider(sliderWallHeight,                vm.maze3D.wallHeightProperty);
        editPropertyWithSlider(sliderWallOpacity,               vm.maze3D.wallOpacityProperty);
        editPropertyWithChoiceBox(comboPerspectives,            vm.common3D.cameraPerspectiveIdProperty);

        cbUsePlayScene3D.setOnAction(_ -> app.commonActions().uiSettingsActions().actionTogglePlayScene2D3D().execute());
        cbWireframeMode .setOnAction(_ -> app.commonActions().camera3DActions().actionToggleDrawMode().execute());
    }

    @Override
    public void update(GameAppContext appContext) {
        super.update(appContext);

        final GameUISettingsVM vm = appContext.ui().viewModel();
        final MiniPlaySceneView miniView = appContext.ui().views().gamePlayView().miniPlaySceneView();

        comboPerspectives.setValue(vm.common3D.cameraPerspectiveIdProperty.get());

        cbUsePlayScene3D.setSelected(vm.common3D.view3DEnabledProperty.get());
        cbAxesVisible   .setSelected(vm.common3D.axesVisibleProperty.get());
        cbWireframeMode .setSelected(vm.common3D.drawModeProperty.get() == DrawMode.LINE);

        // Mini view
        cbMiniViewVisible.setSelected(vm.miniView.activeProperty.getValue());
        sliderMiniViewHeight.setDisable(miniView.isMoving());
    }

    private static SubScene currentSubSceneFX(GameAppContext app) {
        return app.optCurrentGameScene().flatMap(GameScene::optSubSceneFX).orElse(null);
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

        if (gameScene instanceof AbstractGameScene2D gameScene2D) {
            return "%dx%d (scaled: %.0fx%.0f)".formatted(
                gameScene2D.unscaledWidth(), gameScene2D.unscaledHeight(),
                gameScene2D.scaledWidth(), gameScene2D.scaledHeight());
        }

        if (level != null) {
            final WorldMap worldMap = level.worldMap();
            return "%dx%d (map size px)".formatted(worldMap.numCols() * WorldMap.TS, worldMap.numRows() * WorldMap.TS);
        }

        return NO_INFO;
    }
}