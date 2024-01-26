/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.ui.fx.util.Theme;
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

	public InfoBoxAbout(Theme theme, String title) {
		super(theme, title);

		var theAuthorInYoungerYears = new ImageView(theme.image("image.armin1970"));
		theAuthorInYoungerYears.setFitWidth(286);
		theAuthorInYoungerYears.setPreserveRatio(true);

		var madeBy = new Text("Made by     ");
		madeBy.setFont(Font.font("Helvetica", 16));
		madeBy.setFill(Color.grayRgb(150));

		var signature = new Text("Armin Reichert");
		var font = theme.font("font.handwriting", 18);
		signature.setFont(font);
		signature.setFill(Color.grayRgb(225));

		var tf = new TextFlow(madeBy, signature);
		tf.setPadding(new Insets(5, 5, 5, 5));
		content.add(theAuthorInYoungerYears, 0, 0);
		content.add(tf, 0, 1);
	}
}