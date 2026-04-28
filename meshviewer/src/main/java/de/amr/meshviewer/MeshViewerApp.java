/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.meshviewer;

import javafx.application.Application;
import javafx.stage.Stage;

public class MeshViewerApp extends Application {

    record SampleModel(String title, String path) {}

    private static final SampleModel[] SAMPLES = {
        new SampleModel("Teapot", "/newell_teaset/teapot.obj"),
        new SampleModel("Alien Animal", "/alien_animal/Alien Animal.obj"),
        new SampleModel("Beagle", "/beagle/13041_Beagle_v1_L1.obj"),
    };

    @Override
    public void start(Stage stage) {
        MeshViewerUI ui = new MeshViewerUI(stage);
        for (SampleModel sample : SAMPLES) {
            ui.addSampleModel(sample.title(), getClass().getResource(sample.path()));
        }
        ui.showObjModel(getClass().getResource(SAMPLES[0].path()));
        ui.startAutoplay();
        ui.show();
    }
}
