package de.amr.games.pacman.ui.fx.mspacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.util.stream.Stream;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.ui.fx.common.AbstractPacManGameScene;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;

/**
 * Intermission scene 3: "Junior".
 * 
 * <p>
 * Pac-Man and Ms. Pac-Man gradually wait for a stork, who flies overhead with a little blue bundle.
 * The stork drops the bundle, which falls to the ground in front of Pac-Man and Ms. Pac-Man, and
 * finally opens up to reveal a tiny Pac-Man. (Played after rounds 9, 13, and 17)
 * 
 * @author Armin Reichert
 */
public class IntermissionScene3 extends AbstractPacManGameScene<MsPacManSceneRendering> {

	enum Phase {

		FLAP, CHASED_BY_GHOSTS, COMING_TOGETHER, READY_TO_PLAY;

		final CountdownTimer timer = new CountdownTimer();
	}

	private Phase phase;

	private int upperY = t(12), lowerY = t(24), middleY = t(18);
	private Pac pac, msPac;
	private Ghost pinky, inky;
	private boolean heartVisible;
	private boolean ghostsMet;

	private void enter(Phase newPhase, long ticks) {
		phase = newPhase;
		phase.timer.setDuration(ticks);
	}

	public IntermissionScene3(PacManGameModel game, double width, double height, double scaling) {
		super(width, height, scaling, game, Scenes.rendering, Scenes.soundManager);
	}

	@Override
	public void start() {

		pac = new Pac("Pac-Man", Direction.RIGHT);
		pac.position = new V2f(-t(2), upperY);
		pac.visible = true;
		pac.couldMove = true;
		rendering.getPacManMunching().values().forEach(Animation::restart);

		inky = new Ghost(2, "Inky", Direction.RIGHT);
		inky.position = pac.position.sum(-t(3), 0);
		inky.visible = true;

		msPac = new Pac("Ms. Pac-Man", Direction.LEFT);
		msPac.position = new V2f(t(30), lowerY);
		msPac.visible = true;
		msPac.couldMove = true;
		rendering.pacMunching(msPac).forEach(Animation::restart);

		pinky = new Ghost(1, "Pinky", Direction.LEFT);
		pinky.position = msPac.position.sum(t(3), 0);
		pinky.visible = true;

		rendering.ghostsKicking(Stream.of(inky, pinky)).forEach(Animation::restart);
		rendering.getFlapAnim().restart();
		soundManager.loop(PacManGameSound.INTERMISSION_1, 1);

		heartVisible = false;
		ghostsMet = false;

		enter(Phase.FLAP, clock.sec(1));
	}

	@Override
	public void update() {
		switch (phase) {
		case FLAP:
			if (phase.timer.expired()) {
				startChasedByGhosts();
			}
			break;
		case CHASED_BY_GHOSTS:
			inky.move();
			pac.move();
			pinky.move();
			msPac.move();
			if (inky.position.x > t(30)) {
				startComingTogether();
			}
			break;
		case COMING_TOGETHER:
			inky.move();
			pinky.move();
			pac.move();
			msPac.move();
			if (pac.dir == Direction.LEFT && pac.position.x < t(15)) {
				pac.dir = msPac.dir = Direction.UP;
			}
			if (pac.dir == Direction.UP && pac.position.y < upperY) {
				pac.speed = msPac.speed = 0;
				pac.dir = Direction.LEFT;
				msPac.dir = Direction.RIGHT;
				heartVisible = true;
				rendering.ghostKicking(inky).forEach(Animation::reset);
				rendering.ghostKicking(pinky).forEach(Animation::reset);
				enter(Phase.READY_TO_PLAY, clock.sec(3));
			}
			if (!ghostsMet && inky.position.x - pinky.position.x < 16) {
				ghostsMet = true;
				inky.dir = inky.wishDir = inky.dir.opposite();
				pinky.dir = pinky.wishDir = pinky.dir.opposite();
				inky.speed = pinky.speed = 0.2f;
			}
			break;
		case READY_TO_PLAY:
			if (phase.timer.running() == clock.sec(1)) {
				inky.visible = false;
				pinky.visible = false;
			}
			if (phase.timer.expired()) {
				game.state.duration(0);
			}
			break;
		default:
			break;
		}
		phase.timer.tick();
	}

	private void startChasedByGhosts() {
		pac.speed = msPac.speed = 1;
		inky.speed = pinky.speed = 1.04f;
		enter(Phase.CHASED_BY_GHOSTS, Long.MAX_VALUE);
	}

	private void startComingTogether() {
		pac.position = new V2f(t(30), middleY);
		inky.position = new V2f(t(33), middleY);
		pac.dir = Direction.LEFT;
		inky.dir = inky.wishDir = Direction.LEFT;
		pinky.position = new V2f(t(-5), middleY);
		msPac.position = new V2f(t(-2), middleY);
		msPac.dir = Direction.RIGHT;
		pinky.dir = pinky.wishDir = Direction.RIGHT;
		enter(Phase.COMING_TOGETHER, Long.MAX_VALUE);
	}

	@Override
	public void render() {
		fill(Color.BLACK);
		if (phase == Phase.FLAP) {
			drawFlapAnimation(t(3), t(10));
		}
		drawPacMan();
		rendering.drawGhost(g, inky, game);
		rendering.drawPac(g, msPac, game);
		rendering.drawGhost(g, pinky, game);
		if (heartVisible) {
			rendering.drawRegion(g, rendering.getHeart(), msPac.position.x + 4, pac.position.y - 20);
		}
	}

	private void drawPacMan() {
		Animation<Rectangle2D> munching = rendering.getPacManMunching().get(pac.dir);
		if (pac.speed > 0) {
			rendering.drawRegion(g, munching.animate(), pac.position.x - 4, pac.position.y - 4);
		} else {
			rendering.drawRegion(g, munching.frame(1), pac.position.x - 4, pac.position.y - 4);
		}
	}

	private void drawFlapAnimation(int flapX, int flapY) {
		rendering.drawRegion(g, rendering.getFlapAnim().animate(), flapX, flapY);
		g.setFill(Color.rgb(222, 222, 225));
		g.setFont(rendering.getScoreFont());
		g.fillText("1", flapX + 20, flapY + 30);
		if (rendering.getFlapAnim().isRunning()) {
			g.fillText("THEY MEET", flapX + 40, flapY + 20);
		}
	}
}