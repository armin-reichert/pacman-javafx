package de.amr.games.pacman.ui.fx.pacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;
import static de.amr.games.pacman.ui.pacman.PacMan_IntroScene_Controller.TOP_Y;

import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.fx.common.scene2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.rendering.Assets2D;
import de.amr.games.pacman.ui.pacman.PacMan_IntroScene_Controller;
import de.amr.games.pacman.ui.pacman.PacMan_IntroScene_Controller.GhostPortrait;
import de.amr.games.pacman.ui.pacman.PacMan_IntroScene_Controller.Phase;
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
public class PacMan_IntroScene extends AbstractGameScene2D {

	private PacMan_IntroScene_Controller animation;

	public PacMan_IntroScene() {
		super(Assets2D.RENDERING_2D.get(GameVariant.PACMAN), Assets2D.SOUND.get(GameVariant.PACMAN));
	}

	@Override
	public void start() {
		animation = new PacMan_IntroScene_Controller(gameController, rendering);
		animation.start();
	}

	@Override
	public void end() {
	}

	@Override
	public void update() {
		animation.update();
		rendering.drawScore(gc, gameController.game(), true);
		drawGallery();
		if (animation.phase == Phase.CHASING_PAC) {
			if (animation.blinking.animate()) {
				gc.setFill(Color.PINK);
				gc.fillOval(t(2), animation.pac.position.y, TS, TS);
			}
		}
		drawGuys();
		if (animation.phase.ordinal() >= Phase.CHASING_GHOSTS.ordinal()) {
			drawPointsAnimation(11, 26);
		}
		if (animation.phase == Phase.READY_TO_PLAY) {
			drawPressKeyToStart(32);
		}
	}

	private void drawGuys() {
		for (Ghost ghost : animation.ghosts) {
			rendering.drawGhost(gc, ghost, !animation.pac.powerTimer.hasExpired());
		}
		rendering.drawPlayer(gc, animation.pac);
	}

	private void drawGallery() {
		gc.setFill(Color.WHITE);
		gc.setFont(rendering.getScoreFont());
		gc.fillText("CHARACTER", t(6), TOP_Y);
		gc.fillText("/", t(16), TOP_Y);
		gc.fillText("NICKNAME", t(18), TOP_Y);
		for (int i = 0; i < 4; ++i) {
			GhostPortrait portrait = animation.gallery[i];
			if (portrait.ghost.visible) {
				int y = TOP_Y + t(2 + 3 * i);
				rendering.drawGhost(gc, animation.gallery[i].ghost, false);
				gc.setFill(getGhostColor(i));
				gc.setFont(rendering.getScoreFont());
				if (portrait.characterVisible) {
					gc.fillText("-" + portrait.character, t(6), y + 8);
				}
				if (portrait.nicknameVisible) {
					gc.fillText("\"" + portrait.ghost.name + "\"", t(18), y + 8);
				}
			}
		}
	}

	private Color getGhostColor(int i) {
		return i == 0 ? Color.RED : i == 1 ? Color.PINK : i == 2 ? Color.CYAN : Color.ORANGE;
	}

	private void drawPressKeyToStart(int yTile) {
		if (animation.blinking.frame()) {
			String text = "PRESS SPACE TO PLAY";
			gc.setFill(Color.ORANGE);
			gc.setFont(rendering.getScoreFont());
			gc.fillText(text, t(14 - text.length() / 2), t(yTile));
		}
	}

	private void drawPointsAnimation(int tileX, int tileY) {
		if (animation.blinking.frame()) {
			gc.setFill(Color.PINK);
			gc.fillRect(t(tileX) + 6, t(tileY - 1) + 2, 2, 2);
			gc.fillOval(t(tileX), t(tileY + 1) - 2, 10, 10);
		}
		gc.setFill(Color.WHITE);
		gc.setFont(rendering.getScoreFont());
		gc.fillText("10", t(tileX + 2), t(tileY));
		gc.fillText("50", t(tileX + 2), t(tileY + 2));
		gc.setFont(Font.font(rendering.getScoreFont().getName(), 6));
		gc.fillText("PTS", t(tileX + 5), t(tileY));
		gc.fillText("PTS", t(tileX + 5), t(tileY + 2));
	}
}