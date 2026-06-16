/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.window;

import de.amr.pacmanfx.ui.game.Game;
import javafx.stage.Stage;

public interface GameWindow {

    void connect(Game game);

    Stage stage();

    GameMainScene mainScene();

    void show();
}
