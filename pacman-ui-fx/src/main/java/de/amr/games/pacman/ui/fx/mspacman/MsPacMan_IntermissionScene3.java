package de.amr.games.pacman.ui.fx.mspacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX.MS_PACMAN_RENDERING;
import static de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX.MS_PACMAN_SOUNDS;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.model.guys.GameEntity;
import de.amr.games.pacman.model.guys.Pac;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.ui.fx.common.GameScene;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
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

	static class Bird extends GameEntity {

		Animation<Rectangle2D> animation;

		public void draw(GraphicsContext g) {
			if (visible) {
				MS_PACMAN_RENDERING.drawBirdAnim(g, position.x, position.y);
			}
		}
	}

	static class Bag extends GameEntity {

		boolean released;
		boolean open;
		int bounces;

		@Override
		public void move() {
			if (released) {
				velocity = velocity.sum(GRAVITY);
			}
			super.move();
		}

		public void draw(GraphicsContext g) {
			if (open) {
				MS_PACMAN_RENDERING.drawJunior(g, position.x, position.y);
			} else {
				MS_PACMAN_RENDERING.drawBlueBag(g, position.x, position.y);
			}
		}
	}

	enum Phase {

		FLAP, ACTION, READY_TO_PLAY;

		final CountdownTimer timer = new CountdownTimer();
	}

	private static final int BIRD_Y = t(12), GROUND_Y = t(24);
	private static final V2f GRAVITY = new V2f(0, 0.04f);

	private Flap flap;
	private Pac pacMan;
	private Pac msPacMan;
	private Bird bird;
	private Bag bag;

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

		flap = new Flap(3, "JUNIOR");
		flap.setPosition(t(3), t(10));
		flap.visible = true;
		flap.animation.restart();

		pacMan = new Pac("Pac-Man", Direction.RIGHT);
		pacMan.setPosition(t(3), GROUND_Y - 4);

		msPacMan = new Pac("Ms. Pac-Man", Direction.RIGHT);
		msPacMan.setPosition(t(5), GROUND_Y - 4);

		bird = new Bird();
		bird.setPosition(t(30), BIRD_Y);
		bird.animation = MS_PACMAN_RENDERING.getBirdAnim();
		bird.animation.restart();

		bag = new Bag();
		bag.setPosition(bird.position.sum(-14, 3));

		MS_PACMAN_SOUNDS.play(PacManGameSound.INTERMISSION_3);
		enter(Phase.FLAP, Long.MAX_VALUE);
	}

	@Override
	public void update() {
		switch (phase) {
		case FLAP:
			if (phase.timer.running() == clock.sec(1)) {
				flap.visible = false;
				enter(Phase.ACTION, Long.MAX_VALUE);
			}
			break;

		case ACTION:
			bird.move();
			bag.move();
			if (phase.timer.running() == 0) {
				pacMan.visible = true;
				msPacMan.visible = true;
				bird.visible = true;
				bag.visible = true;
				bird.velocity = new V2f(-1.25f, 0);
				bag.velocity = bird.velocity;
			}
			// release bag?
			if (!bag.released && bird.position.x <= t(24)) {
				bag.released = true;
			}
			// closed bag reaches ground?
			if (!bag.open && bag.position.y > GROUND_Y) {
				++bag.bounces;
				if (bag.bounces < 5) {
					bag.velocity = new V2f(-0.2f, -1f / bag.bounces);
					bag.setPosition(bag.position.x, GROUND_Y);
				} else {
					bag.open = true;
					bag.velocity = V2f.NULL;
					enter(Phase.READY_TO_PLAY, clock.sec(3));
				}
			}
			break;
		case READY_TO_PLAY:
			bird.move();
			if (phase.timer.expired()) {
				game.state.timer.setDuration(0);
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
		flap.draw(g);
		MS_PACMAN_RENDERING.drawPac(g, msPacMan);
		MS_PACMAN_RENDERING.drawMrPacMan(g, pacMan);
		bird.draw(g);
		bag.draw(g);
	}
}