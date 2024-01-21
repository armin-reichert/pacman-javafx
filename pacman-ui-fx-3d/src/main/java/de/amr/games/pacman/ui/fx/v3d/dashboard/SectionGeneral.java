/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.ui.fx.PacManGames2dApp;
import de.amr.games.pacman.ui.fx.util.GameClock;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.PacManGames3dApp;
import de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

/**
 * General settings.
 * 
 * @author Armin Reichert
 */
public class SectionGeneral extends Section {

	public static final int MIN_FRAMERATE = 5;
	public static final int MAX_FRAMERATE = 120;

	private final Button[] buttonsSimulation;
	private final Spinner<Integer> spinnerSimulationSteps;
	private final Slider sliderTargetFPS;
	private final CheckBox cbUsePlayScene3D;
	private final CheckBox cbPoliticallyCorrect;
	private final CheckBox cbDebugUI;
	private final CheckBox cbTimeMeasured;
	private final ImageView iconPlay;
	private final ImageView iconStop;
	private final ImageView iconStep;
	private final Tooltip tooltipPlay = new Tooltip("Play");
	private final Tooltip tooltipStop = new Tooltip("Stop");
	private final Tooltip tooltipStep = new Tooltip("Single Step Mode");

	private GameClock clock;

	public SectionGeneral(Theme theme, String title) {
		super(theme, title, Dashboard.MIN_LABEL_WIDTH, Dashboard.TEXT_COLOR, Dashboard.TEXT_FONT, Dashboard.LABEL_FONT);

		iconPlay = new ImageView(theme.image("icon.play"));
		iconStop = new ImageView(theme.image("icon.stop"));
		iconStep = new ImageView(theme.image("icon.step"));

		buttonsSimulation = addButtonList("Simulation", "Pause", "Step(s)");

		var btnPlayPause = buttonsSimulation[0];
		btnPlayPause.setGraphic(iconPlay);
		btnPlayPause.setText(null);
		btnPlayPause.setStyle("-fx-background-color: transparent");

		var btnStep = buttonsSimulation[1];
		btnStep.setGraphic(iconStep);
		btnStep.setStyle("-fx-background-color: transparent");
		btnStep.setText(null);
		btnStep.setTooltip(tooltipStep);

		spinnerSimulationSteps = addSpinner("Num Steps", 1, 50, PacManGames3dApp.PY_SIMULATION_STEPS.get());
		spinnerSimulationSteps.valueProperty()
				.addListener((obs, oldVal, newVal) -> PacManGames3dApp.PY_SIMULATION_STEPS.set(newVal.intValue()));

		sliderTargetFPS = addSlider("Simulation Speed", MIN_FRAMERATE, MAX_FRAMERATE, 60);
		sliderTargetFPS.setShowTickLabels(false);
		sliderTargetFPS.setShowTickMarks(false);

		addInfo("", () -> String.format("Target %dHz Actual %dHz", clock.targetFrameratePy.get(), clock.getFPS()));
		addInfo("Total Updates", () -> clock.getUpdateCount());

		cbUsePlayScene3D = addCheckBox("3D Play Scene", null);
		cbPoliticallyCorrect = addCheckBox("Woke Pussy Mode", () -> Ufx.toggle(PacManGames3dApp.PY_WOKE_PUSSY));
		cbDebugUI = addCheckBox("Show Debug Info", () -> Ufx.toggle(PacManGames2dApp.PY_SHOW_DEBUG_INFO));
		cbTimeMeasured = addCheckBox("Time Measured", () -> Ufx.toggle(clock.timeMeasuredPy));
	}

	@Override
	public void init(PacManGames3dUI ui) {
		super.init(ui);
		this.clock = ui.clock();
		buttonsSimulation[0].setOnAction(e -> ui.togglePaused());
		buttonsSimulation[1].setOnAction(e -> clock.executeSteps(PacManGames3dApp.PY_SIMULATION_STEPS.get(), true));
		sliderTargetFPS.valueProperty().addListener((py, ov, nv) -> clock.targetFrameratePy.set(nv.intValue()));
		cbUsePlayScene3D.setOnAction(e -> ui.toggle2D3D());
	}

	@Override
	public void update() {
		super.update();
		buttonsSimulation[0].setGraphic(clock.pausedPy.get() ? iconPlay : iconStop);
		buttonsSimulation[0].setTooltip(clock.pausedPy.get() ? tooltipPlay : tooltipStop);
		buttonsSimulation[1].setDisable(!clock.pausedPy.get());
		spinnerSimulationSteps.getValueFactory().setValue(PacManGames3dApp.PY_SIMULATION_STEPS.get());
		sliderTargetFPS.setValue(clock.targetFrameratePy.get());
		cbUsePlayScene3D.setSelected(PacManGames3dApp.PY_3D_ENABLED.get());
		cbPoliticallyCorrect.setSelected(PacManGames3dApp.PY_WOKE_PUSSY.get());
		cbTimeMeasured.setSelected(clock.timeMeasuredPy.get());
		cbDebugUI.setSelected(PacManGames2dApp.PY_SHOW_DEBUG_INFO.get());
	}
}