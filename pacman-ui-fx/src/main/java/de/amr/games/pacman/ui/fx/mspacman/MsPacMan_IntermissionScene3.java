package de.amr.games.pacman.ui.fx.mspacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.heaven.God.differsAtMost;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.model.GameEntity;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.fx.PacManGameFXUI;
import de.amr.games.pacman.ui.fx.common.GameScene;
import javafx.scene.Group;

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

		ANIMATION, READY_TO_PLAY;

		final CountdownTimer timer = new CountdownTimer();
	}

	private static final int BIRD_Y = t(12), GROUND_Y = t(24);
	private static final float BIRD_SPEED = 1.5f;
	private static final V2f GRAVITY = new V2f(0, 0.04f);

	private final MsPacMan_SceneRendering rendering = PacManGameFXUI.MS_PACMAN_RENDERING;
	private final SoundManager sounds = PacManGameFXUI.MS_PACMAN_SOUNDS;

	private Pac pacMan, msPac;
	private GameEntity bird;
	private GameEntity bag;
	private boolean bagDropped;
	private boolean bagOpened;
	private int bagBounces;
	private boolean flapVisible;

	private Phase phase;

	public MsPacMan_IntermissionScene3(Group root, double width, double height, double scaling) {
		super(root, width, height, scaling);
	}

	private void enter(Phase newPhase, long ticks) {
		phase = newPhase;
		phase.timer.setDuration(ticks);
	}

	@Override
	public void start() {
		pacMan = new Pac("Pac-Man", Direction.RIGHT);
		pacMan.setPosition(t(3), GROUND_Y - 4);

		msPac = new Pac("Ms. Pac-Man", Direction.RIGHT);
		msPac.setPosition(t(5), GROUND_Y - 4);

		bird = new GameEntity();
		bird.setPosition(t(30), BIRD_Y);
		rendering.getBirdAnim().restart();

		bag = new GameEntity();
		bag.setPosition(bird.position.sum(-14, 3));
		bagDropped = bagOpened = false;
		bagBounces = 0;

		flapVisible = true;
		rendering.getFlapAnim().restart();

		sounds.play(PacManGameSound.INTERMISSION_3);
		enter(Phase.ANIMATION, Long.MAX_VALUE);
	}

	@Override
	public void update() {
		switch (phase) {
		case ANIMATION:
			bird.move();
			bag.move();
			if (bagDropped) {
				bag.velocity = bag.velocity.sum(GRAVITY);
			}
			if (phase.timer.running() == clock.sec(1)) {
				flapVisible = false;
				pacMan.visible = true;
				msPac.visible = true;
				bird.visible = true;
				bird.velocity = new V2f(-BIRD_SPEED, 0);
				bag.visible = true;
				bag.velocity = new V2f(-BIRD_SPEED, 0);
			}
			// drop bag?
			if (differsAtMost(bird.position.x, t(24), 1)) {
				bagDropped = true;
			}
			// ground contact?
			if (!bagOpened && bagDropped && bag.position.y > GROUND_Y) {
				++bagBounces;
				if (bagBounces < 3) {
					bag.velocity = new V2f(-0.2f, -0.9f / bagBounces);
					bag.setPosition(bag.position.x, GROUND_Y);
				} else {
					bag.velocity = V2f.NULL;
					bagOpened = true;
				}
			}
			if (bagOpened) {
				enter(Phase.READY_TO_PLAY, clock.sec(3));
			}
			break;
		case READY_TO_PLAY:
			bird.move();
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
		clear();
		if (flapVisible) {
			rendering.drawFlapAnimation(g, t(3), t(10), "3", "JUNIOR");
		}
		rendering.drawPac(g, msPac);
		rendering.drawMrPacMan(g, pacMan);
		if (bird.visible) {
			rendering.drawBirdAnim(g, bird.position.x, bird.position.y);
		}
		if (bagOpened) {
			rendering.drawJunior(g, bag.position.x, bag.position.y);
		} else {
			rendering.drawBlueBag(g, bag.position.x, bag.position.y);
		}
	}
}