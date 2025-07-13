/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.PacManGames_UI_Impl;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * @author Armin Reichert
 */
public class InfoBoxAbout extends InfoBox {

    public InfoBoxAbout(GameContext gameContext) {
        super(gameContext);
    }

    @Override
    public void init(GameUI ui) {
        ResourceManager rm = () -> PacManGames_UI_Impl.class;
        Image armin1970 = rm.loadImage("graphics/armin1970.jpg");
        Font handwriting = rm.loadFont("fonts/Molle-Italic.ttf", 20);

        var myImage = new ImageView(armin1970);
        myImage.setFitWidth(250);
        myImage.setPreserveRatio(true);

        var madeBy = new Text("Made by    ");
        madeBy.setFont(Font.font("Helvetica", 16));
        madeBy.setFill(Color.grayRgb(150));

        var signature = new Text("Armin Reichert");
        signature.setFont(handwriting);
        signature.setFill(Color.grayRgb(225));

        var tf = new TextFlow(madeBy, signature);
        tf.setPadding(new Insets(5, 5, 5, 5));
        grid.add(myImage, 0, 0);
        grid.add(tf, 0, 1);
    }
}