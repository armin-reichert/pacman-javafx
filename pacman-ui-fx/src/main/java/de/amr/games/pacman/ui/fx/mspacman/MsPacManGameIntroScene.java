package de.amr.games.pacman.ui.fx.mspacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.GhostState;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.ui.fx.common.AbstractPacManGameScene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Intro scene of the Ms. Pac-Man game. The ghosts and Ms. Pac-Man are introduced one after another.
 * 
 * @author Armin Reichert
 */
public class MsPacManGameIntroScene extends AbstractPacManGameScene<MsPacManSceneRendering> {

	enum Phase {

		BEGIN, GHOSTS, MSPACMAN, END;

		public long start;

		public boolean at(long ticks) {
			return clock.ticksTotal - start == ticks;
		}

	}

	private final V2i frameTopLeftTile = new V2i(6, 8);
	private final int belowFrame = t(17);
	private final int leftOfFrame = t(4);
	private final Animation<Boolean> blinking = Animation.pulse().frameDuration(30).restart();

	private Phase phase;

	private Pac msPac;
	private Ghost[] ghosts;

	private Ghost currentGhost;
	private boolean presentingMsPac;

	public MsPacManGameIntroScene(PacManGameModel game, double width, double height, double scaling) {
		super(game, null, width, height, scaling);
		rendering = new MsPacManSceneRendering(g);
	}

	private void enterPhase(Phase newPhase) {
		phase = newPhase;
		phase.start = clock.ticksTotal;
	}

	@Override
	public void start() {
		log("Intro scene started at clock time %d", clock.ticksTotal);

		msPac = new Pac("Ms. Pac-Man", LEFT);
		msPac.position = new V2f(t(37), belowFrame);
		msPac.visible = true;
		msPac.speed = 0;
		msPac.dead = false;
		msPac.dir = LEFT;

		ghosts = new Ghost[] { new Ghost(0, "Blinky", LEFT), new Ghost(1, "Pinky", LEFT), new Ghost(2, "Inky", LEFT),
				new Ghost(3, "Sue", LEFT), };
		for (Ghost ghost : ghosts) {
			ghost.position = new V2f(t(37), belowFrame);
			ghost.visible = true;
			ghost.dir = ghost.wishDir = LEFT;
			ghost.bounty = 0;
			ghost.speed = 0;
			ghost.state = GhostState.HUNTING_PAC;
		}

		currentGhost = null;
		presentingMsPac = false;

		enterPhase(Phase.BEGIN);
	}

	@Override
	public void update() {
		for (Ghost ghost : ghosts) {
			ghost.move();
		}
		msPac.move();
		switch (phase) {
		case BEGIN:
			if (phase.at(clock.sec(1))) {
				currentGhost = ghosts[0];
				enterPhase(Phase.GHOSTS);
			}
			break;
		case GHOSTS:
			boolean ghostComplete = letCurrentGhostWalkToEndPosition();
			if (ghostComplete) {
				if (currentGhost == ghosts[3]) {
					currentGhost = null;
					presentingMsPac = true;
					enterPhase(Phase.MSPACMAN);
				} else {
					currentGhost = ghosts[currentGhost.id + 1];
					enterPhase(Phase.GHOSTS);
				}
			}
			break;
		case MSPACMAN:
			boolean msPacComplete = letMsPacManWalkToEndPosition();
			if (msPacComplete) {
				enterPhase(Phase.END);
			}
			break;
		case END:
			if (phase.at(clock.sec(5))) {
				game.attractMode = true;
			}
			break;
		default:
			break;
		}
	}

	private boolean letCurrentGhostWalkToEndPosition() {
		if (currentGhost == null) {
			return false;
		}
		if (phase.at(1)) {
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
		if (phase.at(1)) {
			msPac.visible = true;
			msPac.couldMove = true;
			msPac.speed = 1;
			msPac.dir = LEFT;
			rendering.pacMunching().forEach(Animation::restart);
		}
		if (msPac.speed != 0 && msPac.position.x <= t(13)) {
			msPac.speed = 0;
			rendering.pacMunching().forEach(Animation::reset);
			return true;
		}
		return false;
	}

	@Override
	public void render() {
		fill(Color.BLACK);
		g.setFont(rendering.getScoreFont());
		g.setFill(Color.ORANGE);
		g.fillText("\"MS PAC-MAN\"", t(8), t(5));
		drawAnimatedFrame(32, 16, game.state.ticksRun());
		for (Ghost ghost : ghosts) {
			rendering.drawGhost(ghost, game);
		}
		rendering.drawPac(msPac, game);
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