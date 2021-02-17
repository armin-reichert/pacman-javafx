package de.amr.games.pacman.ui.fx.pacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.ui.fx.common.SceneRendering.tileRegion;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.ui.fx.common.AbstractPacManGameScene;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;

/**
 * Second intermission scene: Blinky pursues Pac but kicks a nail that tears his dress apart.
 * 
 * @author Armin Reichert
 */
public class IntermissionScene2 extends AbstractPacManGameScene<PacManSceneRendering> {

	enum Phase {

		APPROACHING_NAIL, HITTING_NAIL, STRETCHED_1, STRETCHED_2, STRETCHED_3, LOOKING_UP, LOOKING_RIGHT;

		final CountdownTimer timer = new CountdownTimer();
	}

	private final int chaseTileY = 20;
	private final Ghost blinky;
	private final Pac pac;
	private final Rectangle2D nail, blinkyLookingUp, blinkyLookingRight, shred, stretchedDress[];
	private final V2i nailPosition;

	private Phase phase;

	public IntermissionScene2(PacManGameModel game, double width, double height, double scaling) {
		super(width, height, scaling, game, Scenes.rendering, Scenes.soundManager);

		blinky = new Ghost(0, "Blinky", Direction.LEFT);
		pac = new Pac("Pac-Man", Direction.LEFT);
		nailPosition = new V2i(t(14), t(chaseTileY) - 6);

		// Sprites
		nail = tileRegion(8, 6, 1, 1);
		shred = tileRegion(12, 6, 1, 1);
		blinkyLookingUp = tileRegion(8, 7, 1, 1);
		blinkyLookingRight = tileRegion(9, 7, 1, 1);
		stretchedDress = new Rectangle2D[] { //
				tileRegion(9, 6, 1, 1), //
				tileRegion(10, 6, 1, 1), //
				tileRegion(11, 6, 1, 1) };
	}

	@Override
	public void start() {
		log("Start of intermission scene %s at %d", this, clock.ticksTotal);

		pac.visible = true;
		pac.dead = false;
		pac.couldMove = true;
		pac.position = new V2f(t(30), t(chaseTileY));
		pac.speed = 1;
		pac.dir = LEFT;

		blinky.visible = true;
		blinky.state = HUNTING_PAC;
		blinky.position = pac.position.sum(t(14), 0);
		blinky.speed = 1;
		blinky.dir = blinky.wishDir = LEFT;

		rendering.pacMunching(pac).forEach(Animation::restart);
		rendering.ghostKickingToDir(blinky, blinky.dir).restart();
		soundManager.play(PacManGameSound.INTERMISSION_2);

		enter(Phase.APPROACHING_NAIL);
	}

	private void enter(Phase nextPhase, long ticks) {
		phase = nextPhase;
		phase.timer.setDuration(ticks);
	}

	private void enter(Phase nextPhase) {
		enter(nextPhase, -1);
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
				game.state.duration(0); // signal end of this scene
			}
			break;
		default:
			throw new IllegalStateException("Illegal phase: " + phase);
		}
		blinky.move();
		pac.move();
		phase.timer.tick();
	}

	@Override
	public void render() {
		fill(Color.BLACK);
		rendering.drawLevelCounter(g, game, t(25), t(34));
		rendering.drawRegion(g, nail, nailPosition.x, nailPosition.y);
		rendering.drawPac(g, pac, game);
		drawBlinky();
	}

	private void drawBlinky() {
		int baselineY = (int) blinky.position.y - 5;
		int blinkySpriteRightEdge = (int) blinky.position.x + 4;
		switch (phase) {
		case APPROACHING_NAIL:
		case HITTING_NAIL:
			rendering.drawGhost(g, blinky, game);
			break;
		case STRETCHED_1:
			rendering.drawRegion(g, stretchedDress[0], blinkySpriteRightEdge - 8, baselineY);
			rendering.drawGhost(g, blinky, game);
			break;
		case STRETCHED_2:
			rendering.drawRegion(g, stretchedDress[1], blinkySpriteRightEdge - 4, baselineY);
			rendering.drawGhost(g, blinky, game);
			break;
		case STRETCHED_3:
			rendering.drawRegion(g, stretchedDress[2], blinkySpriteRightEdge - 2, baselineY);
			rendering.drawGhost(g, blinky, game);
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