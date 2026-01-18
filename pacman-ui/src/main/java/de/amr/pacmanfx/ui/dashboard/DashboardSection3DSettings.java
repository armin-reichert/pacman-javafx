/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._3d.PerspectiveID;
import javafx.scene.SubScene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.shape.DrawMode;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.GameUI.*;
import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_TOGGLE_DRAW_MODE;
import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_TOGGLE_PLAY_SCENE_2D_3D;

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
    private Slider sliderMiniViewOpacity;
    private Slider sliderWallHeight;
    private Slider sliderWallOpacity;
    private CheckBox cbAxesVisible;
    private CheckBox cbWireframeMode;

    public DashboardSection3DSettings(Dashboard dashboard) {
        super(dashboard);
    }

    @Override
    public void init(GameUI ui) {
        cbUsePlayScene3D = addCheckBox("3D Play Scene");
        comboPerspectives = addChoiceBox("Perspective", PerspectiveID.values());
        addColorPicker("Light Color", PROPERTY_3D_LIGHT_COLOR);
        addColorPicker("Floor Color", PROPERTY_3D_FLOOR_COLOR);
        addDynamicLabeledValue("Camera",         () -> subSceneCameraInfo(ui));
        addDynamicLabeledValue("Sub-scene Size", () -> subSceneSizeInfo(ui));
        addDynamicLabeledValue("Scene Size",     () -> sceneSizeInfo(ui));

        cbMiniViewVisible = addCheckBox("Mini View", PROPERTY_MINI_VIEW_ON);
        sliderMiniViewSceneHeight = addSlider(
            " - Height",
            MINI_VIEW_MIN_HEIGHT, MINI_VIEW_MAX_HEIGHT,
            PROPERTY_MINI_VIEW_HEIGHT.get(),
            false, false);
        sliderMiniViewOpacity = addSlider(
            " - Opacity",
            0, 100,
            PROPERTY_MINI_VIEW_OPACITY_PERCENT.get(),
            false, false);

        sliderWallHeight = addSlider(
            "Wall Height",
            0, 16,
            PROPERTY_3D_WALL_HEIGHT.get(),
            false, false);
        sliderWallOpacity = addSlider(
            "Wall Opacity",
            0, 1,
            PROPERTY_3D_WALL_OPACITY.get(),
            false, false);

        cbAxesVisible = addCheckBox("Show Axes", PROPERTY_3D_AXES_VISIBLE);
        cbWireframeMode = addCheckBox("Wireframe Mode");

        setTooltip(sliderMiniViewSceneHeight, sliderMiniViewSceneHeight.valueProperty(), "%.0f px");
        setTooltip(sliderMiniViewOpacity, sliderMiniViewOpacity.valueProperty(), "%.0f %%");

        setEditor(sliderMiniViewSceneHeight, PROPERTY_MINI_VIEW_HEIGHT);
        setEditor(sliderMiniViewOpacity, PROPERTY_MINI_VIEW_OPACITY_PERCENT);
        setEditor(sliderWallHeight, PROPERTY_3D_WALL_HEIGHT);
        setEditor(sliderWallOpacity, PROPERTY_3D_WALL_OPACITY);
        setEditor(comboPerspectives, PROPERTY_3D_PERSPECTIVE_ID);

        cbUsePlayScene3D.setOnAction(_ -> ACTION_TOGGLE_PLAY_SCENE_2D_3D.executeIfEnabled(ui));
        cbWireframeMode.setOnAction(_ -> ACTION_TOGGLE_DRAW_MODE.executeIfEnabled(ui));
    }

    @Override
    public void update(GameUI ui) {
        super.update(ui);

        comboPerspectives.setValue(PROPERTY_3D_PERSPECTIVE_ID.get());
        sliderMiniViewSceneHeight.setValue(PROPERTY_MINI_VIEW_HEIGHT.get());
        sliderMiniViewSceneHeight.setDisable(ui.views().playView().miniView().isMoving());
        sliderMiniViewOpacity.setValue(PROPERTY_MINI_VIEW_OPACITY_PERCENT.get());
        sliderWallHeight.setValue(PROPERTY_3D_WALL_HEIGHT.get());
        sliderWallOpacity.setValue(PROPERTY_3D_WALL_OPACITY.get());
        cbUsePlayScene3D.setSelected(PROPERTY_3D_ENABLED.get());
        cbMiniViewVisible.setSelected(PROPERTY_MINI_VIEW_ON.getValue());
        comboPerspectives.setValue(PROPERTY_3D_PERSPECTIVE_ID.get());
        cbAxesVisible.setSelected(PROPERTY_3D_AXES_VISIBLE.get());
        cbWireframeMode.setSelected(PROPERTY_3D_DRAW_MODE.get() == DrawMode.LINE);
    }

    private String subSceneSizeInfo(GameUI ui) {
        return ui.views().playView().optGameScene()
            .flatMap(GameScene::optSubScene)
            .map(subScene -> "%.0fx%.0f".formatted(subScene.getWidth(), subScene.getHeight()))
            .orElse(NO_INFO);
    }

    private String subSceneCameraInfo(GameUI ui) {
        final GameScene gameScene = ui.views().playView().optGameScene().orElse(null);
        if (gameScene == null) return NO_INFO;
        return gameScene.optSubScene().map(SubScene::getCamera)
            .map(camera -> "rot=%.0f x=%.0f y=%.0f z=%.0f".formatted(
                camera.getRotate(),
                camera.getTranslateX(),
                camera.getTranslateY(),
                camera.getTranslateZ()))
            .orElse(NO_INFO);
    }

    private String sceneSizeInfo(GameUI ui) {
        final Game game = ui.context().currentGame();
        final GameScene gameScene = ui.views().playView().optGameScene().orElse(null);
        if (gameScene == null) return NO_INFO;

        if (gameScene instanceof GameScene2D gameScene2D) {
            final Vector2i size = gameScene2D.unscaledSize();
            final Vector2f scaledSize = size.scaled(gameScene2D.scaling());
            return "%dx%d (scaled: %.0fx%.0f)".formatted(size.x(), size.y(), scaledSize.x(), scaledSize.y());
        }

        if (game.optGameLevel().isPresent()) {
            final WorldMap worldMap = game.level().worldMap();
            return "%dx%d (map size px)".formatted(worldMap.numCols() * TS, worldMap.numRows() * TS);
        }

        return NO_INFO;
    }
}