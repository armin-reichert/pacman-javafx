/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.util.Ufx;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import static de.amr.games.pacman.ui2d.PacManGames2dUI.*;

/**
 * General settings.
 *
 * @author Armin Reichert
 */
public class InfoBoxGeneral extends InfoBox {

    public static final int MIN_FRAME_RATE = 5;
    public static final int MAX_FRAME_RATE = 120;

    private Button[] buttonsSimulation;
    private Spinner<Integer> spinnerSimulationSteps;
    private Slider sliderTargetFPS;
    private CheckBox cbUsePlayScene3D;
    private CheckBox cbCanvasDecoration;
    private CheckBox cbDebugUI;
    private CheckBox cbTimeMeasured;
    private ImageView iconPlay;
    private ImageView iconStop;
    private ImageView iconStep;
    private Tooltip tooltipPlay = new Tooltip("Play");
    private Tooltip tooltipStop = new Tooltip("Stop");
    private Tooltip tooltipStep = new Tooltip("Single Step Mode");

    public void init(GameContext context) {
        this.context = context;

        infoText("Java Version",   Runtime.version().toString());
        infoText("JavaFX Version", System.getProperty("javafx.runtime.version"));

        iconPlay = new ImageView(context.theme().image("icon.play"));
        iconStop = new ImageView(context.theme().image("icon.stop"));
        iconStep = new ImageView(context.theme().image("icon.step"));

        buttonsSimulation = buttonList("Simulation", "Pause", "Step(s)");

        var btnPlayPause = buttonsSimulation[0];
        btnPlayPause.setGraphic(iconPlay);
        btnPlayPause.setText(null);
        btnPlayPause.setStyle("-fx-background-color: transparent");

        var btnStep = buttonsSimulation[1];
        btnStep.setGraphic(iconStep);
        btnStep.setStyle("-fx-background-color: transparent");
        btnStep.setText(null);
        btnStep.setTooltip(tooltipStep);

        spinnerSimulationSteps = integerSpinner("Num Steps", 1, 50, PY_SIMULATION_STEPS.get());
        spinnerSimulationSteps.valueProperty().addListener((obs, oldVal, newVal) -> PY_SIMULATION_STEPS.set(newVal));

        sliderTargetFPS = slider("Simulation Speed", MIN_FRAME_RATE, MAX_FRAME_RATE, 60);
        sliderTargetFPS.setShowTickLabels(false);
        sliderTargetFPS.setShowTickMarks(false);

        infoText("", () -> String.format("FPS: %.1f (Tgt: %.1f)",
            context.gameClock().getActualFrameRate(),
            context.gameClock().getTargetFrameRate()
        ));
        infoText("Total Updates", () -> context.gameClock().getUpdateCount());

        cbUsePlayScene3D = checkBox("3D Play Scene");
        cbCanvasDecoration = checkBox("Canvas Decoration");
        cbDebugUI = checkBox("Show Debug Info");
        cbTimeMeasured = checkBox("Time Measured");

        buttonsSimulation[0].setOnAction(e -> context.actionHandler().togglePaused());
        buttonsSimulation[1].setOnAction(e -> context.gameClock().makeSteps(PY_SIMULATION_STEPS.get(), true));
        sliderTargetFPS.valueProperty().addListener(
            (py, ov, nv) -> context.gameClock().setTargetFrameRate(nv.intValue()));
        cbUsePlayScene3D.setOnAction(e -> context.actionHandler().toggle2D3D());
        cbCanvasDecoration.selectedProperty().bindBidirectional(PY_CANVAS_DECORATED);
        cbDebugUI.setOnAction(e -> Ufx.toggle(PY_DEBUG_INFO));
        cbTimeMeasured.setOnAction(e -> Ufx.toggle(context.gameClock().timeMeasuredPy));
    }

    @Override
    public void update() {
        super.update();
        boolean paused = context.gameClock().pausedPy.get();
        buttonsSimulation[0].setGraphic(paused ? iconPlay : iconStop);
        buttonsSimulation[0].setTooltip(paused ? tooltipPlay : tooltipStop);
        buttonsSimulation[1].setDisable(!paused);
        spinnerSimulationSteps.getValueFactory().setValue(PY_SIMULATION_STEPS.get());
        sliderTargetFPS.setValue(context.gameClock().getTargetFrameRate());
        cbUsePlayScene3D.setSelected(PY_3D_ENABLED.get());
        cbTimeMeasured.setSelected(context.gameClock().timeMeasuredPy.get());
        cbDebugUI.setSelected(PY_DEBUG_INFO.get());
    }
}