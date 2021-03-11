package de.amr.games.pacman.ui.fx.mspacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.fx.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.rendering.PacManGameRendering2D;
import de.amr.games.pacman.ui.mspacman.MsPacMan_IntroScene_Controller;
import de.amr.games.pacman.ui.mspacman.MsPacMan_IntroScene_Controller.Phase;
import de.amr.games.pacman.ui.sound.SoundManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Intro scene of the Ms. Pac-Man game. The ghosts and Ms. Pac-Man are introduced one after another.
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntroScene extends AbstractGameScene2D {

	private MsPacMan_IntroScene_Controller sceneController;
	private TickTimer boardAnimationTimer = new TickTimer();

	public MsPacMan_IntroScene(PacManGameController controller, PacManGameRendering2D rendering, SoundManager sounds) {
		super(controller, rendering, sounds);
	}

	@Override
	public void start() {
		boardAnimationTimer.reset();
		boardAnimationTimer.start();
		sceneController = new MsPacMan_IntroScene_Controller(controller, rendering, sounds);
		sceneController.start();
	}

	@Override
	public void end() {
	}

	@Override
	public void update() {
		sceneController.update();
		boardAnimationTimer.tick();

		clearCanvas();
		GraphicsContext g = canvas.getGraphicsContext2D();
		g.setFont(rendering.getScoreFont());
		g.setFill(Color.ORANGE);
		g.fillText("\"MS PAC-MAN\"", t(8), t(5));
		drawAnimatedBoard(g, 32, 16);
		if (sceneController.phase == Phase.PRESENTING_GHOST) {
			drawPresentingGhost(g, sceneController.ghosts[sceneController.currentGhostIndex]);
		} else if (sceneController.phase == Phase.PRESENTING_MSPACMAN) {
			drawStarringMsPacMan(g);
		} else if (sceneController.phase == Phase.END) {
			drawStarringMsPacMan(g);
			drawPointsAnimation(g, 26);
			drawPressKeyToStart(g, 32);
		}
		for (Ghost ghost : sceneController.ghosts) {
			rendering.drawGhost(g, ghost, false);
		}
		rendering.drawPlayer(g, sceneController.msPacMan);
	}

	private void drawPresentingGhost(GraphicsContext g, Ghost ghost) {
		g.setFill(Color.WHITE);
		g.setFont(rendering.getScoreFont());
		if (ghost == sceneController.ghosts[0]) {
			g.fillText("WITH", t(8), t(11));
		}
		g.setFill(ghost.id == 0 ? Color.RED : ghost.id == 1 ? Color.PINK : ghost.id == 2 ? Color.CYAN : Color.ORANGE);
		g.fillText(ghost.name.toUpperCase(), t(13 - ghost.name.length() / 2), t(14));
	}

	private void drawStarringMsPacMan(GraphicsContext g) {
		g.setFill(Color.WHITE);
		g.setFont(rendering.getScoreFont());
		g.fillText("STARRING", t(8), t(11));
		g.setFill(Color.YELLOW);
		g.fillText("MS PAC-MAN", t(8), t(14));
	}

	private void drawAnimatedBoard(GraphicsContext g, int numDotsX, int numDotsY) {
		long time = boardAnimationTimer.ticked();
		int light = (int) (time / 2) % (numDotsX / 2);
		for (int dot = 0; dot < 2 * (numDotsX + numDotsY); ++dot) {
			int x = 0, y = 0;
			if (dot <= numDotsX) {
				x = dot;
			} else if (dot < numDotsX + numDotsY) {
				x = numDotsX;
				y = dot - numDotsX;
			} else if (dot < 2 * numDotsX + numDotsY + 1) {
				x = 2 * numDotsX + numDotsY - dot;
				y = numDotsY;
			} else {
				y = 2 * (numDotsX + numDotsY) - dot;
			}
			g.setFill((dot + light) % (numDotsX / 2) == 0 ? Color.PINK : Color.RED);
			g.fillRect(t(sceneController.tileBoardTopLeft.x) + 4 * x, t(sceneController.tileBoardTopLeft.y) + 4 * y, 2, 2);
		}
	}

	private void drawPressKeyToStart(GraphicsContext g, int tileY) {
		if (sceneController.blinking.frame()) {
			String text = "PRESS SPACE TO PLAY";
			g.setFill(Color.ORANGE);
			g.setFont(rendering.getScoreFont());
			g.fillText(text, t(13 - text.length() / 2), t(tileY));
		}
	}

	private void drawPointsAnimation(GraphicsContext g, int tileY) {
		int x = t(10), y = t(tileY);
		if (sceneController.blinking.frame()) {
			g.setFill(Color.PINK);
			g.fillOval(x, y + t(1) - 2, 10, 10);
			g.fillRect(x + 6, y - t(1) + 2, 2, 2);
		}
		g.setFill(Color.WHITE);
		g.setFont(rendering.getScoreFont());
		g.fillText("10", x + t(2), y);
		g.fillText("50", x + t(2), y + t(2));
		g.setFont(Font.font(rendering.getScoreFont().getName(), 6));
		g.fillText("PTS", x + t(5), y);
		g.fillText("PTS", x + t(5), y + t(2));
	}
}