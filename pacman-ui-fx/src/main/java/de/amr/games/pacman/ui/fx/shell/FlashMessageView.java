package de.amr.games.pacman.ui.fx.shell;

import java.util.ArrayDeque;
import java.util.Deque;

import de.amr.games.pacman.lib.TickTimer;
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
	
	static class FlashMessage {
		
		private final TickTimer timer = new TickTimer(this.toString());
		public final String text;
		
		public FlashMessage(String text, long ticks) {
			this.text = text;
			timer.reset(ticks);
		}
	}

	private final Deque<FlashMessage> messagesQ = new ArrayDeque<>();
	private final Text display = new Text();
	private Color displayColor;

	public FlashMessageView() {
		displayColor = Color.WHEAT;
		display.setFont(Font.font("Sans", FontWeight.BOLD, 30));
		display.setTextAlignment(TextAlignment.CENTER);
		setAlignment(Pos.CENTER);
		getChildren().add(display);
	}

	public void showMessage(String message, long ticks) {
		if (messagesQ.peek() != null && messagesQ.peek().text.equals(message)) {
			messagesQ.poll();
		}
		messagesQ.add(new FlashMessage(message, ticks));
	}

	public void update() {
		if (messagesQ.isEmpty()) {
			setVisible(false);
		} else {
			setVisible(true);
			FlashMessage message = messagesQ.peek();
			if (message.timer.hasExpired()) {
				messagesQ.remove();
				return;
			}
			double alpha = Math.cos((message.timer.ticked() * Math.PI / 2.0) / message.timer.duration());
			BackgroundFill bgFill = new BackgroundFill(Color.rgb(0, 0, 0, 0.2 + 0.5 * alpha), CornerRadii.EMPTY,
					Insets.EMPTY);
			setBackground(new Background(bgFill));
			display.setFill(displayColor.deriveColor(0, 1, 1, alpha));
			display.setText(message.text);
			if (!message.timer.isRunning()) {
				message.timer.start();
			}
			message.timer.tick();
		}
	}
}