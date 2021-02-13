package de.amr.games.pacman.ui.fx.pacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.fx.common.AbstractPacManGameScene;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;

/**
 * Second intermission scene: Blinky pursues Pac but kicks a nail that tears his dress apart.
 * 
 * @author Armin Reichert
 */
public class PacManGameIntermissionScene2 extends AbstractPacManGameScene {

	enum Phase {
		APPROACHING_NAIL, HITTING_NAIL, STRETCHED_1, STRETCHED_2, STRETCHED_3, LOOKING_UP, LOOKING_RIGHT;
	}

	private final int chaseTileY;
	private final Ghost blinky;
	private final Pac pac;
	private final Rectangle2D nail, blinkyLookingUp, blinkyLookingRight, shred, stretchedDress[];
	private final V2i nailPosition;

	private Phase phase;
	private long timer;

	public PacManGameIntermissionScene2(PacManGameModel game, SoundManager soundManager, double width, double height,
			double scaling) {
		super(game, soundManager, width, height, scaling, false);

		pac = game.pac;
		blinky = game.ghosts[0];
		chaseTileY = 20;
		nailPosition = new V2i(t(14), t(chaseTileY) - 6);

		// Sprites
		nail = tileRegion(8, 6, 1, 1);
		shred = tileRegion(12, 6, 1, 1);
		blinkyLookingUp = tileRegion(8, 7, 1, 1);
		blinkyLookingRight = tileRegion(9, 7, 1, 1);
		stretchedDress = new Rectangle2D[] { tileRegion(9, 6, 1, 1), tileRegion(10, 6, 1, 1), tileRegion(11, 6, 1, 1) };
	}

	@Override
	public void start() {
		log("Start of intermission scene %s", getClass().getSimpleName());

		pac.visible = true;
		pac.dead = false;
		pac.position = new V2f(t(28) + 50, t(chaseTileY));
		pac.speed = 1;
		pac.couldMove = true;
		pac.dir = LEFT;

		blinky.visible = true;
		blinky.state = HUNTING_PAC;
		blinky.position = pac.position.sum(t(14), 0);
		blinky.speed = 1;
		blinky.dir = blinky.wishDir = LEFT;

		rendering.pacMunching().forEach(Animation::restart);
		rendering.ghostKickingToDir(blinky, blinky.dir).restart();
		soundManager.play(PacManGameSound.INTERMISSION_2);

		phase = Phase.APPROACHING_NAIL;
		timer = -1;
	}

	private void enter(Phase nextPhase, long ticks) {
		phase = nextPhase;
		timer = ticks;
	}

	private void enter(Phase nextPhase) {
		phase = nextPhase;
		timer = -1;
	}

	private boolean timeout() {
		return timer == -1;
	}

	private void update() {
		int distFromNail = (int) (blinky.position.x - nailPosition.x) - 6;
		switch (phase) {
		case APPROACHING_NAIL:
			if (distFromNail == 0) {
				blinky.speed = 0;
				enter(Phase.HITTING_NAIL, clock.sec(0.1));
			}
			break;
		case HITTING_NAIL:
			if (timeout()) {
				blinky.speed = 0.3f;
				enter(Phase.STRETCHED_1);
			}
			break;
		case STRETCHED_1:
			if (distFromNail == -3) {
				blinky.speed = 0.2f;
				enter(Phase.STRETCHED_2);
			}
			break;
		case STRETCHED_2:
			if (distFromNail == -6) {
				blinky.speed = 0.1f;
				enter(Phase.STRETCHED_3);
			}
			break;
		case STRETCHED_3:
			if (distFromNail == -9) {
				blinky.speed = 0;
				enter(Phase.LOOKING_UP, clock.sec(3));
			}
			break;
		case LOOKING_UP:
			if (timeout()) {
				enter(Phase.LOOKING_RIGHT, clock.sec(3));
			}
			break;
		case LOOKING_RIGHT:
			if (timeout()) {
				game.state.duration(0); // signal end of this scene
			}
			break;
		default:
			throw new IllegalStateException("Illegal phase: " + phase);
		}
		blinky.move();
		pac.move();
		if (timer >= 0) {
			--timer;
		}
	}

	@Override
	public void render() {

		update();

		fill(Color.BLACK);
		rendering.drawLevelCounter(game, t(25), t(34));
		rendering.drawRegion(nail, nailPosition.x, nailPosition.y);
		rendering.drawPac(pac, game);
		drawBlinky();
	}

	private void drawBlinky() {
		int baselineY = (int) blinky.position.y - 5;
		int blinkySpriteRightEdge = (int) blinky.position.x + 6;
		switch (phase) {
		case APPROACHING_NAIL:
		case HITTING_NAIL:
			rendering.drawGhost(blinky, game);
			break;
		case STRETCHED_1:
			rendering.drawRegion(stretchedDress[0], blinkySpriteRightEdge - 8, baselineY);
			rendering.drawGhost(blinky, game);
			break;
		case STRETCHED_2:
			rendering.drawRegion(stretchedDress[1], blinkySpriteRightEdge - 4, baselineY);
			rendering.drawGhost(blinky, game);
			break;
		case STRETCHED_3:
			rendering.drawRegion(stretchedDress[2], blinkySpriteRightEdge - 2, baselineY);
			rendering.drawGhost(blinky, game);
			break;
		case LOOKING_UP:
			rendering.drawRegion(blinkyLookingUp, blinky.position.x - 4, blinky.position.y - 4);
			rendering.drawRegion(shred, nailPosition.x, baselineY);
			break;
		case LOOKING_RIGHT:
			rendering.drawRegion(blinkyLookingRight, blinky.position.x - 4, blinky.position.y - 4);
			rendering.drawRegion(shred, nailPosition.x, baselineY);
			break;
		default:
			break;
		}
	}

}