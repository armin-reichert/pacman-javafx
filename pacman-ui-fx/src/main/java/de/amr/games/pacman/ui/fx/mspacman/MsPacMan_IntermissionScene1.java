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
import de.amr.games.pacman.ui.fx.PacManGameFXUI;
import de.amr.games.pacman.ui.fx.common.AbstractPacManGameScene;
import javafx.scene.paint.Color;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they
 * quickly move upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms.
 * Pac-Man face each other at the top of the screen and a big pink heart appears above them. (Played
 * after round 2)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene1 extends AbstractPacManGameScene<MsPacManSceneRendering> {

	enum Phase {

		FLAP, CHASED_BY_GHOSTS, COMING_TOGETHER, READY_TO_PLAY;

		final CountdownTimer timer = new CountdownTimer();
	}

	private Phase phase;

	private int upperY = t(12), lowerY = t(24), middleY = t(18);
	private Pac pacMan, msPac;
	private Ghost pinky, inky;
	private boolean heartVisible;
	private boolean ghostsMet;

	private void enter(Phase newPhase, long ticks) {
		phase = newPhase;
		phase.timer.setDuration(ticks);
	}

	public MsPacMan_IntermissionScene1(PacManGameModel game, double width, double height, double scaling) {
		super(width, height, scaling, game, MsPacManSceneRendering.IT, PacManGameFXUI.msPacManSounds);
	}

	@Override
	public void start() {

		pacMan = new Pac("Pac-Man", Direction.RIGHT);
		pacMan.position = new V2f(-t(2), upperY);
		pacMan.visible = true;
		pacMan.couldMove = true;
		rendering.pacManMunching().values().forEach(Animation::restart);

		inky = new Ghost(2, "Inky", Direction.RIGHT);
		inky.position = pacMan.position.sum(-t(3), 0);
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
			pacMan.move();
			pinky.move();
			msPac.move();
			if (inky.position.x > t(30)) {
				startComingTogether();
			}
			break;
		case COMING_TOGETHER:
			inky.move();
			pinky.move();
			pacMan.move();
			msPac.move();
			if (pacMan.dir == Direction.LEFT && pacMan.position.x < t(15)) {
				pacMan.dir = msPac.dir = Direction.UP;
			}
			if (pacMan.dir == Direction.UP && pacMan.position.y < upperY) {
				pacMan.speed = msPac.speed = 0;
				pacMan.dir = Direction.LEFT;
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
			if (phase.timer.running() == clock.sec(0.5)) {
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
		phase.timer.run();
	}

	private void startChasedByGhosts() {
		pacMan.speed = msPac.speed = 1;
		inky.speed = pinky.speed = 1.04f;
		enter(Phase.CHASED_BY_GHOSTS, Long.MAX_VALUE);
	}

	private void startComingTogether() {
		pacMan.position = new V2f(t(30), middleY);
		inky.position = new V2f(t(33), middleY);
		pacMan.dir = Direction.LEFT;
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
			rendering.drawFlapAnimation(g, t(3), t(10), "1", "THEY MEET");
		}
		rendering.drawMrPacMan(g, pacMan);
		rendering.drawGhost(g, inky, game);
		rendering.drawPac(g, msPac, game);
		rendering.drawGhost(g, pinky, game);
		if (heartVisible) {
			rendering.drawRegion(g, rendering.getHeart(), msPac.position.x + 4, pacMan.position.y - 20);
		}
	}
}