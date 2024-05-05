/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.ui.fx.GameSceneContext;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.v3d.scene3d.Perspective;
import de.amr.games.pacman.ui.fx.v3d.scene3d.PlayScene3D;
import javafx.beans.value.ObservableValue;
import javafx.scene.Camera;
import javafx.scene.control.*;
import javafx.scene.shape.DrawMode;

import java.util.ArrayList;

import static de.amr.games.pacman.ui.fx.util.Ufx.toggle;
import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.*;

/**
 * 3D related settings.
 *
 * @author Armin Reichert
 */
public class InfoBox3D extends InfoBox {

    private final ColorPicker pickerLightColor;
    private final ColorPicker pickerFloorColor;
    private final ComboBox<Object> comboFloorTexture;
    private final CheckBox cbFloorTextureRandom;
    private final ComboBox<Perspective> comboPerspectives;
    private final Slider sliderPiPSceneHeight;
    private final Slider sliderPiPOpacity;
    private final CheckBox cbEnergizerExplodes;
    private final Slider sliderWallHeight;
    private final Slider sliderWallOpacity;
    private final Slider sliderWallThickness;
    private final CheckBox cbPacLighted;
    private final CheckBox cbNightMode;
    private final CheckBox cbAxesVisible;
    private final CheckBox cbWireframeMode;

    private final Spinner<Integer> spinnerCamRotate;
    private final Spinner<Integer> spinnerCamX;
    private final Spinner<Integer> spinnerCamY;
    private final Spinner<Integer> spinnerCamZ;

    public InfoBox3D(Theme theme, String title) {
        super(theme, title);

        pickerLightColor = addColorPicker("Light Color", PY_3D_LIGHT_COLOR.get());
        pickerFloorColor = addColorPicker("Floor Color", PY_3D_FLOOR_COLOR.get());
        comboFloorTexture = addComboBox("Floor Texture", floorTextureComboBoxEntries());
        cbFloorTextureRandom = addCheckBox("Random Floor Texture", () -> toggle(PY_3D_FLOOR_TEXTURE_RND));
        comboPerspectives = addComboBox("Perspective", Perspective.values());

        addInfo("Camera", this::currentSceneCameraInfo).available(this::isCurrentGameScene3D);

        // Editors for perspective TOTAL:
        spinnerCamRotate = addSpinner("- Rotate X", -180, 180, TOTAL_ROTATE);
        spinnerCamX      = addSpinner("- Translate X", -1000, 1000, TOTAL_TRANSLATE_X);
        spinnerCamY      = addSpinner("- Translate Y", -1000, 1000, TOTAL_TRANSLATE_Y);
        spinnerCamZ      = addSpinner("- Translate Z", -1000, 1000, TOTAL_TRANSLATE_Z);

        spinnerCamRotate.valueProperty().addListener(this::updatePlayScene3DCamera);
        spinnerCamX.valueProperty().addListener(this::updatePlayScene3DCamera);
        spinnerCamY.valueProperty().addListener(this::updatePlayScene3DCamera);
        spinnerCamZ.valueProperty().addListener(this::updatePlayScene3DCamera);

        sliderPiPSceneHeight = addSlider("PiP Size", PIP_MIN_HEIGHT, PIP_MAX_HEIGHT, PY_PIP_HEIGHT.get());
        sliderPiPOpacity = addSlider("PiP Opacity", 0.0, 1.0, PY_PIP_OPACITY.get());
        sliderWallHeight = addSlider("Wall Height", 1, 9, PY_3D_WALL_HEIGHT.get());
        sliderWallOpacity = addSlider("Wall Opacity", 0.1, 1, PY_3D_WALL_OPACITY.get());
        sliderWallThickness = addSlider("Wall Thickness", 0.1, 2.0, PY_3D_WALL_THICKNESS.get());
        cbEnergizerExplodes = addCheckBox("Energizer Explosion");
        cbNightMode = addCheckBox("Night Mode");
        cbPacLighted = addCheckBox("Pac-Man Lighted");
        cbAxesVisible = addCheckBox("Show Axes");
        cbWireframeMode = addCheckBox("Wireframe Mode");

        pickerLightColor.setOnAction(e -> PY_3D_LIGHT_COLOR.set(pickerLightColor.getValue()));
        pickerFloorColor.setOnAction(e -> PY_3D_FLOOR_COLOR.set(pickerFloorColor.getValue()));
        comboFloorTexture.setOnAction(e -> PY_3D_FLOOR_TEXTURE.set(comboFloorTexture.getValue().toString()));
    }

