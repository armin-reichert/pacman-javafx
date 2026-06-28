/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class DS_About extends DashboardSection {

    public DS_About() {}

    @Override
    public void connect(Game game) {
        final ResourceManager rm = () -> DS_About.class;
        final Image armin1970 = rm.loadImage("/de/amr/pacmanfx/ui/graphics/armin1970.jpg");
        final Font handwriting = rm.loadFont("/de/amr/pacmanfx/ui/fonts/Molle-Italic.ttf", 20);

        final var myImage = new ImageView(armin1970);
        myImage.setFitWidth(250);
        myImage.setPreserveRatio(true);

        final var madeBy = new Text("Made by    ");
        madeBy.setFont(Font.font("Helvetica", 16));
        madeBy.setFill(Color.grayRgb(150));

        final var signature = new Text("Armin Reichert");
        signature.setFont(handwriting);
        signature.setFill(Color.grayRgb(225));

        final var tf = new TextFlow(madeBy, signature);
        tf.setPadding(new Insets(5, 5, 5, 5));
        grid.add(myImage, 0, 0);
        grid.add(tf, 0, 1);
    }
}