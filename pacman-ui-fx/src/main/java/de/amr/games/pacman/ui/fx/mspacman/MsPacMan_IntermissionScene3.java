package de.amr.games.pacman.ui.fx.mspacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.JuniorBag;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.fx.common.GameScene;
import de.amr.games.pacman.ui.fx.rendering.FXRendering;
import de.amr.games.pacman.ui.fx.rendering.standard.FlapUI;
import de.amr.games.pacman.ui.fx.rendering.standard.StorkUI;
import javafx.scene.canvas.GraphicsContext;

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
public class MsPacMan_IntermissionScene3 extends GameScene {

	enum Phase {

		FLAP, ACTION, READY_TO_PLAY;

		final CountdownTimer timer = new CountdownTimer();
	}

	private static final int CEILING_Y = t(12), GROUND_Y = t(24);

	private FlapUI flap;
	private Pac pacMan;
	private Pac msPacMan;
	private StorkUI stork;
	private JuniorBag bag;

	private Phase phase;

	public MsPacMan_IntermissionScene3(PacManGameController controller, double scaling, FXRendering rendering,
			SoundManager sounds) {
		super(controller, scaling, rendering, sounds);
	}

	private void enter(Phase newPhase, long ticks) {
		phase = newPhase;
		phase.timer.setDuration(ticks);
	}

	@Override
	public void start() {

		flap = new FlapUI(3, "JUNIOR", rendering);
		flap.setTilePosition(3, 10);
		flap.visible = true;

		pacMan = new Pac("Pac-Man", Direction.RIGHT);
		pacMan.setPosition(t(3), GROUND_Y - 4);

		msPacMan = new Pac("Ms. Pac-Man", Direction.RIGHT);
		msPacMan.setPosition(t(5), GROUND_Y - 4);

		stork = new StorkUI(rendering);
		stork.setPosition(t(30), CEILING_Y);

		bag = new JuniorBag();
		bag.setPositionRelativeTo(stork, -14, 3);

		enter(Phase.FLAP, Long.MAX_VALUE);
	}

	@Override
	public void update() {
		switch (phase) {
		case FLAP:
			if (phase.timer.running() == clock.sec(2)) {
				flap.visible = false;
				sounds.play(PacManGameSound.INTERMISSION_3);
				enter(Phase.ACTION, Long.MAX_VALUE);
			}
			break;

		case ACTION:
			stork.move();
			bag.move();
			if (phase.timer.running() == 0) {
				pacMan.visible = true;
				msPacMan.visible = true;
				stork.visible = true;
				bag.visible = true;
				stork.velocity = new V2d(-1.25f, 0);
				bag.velocity = stork.velocity;
			}
			// release bag?
			if (!bag.released && stork.position.x <= t(24)) {
				bag.released = true;
			}
			// closed bag reaches ground?
			if (!bag.open && bag.position.y > GROUND_Y) {
				++bag.bounces;
				if (bag.bounces < 5) {
					bag.velocity = new V2d(-0.2f, -1f / bag.bounces);
					bag.setPosition(bag.position.x, GROUND_Y);
				} else {
					bag.open = true;
					bag.velocity = V2d.NULL;
					enter(Phase.READY_TO_PLAY, clock.sec(3));
				}
			}
			break;

		case READY_TO_PLAY:
			stork.move();
			if (phase.timer.expired()) {
				controller.finishCurrentState();
			}
			break;

		default:
			break;
		}
		phase.timer.run();
	}

	@Override
	public void draw(GraphicsContext g) {
		rendering.drawFlap(g, flap);
		rendering.drawStork(g, stork);
		rendering.drawPlayer(g, msPacMan);
		rendering.drawSpouse(g, pacMan);
		rendering.drawJuniorBag(g, bag);
	}
}