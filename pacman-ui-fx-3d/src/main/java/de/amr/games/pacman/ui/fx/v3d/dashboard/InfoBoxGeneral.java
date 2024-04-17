/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.ui.fx.GameSceneContext;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import static de.amr.games.pacman.ui.fx.PacManGames2dUI.PY_SHOW_DEBUG_INFO;

/**
 * General settings.
 *
 * @author Armin Reichert
 */
public class InfoBoxGeneral extends InfoBox {

    public static final int MIN_FRAME_RATE = 5;
    public static final int MAX_FRAME_RATE = 120;

    private final Button[] buttonsSimulation;
    private final Spinner<Integer> spinnerSimulationSteps;
    private final Slider sliderTargetFPS;
    private final CheckBox cbUsePlayScene3D;
    private final CheckBox cbDebugUI;
    private final CheckBox cbTimeMeasured;
    private final ImageView iconPlay;
    private final ImageView iconStop;
    private final ImageView iconStep;
    private final Tooltip tooltipPlay = new Tooltip("Play");
    private final Tooltip tooltipStop = new Tooltip("Stop");
    private final Tooltip tooltipStep = new Tooltip("Single Step Mode");

    public InfoBoxGeneral(Theme theme, String title) {
        super(theme, title);

        addInfo("Java Version",   Runtime.version().toString());
        addInfo("JavaFX Version", System.getProperty("javafx.runtime.version"));

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

        spinnerSimulationSteps = addSpinner("Num Steps", 1, 50, PacManGames3dUI.PY_SIMULATION_STEPS.get());
        spinnerSimulationSteps.valueProperty().addListener((obs, oldVal, newVal) -> PacManGames3dUI.PY_SIMULATION_STEPS.set(newVal));

        sliderTargetFPS = addSlider("Simulation Speed", MIN_FRAME_RATE, MAX_FRAME_RATE, 60);
        sliderTargetFPS.setShowTickLabels(false);
        sliderTargetFPS.setShowTickMarks(false);

        addInfo("", () -> String.format("Target %dHz Actual %dHz",
            sceneContext.gameClock().targetFrameRatePy.get(), sceneContext.gameClock().getActualFrameRate()));
        addInfo("Total Updates", () -> sceneContext.gameClock().getUpdateCount());

        cbUsePlayScene3D = addCheckBox("3D Play Scene");
        cbDebugUI = addCheckBox("Show Debug Info");
        cbTimeMeasured = addCheckBox("Time Measured");
    }

    @Override
    public void init(GameSceneContext sceneContext) {
        super.init(sceneContext);
        buttonsSimulation[0].setOnAction(e -> actionHandler().togglePaused());
        buttonsSimulation[1].setOnAction(e -> sceneContext.gameClock().makeSteps(PacManGames3dUI.PY_SIMULATION_STEPS.get(), true));
        sliderTargetFPS.valueProperty().addListener(
            (py, ov, nv) -> sceneContext.gameClock().targetFrameRatePy.set(nv.intValue()));
        cbUsePlayScene3D.setOnAction(e -> actionHandler().toggle2D3D());
        cbDebugUI.setOnAction(e -> Ufx.toggle(PY_SHOW_DEBUG_INFO));
        cbTimeMeasured.setOnAction(e -> Ufx.toggle(sceneContext.gameClock().timeMeasuredPy));
    }

    @Override
    public void update() {
        super.update();
        boolean paused = sceneContext.gameClock().pausedPy.get();
        buttonsSimulation[0].setGraphic(paused ? iconPlay : iconStop);
        buttonsSimulation[0].setTooltip(paused ? tooltipPlay : tooltipStop);
        buttonsSimulation[1].setDisable(!paused);
        spinnerSimulationSteps.getValueFactory().setValue(PacManGames3dUI.PY_SIMULATION_STEPS.get());
        sliderTargetFPS.setValue(sceneContext.gameClock().targetFrameRatePy.get());
        cbUsePlayScene3D.setSelected(PacManGames3dUI.PY_3D_ENABLED.get());
        cbTimeMeasured.setSelected(sceneContext.gameClock().timeMeasuredPy.get());
        cbDebugUI.setSelected(PY_SHOW_DEBUG_INFO.get());
    }
}