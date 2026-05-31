package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui.layout.StatusIconBox;
import javafx.scene.Scene;
import javafx.stage.Stage;

public interface GameUI_View {

    /**
     * @return  the primary JavaFX stage.
     */
    Stage stage();

    /**
     * @return the mains scene of the game UI
     */
    Scene scene();

    StatusIconBox statusIconBox();
}
