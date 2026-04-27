/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package experiments.meshviewer;

import javafx.application.Application;
import javafx.stage.Stage;

public class MeshViewerApp extends Application {

    public static final String TEAPOT_MODEL = "/newell_teaset/teapot.obj";

    @Override
    public void start(Stage stage) {
        MeshViewerUI ui = new MeshViewerUI(stage);
        ui.showObjModel(getClass().getResource(TEAPOT_MODEL));
        ui.show();
    }
}
