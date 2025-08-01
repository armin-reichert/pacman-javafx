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
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.shape.DrawMode;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.ACTION_TOGGLE_DRAW_MODE;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.ACTION_TOGGLE_PLAY_SCENE_2D_3D;

/**
 * Infobox with 3D related settings.
 */
public class InfoBox3DSettings extends InfoBox {

    private static final int PIP_MIN_HEIGHT = 200;
    private static final int PIP_MAX_HEIGHT = 600;

    private CheckBox cbUsePlayScene3D;
    private ColorPicker pickerLightColor;
    private ColorPicker pickerFloorColor;
    private ChoiceBox<PerspectiveID> comboPerspectives;
    private CheckBox cbPiPOn;
    private Slider sliderPiPSceneHeight;
    private Slider sliderPiPOpacity;
    private CheckBox cbEnergizerExplodes;
    private Slider sliderWallHeight;
    private Slider sliderWallOpacity;
    private CheckBox cbAxesVisible;
    private CheckBox cbWireframeMode;

    public InfoBox3DSettings(GameUI ui) {
        super(ui);
    }

    @Override
    public void init(GameUI ui) {
        cbUsePlayScene3D     = addCheckBox("3D Play Scene");
        pickerLightColor     = addColorPicker("Light Color", ui.property3DLightColor());
        pickerFloorColor     = addColorPicker("Floor Color", ui.property3DFloorColor());
        comboPerspectives    = addChoiceBox("Perspective", PerspectiveID.values());
        addDynamicLabeledValue("Camera",        this::subSceneCameraInfo);
        addDynamicLabeledValue("Subscene Size", this::subSceneSizeInfo);
        addDynamicLabeledValue("Scene Size",    this::sceneSizeInfo);
        cbPiPOn              = addCheckBox("Picture-In-Picture", ui.propertyMiniViewOn());
        sliderPiPSceneHeight = addSlider("- Height", PIP_MIN_HEIGHT, PIP_MAX_HEIGHT, ui.propertyMiniViewHeight().get(), false, false);
        sliderPiPOpacity     = addSlider("- Opacity", 0, 100, ui.propertyMiniViewOpacityPercent().get(), false, false);
        sliderWallHeight     = addSlider("Obstacle Height", 0, 16, ui.property3DWallHeight().get(), false, false);
        sliderWallOpacity    = addSlider("Wall Opacity", 0, 1, ui.property3DWallOpacity().get(), false, false);
        cbEnergizerExplodes  = addCheckBox("Energizer Explosion", ui.property3DEnergizerExplodes());
        cbAxesVisible        = addCheckBox("Show Axes", ui.property3DAxesVisible());
        cbWireframeMode      = addCheckBox("Wireframe Mode");

        setTooltip(sliderPiPSceneHeight, sliderPiPSceneHeight.valueProperty(), "%.0f px");
        setTooltip(sliderPiPOpacity, sliderPiPOpacity.valueProperty(), "%.0f %%");

        setEditor(sliderPiPSceneHeight, ui.propertyMiniViewHeight());
        setEditor(sliderPiPOpacity, ui.propertyMiniViewOpacityPercent());
        setEditor(sliderWallHeight, ui.property3DWallHeight());
        setEditor(sliderWallOpacity, ui.property3DWallOpacity());
        setEditor(comboPerspectives, ui.property3DPerspective());

        //TODO check these
        cbUsePlayScene3D.setOnAction(e -> ACTION_TOGGLE_PLAY_SCENE_2D_3D.executeIfEnabled(ui));
        cbWireframeMode.setOnAction(e -> ACTION_TOGGLE_DRAW_MODE.executeIfEnabled(ui));
    }

    private void updateControlsFromProperties(GameUI ui) {
        comboPerspectives.setValue(ui.property3DPerspective().get());
        sliderPiPSceneHeight.setValue(ui.propertyMiniViewHeight().get());
        sliderPiPOpacity.setValue(ui.propertyMiniViewOpacityPercent().get());
        sliderWallHeight.setValue(ui.property3DWallHeight().get());
        sliderWallOpacity.setValue(ui.property3DWallOpacity().get());
        cbUsePlayScene3D.setSelected(ui.property3DEnabled().get());
        cbPiPOn.setSelected(ui.propertyMiniViewOn().getValue());
        comboPerspectives.setValue(ui.property3DPerspective().get());
        cbEnergizerExplodes.setSelected(ui.property3DEnergizerExplodes().get());
        cbAxesVisible.setSelected(ui.property3DAxesVisible().get());
        cbWireframeMode.setSelected(ui.property3DDrawMode().get() == DrawMode.LINE);
    }

    @Override
    public void update() {
        super.update();
        //TODO this should not be necessary on every update, when to initialize controls?
        updateControlsFromProperties(ui);
    }

    private String subSceneSizeInfo() {
        if (ui.currentGameScene().isPresent() && ui.currentGameScene().get().optSubScene().isPresent()) {
            SubScene subScene = ui.currentGameScene().get().optSubScene().get();
            return "%.0fx%.0f".formatted(subScene.getWidth(), subScene.getHeight());
        }
        return NO_INFO;
    }

    private String subSceneCameraInfo() {
        if (ui.currentGameScene().isPresent() && ui.currentGameScene().get().optSubScene().isPresent()) {
            SubScene subScene = ui.currentGameScene().get().optSubScene().get();
            var camera = subScene.getCamera();
            return String.format("rot=%.0f x=%.0f y=%.0f z=%.0f",
                    camera.getRotate(), camera.getTranslateX(), camera.getTranslateY(), camera.getTranslateZ());
        }
        return NO_INFO;
    }

    private String sceneSizeInfo() {
        if (ui.currentGameScene().isPresent()) {
            GameScene gameScene = ui.currentGameScene().get();
            if (gameScene instanceof GameScene2D gameScene2D) {
                Vector2f size = gameScene2D.sizeInPx();
                double scaling = gameScene2D.scaling();
                return "%.0fx%.0f (scaled: %.0fx%.0f)".formatted(
                        size.x(), size.y(), size.x() * scaling, size.y() * scaling);
            } else {
                if (ui.theGameContext().optGameLevel().isPresent()) {
                    int width = ui.theGameContext().theGameLevel().worldMap().numCols() * TS;
                    int height = ui.theGameContext().theGameLevel().worldMap().numRows() * TS;
                    return "%dx%d (map size px)".formatted(width, height);
                }
            }
        }
        return NO_INFO;
    }
}