package de.amr.pacmanfx.ui.view;

import javafx.stage.Stage;

public interface GameUI_View {

    Stage stage();

    GameUI_MainScene mainScene();

    StatusIconBox statusIconBox();
}
