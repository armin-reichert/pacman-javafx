/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D;
import de.amr.pacmanfx.ui.config.UISettings3DProperties;
import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.config.UISettingsProperties;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
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
        final UISettings3DProperties settings3D = game.ui().settings().d3();

        cbUsePlayScene3D = addCheckBox("3D Play Scene");
        comboPerspectives = addChoiceBox("Perspective", PerspectiveID.values());
        addColorPicker("Light Color", settings3D.mazeLightColorProperty());
        addColorPicker("Floor Color", settings3D.mazeFloorColorProperty());
        addDynamicLabeledValue("Camera",         () -> subSceneCameraInfo(game));
        addDynamicLabeledValue("Sub-scene Size", () -> subSceneSizeInfo(game));
        addDynamicLabeledValue("Scene Size",     () -> sceneSizeInfo(game));

        cbMiniViewVisible = addCheckBox("Mini View", game.ui().settings().miniView().activeProperty());

        sliderMiniViewSceneHeight = addSlider(
            " - Height",
            MINI_VIEW_MIN_HEIGHT, MINI_VIEW_MAX_HEIGHT,
            game.ui().settings().miniView().heightProperty().get(),
            false, false);

        sliderMiniViewOpacityPercentage = addSlider(
            " - Opacity",
            0, 100,
            game.ui().settings().miniView().opacityPercentageProperty().get(),
            false, false);

        sliderWallHeight = addSlider(
            "Wall Height",
            0, 16,
            settings3D.mazeWallHeightProperty().get(),
            false, false);

        sliderWallOpacity = addSlider(
            "Wall Opacity",
            0, 1,
            settings3D.mazeWallOpacityProperty().get(),
            false, false);

        cbAxesVisible = addCheckBox("Show Axes", settings3D.axesVisibleProperty());
        cbWireframeMode = addCheckBox("Wireframe Mode");

        setTooltip(sliderMiniViewSceneHeight, sliderMiniViewSceneHeight.valueProperty(), "%.0f px");
        setTooltip(sliderMiniViewOpacityPercentage, sliderMiniViewOpacityPercentage.valueProperty(), "%.0f %%");

        setTooltip(sliderWallHeight, sliderWallHeight.valueProperty(), "%.0f px");
        setTooltip(sliderWallOpacity, sliderWallOpacity.valueProperty().multiply(100), "%.0f %%");

        setEditor(sliderMiniViewSceneHeight, game.ui().settings().miniView().heightProperty());
        setEditor(sliderMiniViewOpacityPercentage, game.ui().settings().miniView().opacityPercentageProperty());
        setEditor(sliderWallHeight, settings3D.mazeWallHeightProperty());
        setEditor(sliderWallOpacity, settings3D.mazeWallOpacityProperty());
        setEditor(comboPerspectives, settings3D.cameraPerspectiveIdProperty());

        cbUsePlayScene3D.setOnAction(_ -> game.actions().uiSettingsActions().actionTogglePlayScene2D3D().execute());
        cbWireframeMode.setOnAction(_ -> game.actions().camera3DActions().actionToggleDrawMode().execute());
    }

    @Override
    public void update() {
        super.update();

        final UISettingsProperties settings = game().ui().settings();
        final UISettings3DProperties settings3D = game().ui().settings().d3();

        comboPerspectives.setValue(settings3D.cameraPerspectiveIdProperty().get());
        sliderMiniViewSceneHeight.setValue(settings.miniView().heightProperty().get());
        if (dashboard.game() != null) {
            sliderMiniViewSceneHeight.setDisable(dashboard.game().ui().views().gamePlayView().miniPlaySceneView().isMoving());
        }
        sliderMiniViewOpacityPercentage.setValue(settings.miniView().opacityPercentageProperty().get());
        sliderWallHeight.setValue(settings3D.mazeWallHeightProperty().get());
        sliderWallOpacity.setValue(settings3D.mazeWallOpacityProperty().get());
        cbUsePlayScene3D.setSelected(settings3D.view3DEnabledProperty().get());
        cbMiniViewVisible.setSelected(settings.miniView().activeProperty().getValue());
        comboPerspectives.setValue(settings3D.cameraPerspectiveIdProperty().get());
        cbAxesVisible.setSelected(settings3D.axesVisibleProperty().get());
        cbWireframeMode.setSelected(settings3D.drawModeProperty().get() == DrawMode.LINE);
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