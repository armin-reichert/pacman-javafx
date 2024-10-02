/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.dashboard;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.dashboard.InfoBox;
import de.amr.games.pacman.ui2d.dashboard.InfoText;
import de.amr.games.pacman.ui3d.GameAction3D;
import de.amr.games.pacman.ui3d.GameAssets3D;
import de.amr.games.pacman.ui3d.Perspective;
import de.amr.games.pacman.ui3d.PlayScene3D;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;

import java.util.ArrayList;
import java.util.Map;

import static de.amr.games.pacman.ui2d.PacManGames2dApp.*;
import static de.amr.games.pacman.ui3d.PacManGames3dApp.*;

/**
 * 3D related settings.
 *
 * @author Armin Reichert
 */
public class InfoBox3D extends InfoBox {

    private static final int PIP_MIN_HEIGHT = GameModel.ARCADE_MAP_SIZE_Y * 3 / 4;
    private static final int PIP_MAX_HEIGHT = GameModel.ARCADE_MAP_SIZE_Y * 2;

    private CheckBox cbUsePlayScene3D;
    private ColorPicker pickerLightColor;
    private ColorPicker pickerFloorColor;
    private ComboBox<String> comboFloorTexture;
    private ComboBox<Perspective.Name> comboPerspectives;
    private CheckBox cbPiPOn;
    private Slider sliderPiPSceneHeight;
    private Slider sliderPiPOpacity;
    private CheckBox cbEnergizerExplodes;
    private Slider sliderWallHeight;
    private Slider sliderWallOpacity;
    private CheckBox cbPacLighted;
    private CheckBox cbNightMode;
    private CheckBox cbAxesVisible;
    private CheckBox cbWireframeMode;

    public void init(GameContext context) {
        super.init(context);

        cbUsePlayScene3D     = checkBox("3D Play Scene");
        pickerLightColor     = colorPicker("Light Color", PY_3D_LIGHT_COLOR.get());
        pickerFloorColor     = colorPicker("Floor Color", PY_3D_FLOOR_COLOR.get());
        comboFloorTexture    = comboBox("Floor Texture", floorTextureComboBoxEntries());
        comboPerspectives    = comboBox("Perspective", Perspective.Name.values());
        labelledValue("Camera", this::currentSceneCameraInfo);
        cbPiPOn              = checkBox("Picture-In-Picture");
        sliderPiPSceneHeight = slider("- Height", PIP_MIN_HEIGHT, PIP_MAX_HEIGHT, PY_PIP_HEIGHT.get(), false, false);
        sliderPiPOpacity     = slider("- Opacity", 0, 100, PY_PIP_OPACITY_PERCENT.get(), false, false);
        sliderWallHeight     = slider("Obstacle Height", 0, 16, PY_3D_WALL_HEIGHT.get(), false, false);
        sliderWallOpacity    = slider("Wall Opacity", 0, 1, PY_3D_WALL_OPACITY.get(), false, false);
        cbEnergizerExplodes  = checkBox("Energizer Explosion");
        cbNightMode          = checkBox("Night Mode");
        cbPacLighted         = checkBox("Pac-Man Lighted");
        cbAxesVisible        = checkBox("Show Axes");
        cbWireframeMode      = checkBox("Wireframe Mode");

        setTooltip(sliderPiPSceneHeight, sliderPiPSceneHeight.valueProperty(), "%.0f px");
        setTooltip(sliderPiPOpacity, sliderPiPOpacity.valueProperty(), "%.0f %%");

        assignEditor(pickerLightColor, PY_3D_LIGHT_COLOR);
        assignEditor(pickerFloorColor, PY_3D_FLOOR_COLOR);
        assignEditor(pickerLightColor, PY_3D_LIGHT_COLOR);
        assignEditor(comboFloorTexture, PY_3D_FLOOR_TEXTURE);
        assignEditor(sliderPiPSceneHeight, PY_PIP_HEIGHT);
        assignEditor(sliderPiPOpacity, PY_PIP_OPACITY_PERCENT);
        assignEditor(sliderWallHeight, PY_3D_WALL_HEIGHT);
        assignEditor(sliderWallOpacity, PY_3D_WALL_OPACITY);
        assignEditor(cbPiPOn, PY_PIP_ON);
        assignEditor(comboPerspectives, PY_3D_PERSPECTIVE);
        assignEditor(cbEnergizerExplodes, PY_3D_ENERGIZER_EXPLODES);
        assignEditor(cbNightMode, PY_NIGHT_MODE);
        assignEditor(cbPacLighted, PY_3D_PAC_LIGHT_ENABLED);
        assignEditor(cbAxesVisible, PY_3D_AXES_VISIBLE);

        //TODO check these
        cbUsePlayScene3D.setOnAction(e -> GameAction3D.TOGGLE_PLAY_SCENE_2D_3D.execute(context));
        cbWireframeMode.setOnAction(e -> GameAction3D.TOGGLE_DRAW_MODE.execute(context));
    }

    private void updateControlsFromProperties() {
        comboPerspectives.setValue(PY_3D_PERSPECTIVE.get());
        sliderPiPSceneHeight.setValue(PY_PIP_HEIGHT.get());
        sliderPiPOpacity.setValue(PY_PIP_OPACITY_PERCENT.get());
        sliderWallHeight.setValue(PY_3D_WALL_HEIGHT.get());
        sliderWallOpacity.setValue(PY_3D_WALL_OPACITY.get());
        cbUsePlayScene3D.setSelected(PY_3D_ENABLED.get());
        cbPiPOn.setSelected(PY_PIP_ON.getValue());
        comboFloorTexture.setValue(PY_3D_FLOOR_TEXTURE.get());
        comboPerspectives.setValue(PY_3D_PERSPECTIVE.get());
        cbEnergizerExplodes.setSelected(PY_3D_ENERGIZER_EXPLODES.get());
        cbNightMode.setSelected(PY_NIGHT_MODE.get());
        cbPacLighted.setSelected(PY_3D_PAC_LIGHT_ENABLED.get());
        cbAxesVisible.setSelected(PY_3D_AXES_VISIBLE.get());
        cbWireframeMode.setSelected(PY_3D_DRAW_MODE.get() == DrawMode.LINE);
    }

    @Override
    public void update() {
        //TODO this should not be necessary on every update, when to initialize controls?
        updateControlsFromProperties();
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

    private String[] floorTextureComboBoxEntries() {
        Map<String, PhongMaterial> texturesByName = context.assets().get("floor_textures");
        var names = new ArrayList<String>();
        names.add(GameAssets3D.NO_TEXTURE);
        names.addAll(texturesByName.keySet());
        return names.toArray(String[]::new);
    }
}