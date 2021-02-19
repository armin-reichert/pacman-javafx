package de.amr.games.pacman.ui.fx.mspacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.fx.PacManGameFXUI;
import de.amr.games.pacman.ui.fx.common.GameScene;
import javafx.scene.Group;

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

		ANIMATION;

		final CountdownTimer timer = new CountdownTimer();
	}

	private final MsPacMan_SceneRendering rendering = PacManGameFXUI.MS_PACMAN_RENDERING;
	private final SoundManager sounds = PacManGameFXUI.MS_PACMAN_SOUNDS;

	private Phase phase;

	private int upperY = t(12), lowerY = t(24), middleY = t(18);
	private Pac pacMan, msPac;
	private boolean flapVisible;

	public MsPacMan_IntermissionScene2(Group root, double width, double height, double scaling) {
		super(root, width, height, scaling);
	}

	private void enter(Phase newPhase, long ticks) {
		phase = newPhase;
		phase.timer.setDuration(ticks);
	}

	@Override
	public void start() {
		pacMan = new Pac("Pac-Man", Direction.RIGHT);
		msPac = new Pac("Ms. Pac-Man", Direction.RIGHT);
		enter(Phase.ANIMATION, Long.MAX_VALUE);
	}

	@Override
	public void update() {
		switch (phase) {
		case ANIMATION:
			if (phase.timer.running() == 0) {
				sounds.play(PacManGameSound.INTERMISSION_2);
				flapVisible = true;
				rendering.getFlapAnim().restart();
			}
			if (phase.timer.running() == clock.sec(2)) {
				flapVisible = false;
			}
			if (phase.timer.running() == clock.sec(4.5)) {
				pacMan.visible = true;
				pacMan.position = new V2f(-t(2), upperY);
				msPac.visible = true;
				msPac.position = new V2f(-t(8), upperY);
				pacMan.dir = msPac.dir = Direction.RIGHT;
				pacMan.speed = msPac.speed = 2;
				rendering.pacManMunching().values().forEach(Animation::restart);
				rendering.pacMunching(msPac).forEach(Animation::restart);
			}
			if (phase.timer.running() == clock.sec(9)) {
				msPac.position = new V2f(t(30), lowerY);
				msPac.visible = true;
				pacMan.position = new V2f(t(36), lowerY);
				msPac.dir = pacMan.dir = Direction.LEFT;
				msPac.speed = pacMan.speed = 2;
			}
			if (phase.timer.running() == clock.sec(13.5)) {
				msPac.position = new V2f(t(-8), middleY);
				pacMan.position = new V2f(t(-2), middleY);
				msPac.dir = pacMan.dir = Direction.RIGHT;
				msPac.speed = pacMan.speed = 2;
			}
			if (phase.timer.running() == clock.sec(18)) {
				msPac.position = new V2f(t(30), upperY);
				pacMan.position = new V2f(t(42), upperY);
				msPac.dir = pacMan.dir = Direction.LEFT;
				msPac.speed = pacMan.speed = 4;
			}
			if (phase.timer.running() == clock.sec(19)) {
				msPac.position = new V2f(t(-14), lowerY);
				pacMan.position = new V2f(t(-2), lowerY);
				msPac.dir = pacMan.dir = Direction.RIGHT;
				msPac.speed = pacMan.speed = 4;
			}
			if (phase.timer.running() == clock.sec(24)) {
				game.state.duration(0);
			}
			break;
		default:
			break;
		}
		pacMan.move();
		msPac.move();
		phase.timer.run();
	}

	@Override
	public void render() {
		clear();
		if (flapVisible) {
			rendering.drawFlapAnimation(g, t(3), t(10), "2", "THE CHASE");
		}
		rendering.drawMrPacMan(g, pacMan);
		rendering.drawPac(g, msPac);
	}
}