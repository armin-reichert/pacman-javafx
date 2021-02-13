package de.amr.games.pacman.ui.fx.scene.pacman;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.GhostState;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.fx.scene.common.AbstractPacManGameScene;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;

/**
 * Third intermission scene: Blinky in shred dress chases Pac-Man, comes back half-naked drawing
 * dress over the floor.
 * 
 * @author Armin Reichert
 */
public class PacManGameIntermissionScene3 extends AbstractPacManGameScene {

	enum Phase {
		CHASING_PACMAN, RETURNING_HALF_NAKED
	}

	private final Animation<Rectangle2D> blinkyDamaged, blinkyHalfNaked;
	private final int chaseTileY;
	private final Ghost blinky;
	private final Pac pac;
	private Phase phase;
	private long timer;

	public PacManGameIntermissionScene3(PacManGameModel game, SoundManager soundManager, double width, double height,
			double scaling) {
		super(game, soundManager, width, height, scaling, false);

		pac = game.pac;
		blinky = game.ghosts[0];
		chaseTileY = 20;

		blinkyDamaged = Animation.of(tileRegion(10, 7, 1, 1), tileRegion(11, 7, 1, 1));
		blinkyDamaged.frameDuration(4).endless();
		blinkyHalfNaked = Animation.of(tileRegion(8, 8, 2, 1), tileRegion(10, 8, 2, 1));
		blinkyHalfNaked.frameDuration(4).endless();
	}

	@Override
	public void start() {
		log("Start intermission scene %s", getClass().getSimpleName());

		pac.visible = true;
		pac.dead = false;
		pac.position = new V2f(t(28) + 50, t(chaseTileY));
		pac.speed = 1;
		pac.couldMove = true;
		pac.dir = LEFT;

		blinky.visible = true;
		blinky.state = GhostState.HUNTING_PAC;
		blinky.position = pac.position.sum(64, 0);
		blinky.speed = pac.speed;
		blinky.dir = blinky.wishDir = LEFT;

		rendering.pacMunching().forEach(Animation::restart);
		blinkyDamaged.restart();
		soundManager.loop(PacManGameSound.INTERMISSION_3, 2);

		phase = Phase.CHASING_PACMAN;
	}

	private void update() {
		switch (phase) {
		case CHASING_PACMAN:
			if (blinky.position.x <= -50) {
				pac.speed = 0;
				blinky.dir = blinky.wishDir = RIGHT;
				blinkyHalfNaked.restart();
				phase = Phase.RETURNING_HALF_NAKED;
			}
			break;
		case RETURNING_HALF_NAKED:
			if (blinky.position.x > t(28) + 50) {
				game.state.duration(0); // end scene
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
		rendering.drawPac(pac, game);
		drawBlinky();
	}

	private void drawBlinky() {
		switch (phase) {
		case CHASING_PACMAN:
			rendering.drawRegion(blinkyDamaged.animate(), blinky.position.x - 4, blinky.position.y - 4);
			break;
		case RETURNING_HALF_NAKED:
			rendering.drawRegion(blinkyHalfNaked.animate(), blinky.position.x - 4, blinky.position.y - 4);
			break;
		default:
			break;
		}
	}
}