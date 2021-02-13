package de.amr.games.pacman.ui.fx.scene.common;

import java.util.Optional;

import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.PacManGameAnimations;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering.MsPacManGameRendering;
import de.amr.games.pacman.ui.fx.rendering.PacManGameRendering;
import de.amr.games.pacman.ui.fx.rendering.RenderingWithAnimatedSprites;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public abstract class AbstractPacManGameScene implements PacManGameScene {

	protected final Scene scene;
	protected final Keyboard keyboard;
	protected final PacManGameModel game;
	protected final GraphicsContext g;
	protected final RenderingWithAnimatedSprites rendering;
	protected final SoundManager soundManager;

	public AbstractPacManGameScene(PacManGameModel game, SoundManager soundManager, double width, double height,
			double scaling, boolean msPacMan) {
		this.game = game;
		this.soundManager = soundManager;
		Canvas canvas = new Canvas(width, height);
		g = canvas.getGraphicsContext2D();
		g.scale(scaling, scaling);
		StackPane pane = new StackPane();
		pane.getChildren().add(canvas);
		scene = new Scene(pane, width, height);
		keyboard = new Keyboard(scene);
		rendering = msPacMan ? new MsPacManGameRendering(g) : new PacManGameRendering(g);
	}

	public void fill(Color color) {
		g.setFill(color);
		g.fillRect(0, 0, g.getCanvas().getWidth(), g.getCanvas().getHeight());
	}

	@Override
	public void start() {
	}

	@Override
	public void end() {
	}

	@Override
	public Scene getFXScene() {
		return scene;
	}

	@Override
	public Keyboard keyboard() {
		return keyboard;
	}

	@Override
	public Optional<PacManGameAnimations> animations() {
		return rendering instanceof PacManGameAnimations ? Optional.of(rendering) : Optional.empty();
	}

	public Rectangle2D tileRegion(int tileX, int tileY, int cols, int rows) {
		return new Rectangle2D(tileX * 16, tileY * 16, cols * 16, rows * 16);
	}
}