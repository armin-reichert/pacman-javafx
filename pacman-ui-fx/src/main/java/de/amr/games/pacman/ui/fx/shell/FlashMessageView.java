/*
MIT License

Copyright (c) 2021 Armin Reichert

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
package de.amr.games.pacman.ui.fx.shell;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class FlashMessageView extends HBox {

	private static class FlashMessage {

		String text;
		double durationSeconds;
		Instant activeFrom;
		Instant activeTo;

		public FlashMessage(String text, double seconds) {
			this.text = text;
			this.durationSeconds = seconds;
		}

		public void activate() {
			activeFrom = Instant.now();
			activeTo = activeFrom.plusMillis(durationMillis());
		}

		public long durationMillis() {
			return Math.round(durationSeconds * 1000);
		}

		public boolean hasExpired() {
			return Instant.now().isAfter(activeTo);
		}
	}

	private final Deque<FlashMessage> activeMessages = new ArrayDeque<>();
	private final Text display = new Text();
	private Color displayColor;

	public FlashMessageView() {
		displayColor = Color.WHEAT;
		display.setFont(Font.font("Sans", FontWeight.BOLD, 30));
		display.setTextAlignment(TextAlignment.CENTER);
		setAlignment(Pos.CENTER);
		getChildren().add(display);
	}

	public void showMessage(String messageText, double seconds) {
		if (activeMessages.peek() != null && activeMessages.peek().text.equals(messageText)) {
			activeMessages.poll();
		}
		FlashMessage message = new FlashMessage(messageText, seconds);
		activeMessages.add(message);
		message.activate();
	}

	public void update() {
		if (activeMessages.isEmpty()) {
			setVisible(false);
			return;
		}
		FlashMessage message = activeMessages.peek();
		if (message.hasExpired()) {
			activeMessages.remove();
			return;
		}
		double activeMillis = Duration.between(message.activeFrom, Instant.now()).toMillis();
		double alpha = Math.cos(0.5 * Math.PI * activeMillis / message.durationMillis());
		Color color = Color.rgb(0, 0, 0, 0.2 + 0.5 * alpha);
		setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
		display.setFill(displayColor.deriveColor(0, 1, 1, alpha));
		display.setText(message.text);
		setVisible(true);
	}
}