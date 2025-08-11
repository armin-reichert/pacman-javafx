/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._3d.PerspectiveID;
import javafx.scene.SubScene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.shape.DrawMode;

import java.util.Optional;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.GameUI_Properties.*;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.ACTION_TOGGLE_DRAW_MODE;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.ACTION_TOGGLE_PLAY_SCENE_2D_3D;

/**
 * Infobox with 3D related settings.
 */
public class InfoBox3DSettings extends InfoBox {

    private static final int MINI_VIEW_MIN_HEIGHT = 200;
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

    public InfoBox3DSettings(GameUI ui) {
        super(ui);
    }

    @Override
    public void init(GameUI ui) {
        cbUsePlayScene3D = addCheckBox("3D Play Scene");
        addColorPicker("Light Color", PROPERTY_3D_LIGHT_COLOR);
        addColorPicker("Floor Color", PROPERTY_3D_FLOOR_COLOR);
        comboPerspectives = addChoiceBox("Perspective", PerspectiveID.values());
        addDynamicLabeledValue("Camera",         this::subSceneCameraInfo);
        addDynamicLabeledValue("Sub-scene Size", this::subSceneSizeInfo);
        addDynamicLabeledValue("Scene Size",     this::sceneSizeInfo);
        cbMiniViewVisible = addCheckBox("Mini View", PROPERTY_MINI_VIEW_ON);
        sliderMiniViewSceneHeight = addSlider(
            "- Height",
            MINI_VIEW_MIN_HEIGHT, MINI_VIEW_MAX_HEIGHT,
            PROPERTY_MINI_VIEW_HEIGHT.get(),
            false, false);
        sliderMiniViewOpacity = addSlider(
            "- Opacity",
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
        setEditor(comboPerspectives, PROPERTY_3D_PERSPECTIVE);

        cbUsePlayScene3D.setOnAction(e -> ACTION_TOGGLE_PLAY_SCENE_2D_3D.executeIfEnabled(ui));
        cbWireframeMode.setOnAction(e -> ACTION_TOGGLE_DRAW_MODE.executeIfEnabled(ui));
    }

    @Override
    public void update() {
        super.update();
        comboPerspectives.setValue(PROPERTY_3D_PERSPECTIVE.get());
        sliderMiniViewSceneHeight.setValue(PROPERTY_MINI_VIEW_HEIGHT.get());
        sliderMiniViewOpacity.setValue(PROPERTY_MINI_VIEW_OPACITY_PERCENT.get());
        sliderWallHeight.setValue(PROPERTY_3D_WALL_HEIGHT.get());
        sliderWallOpacity.setValue(PROPERTY_3D_WALL_OPACITY.get());
        cbUsePlayScene3D.setSelected(PROPERTY_3D_ENABLED.get());
        cbMiniViewVisible.setSelected(PROPERTY_MINI_VIEW_ON.getValue());
        comboPerspectives.setValue(PROPERTY_3D_PERSPECTIVE.get());
        cbAxesVisible.setSelected(PROPERTY_3D_AXES_VISIBLE.get());
        cbWireframeMode.setSelected(PROPERTY_3D_DRAW_MODE.get() == DrawMode.LINE);
    }

    private Optional<SubScene> optSubScene() {
        return ui.currentGameScene().flatMap(GameScene::optSubScene);
    }

    private String subSceneSizeInfo() {
        return optSubScene().map(ss -> "%.0fx%.0f".formatted(ss.getWidth(), ss.getHeight())).orElse(NO_INFO);
    }

    private String subSceneCameraInfo() {
        return optSubScene()
            .map(ss -> String.format("rot=%.0f x=%.0f y=%.0f z=%.0f",
                ss.getCamera().getRotate(),
                ss.getCamera().getTranslateX(),
                ss.getCamera().getTranslateY(),
                ss.getCamera().getTranslateZ()))
            .orElse(NO_INFO);
    }

    private String sceneSizeInfo() {
        if (ui.currentGameScene().isPresent()) {
            GameScene gameScene = ui.currentGameScene().get();
            if (gameScene instanceof GameScene2D gameScene2D) {
                Vector2f size = gameScene2D.sizeInPx(), scaledSize = size.scaled(gameScene2D.scaling());
                return "%.0fx%.0f (scaled: %.0fx%.0f)".formatted(size.x(), size.y(), scaledSize.x(), scaledSize.y());
            } else if (ui.gameContext().optGameLevel().isPresent()) {
                var worldMap = ui.gameContext().theGameLevel().worldMap();
                return "%dx%d (map size px)".formatted(worldMap.numCols() * TS, worldMap.numRows() * TS);
            }
        }
        return NO_INFO;
    }
}