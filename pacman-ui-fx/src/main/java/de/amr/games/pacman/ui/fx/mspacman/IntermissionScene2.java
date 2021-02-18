package de.amr.games.pacman.ui.fx.mspacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.ui.fx.common.AbstractPacManGameScene;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they
 * both rapidly run from left to right and right to left. (Played after round 5)
 * 
 * @author Armin Reichert
 */
public class IntermissionScene2 extends AbstractPacManGameScene<MsPacManSceneRendering> {

	enum Phase {

		ANIMATION;

		final CountdownTimer timer = new CountdownTimer();
	}

	private Phase phase;

	private int upperY = t(12), lowerY = t(24), middleY = t(18);
	private Pac pac, msPac;
	private boolean flapVisible;

	private void enter(Phase newPhase, long ticks) {
		phase = newPhase;
		phase.timer.setDuration(ticks);
	}

	public IntermissionScene2(PacManGameModel game, double width, double height, double scaling) {
		super(width, height, scaling, game, Scenes.rendering, Scenes.soundManager);
	}

	@Override
	public void start() {
		pac = new Pac("Pac-Man", Direction.RIGHT);
		msPac = new Pac("Ms. Pac-Man", Direction.RIGHT);
		enter(Phase.ANIMATION, Long.MAX_VALUE);
	}

	@Override
	public void update() {
		switch (phase) {
		case ANIMATION:
			if (phase.timer.running() == 0) {
				soundManager.play(PacManGameSound.INTERMISSION_2);
				flapVisible = true;
				rendering.getFlapAnim().restart();
			}
			if (phase.timer.running() == clock.sec(2)) {
				flapVisible = false;
			}
			if (phase.timer.running() == clock.sec(4.5)) {
				pac.visible = true;
				pac.position = new V2f(-t(2), upperY);
				msPac.visible = true;
				msPac.position = new V2f(-t(8), upperY);
				pac.dir = msPac.dir = Direction.RIGHT;
				pac.speed = msPac.speed = 2;
				rendering.pacManMunching().values().forEach(Animation::restart);
				rendering.msPacManMunching(msPac).forEach(Animation::restart);
			}
			if (phase.timer.running() == clock.sec(9)) {
				msPac.position = new V2f(t(30), lowerY);
				msPac.visible = true;
				pac.position = new V2f(t(36), lowerY);
				msPac.dir = pac.dir = Direction.LEFT;
				msPac.speed = pac.speed = 2;
			}
			if (phase.timer.running() == clock.sec(13.5)) {
				msPac.position = new V2f(t(-8), middleY);
				pac.position = new V2f(t(-2), middleY);
				msPac.dir = pac.dir = Direction.RIGHT;
				msPac.speed = pac.speed = 2;
			}
			if (phase.timer.running() == clock.sec(18)) {
				msPac.position = new V2f(t(30), upperY);
				pac.position = new V2f(t(42), upperY);
				msPac.dir = pac.dir = Direction.LEFT;
				msPac.speed = pac.speed = 4;
			}
			if (phase.timer.running() == clock.sec(19)) {
				msPac.position = new V2f(t(-14), lowerY);
				pac.position = new V2f(t(-2), lowerY);
				msPac.dir = pac.dir = Direction.RIGHT;
				msPac.speed = pac.speed = 4;
			}
			if (phase.timer.running() == clock.sec(24)) {
				game.state.duration(0);
			}
			break;
		default:
			break;
		}
		pac.move();
		msPac.move();
		phase.timer.run();
	}

	@Override
	public void render() {
		fill(Color.BLACK);
		drawFlapAnimation(t(3), t(10));
		drawPacMan();
		rendering.drawMsPacMan(g, msPac, game);
	}

	private void drawPacMan() {
		if (pac.visible) {
			Animation<Rectangle2D> munching = rendering.pacManMunching().get(pac.dir);
			if (pac.speed > 0) {
				rendering.drawRegion(g, munching.animate(), pac.position.x - 4, pac.position.y - 4);
			} else {
				rendering.drawRegion(g, munching.frame(1), pac.position.x - 4, pac.position.y - 4);
			}
		}
	}

	private void drawFlapAnimation(int flapX, int flapY) {
		if (flapVisible) {
			rendering.drawRegion(g, rendering.getFlapAnim().animate(), flapX, flapY);
			g.setFill(Color.rgb(222, 222, 225));
			g.setFont(rendering.getScoreFont());
			g.fillText("2", flapX + 20, flapY + 30);
			if (rendering.getFlapAnim().isRunning()) {
				g.fillText("THE CHASE", flapX + 40, flapY + 20);
			}
		}
	}
}