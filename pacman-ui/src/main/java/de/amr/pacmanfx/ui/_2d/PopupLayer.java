/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.uilib.widgets.FadingPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import static de.amr.pacmanfx.uilib.Ufx.opaqueColor;

/**
 * @author Armin Reichert
 */
public class PopupLayer extends Pane {

    private final FadingPane helpPopUp = new FadingPane();

    public PopupLayer(CrudeCanvasContainer canvasContainer) {
        getChildren().addAll(helpPopUp);

        minHeightProperty().bind(canvasContainer.minHeightProperty());
        maxHeightProperty().bind(canvasContainer.maxHeightProperty());
        prefHeightProperty().bind(canvasContainer.prefHeightProperty());
        minWidthProperty().bind(canvasContainer.minWidthProperty());
        maxWidthProperty().bind(canvasContainer.maxWidthProperty());
        prefWidthProperty().bind(canvasContainer.prefWidthProperty());
    }

    public void showHelp(GameUI ui, double scaling) {
        Color bgColor = ui.theGameContext().theGameController().isSelected("MS_PACMAN") ? Color.RED : Color.BLUE;
        var font = ui.theAssets().font("font.monospaced", Math.max(6, 14 * scaling));
        var helpPane = HelpInfo.build(ui).createPane(ui, opaqueColor(bgColor, 0.8), font);
        helpPopUp.setTranslateX(10 * scaling);
        helpPopUp.setTranslateY(30 * scaling);
        helpPopUp.setContent(helpPane);
        helpPopUp.show(Duration.seconds(1.5));
    }
}