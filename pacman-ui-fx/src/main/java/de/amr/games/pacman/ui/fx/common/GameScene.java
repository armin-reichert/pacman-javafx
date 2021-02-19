package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.ui.fx.PacManGameFXUI.msPacManRendering;
import static de.amr.games.pacman.ui.fx.PacManGameFXUI.pacManRendering;

import java.util.Optional;

import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.FlashMessage;
import de.amr.games.pacman.ui.PacManGameAnimation;
import de.amr.games.pacman.ui.fx.PacManGameFXUI;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Each game scene corresponds to a JavaFX scene.
 * 
 * @author Armin Reichert
 */
public class GameScene<RENDERING extends SceneRendering> extends Scene {

	protected final Text flashMessageView;
	protected final Keyboard keyboard;
	protected final GraphicsContext g;
	protected final int gameType;
	protected PacManGameModel game;

	public GameScene(Group root, double width, double height, double scaling, int gameType) {
		super(root, width, height, Color.BLACK);
		this.gameType = gameType;
		Canvas canvas = new Canvas(width, height);
		g = canvas.getGraphicsContext2D();
		g.scale(scaling, scaling);
		StackPane pane = new StackPane();
		root.getChildren().add(pane);
		flashMessageView = new Text();
		flashMessageView.setFont(Font.font("Serif", FontWeight.BOLD, 10 * scaling));
		flashMessageView.setFill(Color.YELLOW);
		StackPane.setAlignment(flashMessageView, Pos.BOTTOM_CENTER);
		pane.getChildren().addAll(canvas, flashMessageView);
		keyboard = new Keyboard(this);
	}

	public void update() {
	}

	public void render() {

	}

	public void setGame(PacManGameModel game) {
		this.game = game;
	}

	@SuppressWarnings("unchecked")
	public RENDERING rendering() {
		return (RENDERING) (gameType == PacManGameFXUI.MS_PACMAN ? msPacManRendering : pacManRendering);
	}

	public SoundManager soundManager() {
		return PacManGameFXUI.sounds[gameType];
	}

	public Optional<PacManGameModel> game() {
		return Optional.ofNullable(game);
	}

	public void fill(Color color) {
		g.setFill(color);
		g.fillRect(0, 0, g.getCanvas().getWidth(), g.getCanvas().getHeight());
	}

	public GraphicsContext gc() {
		return g;
	}

	public void start() {
	}

	public void end() {
	}

	public Keyboard keyboard() {
		return keyboard;
	}

	public Optional<PacManGameAnimation> animation() {
		return rendering() instanceof PacManGameAnimation ? Optional.of(rendering()) : Optional.empty();
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