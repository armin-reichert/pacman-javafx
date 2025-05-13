/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.ui.GameAction;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.dashboard.InfoBox;
import de.amr.pacmanfx.ui.dashboard.InfoText;
import de.amr.pacmanfx.uilib.CameraControlledView;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.shape.DrawMode;

import static de.amr.pacmanfx.ui.PacManGamesEnv.*;

/**
 * 3D related settings.
 *
 * @author Armin Reichert
 */
public class InfoBox3D extends InfoBox {

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
    private CheckBox cbPacLighted;
    private CheckBox cbAxesVisible;
    private CheckBox cbWireframeMode;

    public void init() {
        super.init();

        cbUsePlayScene3D     = addCheckBox("3D Play Scene");
        pickerLightColor     = addColorPicker("Light Color", PY_3D_LIGHT_COLOR.get());
        pickerFloorColor     = addColorPicker("Floor Color", PY_3D_FLOOR_COLOR.get());
        comboPerspectives    = addChoiceBox("Perspective", PerspectiveID.values());
        addLabeledValue("Camera",        this::sceneCameraInfo);
        addLabeledValue("Viewport Size", this::sceneViewportSizeInfo);
        addLabeledValue("Scene Size",    this::sceneSizeInfo);
        cbPiPOn              = addCheckBox("Picture-In-Picture");
        sliderPiPSceneHeight = addSlider("- Height", PIP_MIN_HEIGHT, PIP_MAX_HEIGHT, PY_PIP_HEIGHT.get(), false, false);
        sliderPiPOpacity     = addSlider("- Opacity", 0, 100, PY_PIP_OPACITY_PERCENT.get(), false, false);
        sliderWallHeight     = addSlider("Obstacle Height", 0, 16, PY_3D_WALL_HEIGHT.get(), false, false);
        sliderWallOpacity    = addSlider("Wall Opacity", 0, 1, PY_3D_WALL_OPACITY.get(), false, false);
        cbEnergizerExplodes  = addCheckBox("Energizer Explosion");
        cbPacLighted         = addCheckBox("Pac-Man Lighted");
        cbAxesVisible        = addCheckBox("Show Axes");
        cbWireframeMode      = addCheckBox("Wireframe Mode");

        setTooltip(sliderPiPSceneHeight, sliderPiPSceneHeight.valueProperty(), "%.0f px");
        setTooltip(sliderPiPOpacity, sliderPiPOpacity.valueProperty(), "%.0f %%");

        setEditor(pickerLightColor, PY_3D_LIGHT_COLOR);
        setEditor(pickerFloorColor, PY_3D_FLOOR_COLOR);
        setEditor(pickerLightColor, PY_3D_LIGHT_COLOR);
        setEditor(sliderPiPSceneHeight, PY_PIP_HEIGHT);
        setEditor(sliderPiPOpacity, PY_PIP_OPACITY_PERCENT);
        setEditor(sliderWallHeight, PY_3D_WALL_HEIGHT);
        setEditor(sliderWallOpacity, PY_3D_WALL_OPACITY);
        setEditor(cbPiPOn, PY_PIP_ON);
        setEditor(comboPerspectives, PY_3D_PERSPECTIVE);
        setEditor(cbEnergizerExplodes, PY_3D_ENERGIZER_EXPLODES);
        setEditor(cbPacLighted, PY_3D_PAC_LIGHT_ENABLED);
        setEditor(cbAxesVisible, PY_3D_AXES_VISIBLE);

        //TODO check these
        cbUsePlayScene3D.setOnAction(e -> GameAction.TOGGLE_PLAY_SCENE_2D_3D.execute());
        cbWireframeMode.setOnAction(e -> GameAction.TOGGLE_DRAW_MODE.execute());
    }

    private void updateControlsFromProperties() {
        comboPerspectives.setValue(PY_3D_PERSPECTIVE.get());
        sliderPiPSceneHeight.setValue(PY_PIP_HEIGHT.get());
        sliderPiPOpacity.setValue(PY_PIP_OPACITY_PERCENT.get());
        sliderWallHeight.setValue(PY_3D_WALL_HEIGHT.get());
        sliderWallOpacity.setValue(PY_3D_WALL_OPACITY.get());
        cbUsePlayScene3D.setSelected(PY_3D_ENABLED.get());
        cbPiPOn.setSelected(PY_PIP_ON.getValue());
        comboPerspectives.setValue(PY_3D_PERSPECTIVE.get());
        cbEnergizerExplodes.setSelected(PY_3D_ENERGIZER_EXPLODES.get());
        cbPacLighted.setSelected(PY_3D_PAC_LIGHT_ENABLED.get());
        cbAxesVisible.setSelected(PY_3D_AXES_VISIBLE.get());
        cbWireframeMode.setSelected(PY_3D_DRAW_MODE.get() == DrawMode.LINE);
    }

    @Override
    public void update() {
        super.update();
        //TODO this should not be necessary on every update, when to initialize controls?
        updateControlsFromProperties();
    }

    private String sceneViewportSizeInfo() {
        if (theUI().currentGameScene().isPresent()
            && theUI().currentGameScene().get() instanceof CameraControlledView sgs) {
            return "%.0fx%.0f".formatted(
                sgs.viewPortWidthProperty().get(),
                sgs.viewPortHeightProperty().get()
            );
        }
        return InfoText.NO_INFO;
    }

    private String sceneSizeInfo() {
        if (theUI().currentGameScene().isPresent()) {
            GameScene gameScene = theUI().currentGameScene().get();
            Vector2f size = gameScene.sizeInPx();
            if (gameScene instanceof GameScene2D gameScene2D) {
                double scaling = gameScene2D.scaling();
                return "%.0fx%.0f (scaled: %.0fx%.0f)".formatted(
                        size.x(), size.y(), size.x() * scaling, size.y() * scaling);
            } else {
                return "%.0fx%.0f".formatted(size.x(), size.y());
            }
        }
        return InfoText.NO_INFO;
    }

    private String sceneCameraInfo() {
        if (theUI().currentGameScene().isPresent()
            && theUI().currentGameScene().get() instanceof CameraControlledView scrollableGameScene) {
            var cam = scrollableGameScene.camera();
            return String.format("rot=%.0f x=%.0f y=%.0f z=%.0f",
                cam.getRotate(), cam.getTranslateX(), cam.getTranslateY(), cam.getTranslateZ());
        }
        return InfoText.NO_INFO;
    }
}