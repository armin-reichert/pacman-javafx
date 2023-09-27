/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.PacManGames3dApp;
import de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI;
import de.amr.games.pacman.ui.fx.v3d.scene.Perspective;
import de.amr.games.pacman.ui.fx.v3d.scene.PlayScene3D;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.shape.DrawMode;

/**
 * 3D related settings.
 * 
 * @author Armin Reichert
 */
public class Section3D extends Section {

	private final ComboBox<Perspective> comboPerspective;
	private final Slider sliderPiPSceneHeight;
	private final Slider sliderPiPOpacity;
	private final CheckBox cbEnergizerExplodes;
	private final Slider sliderWallHeight;
	private final Slider sliderWallThickness;
	private final CheckBox cbPacLighted;
	private final CheckBox cbAxesVisible;
	private final CheckBox cbWireframeMode;

	public Section3D(PacManGames3dUI ui, String title) {
		super(ui, title, Dashboard.MIN_LABEL_WIDTH, Dashboard.TEXT_COLOR, Dashboard.TEXT_FONT, Dashboard.LABEL_FONT);
		comboPerspective = addComboBox("Perspective", Perspective.values());
		comboPerspective.setOnAction(e -> PacManGames3dApp.PY_3D_PERSPECTIVE.set(comboPerspective.getValue()));
		addInfo("Camera", () -> (gameScene() instanceof PlayScene3D playScene3D) ? playScene3D.camInfo() : "")
				.available(() -> gameScene().is3D());
		sliderPiPSceneHeight = addSlider("PiP Size", PacManGames3dApp.PIP_MIN_HEIGHT, PacManGames3dApp.PIP_MAX_HEIGHT,
				PacManGames3dApp.PY_PIP_HEIGHT.get());
		sliderPiPSceneHeight.valueProperty()
				.addListener((obs, oldValue, newValue) -> PacManGames3dApp.PY_PIP_HEIGHT.set(newValue.doubleValue()));
		sliderPiPOpacity = addSlider("PiP Transparency", 0.0, 1.0, PacManGames3dApp.PY_PIP_OPACITY.get());
		sliderPiPOpacity.valueProperty()
				.addListener((obs, oldValue, newValue) -> PacManGames3dApp.PY_PIP_OPACITY.set(newValue.doubleValue()));
		sliderWallHeight = addSlider("Wall Height", 0.1, 8.5, PacManGames3dApp.PY_3D_WALL_HEIGHT.get());
		sliderWallHeight.valueProperty()
				.addListener((obs, oldVal, newVal) -> PacManGames3dApp.PY_3D_WALL_HEIGHT.set(newVal.doubleValue()));
		sliderWallThickness = addSlider("Wall Thickness", 0.1, 2.0, PacManGames3dApp.PY_3D_WALL_THICKNESS.get());
		sliderWallThickness.valueProperty()
				.addListener((obs, oldVal, newVal) -> PacManGames3dApp.PY_3D_WALL_THICKNESS.set(newVal.doubleValue()));
		cbEnergizerExplodes = addCheckBox("Energizer Explosion", () -> Ufx.toggle(PacManGames3dApp.PY_3D_ENERGIZER_EXPLODES));
		cbPacLighted = addCheckBox("Pac-Man Lighted", () -> Ufx.toggle(PacManGames3dApp.PY_3D_PAC_LIGHT_ENABLED));
		cbAxesVisible = addCheckBox("Show Axes", () -> Ufx.toggle(PacManGames3dApp.PY_3D_AXES_VISIBLE));
		cbWireframeMode = addCheckBox("Wireframe Mode", ui::toggleDrawMode);
	}

	@Override
	public void update() {
		super.update();
		comboPerspective.setValue(PacManGames3dApp.PY_3D_PERSPECTIVE.get());
		sliderPiPSceneHeight.setValue(PacManGames3dApp.PY_PIP_HEIGHT.get());
		sliderPiPOpacity.setValue(PacManGames3dApp.PY_PIP_OPACITY.get());
		sliderWallHeight.setValue(PacManGames3dApp.PY_3D_WALL_HEIGHT.get());
		cbEnergizerExplodes.setSelected(PacManGames3dApp.PY_3D_ENERGIZER_EXPLODES.get());
		cbPacLighted.setSelected(PacManGames3dApp.PY_3D_PAC_LIGHT_ENABLED.get());
		cbAxesVisible.setSelected(PacManGames3dApp.PY_3D_AXES_VISIBLE.get());
		cbWireframeMode.setSelected(PacManGames3dApp.PY_3D_DRAW_MODE.get() == DrawMode.LINE);
	}
}