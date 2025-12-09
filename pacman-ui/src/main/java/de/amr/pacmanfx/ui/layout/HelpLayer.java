/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.model.StandardGameVariant;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.uilib.widgets.CanvasDecorationPane;
import de.amr.pacmanfx.uilib.widgets.FadingPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
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
        boolean msPacMan = ui.context().gameVariantName().equals(StandardGameVariant.MS_PACMAN.name())
            || ui.context().gameVariantName().equals(StandardGameVariant.MS_PACMAN_XXL.name());
        Color bgColor = msPacMan ? Color.RED : Color.BLUE;
        var font = Font.font(ui.globalAssets().font_Monospaced.getFamily(), Math.max(6, 14 * scaling));
        var helpPane = HelpInfo.build(ui).createPane(ui, colorWithOpacity(bgColor, 0.8), font);
        helpPopUp.setTranslateX(10 * scaling);
        helpPopUp.setTranslateY(30 * scaling);
        helpPopUp.setContent(helpPane);
        helpPopUp.show(Duration.seconds(1.5));
    }
}