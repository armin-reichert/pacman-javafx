/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.view;

import de.amr.pacmanfx.ui.GameUI_ServiceFacade;
import de.amr.pacmanfx.ui.subviews.GameUI_SubView;
import javafx.stage.Stage;

public interface GameUI_View {

    void attachServices(GameUI_ServiceFacade services);

    Stage stage();

    GameUI_MainScene mainScene();

    StatusIconBox statusIconBox();

    void replaceSubView(GameUI_SubView subView);
}
