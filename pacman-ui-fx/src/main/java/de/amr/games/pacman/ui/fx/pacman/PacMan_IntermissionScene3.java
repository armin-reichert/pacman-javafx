package de.amr.games.pacman.ui.fx.pacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX.RENDERING_PACMAN;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.guys.Ghost;
import de.amr.games.pacman.model.guys.GhostState;
import de.amr.games.pacman.model.guys.Pac;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX;
import de.amr.games.pacman.ui.fx.common.GameScene;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;

/**
 * Third intermission scene: Blinky in shred dress chases Pac-Man, comes back half-naked drawing
 * dress over the floor.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene3 extends GameScene {

	enum Phase {
		CHASING_PACMAN, RETURNING_HALF_NAKED;
	}

	private final SoundManager sounds = PacManGameUI_JavaFX.SOUNDS_PACMAN;

	private final Animation<?> blinkyDamaged, blinkyNaked;
	private final int chaseTileY = 20;
	private final Ghost blinky;
	private final Pac pac;
	private Phase phase;

	public PacMan_IntermissionScene3(Group root, double width, double height, double scaling) {
		super(root, width, height, scaling, RENDERING_PACMAN);
		blinkyDamaged = RENDERING_PACMAN.blinkyDamaged();
		blinkyNaked = RENDERING_PACMAN.blinkyNaked();
		pac = new Pac("Pac-Man", Direction.LEFT);
		blinky = new Ghost(0, "Blinky", Direction.LEFT);
	}

	@Override
	public void start() {
		log("Start intermission scene %s at %d", this, clock.ticksTotal);

		pac.visible = true;
		pac.dead = false;
		pac.setPosition(t(30), t(chaseTileY));
		pac.speed = 1.2f;
		pac.couldMove = true;
		pac.dir = LEFT;
		pac.couldMove = true;

		blinky.visible = true;
		blinky.state = GhostState.HUNTING_PAC;
		blinky.setPosition(pac.position.sum(64, 0));
		blinky.speed = pac.speed;
		blinky.dir = blinky.wishDir = LEFT;

		rendering.playerMunching(pac).forEach(Animation::restart);
		blinkyDamaged.restart();
		sounds.loop(PacManGameSound.INTERMISSION_3, 2);

		phase = Phase.CHASING_PACMAN;
	}

	@Override
	public void update() {
		switch (phase) {
		case CHASING_PACMAN:
			if (blinky.position.x <= -50) {
				pac.speed = 0;
				blinky.dir = blinky.wishDir = RIGHT;
				blinkyNaked.restart();
				phase = Phase.RETURNING_HALF_NAKED;
			}
			break;
		case RETURNING_HALF_NAKED:
			if (blinky.position.x > t(28) + 200) {
				game.state.timer.setDuration(0); // end scene
			}
			break;
		default:
			throw new IllegalStateException("Illegal phase: " + phase);
		}
		blinky.move();
		pac.move();
	}

	@Override
	public void render() {
		rendering.drawLevelCounter(g, game, t(25), t(34));
		rendering.drawPlayer(g, pac);
		drawBlinky();
	}

	private void drawBlinky() {
		switch (phase) {
		case CHASING_PACMAN:
			rendering.drawSprite(g, (Rectangle2D) blinkyDamaged.animate(), blinky.position.x - 4, blinky.position.y - 4);
			break;
		case RETURNING_HALF_NAKED:
			rendering.drawSprite(g, (Rectangle2D) blinkyNaked.animate(), blinky.position.x - 4, blinky.position.y - 4);
			break;
		default:
			break;
		}
	}
}