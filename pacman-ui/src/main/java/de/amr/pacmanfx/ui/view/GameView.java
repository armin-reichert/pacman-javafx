/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.view;

import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.subviews.SubView;
import javafx.stage.Stage;

public interface GameView {

    void setAppContext(AppContext context);

    Stage stage();

    GameViewMainScene mainScene();

    StatusIconBox statusIconBox();

    void replaceSubView(SubView subView);

    void show();
}
