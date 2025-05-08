/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.uilib.widgets.FadingPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.ui.Globals.THE_ASSETS;
import static de.amr.games.pacman.uilib.Ufx.opaqueColor;

/**
 * @author Armin Reichert
 */
public class PopupLayer extends Pane {

    private final FadingPane helpPopUp = new FadingPane();

    public PopupLayer(TooFancyCanvasContainer canvasContainer) {
        getChildren().addAll(helpPopUp);

        minHeightProperty().bind(canvasContainer.minHeightProperty());
        maxHeightProperty().bind(canvasContainer.maxHeightProperty());
        prefHeightProperty().bind(canvasContainer.prefHeightProperty());
        minWidthProperty().bind(canvasContainer.minWidthProperty());
        maxWidthProperty().bind(canvasContainer.maxWidthProperty());
        prefWidthProperty().bind(canvasContainer.prefWidthProperty());
    }

    public void showHelp(double scaling) {
        Color bgColor = Color.web(THE_GAME_CONTROLLER.isSelected(GameVariant.MS_PACMAN) ? Arcade.Palette.RED : Arcade.Palette.BLUE);
        var font = THE_ASSETS.font("font.monospaced", Math.max(6, 14 * scaling));
        var helpPane = HelpInfo.build().createPane(opaqueColor(bgColor, 0.8), font);
        helpPopUp.setTranslateX(10 * scaling);
        helpPopUp.setTranslateY(30 * scaling);
        helpPopUp.setContent(helpPane);
        helpPopUp.show(Duration.seconds(1.5));
    }
}