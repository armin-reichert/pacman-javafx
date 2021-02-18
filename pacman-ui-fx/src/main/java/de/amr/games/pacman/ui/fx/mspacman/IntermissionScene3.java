package de.amr.games.pacman.ui.fx.mspacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.ui.fx.common.AbstractPacManGameScene;
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

		ANIMATION, READY_TO_PLAY;

		final CountdownTimer timer = new CountdownTimer();
	}

	private Phase phase;
	private boolean flapVisible;
	private Pac msPacMan, pacMan;

	public IntermissionScene3(PacManGameModel game, double width, double height, double scaling) {
		super(width, height, scaling, game, Scenes.rendering, Scenes.soundManager);
	}

	private void enter(Phase newPhase, long ticks) {
		phase = newPhase;
		phase.timer.setDuration(ticks);
	}

	@Override
	public void start() {
		pacMan = new Pac("Pac-Man", Direction.RIGHT);
		msPacMan = new Pac("Ms. Pac-Man", Direction.RIGHT);
		enter(Phase.ANIMATION, Long.MAX_VALUE);
	}

	@Override
	public void update() {
		switch (phase) {
		case ANIMATION:
			if (phase.timer.running() == 0) {
				flapVisible = true;
				rendering.getFlapAnim().restart();
				soundManager.play(PacManGameSound.INTERMISSION_3);
			}
			if (phase.timer.running() == clock.sec(2)) {
				flapVisible = false;
			}
			if (phase.timer.running() == clock.sec(5)) {
				enter(Phase.READY_TO_PLAY, clock.sec(3));
			}
			break;
		case READY_TO_PLAY:
			if (phase.timer.expired()) {
				game.state.duration(0);
			}
			break;
		default:
			break;
		}
		phase.timer.run();
	}

	@Override
	public void render() {
		fill(Color.BLACK);
		if (flapVisible) {
			rendering.drawFlapAnimation(g, t(3), t(10), "3", "JUNIOR");
		}
		rendering.drawPac(g, msPacMan, game);
		rendering.drawMrPacMan(g, pacMan);
	}
}