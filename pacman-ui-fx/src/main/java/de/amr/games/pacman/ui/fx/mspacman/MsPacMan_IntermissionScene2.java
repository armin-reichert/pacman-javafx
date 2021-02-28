package de.amr.games.pacman.ui.fx.mspacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.fx.common.GameScene;
import de.amr.games.pacman.ui.fx.rendering.FXRendering;
import de.amr.games.pacman.ui.fx.rendering.standard.FlapUI;
import javafx.scene.canvas.GraphicsContext;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they
 * both rapidly run from left to right and right to left. (Played after round 5)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene2 extends GameScene {

	enum Phase {

		FLAP, ACTION;

		final CountdownTimer timer = new CountdownTimer();
	}

	private static final int UPPER_Y = t(12), LOWER_Y = t(24), MIDDLE_Y = t(18);

	private Phase phase;

	private FlapUI flap;
	private Pac pacMan, msPacMan;

	private void enter(Phase newPhase, long ticks) {
		phase = newPhase;
		phase.timer.setDuration(ticks);
	}

	public MsPacMan_IntermissionScene2(double scaling, FXRendering rendering, SoundManager sounds) {
		super(scaling, rendering, sounds);
	}

	@Override
	public void start() {
		flap = new FlapUI(2, "THE CHASE", rendering);
		flap.setTilePosition(3, 10);
		flap.visible = true;

		pacMan = new Pac("Pac-Man", Direction.RIGHT);
		msPacMan = new Pac("Ms. Pac-Man", Direction.RIGHT);

		enter(Phase.FLAP, Long.MAX_VALUE);
	}

	@Override
	public void update() {
		switch (phase) {
		case FLAP:
			if (phase.timer.running() == clock.sec(2)) {
				flap.visible = false;
				sounds.play(PacManGameSound.INTERMISSION_2);
			}
			if (phase.timer.running() == clock.sec(4.5)) {
				enter(Phase.ACTION, Long.MAX_VALUE);
			}
			break;

		case ACTION:
			if (phase.timer.running() == clock.sec(1.5)) {
				pacMan.visible = true;
				pacMan.setPosition(-t(2), UPPER_Y);
				msPacMan.visible = true;
				msPacMan.setPosition(-t(8), UPPER_Y);
				pacMan.dir = msPacMan.dir = Direction.RIGHT;
				pacMan.speed = msPacMan.speed = 2;
				rendering.playerAnimations().playerMunching(msPacMan).forEach(Animation::restart);
				rendering.playerAnimations().spouseMunching(pacMan).forEach(Animation::restart);
			}
			if (phase.timer.running() == clock.sec(6)) {
				msPacMan.setPosition(t(30), LOWER_Y);
				msPacMan.visible = true;
				pacMan.setPosition(t(36), LOWER_Y);
				msPacMan.dir = pacMan.dir = Direction.LEFT;
				msPacMan.speed = pacMan.speed = 2;
			}
			if (phase.timer.running() == clock.sec(10.5)) {
				msPacMan.setPosition(t(-8), MIDDLE_Y);
				pacMan.setPosition(t(-2), MIDDLE_Y);
				msPacMan.dir = pacMan.dir = Direction.RIGHT;
				msPacMan.speed = pacMan.speed = 2;
			}
			if (phase.timer.running() == clock.sec(14.5)) {
				msPacMan.setPosition(t(30), UPPER_Y);
				pacMan.setPosition(t(42), UPPER_Y);
				msPacMan.dir = pacMan.dir = Direction.LEFT;
				msPacMan.speed = pacMan.speed = 4;
			}
			if (phase.timer.running() == clock.sec(15.5)) {
				msPacMan.setPosition(t(-14), LOWER_Y);
				pacMan.setPosition(t(-2), LOWER_Y);
				msPacMan.dir = pacMan.dir = Direction.RIGHT;
				msPacMan.speed = pacMan.speed = 4;
			}
			if (phase.timer.running() == clock.sec(20)) {
				game.state.timer.setDuration(0);
			}
			break;
		default:
			break;
		}
		pacMan.move();
		msPacMan.move();
		phase.timer.run();
	}

	@Override
	public void drawCanvas() {
		GraphicsContext g = canvas.getGraphicsContext2D();
		rendering.drawFlap(g, flap);
		rendering.drawPlayer(g, msPacMan);
		rendering.drawSpouse(g, pacMan);
	}
}