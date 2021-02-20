package de.amr.games.pacman.ui.fx.mspacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.GhostState;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.ui.fx.PacManGameFXUI;
import de.amr.games.pacman.ui.fx.common.GameScene;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Intro scene of the Ms. Pac-Man game. The ghosts and Ms. Pac-Man are introduced one after another.
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntroScene extends GameScene {

	enum Phase {

		BEGIN, GHOSTS, MSPACMAN, END;

		final CountdownTimer timer = new CountdownTimer();
	}

	private final V2i frameTopLeftTile = new V2i(6, 8);
	private final int belowFrame = t(17);
	private final int leftOfFrame = t(4);
	private final Animation<Boolean> blinking = Animation.pulse().frameDuration(30).restart();

	private final MsPacMan_SceneRendering rendering = PacManGameFXUI.MS_PACMAN_RENDERING;

	private Phase phase;

	private Pac msPac;
	private Ghost[] ghosts;

	private Ghost currentGhost;
	private boolean presentingMsPac;

	public MsPacMan_IntroScene(Group root, double width, double height, double scaling) {
		super(root, width, height, scaling);
	}

	private void enter(Phase newPhase, long ticks) {
		phase = newPhase;
		phase.timer.setDuration(ticks);
	}

	@Override
	public void start() {
		log("Intro scene started at clock time %d", clock.ticksTotal);

		msPac = new Pac("Ms. Pac-Man", LEFT);
		msPac.setPosition(t(37), belowFrame);
		msPac.visible = true;
		msPac.speed = 0;
		msPac.dead = false;
		msPac.dir = LEFT;

		ghosts = new Ghost[] { //
				new Ghost(0, "Blinky", LEFT), //
				new Ghost(1, "Pinky", LEFT), //
				new Ghost(2, "Inky", LEFT), //
				new Ghost(3, "Sue", LEFT),//
		};

		for (Ghost ghost : ghosts) {
			ghost.setPosition(t(37), belowFrame);
			ghost.visible = true;
			ghost.bounty = 0;
			ghost.speed = 0;
			ghost.state = GhostState.HUNTING_PAC;
		}

		currentGhost = null;
		presentingMsPac = false;

		enter(Phase.BEGIN, Long.MAX_VALUE);
	}

	@Override
	public void update() {
		for (Ghost ghost : ghosts) {
			ghost.move();
		}
		msPac.move();
		switch (phase) {
		case BEGIN:
			if (phase.timer.running() == clock.sec(1)) {
				currentGhost = ghosts[0];
				enter(Phase.GHOSTS, Long.MAX_VALUE);
			}
			break;
		case GHOSTS:
			boolean ghostComplete = letCurrentGhostWalkToEndPosition();
			if (ghostComplete) {
				if (currentGhost == ghosts[3]) {
					currentGhost = null;
					presentingMsPac = true;
					enter(Phase.MSPACMAN, Long.MAX_VALUE);
				} else {
					currentGhost = ghosts[currentGhost.id + 1];
					enter(Phase.GHOSTS, Long.MAX_VALUE);
				}
			}
			break;
		case MSPACMAN:
			boolean msPacComplete = letMsPacManWalkToEndPosition();
			if (msPacComplete) {
				enter(Phase.END, Long.MAX_VALUE);
			}
			break;
		case END:
			if (phase.timer.running() == clock.sec(5)) {
				game.attractMode = true;
			}
			break;
		default:
			break;
		}
		phase.timer.run();
	}

	private boolean letCurrentGhostWalkToEndPosition() {
		if (currentGhost == null) {
			return false;
		}
		if (phase.timer.running() == 0) {
			currentGhost.speed = 1;
			rendering.ghostKicking(currentGhost).forEach(Animation::restart);
		}
		if (currentGhost.dir == LEFT && currentGhost.position.x <= leftOfFrame) {
			currentGhost.dir = currentGhost.wishDir = UP;
		}
		if (currentGhost.dir == UP && currentGhost.position.y <= t(frameTopLeftTile.y) + currentGhost.id * 18) {
			currentGhost.speed = 0;
			rendering.ghostKicking(currentGhost).forEach(Animation::reset);
			return true;
		}
		return false;
	}

	private boolean letMsPacManWalkToEndPosition() {
		if (phase.timer.running() == 0) {
			msPac.visible = true;
			msPac.couldMove = true;
			msPac.speed = 1;
			msPac.dir = LEFT;
			rendering.pacMunching(msPac).forEach(Animation::restart);
		}
		if (msPac.speed != 0 && msPac.position.x <= t(13)) {
			msPac.speed = 0;
			rendering.pacMunching(msPac).forEach(Animation::reset);
			return true;
		}
		return false;
	}

	@Override
	public void render() {
		clear();
		g.setFont(rendering.getScoreFont());
		g.setFill(Color.ORANGE);
		g.fillText("\"MS PAC-MAN\"", t(8), t(5));
		drawAnimatedFrame(32, 16, game.state.ticksRun());
		for (Ghost ghost : ghosts) {
			rendering.drawGhost(g, ghost, false);
		}
		rendering.drawPac(g, msPac);
		presentGhost();
		presentMsPacMan();
		if (phase == Phase.END) {
			drawPointsAnimation(26);
			drawPressKeyToStart(32);
		}
	}

	private void presentGhost() {
		if (currentGhost == null) {
			return;
		}
		g.setFill(Color.WHITE);
		g.setFont(rendering.getScoreFont());
		if (currentGhost == ghosts[0]) {
			g.fillText("WITH", t(8), t(11));
		}
		g.setFill(currentGhost.id == 0 ? Color.RED
				: currentGhost.id == 1 ? Color.PINK : currentGhost.id == 2 ? Color.CYAN : Color.ORANGE);
		g.fillText(currentGhost.name.toUpperCase(), t(13 - currentGhost.name.length() / 2), t(14));
	}

	private void presentMsPacMan() {
		if (!presentingMsPac) {
			return;
		}
		g.setFill(Color.WHITE);
		g.setFont(rendering.getScoreFont());
		g.fillText("STARRING", t(8), t(11));
		g.setFill(Color.YELLOW);
		g.fillText("MS PAC-MAN", t(8), t(14));
	}

	private void drawAnimatedFrame(int numDotsX, int numDotsY, long time) {
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
			g.fillRect(t(frameTopLeftTile.x) + 4 * x, t(frameTopLeftTile.y) + 4 * y, 2, 2);
		}
	}

	private void drawPressKeyToStart(int tileY) {
		if (blinking.animate()) {
			String text = "PRESS SPACE TO PLAY";
			g.setFill(Color.ORANGE);
			g.setFont(rendering.getScoreFont());
			g.fillText(text, t(13 - text.length() / 2), t(tileY));
		}
	}

	private void drawPointsAnimation(int tileY) {
		int x = t(10), y = t(tileY);
		if (blinking.animate()) {
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