package de.amr.games.pacman.ui.fx.pacman;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.ui.pacman.PacMan_IntroAnimation.TOP_Y;
import static de.amr.games.pacman.world.PacManGameWorld.TS;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.heaven.God;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.fx.common.GameScene;
import de.amr.games.pacman.ui.fx.rendering.FXRendering;
import de.amr.games.pacman.ui.pacman.PacMan_IntroAnimation;
import de.amr.games.pacman.ui.pacman.PacMan_IntroAnimation.GhostPortrait;
import de.amr.games.pacman.ui.pacman.PacMan_IntroAnimation.Phase;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Intro scene of the PacMan game.
 * <p>
 * The ghost are presented one after another, then Pac-Man is chased by the ghosts, turns the card
 * and hunts the ghost himself.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntroScene extends GameScene {

	private PacMan_IntroAnimation animation;

	public PacMan_IntroScene(PacManGameController controller, double scaling, FXRendering rendering,
			SoundManager sounds) {
		super(controller, scaling, rendering, sounds);
	}

	@Override
	public void start() {
		animation = new PacMan_IntroAnimation(controller, rendering);
		animation.start();
		log("%s: PacMan intro scene started at clock tick %d", this, God.clock.ticksTotal);
	}

	@Override
	public void update() {
		animation.update();
	}

	@Override
	public void draw(GraphicsContext g) {
		rendering.drawScore(g, controller.getGame(), true);
		drawGallery(g);
		if (animation.phase == Phase.CHASING_PAC) {
			if (animation.blinking.animate()) {
				g.setFill(Color.PINK);
				g.fillOval(t(2), animation.pac.position.y, TS, TS);
			}
		}
		drawGuys(g);
		if (animation.phase.ordinal() >= Phase.CHASING_GHOSTS.ordinal()) {
			drawPointsAnimation(g, 11, 26);
		}
		if (animation.phase == Phase.READY_TO_PLAY) {
			drawPressKeyToStart(g, 32);
		}
	}

	private void drawGuys(GraphicsContext g) {
		rendering.drawPlayer(g, animation.pac);
		for (Ghost ghost : animation.ghosts) {
			rendering.drawGhost(g, ghost, animation.pac.powerTicksLeft > 0);
		}
	}

	private void drawGallery(GraphicsContext g) {
		g.setFill(Color.WHITE);
		g.setFont(rendering.getScoreFont());
		g.fillText("CHARACTER", t(6), TOP_Y);
		g.fillText("/", t(16), TOP_Y);
		g.fillText("NICKNAME", t(18), TOP_Y);
		for (int i = 0; i < 4; ++i) {
			GhostPortrait portrait = animation.gallery[i];
			if (portrait.ghost.visible) {
				int y = TOP_Y + t(2 + 3 * i);
				rendering.drawGhost(g, animation.gallery[i].ghost, false);
				g.setFill(getGhostColor(i));
				g.setFont(rendering.getScoreFont());
				if (portrait.characterVisible) {
					g.fillText("-" + portrait.character, t(6), y + 8);
				}
				if (portrait.nicknameVisible) {
					g.fillText("\"" + portrait.ghost.name + "\"", t(18), y + 8);
				}
			}
		}
	}

	private Color getGhostColor(int i) {
		return i == 0 ? Color.RED : i == 1 ? Color.PINK : i == 2 ? Color.CYAN : Color.ORANGE;
	}

	private void drawPressKeyToStart(GraphicsContext g, int yTile) {
		if (animation.blinking.frame()) {
			String text = "PRESS SPACE TO PLAY";
			g.setFill(Color.ORANGE);
			g.setFont(rendering.getScoreFont());
			g.fillText(text, t(14 - text.length() / 2), t(yTile));
		}
	}

	private void drawPointsAnimation(GraphicsContext g, int tileX, int tileY) {
		if (animation.blinking.frame()) {
			g.setFill(Color.PINK);
			g.fillRect(t(tileX) + 6, t(tileY - 1) + 2, 2, 2);
			g.fillOval(t(tileX), t(tileY + 1) - 2, 10, 10);
		}
		g.setFill(Color.WHITE);
		g.setFont(rendering.getScoreFont());
		g.fillText("10", t(tileX + 2), t(tileY));
		g.fillText("50", t(tileX + 2), t(tileY + 2));
		g.setFont(Font.font(rendering.getScoreFont().getName(), 6));
		g.fillText("PTS", t(tileX + 5), t(tileY));
		g.fillText("PTS", t(tileX + 5), t(tileY + 2));
	}
}