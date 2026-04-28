/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.meshviewer;

import javafx.application.Application;
import javafx.stage.Stage;

public class MeshViewerApp extends Application {

    public static final String BEAGLE_MODEL = "/beagle/13041_Beagle_v1_L1.obj";
    public static final String CORNELL_BOX_MODEL = "/cornell_box/CornellBox-Original.obj";
    public static final String TEAPOT_MODEL = "/newell_teaset/teapot.obj";

    @Override
    public void start(Stage stage) {
        MeshViewerUI ui = new MeshViewerUI(stage);
        ui.addSampleModel("Teapot", getClass().getResource(TEAPOT_MODEL));
        ui.addSampleModel("Beagle", getClass().getResource(BEAGLE_MODEL));
        ui.addSampleModel("Cornell Box", getClass().getResource(CORNELL_BOX_MODEL));
        ui.showObjModel(getClass().getResource(TEAPOT_MODEL));
        ui.startAutoplay();
        ui.show();
    }
}
