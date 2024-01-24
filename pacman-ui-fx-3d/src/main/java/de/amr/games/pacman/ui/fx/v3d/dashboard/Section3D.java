/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.util.Ufx;
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
	private final CheckBox cbNightMode;
	private final CheckBox cbAxesVisible;
	private final CheckBox cbWireframeMode;

	public Section3D(Theme theme, String title) {
		super(theme, title);

		comboPerspective = addComboBox("Perspective", Perspective.values());
		addInfo("Camera", () -> sceneContext.currentGameScene().isPresent()
			&& sceneContext.currentGameScene().get() instanceof PlayScene3D playScene3D ? playScene3D.camInfo() : "")
			.available(this::isCurrentGameScene3D);
		sliderPiPSceneHeight = addSlider("PiP Size", PacManGames3dUI.PIP_MIN_HEIGHT, PacManGames3dUI.PIP_MAX_HEIGHT, PacManGames3dUI.PY_PIP_HEIGHT.get());
		sliderPiPOpacity = addSlider("PiP Opacity", 0.0, 1.0, PacManGames3dUI.PY_PIP_OPACITY.get());
		sliderWallHeight = addSlider("Wall Height", 0.1, 8.5, PacManGames3dUI.PY_3D_WALL_HEIGHT.get());
		sliderWallThickness = addSlider("Wall Thickness", 0.1, 2.0, PacManGames3dUI.PY_3D_WALL_THICKNESS.get());
		cbEnergizerExplodes = addCheckBox("Energizer Explosion");
		cbNightMode = addCheckBox("Night Mode");
		cbPacLighted = addCheckBox("Pac-Man Lighted");
		cbAxesVisible = addCheckBox("Show Axes");
		cbWireframeMode = addCheckBox("Wireframe Mode");
	}

	@Override
	public void init(GameSceneContext sceneContext) {
		super.init(sceneContext);

		comboPerspective.setValue(PacManGames3dUI.PY_3D_PERSPECTIVE.get());
		sliderPiPSceneHeight.setValue(PacManGames3dUI.PY_PIP_HEIGHT.get());
		sliderPiPOpacity.setValue(PacManGames3dUI.PY_PIP_OPACITY.get());
		sliderWallHeight.setValue(PacManGames3dUI.PY_3D_WALL_HEIGHT.get());
		sliderWallThickness.setValue(PacManGames3dUI.PY_3D_WALL_THICKNESS.get());

		//sliderPiPSceneHeight.valueProperty().bindBidirectional(PY_PIP_HEIGHT);
		sliderPiPSceneHeight.valueProperty().addListener((py, ov, nv) -> PacManGames3dUI.PY_PIP_HEIGHT.set(sliderPiPSceneHeight.getValue()));
		sliderPiPOpacity.valueProperty().bindBidirectional(PacManGames3dUI.PY_PIP_OPACITY);
		sliderWallHeight.valueProperty().bindBidirectional(PacManGames3dUI.PY_3D_WALL_HEIGHT);
		sliderWallThickness.valueProperty().bindBidirectional(PacManGames3dUI.PY_3D_WALL_THICKNESS);

		comboPerspective.setOnAction(e -> PacManGames3dUI.PY_3D_PERSPECTIVE.set(comboPerspective.getValue()));
		cbEnergizerExplodes.setOnAction(e -> Ufx.toggle(PacManGames3dUI.PY_3D_ENERGIZER_EXPLODES));
		cbNightMode.setOnAction(e -> Ufx.toggle(PacManGames3dUI.PY_3D_NIGHT_MODE));
		cbPacLighted.setOnAction(e -> Ufx.toggle(PacManGames3dUI.PY_3D_PAC_LIGHT_ENABLED));
		cbAxesVisible.setOnAction(e -> Ufx.toggle(PacManGames3dUI.PY_3D_AXES_VISIBLE));
		cbWireframeMode.setOnAction(e -> actionHandler().toggleDrawMode());
	}

	@Override
	public void update() {
		super.update();
		comboPerspective.setValue(PacManGames3dUI.PY_3D_PERSPECTIVE.get());
		cbEnergizerExplodes.setSelected(PacManGames3dUI.PY_3D_ENERGIZER_EXPLODES.get());
		cbNightMode.setSelected(PacManGames3dUI.PY_3D_NIGHT_MODE.get());
		cbPacLighted.setSelected(PacManGames3dUI.PY_3D_PAC_LIGHT_ENABLED.get());
		cbAxesVisible.setSelected(PacManGames3dUI.PY_3D_AXES_VISIBLE.get());
		cbWireframeMode.setSelected(PacManGames3dUI.PY_3D_DRAW_MODE.get() == DrawMode.LINE);
	}
}