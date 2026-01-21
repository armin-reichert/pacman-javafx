/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.ui.ArcadePalette;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.widgets.FadingPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import static de.amr.pacmanfx.uilib.Ufx.colorWithOpacity;

public class HelpLayer extends Pane {

    private final FadingPane popup = new FadingPane();

    public HelpLayer(Region master) {
        setMouseTransparent(true);
        getChildren().addAll(popup);

        minWidthProperty().bind(master.minWidthProperty());
        maxWidthProperty().bind(master.maxWidthProperty());
        prefWidthProperty().bind(master.prefWidthProperty());

        minHeightProperty().bind(master.minHeightProperty());
        maxHeightProperty().bind(master.maxHeightProperty());
        prefHeightProperty().bind(master.prefHeightProperty());
    }

    public void showHelpPopup(GameUI ui, double scaling, String variantName) {
        final boolean msPacMan = variantName.equals(GameVariant.ARCADE_MS_PACMAN.name())
                || variantName.equals(GameVariant.ARCADE_MS_PACMAN_XXL.name());
        final Color bgColor = msPacMan ? ArcadePalette.ARCADE_RED : ArcadePalette.ARCADE_BLUE;
        final var font = Ufx.deriveFont(GameUI.FONT_MONOSPACED, Math.max(6, 14 * scaling));
        final var infoPane = HelpInfo.build(ui).createPane(ui, colorWithOpacity(bgColor, 0.8), font);
        popup.setTranslateX(10 * scaling);
        popup.setTranslateY(30 * scaling);
        popup.setContent(infoPane);
        popup.show(Duration.seconds(1.5));
    }
}