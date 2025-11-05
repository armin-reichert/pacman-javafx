/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.uilib.widgets.CanvasDecorationPane;
import de.amr.pacmanfx.uilib.widgets.FadingPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import static de.amr.pacmanfx.uilib.Ufx.colorWithOpacity;

public class HelpLayer extends Pane {

    private final FadingPane helpPopUp = new FadingPane();

    public HelpLayer(CanvasDecorationPane canvasDecorationPane) {
        getChildren().addAll(helpPopUp);

        minHeightProperty().bind(canvasDecorationPane.minHeightProperty());
        maxHeightProperty().bind(canvasDecorationPane.maxHeightProperty());
        prefHeightProperty().bind(canvasDecorationPane.prefHeightProperty());
        minWidthProperty().bind(canvasDecorationPane.minWidthProperty());
        maxWidthProperty().bind(canvasDecorationPane.maxWidthProperty());
        prefWidthProperty().bind(canvasDecorationPane.prefWidthProperty());
    }

    public void showHelp(GameUI ui, double scaling) {
        Color bgColor = ui.gameContext().gameController().isCurrentGameVariant("MS_PACMAN") ? Color.RED : Color.BLUE;
        var font = ui.assets().font("font.monospaced", Math.max(6, 14 * scaling));
        var helpPane = HelpInfo.build(ui).createPane(ui, colorWithOpacity(bgColor, 0.8), font);
        helpPopUp.setTranslateX(10 * scaling);
        helpPopUp.setTranslateY(30 * scaling);
        helpPopUp.setContent(helpPane);
        helpPopUp.show(Duration.seconds(1.5));
    }
}