    @Override
    public void init(GameSceneContext context) {
        super.init(context);

        comboPerspectives.setValue(PY_3D_PERSPECTIVE.get());
        sliderPiPSceneHeight.setValue(PY_PIP_HEIGHT.get());
        sliderPiPOpacity.setValue(PY_PIP_OPACITY.get());
        sliderWallHeight.setValue(PY_3D_WALL_HEIGHT.get());
        sliderWallOpacity.setValue(PY_3D_WALL_OPACITY.get());
        sliderWallThickness.setValue(PY_3D_WALL_THICKNESS.get());

        sliderPiPSceneHeight.valueProperty().addListener((py, ov, nv) -> PY_PIP_HEIGHT.set(sliderPiPSceneHeight.getValue()));
        sliderPiPOpacity.valueProperty().bindBidirectional(PY_PIP_OPACITY);
        sliderWallHeight.valueProperty().bindBidirectional(PY_3D_WALL_HEIGHT);
        sliderWallOpacity.valueProperty().bindBidirectional(PY_3D_WALL_OPACITY);
        sliderWallThickness.valueProperty().bindBidirectional(PY_3D_WALL_THICKNESS);

        comboPerspectives.setOnAction(e -> PY_3D_PERSPECTIVE.set(comboPerspectives.getValue()));
        cbEnergizerExplodes.setOnAction(e -> toggle(PY_3D_ENERGIZER_EXPLODES));
        cbNightMode.setOnAction(e -> toggle(PY_3D_NIGHT_MODE));
        cbPacLighted.setOnAction(e -> toggle(PY_3D_PAC_LIGHT_ENABLED));
        cbAxesVisible.setOnAction(e -> toggle(PY_3D_AXES_VISIBLE));
        cbWireframeMode.setOnAction(e -> actionHandler().toggleDrawMode());
    }

    @Override
    public void update() {
        super.update();
        comboFloorTexture.setValue(PY_3D_FLOOR_TEXTURE.get());
        cbFloorTextureRandom.setSelected(PY_3D_FLOOR_TEXTURE_RND.get());
        comboPerspectives.setValue(PY_3D_PERSPECTIVE.get());
        cbEnergizerExplodes.setSelected(PY_3D_ENERGIZER_EXPLODES.get());
        cbNightMode.setSelected(PY_3D_NIGHT_MODE.get());
        cbPacLighted.setSelected(PY_3D_PAC_LIGHT_ENABLED.get());
        cbAxesVisible.setSelected(PY_3D_AXES_VISIBLE.get());
        cbWireframeMode.setSelected(PY_3D_DRAW_MODE.get() == DrawMode.LINE);

        Perspective perspective = PY_3D_PERSPECTIVE.get();
        spinnerCamRotate.setDisable(perspective != Perspective.TOTAL);
        spinnerCamX.setDisable(perspective != Perspective.TOTAL);
        spinnerCamY.setDisable(perspective != Perspective.TOTAL);
        spinnerCamZ.setDisable(perspective != Perspective.TOTAL);
    }

    private void updatePlayScene3DCamera(ObservableValue<? extends Integer> py, int oldValue, int newValue) {
        context.currentGameScene().ifPresent(gameScene -> {
            if (gameScene instanceof PlayScene3D playScene3D) {
                Camera cam = playScene3D.camera();
                TOTAL_ROTATE = spinnerCamRotate.getValue();
                cam.setRotate(TOTAL_ROTATE);
                TOTAL_TRANSLATE_X = spinnerCamX.getValue();
                cam.setTranslateX(TOTAL_TRANSLATE_X);
                TOTAL_TRANSLATE_Y = spinnerCamY.getValue();
                cam.setTranslateY(TOTAL_TRANSLATE_Y);
                TOTAL_TRANSLATE_Z = spinnerCamZ.getValue();
                cam.setTranslateZ(TOTAL_TRANSLATE_Z);
            }
        });
    }

    private String currentSceneCameraInfo() {
        if (context.currentGameScene().isPresent()
            && context.currentGameScene().get() instanceof PlayScene3D playScene3D) {
            var cam = playScene3D.camera();
            return String.format("rot=%.0f x=%.0f y=%.0f z=%.0f",
                cam.getRotate(), cam.getTranslateX(), cam.getTranslateY(), cam.getTranslateZ());
        }
        return InfoText.NO_INFO;
    }

    private Object[] floorTextureComboBoxEntries() {
        var names = new ArrayList<>();
        names.add(NO_TEXTURE);
        names.addAll(theme.getArray("texture.names"));
        return names.toArray();
    }
}