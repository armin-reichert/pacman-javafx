/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.view;

import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.subviews.SubView;
import javafx.beans.property.ObjectProperty;
import javafx.stage.Stage;

public interface GameView {

    void connect(Game game);

    ObjectProperty<Stage> stageProperty();

    default Stage stage() {
        return stageProperty().get();
    }

    GameMainScene mainScene();

    void replaceSubView(SubView subView);

    void show();
}
