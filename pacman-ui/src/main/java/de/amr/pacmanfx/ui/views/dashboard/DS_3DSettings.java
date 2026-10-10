/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.pacmanfx.engine.gamescene.GameScene;
import de.amr.pacmanfx.engine.runtime.action.GameEngineContext;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.viewmodel.GameViewModel;
import de.amr.pacmanfx.ui.views.miniview.MiniPlaySceneView;
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
    public void setExecutionContext(GameEngineContext engineContext) {
        final GameViewModel viewModel = engineContext.ui().viewModel();

        cbUsePlayScene3D = checkBox("3D Play Scene");

        comboPerspectives = choiceBox("Perspective", PerspectiveID.values());

        colorPicker("Light Color", viewModel.maze3DSettings().lightColorProperty());

        colorPicker("Floor Color", viewModel.maze3DSettings().floorColorProperty());

        addDynamicInfo("Camera", () -> subSceneCameraInfo(currentSubSceneFX(engineContext)));

        addDynamicInfo("Sub-scene Size", () -> subSceneSizeInfo(currentSubSceneFX(engineContext)));

        addDynamicInfo("Scene Size", () -> sceneSizeInfo(engineContext.gameSceneManager().optCurrentGameScene().orElse(null)));

        cbMiniViewVisible = checkBox("Mini View", viewModel.miniViewSettings().activeProperty);

        sliderMiniViewHeight = slider(
            " - Height",
            viewModel.miniViewSettings().minHeightProperty.get(),
            viewModel.miniViewSettings().maxHeightProperty.get(),
            viewModel.miniViewSettings().heightProperty.get(),
            false, false);

        sliderMiniViewOpacityPercentage = slider(
            " - Opacity",
            0, 100,
            viewModel.miniViewSettings().opacityPercentageProperty.get(),
            false, false);

        sliderWallHeight = slider(
            "Wall Height",
            0, 16,
            viewModel.maze3DSettings().wallHeightProperty().get(),
            false, false);

        sliderWallOpacity = slider(
            "Wall Opacity",
            0, 1,
            viewModel.maze3DSettings().wallOpacityProperty().get(),
            false, false);

        cbAxesVisible = checkBox("Show Axes", viewModel.common3DSettings().axesVisibleProperty());

        cbWireframeMode = checkBox("Wireframe Mode");

        setTooltip(sliderMiniViewHeight, sliderMiniViewHeight.valueProperty(), "%.0f px");
        setTooltip(sliderMiniViewOpacityPercentage, sliderMiniViewOpacityPercentage.valueProperty(), "%.0f %%");

        setTooltip(sliderWallHeight, sliderWallHeight.valueProperty(), "%.0f px");
        setTooltip(sliderWallOpacity, sliderWallOpacity.valueProperty().multiply(100), "%.0f %%");

        editPropertyWithSlider(sliderMiniViewHeight,            viewModel.miniViewSettings().heightProperty);
        editPropertyWithSlider(sliderMiniViewOpacityPercentage, viewModel.miniViewSettings().opacityPercentageProperty);
        editPropertyWithSlider(sliderWallHeight,                viewModel.maze3DSettings().wallHeightProperty());
        editPropertyWithSlider(sliderWallOpacity,               viewModel.maze3DSettings().wallOpacityProperty());
        editPropertyWithChoiceBox(comboPerspectives,            viewModel.common3DSettings().cameraPerspectiveIDProperty());

        cbUsePlayScene3D.setOnAction(_ -> engineContext.runAction(CommonGameActions.instance().uiSettingsActions().actionTogglePlayScene2D3D()));
        cbWireframeMode .setOnAction(_ -> engineContext.runAction(CommonGameActions.instance().camera3DActions().actionToggleDrawMode()));
    }

    @Override
    public void update(GameEngineContext context) {
        super.update(context);

        final GameViewModel viewModel = context.ui().viewModel();

        comboPerspectives.setValue(viewModel.common3DSettings().cameraPerspectiveIDProperty().get());

        cbUsePlayScene3D.setSelected(viewModel.common3DSettings().view3DEnabledProperty().get());
        cbAxesVisible   .setSelected(viewModel.common3DSettings().axesVisibleProperty().get());
        cbWireframeMode .setSelected(viewModel.common3DSettings().drawModeProperty().get() == DrawMode.LINE);

        // Mini view
        final MiniPlaySceneView miniView = context.ui().viewManager().gamePlayView().layers().miniViewLayer();
        cbMiniViewVisible.setSelected(viewModel.miniViewSettings().activeProperty.getValue());
        sliderMiniViewHeight.setDisable(miniView.isSliding());
    }

    private static SubScene currentSubSceneFX(GameEngineContext context) {
        return context.gameSceneManager().optCurrentGameScene().flatMap(GameScene::optSubSceneFX).orElse(null);
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

    private static String sceneSizeInfo(GameScene gameScene) {
        if (gameScene == null) return NO_INFO;

        if (!(gameScene instanceof AbstractGameScene abstractGameScene)) {
            return NO_INFO;
        }

        final double s = abstractGameScene.view2D().renderingSurface().scaling();
        return "%.0fx%.0f (scaled: %.0fx%.0f)".formatted(
            abstractGameScene.view2D().unscaledWidth(), abstractGameScene.view2D().unscaledHeight(),
            abstractGameScene.view2D().unscaledWidth() * s,  abstractGameScene.view2D().unscaledHeight() * s);
    }
}