/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.meshviewer;

import javafx.application.Application;
import javafx.stage.Stage;

public class MeshViewerApp extends Application {

    private final SampleModel[] SAMPLES = {
        new SampleModel(
            "Teapot",
            getClass().getResource("/newell_teaset/teapot.obj"),
            new SampleState(-10, 0, 0, 0, true)),
        new SampleModel(
            "Alien Animal",
            getClass().getResource("/alien_animal/Alien Animal.obj"),
            new SampleState(-50, 0, 0, 0, true)),
        new SampleModel("Beagle",
            getClass().getResource("/beagle/13041_Beagle_v1_L1.obj"),
            new SampleState(-150, -90, 0, 0, false)),
    };

    @Override
    public void start(Stage stage) {
        MeshViewerUI ui = new MeshViewerUI(stage);
        for (SampleModel sample : SAMPLES) {
            ui.addSampleModel(sample);
        }
        ui.startAutoplay();
        ui.show();
    }
}
