/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.help;

import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.action.core.GameActionContext;
import de.amr.pacmanfx.uilib.rendering.ArcadePalette;
import de.amr.pacmanfx.uilib.widgets.FadingPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import static de.amr.pacmanfx.uilib.Ufx.colorWithOpacity;

public class HelpView extends Pane {

    private final FadingPane popup = new FadingPane();

    public HelpView(Region parent) {
        setMouseTransparent(true);
        getChildren().addAll(popup);

        minWidthProperty().bind(parent.minWidthProperty());
        maxWidthProperty().bind(parent.maxWidthProperty());
        prefWidthProperty().bind(parent.prefWidthProperty());

        minHeightProperty().bind(parent.minHeightProperty());
        maxHeightProperty().bind(parent.maxHeightProperty());
        prefHeightProperty().bind(parent.prefHeightProperty());
    }

    public void showHelpPopup(GameActionContext actionContext, double scaling, String variantName) {
        final boolean msPacMan = variantName.equals(GameVariantID.ARCADE_MS_PACMAN.name())
                || variantName.equals(GameVariantID.ARCADE_MS_PACMAN_XXL.name());
        final Color bgColor = msPacMan ? ArcadePalette.ARCADE_RED : ArcadePalette.ARCADE_BLUE;
        final var font = GlobalAssets.PredefinedFont.MONOSPACED.font(Math.max(6, 14 * scaling));
        final var infoPane = HelpInfo.build(actionContext).createPane(actionContext, colorWithOpacity(bgColor, 0.8), font);
        popup.setTranslateX(10 * scaling);
        popup.setTranslateY(30 * scaling);
        popup.setContent(infoPane);
        popup.show(Duration.seconds(1.5));
    }
}