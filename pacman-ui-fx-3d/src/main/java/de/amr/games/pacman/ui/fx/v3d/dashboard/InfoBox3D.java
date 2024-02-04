/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.ui.fx.GameSceneContext;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI;
import de.amr.games.pacman.ui.fx.v3d.scene3d.Perspective;
import de.amr.games.pacman.ui.fx.v3d.scene3d.PlayScene3D;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.shape.DrawMode;

import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.*;

/**
 * 3D related settings.
 * 
 * @author Armin Reichert
 */
public class InfoBox3D extends InfoBox {

	private final ColorPicker pickerLightColor;
	private final ColorPicker pickerFloorColor;
	private final ComboBox<String> comboFloorTexture;
	private final CheckBox cbFloorTextureRandom;
	private final ComboBox<Perspective> comboPerspectives;
	private final Slider sliderPiPSceneHeight;
	private final Slider sliderPiPOpacity;
	private final CheckBox cbEnergizerExplodes;
	private final Slider sliderWallHeight;
	private final Slider sliderWallThickness;
	private final CheckBox cbPacLighted;
	private final CheckBox cbNightMode;
	private final CheckBox cbAxesVisible;
	private final CheckBox cbWireframeMode;

	public InfoBox3D(Theme theme, String title) {
		super(theme, title);

		pickerLightColor = addColorPicker("Light Color", PacManGames3dUI.PY_3D_LIGHT_COLOR.get());
		pickerLightColor.setOnAction(e -> PacManGames3dUI.PY_3D_LIGHT_COLOR.set(pickerLightColor.getValue()));
		pickerFloorColor = addColorPicker("Floor Color", PacManGames3dUI.PY_3D_FLOOR_COLOR.get());
		pickerFloorColor.setOnAction(e -> PacManGames3dUI.PY_3D_FLOOR_COLOR.set(pickerFloorColor.getValue()));
		comboFloorTexture = addComboBox("Floor Texture", floorTextureComboBoxEntries());
		comboFloorTexture.setOnAction(e -> PacManGames3dUI.PY_3D_FLOOR_TEXTURE.set(comboFloorTexture.getValue()));
		cbFloorTextureRandom = addCheckBox("Random Floor Texture", () -> Ufx.toggle(PacManGames3dUI.PY_3D_FLOOR_TEXTURE_RND));
		comboPerspectives = addComboBox("Perspective", Perspective.values());
		addInfo("Camera", this::currentSceneCameraInfo).available(this::isCurrentGameScene3D);
		sliderPiPSceneHeight = addSlider("PiP Size", PIP_MIN_HEIGHT, PIP_MAX_HEIGHT, PY_PIP_HEIGHT.get());
		sliderPiPOpacity = addSlider("PiP Opacity", 0.0, 1.0, PY_PIP_OPACITY.get());
		sliderWallHeight = addSlider("Wall Height", 0.1, 8.5, PY_3D_WALL_HEIGHT.get());
		sliderWallThickness = addSlider("Wall Thickness", 0.1, 2.0, PY_3D_WALL_THICKNESS.get());
		cbEnergizerExplodes = addCheckBox("Energizer Explosion");
		cbNightMode = addCheckBox("Night Mode");
		cbPacLighted = addCheckBox("Pac-Man Lighted");
		cbAxesVisible = addCheckBox("Show Axes");
		cbWireframeMode = addCheckBox("Wireframe Mode");
	}

	private String currentSceneCameraInfo() {
		if (sceneContext.currentGameScene().isPresent()
			&& sceneContext.currentGameScene().get() instanceof PlayScene3D playScene3D) {
			var camera = playScene3D.getCamera();
			return String.format("x=%.0f y=%.0f z=%.0f rot=%.0f",
				camera.getTranslateX(), camera.getTranslateY(), camera.getTranslateZ(), camera.getRotate());
		}
		return "n/a";
	}

	@Override
	public void init(GameSceneContext sceneContext) {
		super.init(sceneContext);

		comboPerspectives.setValue(PY_3D_PERSPECTIVE.get());
		sliderPiPSceneHeight.setValue(PY_PIP_HEIGHT.get());
		sliderPiPOpacity.setValue(PY_PIP_OPACITY.get());
		sliderWallHeight.setValue(PY_3D_WALL_HEIGHT.get());
		sliderWallThickness.setValue(PY_3D_WALL_THICKNESS.get());

		//sliderPiPSceneHeight.valueProperty().bindBidirectional(PY_PIP_HEIGHT);
		sliderPiPSceneHeight.valueProperty().addListener((py, ov, nv) -> PY_PIP_HEIGHT.set(sliderPiPSceneHeight.getValue()));
		sliderPiPOpacity.valueProperty().bindBidirectional(PY_PIP_OPACITY);
		sliderWallHeight.valueProperty().bindBidirectional(PY_3D_WALL_HEIGHT);
		sliderWallThickness.valueProperty().bindBidirectional(PY_3D_WALL_THICKNESS);

		comboPerspectives.setOnAction(e -> PY_3D_PERSPECTIVE.set(comboPerspectives.getValue()));
		cbEnergizerExplodes.setOnAction(e -> Ufx.toggle(PY_3D_ENERGIZER_EXPLODES));
		cbNightMode.setOnAction(e -> Ufx.toggle(PY_3D_NIGHT_MODE));
		cbPacLighted.setOnAction(e -> Ufx.toggle(PY_3D_PAC_LIGHT_ENABLED));
		cbAxesVisible.setOnAction(e -> Ufx.toggle(PY_3D_AXES_VISIBLE));
		cbWireframeMode.setOnAction(e -> actionHandler().toggleDrawMode());
	}

	@Override
	public void update() {
		super.update();
		comboFloorTexture.setValue(PacManGames3dUI.PY_3D_FLOOR_TEXTURE.get());
		cbFloorTextureRandom.setSelected(PacManGames3dUI.PY_3D_FLOOR_TEXTURE_RND.get());
		comboPerspectives.setValue(PY_3D_PERSPECTIVE.get());
		cbEnergizerExplodes.setSelected(PY_3D_ENERGIZER_EXPLODES.get());
		cbNightMode.setSelected(PY_3D_NIGHT_MODE.get());
		cbPacLighted.setSelected(PY_3D_PAC_LIGHT_ENABLED.get());
		cbAxesVisible.setSelected(PY_3D_AXES_VISIBLE.get());
		cbWireframeMode.setSelected(PY_3D_DRAW_MODE.get() == DrawMode.LINE);
	}

	private String[] floorTextureComboBoxEntries() {
		var names = new String[] { "hexagon", "knobs", "plastic", "wood" };
		var entries = new String[names.length + 1];
		entries[0] = PacManGames3dUI.NO_TEXTURE;
		System.arraycopy(names, 0, entries, 1, names.length);
		return entries;
	}
}