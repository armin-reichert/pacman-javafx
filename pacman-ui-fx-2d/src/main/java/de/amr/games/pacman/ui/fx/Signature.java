/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public class Signature {

	private final List<Text> texts;
	private final TextFlow sentence = new TextFlow();
	private final Transition animation;

	public Signature(String... words) {
		texts = Stream.of(words).map(Text::new).collect(Collectors.toList());
		sentence.getChildren().addAll(texts);
		setTextColor(Color.WHEAT);

		var fadeIn = new FadeTransition(Duration.seconds(5), sentence);
		fadeIn.setFromValue(0);
		fadeIn.setToValue(1);

		var fadeOut = new FadeTransition(Duration.seconds(1), sentence);
		fadeOut.setFromValue(1);
		fadeOut.setToValue(0);
		//fadeOut.setDelay(Duration.seconds(10)); // for testing

		animation = new SequentialTransition(fadeIn, fadeOut);
	}

	public Text getText(int index) {
		return texts.get(index);
	}

	public void setTextColor(Color color) {
		for (Text text : texts) {
			text.setFill(color);
		}
	}

	public TextFlow root() {
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