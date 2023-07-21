/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.app;

import javafx.animation.FadeTransition;
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

	private final Text madeByText;
	private final Text nameText;
	private final TextFlow sentence = new TextFlow();
	private final Transition animation;

	public Signature() {
		madeByText = new Text("Remake (2023) by ");
		madeByText.setFill(Color.GOLD);
		madeByText.setFont(Font.font("Helvetica", 14));

		nameText = new Text("Armin Reichert");
		nameText.setFill(Color.GOLD);
		nameText.setFont(Font.font("Serif", 14));

		sentence.getChildren().addAll(madeByText, nameText);

		var fadeIn = new FadeTransition(Duration.seconds(5), sentence);
		fadeIn.setFromValue(0);
		fadeIn.setToValue(1);

		var fadeOut = new FadeTransition(Duration.seconds(1), sentence);
		fadeOut.setFromValue(1);
		fadeOut.setToValue(0);

		animation = new SequentialTransition(fadeIn, fadeOut);
	}

	public void setNameFont(Font font) {
		nameText.setFont(font);
	}

	public void setMadeByFont(Font font)
	{
		madeByText.setFont(font);
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