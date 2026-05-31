package de.amr.pacmanfx.ui.view;

import javafx.stage.Stage;

public record GameUI_View_Implementation(
    Stage stage,
    GameUI_MainScene mainScene,
    StatusIconBox statusIconBox
) implements GameUI_View {}
