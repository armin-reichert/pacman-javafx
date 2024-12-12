/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.dashboard;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.dashboard.InfoBox;
import de.amr.games.pacman.ui2d.dashboard.InfoText;
import de.amr.games.pacman.ui2d.scene.common.CameraControlledView;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui2d.util.NightMode;
import de.amr.games.pacman.ui3d.GameActions3D;
import de.amr.games.pacman.ui3d.PacManGamesUI_3D;
import de.amr.games.pacman.ui3d.scene.common.Perspective;
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

    private static final int PIP_MIN_HEIGHT = 200;
    private static final int PIP_MAX_HEIGHT = 600;

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
    private ComboBox<NightMode> comboNightMode;
    private CheckBox cbAxesVisible;
    private CheckBox cbWireframeMode;

    public void init(GameContext context) {
        super.init(context);

        cbUsePlayScene3D     = addCheckBox("3D Play Scene");
        pickerLightColor     = addColorPicker("Light Color", PY_3D_LIGHT_COLOR.get());
        pickerFloorColor     = addColorPicker("Floor Color", PY_3D_FLOOR_COLOR.get());
        comboFloorTexture    = addComboBox("Floor Texture", floorTextureComboBoxEntries());
        comboPerspectives    = addComboBox("Perspective", Perspective.Name.values());
        addLabeledValue("Camera",        this::sceneCameraInfo);
        addLabeledValue("Viewport Size", this::sceneViewportSizeInfo);
        addLabeledValue("Scene Size",    this::sceneSizeInfo);
        cbPiPOn              = addCheckBox("Picture-In-Picture");
        sliderPiPSceneHeight = adddSlider("- Height", PIP_MIN_HEIGHT, PIP_MAX_HEIGHT, PY_PIP_HEIGHT.get(), false, false);
        sliderPiPOpacity     = adddSlider("- Opacity", 0, 100, PY_PIP_OPACITY_PERCENT.get(), false, false);
        sliderWallHeight     = adddSlider("Obstacle Height", 0, 16, PY_3D_WALL_HEIGHT.get(), false, false);
        sliderWallOpacity    = adddSlider("Wall Opacity", 0, 1, PY_3D_WALL_OPACITY.get(), false, false);
        cbEnergizerExplodes  = addCheckBox("Energizer Explosion");
        comboNightMode       = addComboBox("Night Mode", NightMode.values());
        cbPacLighted         = addCheckBox("Pac-Man Lighted");
        cbAxesVisible        = addCheckBox("Show Axes");
        cbWireframeMode      = addCheckBox("Wireframe Mode");

        setTooltip(sliderPiPSceneHeight, sliderPiPSceneHeight.valueProperty(), "%.0f px");
        setTooltip(sliderPiPOpacity, sliderPiPOpacity.valueProperty(), "%.0f %%");

        setEditor(pickerLightColor, PY_3D_LIGHT_COLOR);
        setEditor(pickerFloorColor, PY_3D_FLOOR_COLOR);
        setEditor(pickerLightColor, PY_3D_LIGHT_COLOR);
        setEditor(comboFloorTexture, PY_3D_FLOOR_TEXTURE);
        setEditor(sliderPiPSceneHeight, PY_PIP_HEIGHT);
        setEditor(sliderPiPOpacity, PY_PIP_OPACITY_PERCENT);
        setEditor(sliderWallHeight, PY_3D_WALL_HEIGHT);
        setEditor(sliderWallOpacity, PY_3D_WALL_OPACITY);
        setEditor(cbPiPOn, PY_PIP_ON);
        setEditor(comboPerspectives, PY_3D_PERSPECTIVE);
        setEditor(cbEnergizerExplodes, PY_3D_ENERGIZER_EXPLODES);
        setEditor(comboNightMode, PY_NIGHT_MODE);
        setEditor(cbPacLighted, PY_3D_PAC_LIGHT_ENABLED);
        setEditor(cbAxesVisible, PY_3D_AXES_VISIBLE);

        //TODO check these
        cbUsePlayScene3D.setOnAction(e -> GameActions3D.TOGGLE_PLAY_SCENE_2D_3D.execute(context));
        cbWireframeMode.setOnAction(e -> GameActions3D.TOGGLE_DRAW_MODE.execute(context));
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
        comboNightMode.setValue(PY_NIGHT_MODE.get());
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
        if (context.currentGameScene().isPresent()
            && context.currentGameScene().get() instanceof CameraControlledView sgs) {
            return "%.0fx%.0f".formatted(
                sgs.viewPortWidthProperty().get(),
                sgs.viewPortHeightProperty().get()
            );
        }
        return InfoText.NO_INFO;
    }

    private String sceneSizeInfo() {
        if (context.currentGameScene().isPresent()) {
            GameScene gameScene = context.currentGameScene().get();
            Vector2f size = gameScene.size();
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
        if (context.currentGameScene().isPresent()
            && context.currentGameScene().get() instanceof CameraControlledView scrollableGameScene) {
            var cam = scrollableGameScene.camera();
            return String.format("rot=%.0f x=%.0f y=%.0f z=%.0f",
                cam.getRotate(), cam.getTranslateX(), cam.getTranslateY(), cam.getTranslateZ());
        }
        return InfoText.NO_INFO;
    }

    private String[] floorTextureComboBoxEntries() {
        Map<String, PhongMaterial> texturesByName = context.assets().get("floor_textures");
        var names = new ArrayList<String>();
        names.add(PacManGamesUI_3D.NO_TEXTURE);
        names.addAll(texturesByName.keySet());
        return names.toArray(String[]::new);
    }
}