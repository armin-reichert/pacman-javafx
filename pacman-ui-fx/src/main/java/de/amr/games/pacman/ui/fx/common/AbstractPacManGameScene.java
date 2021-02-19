package de.amr.games.pacman.ui.fx.common;

import java.util.Optional;

import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.FlashMessage;
import de.amr.games.pacman.ui.PacManGameAnimation;
import de.amr.games.pacman.ui.fx.PacManGameFXUI;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Common base class for scenes. Each game scene corresponds to a JavaFX scene.
 * 
 * @author Armin Reichert
 */
public abstract class AbstractPacManGameScene<R extends SceneRendering> implements PacManGameScene {

	protected final Scene fxScene;
	protected final Keyboard keyboard;
	protected final GraphicsContext g;
	protected final R rendering;
	protected final SoundManager soundManager;
	protected final PacManGameModel game;

	private final Text flashMessageView;

	public AbstractPacManGameScene(double width, double height, double scaling, PacManGameModel game, R rendering,
			SoundManager soundManager) {

		Canvas canvas = new Canvas(width, height);
		g = canvas.getGraphicsContext2D();
		g.scale(scaling, scaling);
		this.game = game;
		this.rendering = rendering;
		this.soundManager = soundManager;

		StackPane pane = new StackPane();

		flashMessageView = new Text();
		flashMessageView.setFont(Font.font("Serif", FontWeight.BOLD, 10 * scaling));
		flashMessageView.setFill(Color.YELLOW);
		StackPane.setAlignment(flashMessageView, Pos.BOTTOM_CENTER);

		pane.getChildren().addAll(canvas, flashMessageView);

		fxScene = new Scene(pane, width, height);
		keyboard = new Keyboard(fxScene);
	}

	public void fill(Color color) {
		g.setFill(color);
		g.fillRect(0, 0, g.getCanvas().getWidth(), g.getCanvas().getHeight());
	}

	@Override
	public GraphicsContext gc() {
		return g;
	}

	@Override
	public void start() {
	}

	@Override
	public void end() {
	}

	@Override
	public Scene getFXScene() {
		return fxScene;
	}

	@Override
	public Keyboard keyboard() {
		return keyboard;
	}

	@Override
	public Optional<PacManGameAnimation> animation() {
		return rendering instanceof PacManGameAnimation ? Optional.of(rendering) : Optional.empty();
	}

	protected void drawFlashMessage() {
		Optional<FlashMessage> message = PacManGameFXUI.flashMessage();
		if (message.isPresent()) {
			CountdownTimer timer = message.get().timer;
			double alpha = Math.cos((timer.running() * Math.PI / 2.0) / timer.getDuration());
			flashMessageView.setFill(Color.rgb(255, 255, 0, alpha));
		}
		flashMessageView.setText(message.map(msg -> msg.text).orElse(null));
	}
}