/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.ui2d.GameContext;
import javafx.geometry.Insets;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * @author Armin Reichert
 */
public class InfoBoxAbout extends InfoBox {

    public void init(GameContext context) {
        super.init(context);

        var theAuthor = new ImageView(context.assets().image("photo.armin1970"));
        theAuthor.setFitWidth(250);
        theAuthor.setPreserveRatio(true);

        var madeBy = new Text("Made by     ");
        madeBy.setFont(Font.font("Helvetica", 16));
        madeBy.setFill(Color.grayRgb(150));

        var signature = new Text("Armin Reichert");
        var font = context.assets().font("font.handwriting", 18);
        signature.setFont(font);
        signature.setFill(Color.grayRgb(225));

        var tf = new TextFlow(madeBy, signature);
        tf.setPadding(new Insets(5, 5, 5, 5));
        grid.add(theAuthor, 0, 0);
        grid.add(tf, 0, 1);
    }
}