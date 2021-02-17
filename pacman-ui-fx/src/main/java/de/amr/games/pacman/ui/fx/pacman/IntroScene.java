package de.amr.games.pacman.ui.fx.pacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.TS;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.util.stream.Stream;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.GhostState;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.ui.fx.common.AbstractPacManGameScene;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Intro scene of the PacMan game.
 * 
 * @author Armin Reichert
 */
public class IntroScene extends AbstractPacManGameScene<PacManSceneRendering> {

	enum Phase {

		BEGIN, GHOST_GALLERY, CHASING_PAC, CHASING_GHOSTS, READY_TO_PLAY;

		public long start;

		private boolean at(long ticks) {
			return clock.ticksTotal - start == ticks;
		}
	}

	private final Animation<Boolean> blinking = Animation.pulse().frameDuration(20).restart();

	private final int topY = t(6);

	private Ghost[] gallery;
	private int currentGhost;
	private boolean[] characterVisible;
	private boolean[] nickVisible;
	private long ghostKilledTime;

	private Pac pac;
	private Ghost[] ghosts;

	private Phase phase;

	private void enterPhase(Phase newPhase) {
		phase = newPhase;
		phase.start = clock.ticksTotal;
		log("Phase %s entered at %d", phase, phase.start);
	}

	public IntroScene(PacManGameModel game, double width, double height, double scaling) {
		super(game, null, width, height, scaling, PacManGameScenes.rendering);
	}

	@Override
	public void start() {
		gallery = new Ghost[] { //
				new Ghost(0, "Blinky", Direction.RIGHT), //
				new Ghost(1, "Pinky", Direction.RIGHT), //
				new Ghost(2, "Inky", Direction.RIGHT), //
				new Ghost(3, "Clyde", Direction.RIGHT), //
		};
		characterVisible = new boolean[4];
		nickVisible = new boolean[4];

		pac = new Pac("Ms. Pac-Man", Direction.LEFT);

		ghosts = new Ghost[] { //
				new Ghost(0, "Blinky", Direction.LEFT), //
				new Ghost(1, "Pinky", Direction.LEFT), //
				new Ghost(2, "Inky", Direction.LEFT), //
				new Ghost(3, "Clyde", Direction.LEFT), //
		};

		enterPhase(Phase.BEGIN);
	}

	@Override
	public void update() {
		pac.move();
		for (Ghost ghost : ghosts) {
			ghost.move();
		}
		switch (phase) {
		case BEGIN:
			if (phase.at(clock.sec(2))) {
				presentGhost(0);
				enterPhase(Phase.GHOST_GALLERY);
			}
			break;
		case GHOST_GALLERY:
			if (phase.at(clock.sec(0.5))) {
				characterVisible[currentGhost] = true;
			}
			if (phase.at(clock.sec(1))) {
				nickVisible[currentGhost] = true;
			}
			if (phase.at(clock.sec(2))) {
				if (currentGhost < 3) {
					presentGhost(currentGhost + 1);
					enterPhase(Phase.GHOST_GALLERY);
				} else {
					startGhostsChasingPac();
					enterPhase(Phase.CHASING_PAC);
				}
			}
			break;
		case CHASING_PAC:
			if (pac.position.x < t(2)) {
				startPacChasingGhosts();
				enterPhase(Phase.CHASING_GHOSTS);
			}
			break;
		case CHASING_GHOSTS:
			if (pac.position.x > t(28)) {
				enterPhase(Phase.READY_TO_PLAY);
			}
			if (clock.ticksTotal - ghostKilledTime == clock.sec(0.25)) {
				ghostKilledTime = 0;
				pac.visible = true;
				pac.speed = 1;
				for (Ghost ghost : ghosts) {
					if (ghost.state == GhostState.DEAD) {
						ghost.visible = false;
					}
				}
			}
			for (Ghost ghost : ghosts) {
				if (pac.meets(ghost) && ghost.state != GhostState.DEAD) {
					ghost.state = GhostState.DEAD;
					ghost.bounty = (int) Math.pow(2, ghost.id + 1) * 100;
					pac.visible = false;
					pac.speed = 0;
					ghostKilledTime = clock.ticksTotal;
				}
			}
			break;
		case READY_TO_PLAY:
			blinking.animate();
			if (phase.at(clock.sec(5))) {
				game.attractMode = true;
				log("Entering attract mode at %d", clock.ticksTotal);
			}
			break;
		default:
			break;
		}
	}

