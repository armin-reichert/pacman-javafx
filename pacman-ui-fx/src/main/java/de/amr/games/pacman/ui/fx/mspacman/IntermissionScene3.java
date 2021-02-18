package de.amr.games.pacman.ui.fx.mspacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.heaven.God.differsAtMost;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
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

	private final int upperY = t(12), lowerY = t(24);
	private final V2f gravity = new V2f(0, 0.04f);

	private Pac pacMan, msPac;

	private boolean flapVisible;

	private boolean birdVisible = false;
	private V2f birdPosition = V2f.NULL;
	private V2f birdVelocity = V2f.NULL;

	private boolean bagVisible = false;
	private V2f bagPosition = V2f.NULL;
	private V2f bagVelocity = V2f.NULL;
	private boolean bagDropped;
	private long bagOpenTimer;
	private int bounces;

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
		msPac = new Pac("Ms. Pac-Man", Direction.RIGHT);
		enter(Phase.ANIMATION, Long.MAX_VALUE);
	}

	@Override
	public void update() {
		switch (phase) {
		case ANIMATION:
			if (phase.timer.running() == 0) {
				flapVisible = true;
				rendering.getFlapAnim().restart();
				pacMan.position = new V2f(t(3), lowerY);
				msPac.position = new V2f(t(5), lowerY);
				birdPosition = new V2f(t(29), upperY);
				bagPosition = birdPosition.sum(-2, 6);
				soundManager.play(PacManGameSound.INTERMISSION_3);
			}
			birdPosition = birdPosition.sum(birdVelocity);
			bagPosition = bagPosition.sum(bagVelocity);
			if (bagDropped) {
				bagVelocity = bagVelocity.sum(gravity);
			}
			if (phase.timer.running() == clock.sec(1)) {
				flapVisible = false;
				pacMan.visible = true;
				msPac.visible = true;
				birdVisible = true;
				birdVelocity = new V2f(-1, 0);
				bagVisible = true;
				bagVelocity = new V2f(-1, 0);
			}
			if (differsAtMost(birdPosition.x, t(22), 1)) {
				// drop bag
				bagDropped = true;
				bagVelocity = new V2f(-1f, 0).sum(gravity);
			}
			if (bagDropped && bagPosition.y > lowerY) {
				++bounces;
				if (bounces < 3) {
					bagVelocity = new V2f(-0.2f, -0.8f / bounces);
					bagPosition = new V2f(bagPosition.x, lowerY);
				} else {
					bagVelocity = V2f.NULL;
					bagOpenTimer = clock.sec(2);
				}
			}
			if (bagOpenTimer > 0) {
				--bagOpenTimer;
				if (bagOpenTimer == 0) {
					enter(Phase.READY_TO_PLAY, clock.sec(30));
				}
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
		rendering.drawPac(g, msPac, game);
		rendering.drawMrPacMan(g, pacMan);
		if (birdVisible) {
			rendering.drawBirdAnim(g, birdPosition.x, birdPosition.y);
		}
		if (bagOpenTimer > 0) {
			rendering.drawJunior(g, bagPosition.x, bagPosition.y);
		} else {
			rendering.drawBlueBag(g, bagPosition.x, bagPosition.y);
		}
	}
}