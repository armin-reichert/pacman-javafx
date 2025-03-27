/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.uilib.FadingPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.ui.UIGlobals.THE_UI;
import static de.amr.games.pacman.uilib.Ufx.opaqueColor;

/**
 * @author Armin Reichert
 */
public class PopupLayer extends Pane {

    private final FadingPane helpPopUp = new FadingPane();

    public PopupLayer(TooFancyCanvasContainer canvas) {
        getChildren().addAll(helpPopUp);

        minHeightProperty().bind(canvas.minHeightProperty());
        maxHeightProperty().bind(canvas.maxHeightProperty());
        prefHeightProperty().bind(canvas.prefHeightProperty());
        minWidthProperty().bind(canvas.minWidthProperty());
        maxWidthProperty().bind(canvas.maxWidthProperty());
        prefWidthProperty().bind(canvas.prefWidthProperty());
    }

    public void showHelp(double scaling) {
        Color bgColor = Color.web(THE_GAME_CONTROLLER.selectedGameVariant() == GameVariant.MS_PACMAN
            ? Arcade.Palette.RED : Arcade.Palette.BLUE);
        var font = THE_UI.assets().font("font.monospaced", Math.max(6, 14 * scaling));
        var helpPane = HelpInfo.build().createPane(opaqueColor(bgColor, 0.8), font);
        helpPopUp.setTranslateX(10 * scaling);
        helpPopUp.setTranslateY(30 * scaling);
        helpPopUp.setContent(helpPane);
        helpPopUp.show(Duration.seconds(1.5));
    }
}