	private void startGhostsChasingPac() {
		pac.position = new V2f(t(28), t(22));
		pac.visible = true;
		pac.speed = 1;
		pac.dir = Direction.LEFT;
		pac.couldMove = true;
		rendering.pacMunching(pac).forEach(Animation::restart);

		for (Ghost ghost : ghosts) {
			ghost.position = pac.position.sum(8 + (ghost.id + 1) * 18, 0);
			ghost.visible = true;
			ghost.dir = ghost.wishDir = Direction.LEFT;
			ghost.speed = pac.speed * 1.05f;
			ghost.state = GhostState.HUNTING_PAC;
			rendering.ghostsKicking(Stream.of(ghosts)).forEach(Animation::restart);
		}
	}

	private void startPacChasingGhosts() {
		pac.dir = Direction.RIGHT;
		for (Ghost ghost : ghosts) {
			ghost.dir = ghost.wishDir = Direction.RIGHT;
			ghost.speed = 0.5f;
		}
	}

	@Override
	public void render() {
		fill(Color.BLACK);
		rendering.drawScore(g, game, true);
		drawGallery();
		if (phase == Phase.CHASING_PAC) {
			if (blinking.animate()) {
				g.setFill(Color.PINK);
				g.fillOval(t(2), pac.position.y, TS, TS);
			}
		}
		drawGuys();
		if (phase.ordinal() >= Phase.CHASING_GHOSTS.ordinal()) {
			drawPointsAnimation(11, 26);
		}
		if (phase == Phase.READY_TO_PLAY) {
			drawPressKeyToStart(32);
		}
	}

	private void drawGuys() {
		rendering.drawPac(g, pac, game);
		for (Ghost ghost : ghosts) {
			rendering.drawGhost(g, ghost, game);
		}
	}

	private void presentGhost(int id) {
		currentGhost = id;
		gallery[id].visible = true;
	}

	private void drawGallery() {
		int x = t(2);
		g.setFill(Color.WHITE);
		g.setFont(rendering.getScoreFont());
		g.fillText("CHARACTER", t(6), topY);
		g.fillText("/", t(16), topY);
		g.fillText("NICKNAME", t(18), topY);
		showInGallery(gallery[0], "SHADOW", Color.RED, x, topY + t(2), characterVisible[0], nickVisible[0]);
		showInGallery(gallery[1], "SPEEDY", Color.PINK, x, topY + t(5), characterVisible[1], nickVisible[1]);
		showInGallery(gallery[2], "BASHFUL", Color.CYAN, x, topY + t(8), characterVisible[2], nickVisible[2]);
		showInGallery(gallery[3], "POKEY", Color.ORANGE, x, topY + t(11), characterVisible[3], nickVisible[3]);
	}

	private void showInGallery(Ghost ghost, String character, Color color, int x, int y, boolean showCharacter,
			boolean showName) {
		if (!ghost.visible) {
			return;
		}
		Rectangle2D ghostTile = rendering.ghostKickingToDir(ghost, Direction.RIGHT).frame(0);
		rendering.drawRegion(g, rendering.toRegion(ghostTile), x, y - 4);
		g.setFill(color);
		g.setFont(rendering.getScoreFont());
		if (showCharacter) {
			g.fillText("-" + character, t(6), y + 8);
		}
		if (showName) {
			g.fillText("\"" + ghost.name + "\"", t(18), y + 8);
		}
	}

	private void drawPressKeyToStart(int yTile) {
		if (blinking.frame()) {
			String text = "PRESS SPACE TO PLAY";
			g.setFill(Color.ORANGE);
			g.setFont(rendering.getScoreFont());
			g.fillText(text, t(14 - text.length() / 2), t(yTile));
		}
	}

	private void drawPointsAnimation(int tileX, int tileY) {
		if (blinking.frame()) {
			g.setFill(Color.PINK);
			g.fillRect(t(tileX) + 6, t(tileY - 1) + 2, 2, 2);
			g.fillOval(t(tileX), t(tileY + 1) - 2, 10, 10);
		}
		g.setFill(Color.WHITE);
		g.setFont(rendering.getScoreFont());
		g.fillText("10", t(tileX + 2), t(tileY));
		g.fillText("50", t(tileX + 2), t(tileY + 2));
		g.setFont(Font.font(rendering.getScoreFont().getName(), 6));
		g.fillText("PTS", t(tileX + 5), t(tileY));
		g.fillText("PTS", t(tileX + 5), t(tileY + 2));
	}
}