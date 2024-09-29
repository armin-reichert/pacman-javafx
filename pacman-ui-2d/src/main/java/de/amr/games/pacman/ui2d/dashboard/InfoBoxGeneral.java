/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.util.GameClockFX;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import static de.amr.games.pacman.ui2d.PacManGames2dApp.*;

/**
 * General settings.
 *
 * @author Armin Reichert
 */
public class InfoBoxGeneral extends InfoBox {

    public static final int MIN_FRAME_RATE = 5;
    public static final int MAX_FRAME_RATE = 120;

    private Button[] bgSimulation;
    private Spinner<Integer> spinnerSimulationSteps;
    private Slider sliderTargetFPS;
    private ColorPicker pickerCanvasColor;
    private CheckBox cbCanvasDecoration;
    private CheckBox cbDebugUI;
    private CheckBox cbTimeMeasured;

    private final Tooltip tooltipPlay = new Tooltip("Play");
    private final Tooltip tooltipStop = new Tooltip("Stop");
    private ImageView iconPlay;
    private ImageView iconStop;
    private ImageView iconStep;

    public void init(GameContext context) {
        super.init(context);

        GameClockFX clock = context.gameClock();

        iconPlay = new ImageView(context.assets().image("icon.play"));
        iconStop = new ImageView(context.assets().image("icon.stop"));
        iconStep = new ImageView(context.assets().image("icon.step"));

        labelledValue("Java Version",   Runtime.version().toString());
        labelledValue("JavaFX Version", System.getProperty("javafx.runtime.version"));
        bgSimulation                  = buttonList("Simulation", "Pause", "Step(s)");
        spinnerSimulationSteps        = integerSpinner("Num Steps", 1, 50, PY_SIMULATION_STEPS);
        sliderTargetFPS               = slider("Simulation Speed", MIN_FRAME_RATE, MAX_FRAME_RATE, 60);
        sliderTargetFPS.setShowTickLabels(false);
        sliderTargetFPS.setShowTickMarks(false);
        labelledValue("",              () -> "FPS: %.1f (Tgt: %.1f)".formatted(clock.getActualFrameRate(), clock.getTargetFrameRate()));
        labelledValue("Total Updates", clock::getUpdateCount);
        pickerCanvasColor              = colorPicker("Canvas Color", PY_CANVAS_BG_COLOR.get());
        cbCanvasDecoration             = checkBox("Canvas Decoration");
        cbDebugUI                      = checkBox("Show Debug Info");
        cbTimeMeasured                 = checkBox("Time Measured");

        Button btnPlayPause = bgSimulation[0];
        btnPlayPause.setGraphic(iconPlay);
        btnPlayPause.setText(null);
        btnPlayPause.setStyle("-fx-background-color: transparent");

        Button btnStep = bgSimulation[1];
        btnStep.setGraphic(iconStep);
        btnStep.setStyle("-fx-background-color: transparent");
        btnStep.setText(null);
        btnStep.setTooltip(new Tooltip("Single Step Mode"));

        setAction(bgSimulation[0], context::togglePaused);
        setAction(bgSimulation[1], () -> clock.makeSteps(PY_SIMULATION_STEPS.get(), true));
        assignEditor(sliderTargetFPS, clock.targetFrameRatePy);
        assignEditor(pickerCanvasColor, PY_CANVAS_BG_COLOR);
        assignEditor(cbCanvasDecoration, PY_CANVAS_DECORATED);
        assignEditor(cbDebugUI, PY_DEBUG_INFO);
        assignEditor(cbTimeMeasured, clock.timeMeasuredPy);
    }

    @Override
    public void update() {
        super.update();
        boolean paused = context.gameClock().pausedPy.get();
        bgSimulation[0].setGraphic(paused ? iconPlay : iconStop);
        bgSimulation[0].setTooltip(paused ? tooltipPlay : tooltipStop);
        bgSimulation[1].setDisable(!paused);
    }
}