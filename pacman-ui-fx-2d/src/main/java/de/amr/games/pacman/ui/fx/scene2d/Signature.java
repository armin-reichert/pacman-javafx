/*
MIT License

Copyright (c) 2023 Armin Reichert

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

package de.amr.games.pacman.ui.fx.scene2d;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class Signature {

	private Text remakeText;
	private Text nameText;
	private final TextFlow sentence;
	private final FadeTransition fadeIn;
	private final FadeTransition fadeOut;
	private final Transition animation;

	public Signature() {
		remakeText = new Text("Remake (2023) by ");
		remakeText.setFill(Color.gray(0.6));
		remakeText.setFont(Font.font("Helvetica", 9));

		nameText = new Text("Armin Reichert");
		nameText.setFill(Color.gray(0.6));
		nameText.setFont(Font.font("Serif", 9));

		sentence = new TextFlow(remakeText, nameText);

		fadeIn = new FadeTransition(Duration.seconds(5), sentence);
		fadeIn.setFromValue(0);
		fadeIn.setToValue(1);
		fadeIn.setInterpolator(Interpolator.EASE_IN);

		fadeOut = new FadeTransition(Duration.seconds(1), sentence);
		fadeOut.setFromValue(1);
		fadeOut.setToValue(0);

		animation = new SequentialTransition(fadeIn, fadeOut);
	}

	public void setNameFont(Font font) {
		nameText.setFont(font);
	}

	public Node root() {
		return sentence;
	}

	public void show(double x, double y) {
		sentence.setTranslateX(x);
		sentence.setTranslateY(y);
		animation.play();
	}

	public void hide() {
		animation.stop();
		sentence.setOpacity(0);
	}
}