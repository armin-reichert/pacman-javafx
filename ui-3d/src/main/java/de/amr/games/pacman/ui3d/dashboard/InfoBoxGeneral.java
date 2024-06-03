/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.dashboard;

import de.amr.games.pacman.ui2d.scene.GameSceneContext;
import de.amr.games.pacman.ui2d.util.Theme;
import de.amr.games.pacman.ui2d.util.Ufx;
import de.amr.games.pacman.ui3d.PacManGames3dUI;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import static de.amr.games.pacman.ui2d.PacManGames2dUI.PY_CANVAS_DECORATED;
import static de.amr.games.pacman.ui2d.PacManGames2dUI.PY_SHOW_DEBUG_INFO;
import static de.amr.games.pacman.ui3d.PacManGames3dUI.PY_SIMULATION_STEPS;

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
    private final CheckBox cbCanvasDecoration;
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

        infoText("Java Version",   Runtime.version().toString());
        infoText("JavaFX Version", System.getProperty("javafx.runtime.version"));

        iconPlay = new ImageView(theme.image("icon.play"));
        iconStop = new ImageView(theme.image("icon.stop"));
        iconStep = new ImageView(theme.image("icon.step"));

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
    }

    @Override
    public void init(GameSceneContext sceneContext) {
        super.init(sceneContext);
        buttonsSimulation[0].setOnAction(e -> actionHandler().togglePaused());
        buttonsSimulation[1].setOnAction(e -> sceneContext.gameClock().makeSteps(PY_SIMULATION_STEPS.get(), true));
        sliderTargetFPS.valueProperty().addListener(
            (py, ov, nv) -> sceneContext.gameClock().setTargetFrameRate(nv.intValue()));
        cbUsePlayScene3D.setOnAction(e -> actionHandler().toggle2D3D());
        cbCanvasDecoration.selectedProperty().bindBidirectional(PY_CANVAS_DECORATED);
        cbDebugUI.setOnAction(e -> Ufx.toggle(PY_SHOW_DEBUG_INFO));
        cbTimeMeasured.setOnAction(e -> Ufx.toggle(sceneContext.gameClock().timeMeasuredPy));
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
        cbUsePlayScene3D.setSelected(PacManGames3dUI.PY_3D_ENABLED.get());
        cbTimeMeasured.setSelected(context.gameClock().timeMeasuredPy.get());
        cbDebugUI.setSelected(PY_SHOW_DEBUG_INFO.get());
    }
}