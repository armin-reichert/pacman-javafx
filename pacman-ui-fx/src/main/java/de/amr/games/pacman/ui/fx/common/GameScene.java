package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX.SCENE_HEIGHT_PX;
import static de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX.SCENE_WIDTH_PX;
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
import javafx.scene.PerspectiveCamera;
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
public abstract class GameScene {

	public final Scene fxScene;

	protected final Text flashMessageView;
	protected final GraphicsContext g;
	protected final FXRendering rendering;
	protected final SoundManager sounds;
	protected final double scaling;

	protected GameModel game;

	public GameScene(double scaling, FXRendering rendering, SoundManager sounds) {

		this.scaling = scaling;
		this.rendering = Objects.requireNonNull(rendering);
		this.sounds = Objects.requireNonNull(sounds);

		double width = SCENE_WIDTH_PX * scaling;
		double height = SCENE_HEIGHT_PX * scaling;

		StackPane pane = new StackPane();

		Canvas canvas = new Canvas(width, height);
		g = canvas.getGraphicsContext2D();

		flashMessageView = new Text();
		flashMessageView.setFont(Font.font("Serif", FontWeight.BOLD, 10 * scaling));
		flashMessageView.setFill(Color.YELLOW);
		StackPane.setAlignment(flashMessageView, Pos.BOTTOM_CENTER);

		pane.getChildren().addAll(canvas, flashMessageView);

		fxScene = new Scene(pane, width, height, Color.BLACK);
	}

	public void start() {
	}

	public abstract void update();

	public void end() {
	}

	protected abstract void renderContent();

	public final void render() {
		g.save();
		g.scale(scaling, scaling);
		renderContent();
		g.restore();
	}

	public void setGame(GameModel game) {
		this.game = game;
	}

	public Optional<GameModel> game() {
		return Optional.ofNullable(game);
	}

	public void clear() {
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, g.getCanvas().getWidth(), g.getCanvas().getHeight());
	}

	public void drawFlashMessage(FlashMessage message) {
		if (message != null) {
			g.setFill(Color.BLACK);
			g.fillRect(0, g.getCanvas().getHeight() - t(4), g.getCanvas().getWidth(), t(4));
			CountdownTimer timer = message.timer;
			double alpha = Math.cos((timer.running() * Math.PI / 2.0) / timer.getDuration());
			flashMessageView.setFill(Color.rgb(255, 255, 0, alpha));
			flashMessageView.setText(message.text);
		} else {
			flashMessageView.setText(null);
		}
	}

	@SuppressWarnings("unused")
	private void testCamera() {
		Camera camera = new PerspectiveCamera();
		fxScene.setOnKeyPressed(e -> {
			if (e.isControlDown()) {
				switch (e.getCode()) {
				case DIGIT0:
					camera.setTranslateX(0);
					camera.setTranslateY(0);
					camera.setTranslateZ(0);
					break;
				case LEFT:
					camera.setTranslateX(camera.getTranslateX() + 10);
					break;
				case RIGHT:
					camera.setTranslateX(camera.getTranslateX() - 10);
					break;
				case UP:
					camera.setTranslateY(camera.getTranslateY() + 10);
					break;
				case DOWN:
					camera.setTranslateY(camera.getTranslateY() - 10);
					break;
				case PLUS:
					camera.setTranslateZ(camera.getTranslateZ() + 10);
					break;
				case MINUS:
					camera.setTranslateZ(camera.getTranslateZ() - 10);
					break;
				default:
					break;
				}
			}
		});
		fxScene.setCamera(camera);
	}

}