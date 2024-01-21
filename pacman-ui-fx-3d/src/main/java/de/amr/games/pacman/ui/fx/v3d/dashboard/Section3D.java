/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.ActionHandler3D;
import de.amr.games.pacman.ui.fx.v3d.PacManGames3dApp;
import de.amr.games.pacman.ui.fx.v3d.scene.Perspective;
import de.amr.games.pacman.ui.fx.v3d.scene.PlayScene3D;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.shape.DrawMode;

import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dApp.*;

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

	public Section3D(Theme theme, String title) {
		super(theme, title, Dashboard.MIN_LABEL_WIDTH, Dashboard.TEXT_COLOR, Dashboard.TEXT_FONT, Dashboard.LABEL_FONT);

		comboPerspective = addComboBox("Perspective", Perspective.values());
		addInfo("Camera", () -> isCurrentGameScene3D() ? ((PlayScene3D)sceneContext.currentGameScene().get()).camInfo() : "")
				.available(this::isCurrentGameScene3D);
		sliderPiPSceneHeight = addSlider("PiP Size", PIP_MIN_HEIGHT, PIP_MAX_HEIGHT, PY_PIP_HEIGHT.get());
		sliderPiPOpacity = addSlider("PiP Transparency", 0.0, 1.0, PY_PIP_OPACITY.get());
		sliderWallHeight = addSlider("Wall Height", 0.1, 8.5, PY_3D_WALL_HEIGHT.get());
		sliderWallThickness = addSlider("Wall Thickness", 0.1, 2.0, PY_3D_WALL_THICKNESS.get());
		cbEnergizerExplodes = addCheckBox("Energizer Explosion");
		cbPacLighted = addCheckBox("Pac-Man Lighted");
		cbAxesVisible = addCheckBox("Show Axes");
		cbWireframeMode = addCheckBox("Wireframe Mode");
	}

	@Override
	public void init(GameSceneContext sceneContext, ActionHandler3D actionHandler) {
		super.init(sceneContext, actionHandler);

		comboPerspective.setValue(PY_3D_PERSPECTIVE.get());
		sliderPiPSceneHeight.setValue(PY_PIP_HEIGHT.get());
		sliderPiPOpacity.setValue(PY_PIP_OPACITY.get());
		sliderWallHeight.setValue(PY_3D_WALL_HEIGHT.get());
		sliderWallThickness.setValue(PY_3D_WALL_THICKNESS.get());

		//sliderPiPSceneHeight.valueProperty().bindBidirectional(PY_PIP_HEIGHT);
		sliderPiPSceneHeight.valueProperty().addListener((py, ov, nv) -> PY_PIP_HEIGHT.set(sliderPiPSceneHeight.getValue()));
		sliderPiPOpacity.valueProperty().bindBidirectional(PY_PIP_OPACITY);
		sliderWallHeight.valueProperty().bindBidirectional(PY_3D_WALL_HEIGHT);
		sliderWallThickness.valueProperty().bindBidirectional(PY_3D_WALL_THICKNESS);

		comboPerspective.setOnAction(e -> PY_3D_PERSPECTIVE.set(comboPerspective.getValue()));
		cbEnergizerExplodes.setOnAction(e -> Ufx.toggle(PY_3D_ENERGIZER_EXPLODES));
		cbPacLighted.setOnAction(e -> Ufx.toggle(PY_3D_PAC_LIGHT_ENABLED));
		cbAxesVisible.setOnAction(e -> Ufx.toggle(PY_3D_AXES_VISIBLE));
		cbWireframeMode.setOnAction(e -> actionHandler.toggleDrawMode());
	}

	@Override
	public void update() {
		super.update();
		comboPerspective.setValue(PY_3D_PERSPECTIVE.get());
		cbEnergizerExplodes.setSelected(PY_3D_ENERGIZER_EXPLODES.get());
		cbPacLighted.setSelected(PY_3D_PAC_LIGHT_ENABLED.get());
		cbAxesVisible.setSelected(PY_3D_AXES_VISIBLE.get());
		cbWireframeMode.setSelected(PacManGames3dApp.PY_3D_DRAW_MODE.get() == DrawMode.LINE);
	}
}