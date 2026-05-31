package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui.layout.GameUI_MainScene;
import de.amr.pacmanfx.ui.layout.StatusIconBox;
import javafx.stage.Stage;

public record GameUI_View_Implementation(
    Stage stage,
    GameUI_MainScene mainScene,
    StatusIconBox statusIconBox
) implements GameUI_View {}
