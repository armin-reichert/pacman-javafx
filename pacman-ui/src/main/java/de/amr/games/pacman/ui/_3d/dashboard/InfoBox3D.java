/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._3d.dashboard;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.ui.CameraControlledView;
import de.amr.games.pacman.ui.GameScene;
import de.amr.games.pacman.ui._2d.GameActions;
import de.amr.games.pacman.ui._2d.GameScene2D;
import de.amr.games.pacman.ui._3d.GlobalProperties3d;
import de.amr.games.pacman.ui._3d.scene3d.Perspective;
import de.amr.games.pacman.ui.dashboard.InfoBox;
import de.amr.games.pacman.ui.dashboard.InfoText;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.shape.DrawMode;

import static de.amr.games.pacman.ui.Globals.THE_UI;
import static de.amr.games.pacman.ui._2d.GlobalProperties2d.*;

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
    private ComboBox<Perspective.Name> comboPerspectives;
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
        pickerLightColor     = addColorPicker("Light Color", GlobalProperties3d.PY_3D_LIGHT_COLOR.get());
        pickerFloorColor     = addColorPicker("Floor Color", GlobalProperties3d.PY_3D_FLOOR_COLOR.get());
        comboPerspectives    = addComboBox("Perspective", Perspective.Name.values());
        addLabeledValue("Camera",        this::sceneCameraInfo);
        addLabeledValue("Viewport Size", this::sceneViewportSizeInfo);
        addLabeledValue("Scene Size",    this::sceneSizeInfo);
        cbPiPOn              = addCheckBox("Picture-In-Picture");
        sliderPiPSceneHeight = addSlider("- Height", PIP_MIN_HEIGHT, PIP_MAX_HEIGHT, PY_PIP_HEIGHT.get(), false, false);
        sliderPiPOpacity     = addSlider("- Opacity", 0, 100, PY_PIP_OPACITY_PERCENT.get(), false, false);
        sliderWallHeight     = addSlider("Obstacle Height", 0, 16, GlobalProperties3d.PY_3D_WALL_HEIGHT.get(), false, false);
        sliderWallOpacity    = addSlider("Wall Opacity", 0, 1, GlobalProperties3d.PY_3D_WALL_OPACITY.get(), false, false);
        cbEnergizerExplodes  = addCheckBox("Energizer Explosion");
        cbPacLighted         = addCheckBox("Pac-Man Lighted");
        cbAxesVisible        = addCheckBox("Show Axes");
        cbWireframeMode      = addCheckBox("Wireframe Mode");

        setTooltip(sliderPiPSceneHeight, sliderPiPSceneHeight.valueProperty(), "%.0f px");
        setTooltip(sliderPiPOpacity, sliderPiPOpacity.valueProperty(), "%.0f %%");

        setEditor(pickerLightColor, GlobalProperties3d.PY_3D_LIGHT_COLOR);
        setEditor(pickerFloorColor, GlobalProperties3d.PY_3D_FLOOR_COLOR);
        setEditor(pickerLightColor, GlobalProperties3d.PY_3D_LIGHT_COLOR);
        setEditor(sliderPiPSceneHeight, PY_PIP_HEIGHT);
        setEditor(sliderPiPOpacity, PY_PIP_OPACITY_PERCENT);
        setEditor(sliderWallHeight, GlobalProperties3d.PY_3D_WALL_HEIGHT);
        setEditor(sliderWallOpacity, GlobalProperties3d.PY_3D_WALL_OPACITY);
        setEditor(cbPiPOn, PY_PIP_ON);
        setEditor(comboPerspectives, GlobalProperties3d.PY_3D_PERSPECTIVE);
        setEditor(cbEnergizerExplodes, GlobalProperties3d.PY_3D_ENERGIZER_EXPLODES);
        setEditor(cbPacLighted, GlobalProperties3d.PY_3D_PAC_LIGHT_ENABLED);
        setEditor(cbAxesVisible, GlobalProperties3d.PY_3D_AXES_VISIBLE);

        //TODO check these
        cbUsePlayScene3D.setOnAction(e -> GameActions.TOGGLE_PLAY_SCENE_2D_3D.execute());
        cbWireframeMode.setOnAction(e -> GameActions.TOGGLE_DRAW_MODE.execute());
    }

    private void updateControlsFromProperties() {
        comboPerspectives.setValue(GlobalProperties3d.PY_3D_PERSPECTIVE.get());
        sliderPiPSceneHeight.setValue(PY_PIP_HEIGHT.get());
        sliderPiPOpacity.setValue(PY_PIP_OPACITY_PERCENT.get());
        sliderWallHeight.setValue(GlobalProperties3d.PY_3D_WALL_HEIGHT.get());
        sliderWallOpacity.setValue(GlobalProperties3d.PY_3D_WALL_OPACITY.get());
        cbUsePlayScene3D.setSelected(GlobalProperties3d.PY_3D_ENABLED.get());
        cbPiPOn.setSelected(PY_PIP_ON.getValue());
        comboPerspectives.setValue(GlobalProperties3d.PY_3D_PERSPECTIVE.get());
        cbEnergizerExplodes.setSelected(GlobalProperties3d.PY_3D_ENERGIZER_EXPLODES.get());
        cbPacLighted.setSelected(GlobalProperties3d.PY_3D_PAC_LIGHT_ENABLED.get());
        cbAxesVisible.setSelected(GlobalProperties3d.PY_3D_AXES_VISIBLE.get());
        cbWireframeMode.setSelected(GlobalProperties3d.PY_3D_DRAW_MODE.get() == DrawMode.LINE);
    }

    @Override
    public void update() {
        super.update();
        //TODO this should not be necessary on every update, when to initialize controls?
        updateControlsFromProperties();
    }

    private String sceneViewportSizeInfo() {
        if (THE_UI.currentGameScene().isPresent()
            && THE_UI.currentGameScene().get() instanceof CameraControlledView sgs) {
            return "%.0fx%.0f".formatted(
                sgs.viewPortWidthProperty().get(),
                sgs.viewPortHeightProperty().get()
            );
        }
        return InfoText.NO_INFO;
    }

    private String sceneSizeInfo() {
        if (THE_UI.currentGameScene().isPresent()) {
            GameScene gameScene = THE_UI.currentGameScene().get();
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
        if (THE_UI.currentGameScene().isPresent()
            && THE_UI.currentGameScene().get() instanceof CameraControlledView scrollableGameScene) {
            var cam = scrollableGameScene.camera();
            return String.format("rot=%.0f x=%.0f y=%.0f z=%.0f",
                cam.getRotate(), cam.getTranslateX(), cam.getTranslateY(), cam.getTranslateZ());
        }
        return InfoText.NO_INFO;
    }
}