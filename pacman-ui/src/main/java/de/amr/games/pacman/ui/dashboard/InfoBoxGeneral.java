/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.dashboard;

import de.amr.games.pacman.ui.PacManGamesUI;
import de.amr.games.pacman.ui._2d.GameActions2D;
import de.amr.games.pacman.uilib.ResourceManager;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;

import static de.amr.games.pacman.ui.GameUI.THE_CLOCK;
import static de.amr.games.pacman.ui._2d.GlobalProperties2d.*;

/**
 * General settings.
 *
 * @author Armin Reichert
 */
public class InfoBoxGeneral extends InfoBox {

    public static final int MIN_FRAME_RATE = 5;
    public static final int MAX_FRAME_RATE = 120;

    private Button[] bgSimulation;

    private final Tooltip tooltipPlay = new Tooltip("Play");
    private final Tooltip tooltipStop = new Tooltip("Stop");
    private ImageView iconPlay;
    private ImageView iconStop;
    private ImageView iconStep;

    public void init() {
        ResourceManager rm = () -> PacManGamesUI.class;
        iconPlay = new ImageView(rm.loadImage("graphics/icons/play.png"));
        iconStop = new ImageView(rm.loadImage("graphics/icons/stop.png"));
        iconStep = new ImageView(rm.loadImage("graphics/icons/step.png"));

        addLabeledValue("Java Version",   Runtime.version().toString());
        addLabeledValue("JavaFX Version", System.getProperty("javafx.runtime.version"));
        bgSimulation = addButtonList("Simulation", "Pause", "Step(s)");
        addIntSpinner("Num Steps", 1, 50, PY_SIMULATION_STEPS);
        var sliderTargetFPS = addSlider("Simulation Speed", MIN_FRAME_RATE, MAX_FRAME_RATE, 60, false, false);
        addLabeledValue("", () -> "FPS: %.1f (Tgt: %.1f)".formatted(THE_CLOCK.getActualFrameRate(), THE_CLOCK.getTargetFrameRate()));
        addLabeledValue("Total Updates",  THE_CLOCK::updateCount);
        var pickerCanvasColor = addColorPicker("Canvas Color", PY_CANVAS_BG_COLOR.get());
        var cbCanvasImageSmoothing = addCheckBox("Image Smoothing");
        var cbCanvasFontSmoothing = addCheckBox("Font Smoothing");
        var cbDebugUI = addCheckBox("Show Debug Info");
        var cbTimeMeasured = addCheckBox("Time Measured");

        Button btnPlayPause = bgSimulation[0];
        btnPlayPause.setGraphic(iconPlay);
        btnPlayPause.setText(null);
        btnPlayPause.setStyle("-fx-background-color: transparent");

        Button btnStep = bgSimulation[1];
        btnStep.setGraphic(iconStep);
        btnStep.setStyle("-fx-background-color: transparent");
        btnStep.setText(null);
        btnStep.setTooltip(new Tooltip("Single Step Mode"));

        setAction(bgSimulation[0], GameActions2D.TOGGLE_PAUSED::execute);
        setAction(bgSimulation[1], () -> THE_CLOCK.makeSteps(PY_SIMULATION_STEPS.get(), true));
        setEditor(sliderTargetFPS, THE_CLOCK.targetFrameRateProperty());
        setEditor(pickerCanvasColor, PY_CANVAS_BG_COLOR);
        setEditor(cbCanvasImageSmoothing, PY_CANVAS_IMAGE_SMOOTHING);
        setEditor(cbCanvasFontSmoothing, PY_CANVAS_FONT_SMOOTHING);
        setEditor(cbDebugUI, PY_DEBUG_INFO_VISIBLE);
        setEditor(cbTimeMeasured, THE_CLOCK.timeMeasuredProperty());
    }

    @Override
    public void update() {
        super.update();
        boolean paused = THE_CLOCK.pausedProperty().get();
        bgSimulation[0].setGraphic(paused ? iconPlay : iconStop);
        bgSimulation[0].setTooltip(paused ? tooltipPlay : tooltipStop);
        bgSimulation[1].setDisable(!paused);
    }
}