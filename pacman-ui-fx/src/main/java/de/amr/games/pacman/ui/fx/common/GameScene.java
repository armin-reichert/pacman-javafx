package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX.UNSCALED_SCENE_HEIGHT_PX;
import static de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX.UNSCALED_SCENE_WIDTH_PX;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.util.Objects;
import java.util.Optional;

import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.FlashMessage;
import de.amr.games.pacman.ui.fx.rendering.FXRendering;
import javafx.geometry.Pos;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 * A game scene.
 * 
 * @author Armin Reichert
 */
public abstract class GameScene {

	public final double width, height;
	public final double scaling;

	public final Group root;

	protected final Canvas canvas;
	public final Text flashMessageView;
	public final Text camInfo;

	public final FXRendering rendering;
	public final SoundManager sounds;

	protected GameModel game;

	public GameScene(double scaling, FXRendering rendering, SoundManager sounds) {
		this.scaling = scaling;
		this.rendering = Objects.requireNonNull(rendering);
		this.sounds = Objects.requireNonNull(sounds);

		width = UNSCALED_SCENE_WIDTH_PX * scaling;
		height = UNSCALED_SCENE_HEIGHT_PX * scaling;

		StackPane layout = new StackPane();

		canvas = new Canvas(width, height);
		canvas.setViewOrder(1);

		flashMessageView = new Text();
		flashMessageView.setFont(Font.font("Serif", FontWeight.BOLD, 10 * scaling));
		flashMessageView.setFill(Color.YELLOW);
		StackPane.setAlignment(flashMessageView, Pos.BOTTOM_CENTER);

		camInfo = new Text();
		camInfo.setTextAlignment(TextAlignment.CENTER);
		camInfo.setFill(Color.WHITE);
		camInfo.setFont(Font.font("Sans", 6 * scaling));
		StackPane.setAlignment(camInfo, Pos.CENTER);

		layout.getChildren().addAll(camInfo, flashMessageView, canvas);

		root = new Group();
		root.getChildren().add(layout);
	}

	public void start() {
	}

	public abstract void update();

	public void end() {
	}

	protected abstract void drawCanvas();

	public final void draw() {
		GraphicsContext g = canvas.getGraphicsContext2D();
		g.save();
		g.scale(scaling, scaling);
		clearCanvas();
		drawCanvas();
		g.restore();
	}

	public void updateCamera(Camera cam) {
	}

	public void setGame(GameModel game) {
		this.game = game;
	}

	public Optional<GameModel> game() {
		return Optional.ofNullable(game);
	}

	public void clearCanvas() {
		GraphicsContext g = canvas.getGraphicsContext2D();
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}

	// TODO do this more "FX-like"
	public void drawFlashMessage(FlashMessage message) {
		if (message != null) {
			GraphicsContext g = canvas.getGraphicsContext2D();
			g.setFill(Color.BLACK);
			g.fillRect(0, g.getCanvas().getHeight() - t(4), g.getCanvas().getWidth(), t(4));
			CountdownTimer timer = message.timer;
			double alpha = Math.cos((timer.running() * Math.PI / 2.0) / timer.getDuration());
			flashMessageView.setFill(Color.rgb(255, 255, 0, alpha));
			flashMessageView.setText(message.text);
		} else {
			flashMessageView.setVisible(false);
			flashMessageView.setText(null);
		}
	}
}