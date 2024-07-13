/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.dashboard;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.dashboard.InfoBox;
import de.amr.games.pacman.ui2d.dashboard.InfoText;
import de.amr.games.pacman.ui3d.scene.Perspective;
import de.amr.games.pacman.ui3d.scene.PlayScene3D;
import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Map;

import static de.amr.games.pacman.ui2d.util.Ufx.toggle;
import static de.amr.games.pacman.ui3d.PacManGames3dUI.*;

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
    private ComboBox<Object> comboFloorTexture;
    private ComboBox<Perspective> comboPerspectives;
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
        this.context = context;

        cbUsePlayScene3D = checkBox("3D Play Scene");
        pickerLightColor = addColorPickerRow("Light Color", PY_3D_LIGHT_COLOR.get());
        pickerFloorColor = addColorPickerRow("Floor Color", PY_3D_FLOOR_COLOR.get());
        comboFloorTexture = addComboBoxRow("Floor Texture", floorTextureComboBoxEntries());
        comboPerspectives = addComboBoxRow("Perspective", Perspective.values());

        addTextRow("Camera", this::currentSceneCameraInfo); //TODO .available(this::isCurrentGameScene3D);

        cbPiPOn              = checkBox("Picture-In-Picture");
        sliderPiPSceneHeight = addSliderRow("- Height", PIP_MIN_HEIGHT, PIP_MAX_HEIGHT, PY_PIP_HEIGHT.get());
        sliderPiPOpacity     = addSliderRow("- Opacity", 0, 100, PY_PIP_OPACITY_PERCENT.get());
        sliderWallHeight     = addSliderRow("Obstacle Height", 0, 16, PY_3D_WALL_HEIGHT.get());
        sliderWallOpacity    = addSliderRow("Wall Opacity", 0, 1, PY_3D_WALL_OPACITY.get());
        cbEnergizerExplodes  = checkBox("Energizer Explosion");
        cbNightMode          = checkBox("Night Mode");
        cbPacLighted         = checkBox("Pac-Man Lighted");
        cbAxesVisible        = checkBox("Show Axes");
        cbWireframeMode      = checkBox("Wireframe Mode");

        {
            var tooltip = new Tooltip();
            tooltip.setShowDelay(Duration.millis(100));
            tooltip.textProperty().bind(Bindings.createStringBinding(
                () -> String.format("%.0f px", sliderPiPSceneHeight.getValue()),
                sliderPiPSceneHeight.valueProperty())
            );
            sliderPiPSceneHeight.setTooltip(tooltip);
        }
        {
            var tooltip = new Tooltip();
            tooltip.setShowDelay(Duration.millis(100));
            tooltip.textProperty().bind(Bindings.createStringBinding(
                () -> String.format("%.0f %%", sliderPiPOpacity.getValue()),
                sliderPiPOpacity.valueProperty())
            );
            sliderPiPOpacity.setTooltip(tooltip);
        }

        cbUsePlayScene3D.setOnAction(e -> context.actionHandler().toggle2D3D());

        pickerLightColor.setOnAction(e -> PY_3D_LIGHT_COLOR.set(pickerLightColor.getValue()));
        pickerFloorColor.setOnAction(e -> PY_3D_FLOOR_COLOR.set(pickerFloorColor.getValue()));
        comboFloorTexture.setOnAction(e -> PY_3D_FLOOR_TEXTURE.set(comboFloorTexture.getValue().toString()));

        comboPerspectives.setValue(PY_3D_PERSPECTIVE.get());
        sliderPiPSceneHeight.setValue(PY_PIP_HEIGHT.get());
        sliderPiPOpacity.setValue(PY_PIP_OPACITY_PERCENT.get());
        sliderWallHeight.setValue(PY_3D_WALL_HEIGHT.get());
        sliderWallOpacity.setValue(PY_3D_WALL_OPACITY.get());

        sliderPiPSceneHeight.valueProperty().addListener((py, ov, nv) -> PY_PIP_HEIGHT.set((int) sliderPiPSceneHeight.getValue()));
        sliderPiPOpacity.valueProperty().bindBidirectional(PY_PIP_OPACITY_PERCENT);
        sliderWallHeight.valueProperty().bindBidirectional(PY_3D_WALL_HEIGHT);
        sliderWallOpacity.valueProperty().bindBidirectional(PY_3D_WALL_OPACITY);

        cbPiPOn.setOnAction(e -> toggle(PY_PIP_ON));
        comboPerspectives.setOnAction(e -> PY_3D_PERSPECTIVE.set(comboPerspectives.getValue()));
        cbEnergizerExplodes.setOnAction(e -> toggle(PY_3D_ENERGIZER_EXPLODES));
        cbNightMode.setOnAction(e -> toggle(PY_3D_NIGHT_MODE));
        cbPacLighted.setOnAction(e -> toggle(PY_3D_PAC_LIGHT_ENABLED));
        cbAxesVisible.setOnAction(e -> toggle(PY_3D_AXES_VISIBLE));
        cbWireframeMode.setOnAction(e -> context.actionHandler().toggleDrawMode());
    }

    @Override
    public void update() {
        super.update();
        cbUsePlayScene3D.setSelected(PY_3D_ENABLED.get());
        cbPiPOn.setSelected(PY_PIP_ON.getValue());
        comboFloorTexture.setValue(PY_3D_FLOOR_TEXTURE.get());
        comboPerspectives.setValue(PY_3D_PERSPECTIVE.get());
        cbEnergizerExplodes.setSelected(PY_3D_ENERGIZER_EXPLODES.get());
        cbNightMode.setSelected(PY_3D_NIGHT_MODE.get());
        cbPacLighted.setSelected(PY_3D_PAC_LIGHT_ENABLED.get());
        cbAxesVisible.setSelected(PY_3D_AXES_VISIBLE.get());
        cbWireframeMode.setSelected(PY_3D_DRAW_MODE.get() == DrawMode.LINE);
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
        Map<String, PhongMaterial> texturesByName = context.theme().get("floorTextures");
        var names = new ArrayList<>();
        names.add(NO_TEXTURE);
        names.addAll(texturesByName.keySet());
        return names.toArray();
    }
}