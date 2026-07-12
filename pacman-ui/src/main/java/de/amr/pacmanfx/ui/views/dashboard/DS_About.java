/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.game.PacManGamesCollection;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class DS_About extends GameDashboardSection {

    static final ResourceManager RM = () -> DS_About.class;
    static final Image ARMIN_1970 = RM.loadImage("/de/amr/pacmanfx/ui/graphics/armin1970.jpg");

    public DS_About() {
        super(DashboardID.ABOUT);
    }

    @Override
    public void connect(PacManGamesCollection game) {
        final var myImage = new ImageView(ARMIN_1970);
        myImage.setFitWidth(250);
        myImage.setPreserveRatio(true);

        final var madeBy = new Text("Created by  ");
        madeBy.setFont(Font.font("Helvetica", 16));

        final var signature = new Text("Armin Reichert");
        signature.setFont(GlobalAssets.PredefinedFont.HANDWRITING.font(20));
        signature.setFill(Color.grayRgb(66));

        final var tf = new TextFlow(madeBy, signature);
        tf.setPadding(new Insets(5, 5, 5, 5));
        grid.add(myImage, 0, 0);
        grid.add(tf, 0, 1);
    }
}