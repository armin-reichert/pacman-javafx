package de.amr.games.pacman.ui.fx.pacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.model.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.ui.fx.common.SceneRendering.tileRegion;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.fx.PacManGameFXUI;
import de.amr.games.pacman.ui.fx.common.GameScene;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;

/**
 * Second intermission scene: Blinky pursues Pac but kicks a nail that tears his dress apart.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene2 extends GameScene {

	enum Phase {

		APPROACHING_NAIL, HITTING_NAIL, STRETCHED_1, STRETCHED_2, STRETCHED_3, LOOKING_UP, LOOKING_RIGHT;

		final CountdownTimer timer = new CountdownTimer();
	}

	private final PacMan_SceneRendering rendering = PacManGameFXUI.PACMAN_RENDERING;
	private final SoundManager sounds = PacManGameFXUI.PACMAN_SOUNDS;

	private final int chaseTileY = 20;
	private final V2i nailPosition = new V2i(t(14), t(chaseTileY) - 6);

	private final Rectangle2D nail = tileRegion(8, 6, 1, 1);
	private final Rectangle2D blinkyLookingUp = tileRegion(8, 7, 1, 1);
	private final Rectangle2D blinkyLookingRight = tileRegion(9, 7, 1, 1);
	private final Rectangle2D shred = tileRegion(12, 6, 1, 1);
	private final Rectangle2D[] stretchedDress = new Rectangle2D[] { //
			tileRegion(9, 6, 1, 1), //
			tileRegion(10, 6, 1, 1), //
			tileRegion(11, 6, 1, 1) };

	private Ghost blinky;
	private Pac pac;

	private Phase phase;

	public PacMan_IntermissionScene2(Group root, double width, double height, double scaling) {
		super(root, width, height, scaling);
	}

	@Override
	public void start() {
		pac = new Pac("Pac-Man", Direction.LEFT);
		pac.visible = true;
		pac.setPosition(t(30), t(chaseTileY));
		pac.speed = 1;
		rendering.pacMunching(pac).forEach(Animation::restart);

		blinky = new Ghost(0, "Blinky", Direction.LEFT);
		blinky.visible = true;
		blinky.state = HUNTING_PAC;
		blinky.setPosition(pac.position.sum(t(14), 0));
		blinky.speed = 1;
		rendering.ghostKickingToDir(blinky, blinky.dir).restart();

		sounds.play(PacManGameSound.INTERMISSION_2);

		enter(Phase.APPROACHING_NAIL, Long.MAX_VALUE);
	}

	private void enter(Phase nextPhase, long ticks) {
		phase = nextPhase;
		phase.timer.setDuration(ticks);
	}

	@Override
	public void update() {
		int distFromNail = (int) (blinky.position.x - nailPosition.x) - 6;
		switch (phase) {
		case APPROACHING_NAIL:
			if (distFromNail == 0) {
				blinky.speed = 0;
				enter(Phase.HITTING_NAIL, clock.sec(0.1));
			}
			break;
		case HITTING_NAIL:
			if (phase.timer.expired()) {
				blinky.speed = 0.2f;
				enter(Phase.STRETCHED_1, Long.MAX_VALUE);
			}
			break;
		case STRETCHED_1:
			if (distFromNail == -3) {
				blinky.speed = 0.15f;
				enter(Phase.STRETCHED_2, Long.MAX_VALUE);
			}
			break;
		case STRETCHED_2:
			if (distFromNail == -6) {
				blinky.speed = 0.1f;
				enter(Phase.STRETCHED_3, Long.MAX_VALUE);
			}
			break;
		case STRETCHED_3:
			if (distFromNail == -9) {
				blinky.speed = 0;
				enter(Phase.LOOKING_UP, clock.sec(3));
			}
			break;
		case LOOKING_UP:
			if (phase.timer.expired()) {
				enter(Phase.LOOKING_RIGHT, clock.sec(3));
			}
			break;
		case LOOKING_RIGHT:
			if (phase.timer.expired()) {
				game.state.timer.setDuration(0); // signal end of this scene
			}
			break;
		default:
			throw new IllegalStateException("Illegal phase: " + phase);
		}
		blinky.move();
		pac.move();
		phase.timer.run();
	}

	@Override
	public void render() {
		clear();
		rendering.drawLevelCounter(g, game, t(25), t(34));
		rendering.drawRegion(g, nail, nailPosition.x, nailPosition.y);
		rendering.drawPac(g, pac);
		drawBlinky();
	}

	private void drawBlinky() {
		int baselineY = (int) blinky.position.y - 5;
		int blinkySpriteRightEdge = (int) blinky.position.x + 4;
		switch (phase) {
		case APPROACHING_NAIL:
		case HITTING_NAIL:
			rendering.drawGhost(g, blinky, false);
			break;
		case STRETCHED_1:
			rendering.drawRegion(g, stretchedDress[0], blinkySpriteRightEdge - 8, baselineY);
			rendering.drawGhost(g, blinky, false);
			break;
		case STRETCHED_2:
			rendering.drawRegion(g, stretchedDress[1], blinkySpriteRightEdge - 4, baselineY);
			rendering.drawGhost(g, blinky, false);
			break;
		case STRETCHED_3:
			rendering.drawRegion(g, stretchedDress[2], blinkySpriteRightEdge - 2, baselineY);
			rendering.drawGhost(g, blinky, false);
			break;
		case LOOKING_UP:
			rendering.drawRegion(g, blinkyLookingUp, blinky.position.x - 4, blinky.position.y - 4);
			rendering.drawRegion(g, shred, nailPosition.x, baselineY);
			break;
		case LOOKING_RIGHT:
			rendering.drawRegion(g, blinkyLookingRight, blinky.position.x - 4, blinky.position.y - 4);
			rendering.drawRegion(g, shred, nailPosition.x, baselineY);
			break;
		default:
			break;
		}
	}
}