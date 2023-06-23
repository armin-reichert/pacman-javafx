/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.app;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class Signature {

	private Text remakeText;
	private Text nameText;
	// Does not work in GWT
//	private final TextFlow sentence;
	private final GridPane sentence;
	private final FadeTransition fadeIn;
	private final FadeTransition fadeOut;
	private final Transition animation;

	public Signature() {
		remakeText = new Text("Remake (2023) by  ");
		remakeText.setFill(Color.gray(0.6));
		remakeText.setFont(Font.font("Helvetica", 14));

		nameText = new Text("Armin Reichert");
		nameText.setFill(Color.gray(0.6));
		nameText.setFont(Font.font("Serif", 14));

		sentence = new GridPane();
		sentence.setHgap(4); // no effect in GWT
		sentence.add(remakeText, 0, 0);
		GridPane.setValignment(remakeText, VPos.BASELINE);
		sentence.add(nameText, 1, 0);
		GridPane.setValignment(nameText, VPos.BASELINE);

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

	public void setMadeByFont(Font font)
	{
		remakeText.setFont(font);
	}

	public Node root() {
		return sentence;
	}

	public void showAfterSeconds(double afterSeconds) {
		if (afterSeconds > 0) {
			animation.setDelay(Duration.seconds(afterSeconds));
		}
		animation.play();
	}

	public void hide() {
		animation.stop();
		sentence.setOpacity(0);
	}
}