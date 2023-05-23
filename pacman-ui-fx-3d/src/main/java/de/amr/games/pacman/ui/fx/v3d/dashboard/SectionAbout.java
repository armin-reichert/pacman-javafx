/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.ui.fx.v3d.app.PacManGames3dUI;
import javafx.geometry.Insets;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * @author Armin Reichert
 */
public class SectionAbout extends Section {

	public SectionAbout(PacManGames3dUI ui, String title) {
		super(ui, title, Dashboard.MIN_LABEL_WIDTH, Dashboard.TEXT_COLOR, Dashboard.TEXT_FONT, Dashboard.LABEL_FONT);

		var theAuthorInYoungerYears = new ImageView(ui.assets().armin1970);
		theAuthorInYoungerYears.setFitWidth(286);
		theAuthorInYoungerYears.setPreserveRatio(true);

		var madeBy = new Text("Made by     ");
		madeBy.setFont(Font.font("Helvetica", 16));
		madeBy.setFill(Color.grayRgb(150));

		var signature = new Text("Armin Reichert");
		var theme = ui.assets().arcadeTheme; // TODO
		var font = theme.font("font.handwriting.9", 18);
		signature.setFont(font);
		signature.setFill(Color.grayRgb(225));

		var tf = new TextFlow(madeBy, signature);
		tf.setPadding(new Insets(5, 5, 5, 5));
		content.add(theAuthorInYoungerYears, 0, 0);
		content.add(tf, 0, 1);
	}
}