/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.view;

import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.subviews.SubView;
import javafx.beans.property.ObjectProperty;
import javafx.stage.Stage;

public interface GameView {

    void setGame(Game game);

    ObjectProperty<Stage> stageProperty();

    default Stage stage() {
        return stageProperty().get();
    }

    GameViewMainScene mainScene();

    StatusIconBox statusIconBox();

    void replaceSubView(SubView subView);

    void show();
}
