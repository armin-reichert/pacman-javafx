/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.subviews.dashboard;

import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.ui.game.GameConstants;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.gamescene.GameScene;
import javafx.scene.SubScene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.shape.DrawMode;

import static de.amr.pacmanfx.core.Globals.TS;
import static de.amr.pacmanfx.ui.action.CommonActions.ACTION_TOGGLE_DRAW_MODE;
import static de.amr.pacmanfx.ui.action.CommonActions.ACTION_TOGGLE_PLAY_SCENE_2D_3D;

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
    public void connect(Game context) {
        cbUsePlayScene3D = addCheckBox("3D Play Scene");
        comboPerspectives = addChoiceBox("Perspective", PerspectiveID.values());
        addColorPicker("Light Color", GameConstants.PROPERTY_3D_LIGHT_COLOR);
        addColorPicker("Floor Color", GameConstants.PROPERTY_3D_FLOOR_COLOR);
        addDynamicLabeledValue("Camera",         () -> subSceneCameraInfo(context));
        addDynamicLabeledValue("Sub-scene Size", () -> subSceneSizeInfo(context));
        addDynamicLabeledValue("Scene Size",     () -> sceneSizeInfo(context));

        cbMiniViewVisible = addCheckBox("Mini View", GameConstants.PROPERTY_MINI_VIEW_ON);

        sliderMiniViewSceneHeight = addSlider(
            " - Height",
            MINI_VIEW_MIN_HEIGHT, MINI_VIEW_MAX_HEIGHT,
            GameConstants.PROPERTY_MINI_VIEW_HEIGHT.get(),
            false, false);

        sliderMiniViewOpacityPercentage = addSlider(
            " - Opacity",
            0, 100,
            GameConstants.PROPERTY_MINI_VIEW_OPACITY_PERCENT.get(),
            false, false);

        sliderWallHeight = addSlider(
            "Wall Height",
            0, 16,
            GameConstants.PROPERTY_3D_WALL_HEIGHT.get(),
            false, false);

        sliderWallOpacity = addSlider(
            "Wall Opacity",
            0, 1,
            GameConstants.PROPERTY_3D_WALL_OPACITY.get(),
            false, false);

        cbAxesVisible = addCheckBox("Show Axes", GameConstants.PROPERTY_3D_AXES_VISIBLE);
        cbWireframeMode = addCheckBox("Wireframe Mode");

        setTooltip(sliderMiniViewSceneHeight, sliderMiniViewSceneHeight.valueProperty(), "%.0f px");
        setTooltip(sliderMiniViewOpacityPercentage, sliderMiniViewOpacityPercentage.valueProperty(), "%.0f %%");

        setTooltip(sliderWallHeight, sliderWallHeight.valueProperty(), "%.0f px");
        setTooltip(sliderWallOpacity, sliderWallOpacity.valueProperty().multiply(100), "%.0f %%");

        setEditor(sliderMiniViewSceneHeight, GameConstants.PROPERTY_MINI_VIEW_HEIGHT);
        setEditor(sliderMiniViewOpacityPercentage, GameConstants.PROPERTY_MINI_VIEW_OPACITY_PERCENT);
        setEditor(sliderWallHeight, GameConstants.PROPERTY_3D_WALL_HEIGHT);
        setEditor(sliderWallOpacity, GameConstants.PROPERTY_3D_WALL_OPACITY);
        setEditor(comboPerspectives, GameConstants.PROPERTY_3D_PERSPECTIVE_ID);

        cbUsePlayScene3D.setOnAction(_ -> ACTION_TOGGLE_PLAY_SCENE_2D_3D.executeIfEnabled(context));
        cbWireframeMode.setOnAction(_ -> ACTION_TOGGLE_DRAW_MODE.executeIfEnabled(context));
    }

    @Override
    public void update() {
        super.update();

        comboPerspectives.setValue(GameConstants.PROPERTY_3D_PERSPECTIVE_ID.get());
        sliderMiniViewSceneHeight.setValue(GameConstants.PROPERTY_MINI_VIEW_HEIGHT.get());
        if (dashboard.game() != null) {
            sliderMiniViewSceneHeight.setDisable(dashboard.game().ui().subViews().gamePlayView().miniPlaySceneView().isMoving());
        }
        sliderMiniViewOpacityPercentage.setValue(GameConstants.PROPERTY_MINI_VIEW_OPACITY_PERCENT.get());
        sliderWallHeight.setValue(GameConstants.PROPERTY_3D_WALL_HEIGHT.get());
        sliderWallOpacity.setValue(GameConstants.PROPERTY_3D_WALL_OPACITY.get());
        cbUsePlayScene3D.setSelected(GameConstants.PROPERTY_3D_ENABLED.get());
        cbMiniViewVisible.setSelected(GameConstants.PROPERTY_MINI_VIEW_ON.getValue());
        comboPerspectives.setValue(GameConstants.PROPERTY_3D_PERSPECTIVE_ID.get());
        cbAxesVisible.setSelected(GameConstants.PROPERTY_3D_AXES_VISIBLE.get());
        cbWireframeMode.setSelected(GameConstants.PROPERTY_3D_DRAW_MODE.get() == DrawMode.LINE);
    }

    private String subSceneSizeInfo(Game context) {
        return context.ui().gameScenes().optCurrentGameScene()
            .flatMap(GameScene::optSubSceneFX)
            .map(subScene -> "%.0fx%.0f".formatted(subScene.getWidth(), subScene.getHeight()))
            .orElse(NO_INFO);
    }

    private String subSceneCameraInfo(Game context) {
        final GameScene gameScene = context.ui().gameScenes().optCurrentGameScene().orElse(null);
        if (gameScene == null) return NO_INFO;
        return gameScene.optSubSceneFX().map(SubScene::getCamera)
            .map(camera -> "rot=%.0f x=%.0f y=%.0f z=%.0f".formatted(
                camera.getRotate(),
                camera.getTranslateX(),
                camera.getTranslateY(),
                camera.getTranslateZ()))
            .orElse(NO_INFO);
    }

    private String sceneSizeInfo(Game context) {
        final GameModel game = context.currentGameContext().model();
        final GameScene gameScene = context.ui().gameScenes().optCurrentGameScene().orElse(null);
        if (gameScene == null) return NO_INFO;

        if (gameScene instanceof GameScene2D gameScene2D) {
            return "%dx%d (scaled: %.0fx%.0f)".formatted(
                gameScene2D.unscaledWidth(), gameScene2D.unscaledHeight(),
                gameScene2D.width(), gameScene2D.height());
        }

        if (game.optGameLevel().isPresent()) {
            final WorldMap worldMap = game.optGameLevel().get().worldMap();
            return "%dx%d (map size px)".formatted(worldMap.numCols() * TS, worldMap.numRows() * TS);
        }

        return NO_INFO;
    }
}