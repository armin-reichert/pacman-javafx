/*
MIT License

Copyright (c) 2022 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package de.amr.games.pacman.ui.fx._2d.rendering.common;

import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.t;

import java.util.List;
import java.util.Optional;

import de.amr.games.pacman.lib.anim.EntityAnimationByDirection;
import de.amr.games.pacman.lib.anim.EntityAnimationMap;
import de.amr.games.pacman.lib.anim.SingleEntityAnimation;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.common.world.World;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class TestRenderer2D implements Rendering2D {

	@Override
	public Font arcadeFont(double size) {
		return Font.font(SpritesheetGameRenderer.ARCADE_FONT_TS.getFamily(), size);
	}

	@Override
	public Color ghostColor(int ghostID) {
		return switch (ghostID) {
		case Ghost.ID_RED_GHOST -> Palette.RED;
		case Ghost.ID_PINK_GHOST -> Palette.PINK;
		case Ghost.ID_CYAN_GHOST -> Palette.CYAN;
		case Ghost.ID_ORANGE_GHOST -> Palette.ORANGE;
		default -> throw new IllegalArgumentException();
		};
	}

	@Override
	public Color mazeBackgroundColor(int mazeNumber) {
		return Color.BLACK;
	}

	@Override
	public Color mazeFoodColor(int mazeNumber) {
		return Color.PINK;
	}

	@Override
	public Color mazeTopColor(int mazeNumber) {
		return Color.BLUE;
	}

	@Override
	public Color mazeSideColor(int mazeNumber) {
		return Color.BLUE;
	}

	@Override
	public Color ghostHouseDoorColor() {
		return Color.PINK;
	}

	@Override
	public EntityAnimationMap<AnimKeys> createPacAnimations(Pac pac) {
		return null;
	}

	@Override
	public EntityAnimationByDirection createPacMunchingAnimation(Pac pac) {
		return null;
	}

	@Override
	public SingleEntityAnimation<Rectangle2D> createPacDyingAnimation() {
		return null;
	}

	@Override
	public EntityAnimationMap<AnimKeys> createGhostAnimations(Ghost ghost) {
		return null;
	}

	@Override
	public EntityAnimationByDirection createGhostColorAnimation(Ghost ghost) {
		return null;
	}

	@Override
	public SingleEntityAnimation<Rectangle2D> createGhostBlueAnimation() {
		return null;
	}

	@Override
	public SingleEntityAnimation<Rectangle2D> createGhostFlashingAnimation() {
		return null;
	}

	@Override
	public EntityAnimationByDirection createGhostEyesAnimation(Ghost ghost) {
		return null;
	}

	@Override
	public SingleEntityAnimation<Boolean> createMazeFlashingAnimation() {
		return null;
	}

	@Override
	public void drawText(GraphicsContext g, String text, Color color, Font font, double x, double y) {
		g.setFont(font);
		g.setFill(color);
		g.fillText(text, x, y);
	}

	@Override
	public void drawPac(GraphicsContext g, Pac pac) {
		if (pac.isVisible()) {
			g.setFill(Color.YELLOW);
			g.fillOval(pac.position().x() - HTS, pac.position().y() - HTS, 2 * TS, 2 * TS);
		}
	}

	@Override
	public void drawGhost(GraphicsContext g, Ghost ghost) {
		if (!ghost.isVisible()) {
			return;
		}
		switch (ghost.state()) {
		case EATEN, RETURNING_TO_HOUSE, ENTERING_HOUSE -> {
			var color = Color.WHITE;
			g.setStroke(color);
			g.strokeOval(ghost.position().x() - 3, ghost.position().y() + 2, 4, 4);
			g.strokeOval(ghost.position().x() + 3, ghost.position().y() + 2, 4, 4);
		}
		case FRIGHTENED -> {
			var color = Color.BLUE;
			g.setFill(color);
			g.fillOval(ghost.position().x() - HTS, ghost.position().y() - HTS, 2 * TS, 2 * TS);

		}
		default -> {
			var color = ghostColor(ghost.id());
			g.setFill(color);
			g.fillOval(ghost.position().x() - HTS, ghost.position().y() - HTS, 2 * TS, 2 * TS);
		}
		}
	}

	@Override
	public void drawGhostFacingRight(GraphicsContext g, int id, int x, int y) {
		var color = ghostColor(id);
		g.setFill(color);
		g.fillOval(x - HTS, y - HTS, 2 * TS, 2 * TS);
	}

	@Override
	public void drawBonus(GraphicsContext g, Bonus bonus) {
	}

	@Override
	public void drawCopyright(GraphicsContext g, int tileY) {
	}

	@Override
	public void drawLevelCounter(GraphicsContext g, Optional<Integer> levelNumber, List<Byte> levelCounter) {
		levelNumber.ifPresent(number -> {
			drawText(g, "Level %s".formatted(number), Color.WHITE, arcadeFont(8), 18 * TS, 36 * TS - 2);
		});
	}

	@Override
	public void drawLivesCounter(GraphicsContext g, int numLivesDisplayed) {
		if (numLivesDisplayed <= 0) {
			return;
		}
		int x = t(2);
		int y = t(ArcadeWorld.SIZE_TILES.y() - 2);
		int maxLives = 5;
		for (int i = 0; i < Math.min(numLivesDisplayed, maxLives); ++i) {
			g.setFill(Color.YELLOW);
			g.fillOval(x + t(2 * i) - HTS, y - HTS, 2 * TS, 2 * TS);
		}
	}

	@Override
	public void drawScore(GraphicsContext g, int points, int levelNumber, String title, Color color, double x, double y) {
		var font = arcadeFont(TS);
		drawText(g, title, color, font, x, y);
		var pointsText = "%02d".formatted(points);
		drawText(g, "%7s".formatted(pointsText), color, font, x, y + TS + 1);
		if (points != 0) {
			drawText(g, "L" + levelNumber, color, font, x + t(8), y + TS + 1);
		}
	}

	@Override
	public void drawCredit(GraphicsContext g, int credit) {
		drawText(g, "CREDIT  %d".formatted(credit), Palette.PALE, arcadeFont(TS), t(2), t(36) - 1);
	}

	@Override
	public void drawEmptyMaze(GraphicsContext g, int x, int y, int mazeNumber, boolean flash) {
	}

	@Override
	public void drawMaze(GraphicsContext g, int x, int y, int mazeNumber, World world, boolean energizersHidden) {
		for (int row = 0; row < world.numRows(); ++row) {
			for (int col = 0; col < world.numCols(); ++col) {
				var tile = new Vector2i(col, row);
				if (world.isWall(tile)) {
					g.setFill(Color.BLUE.darker());
					g.fillRect(tile.x() * TS, tile.y() * TS, TS, TS);
				}
				if (world.containsFood(tile)) {
					g.setFill(mazeFoodColor(mazeNumber));
					if (world.isEnergizerTile(tile)) {
						g.fillOval(tile.x() * TS + 1, tile.y() * TS + 1, 6, 6);
					} else {
						g.fillRect(tile.x() * TS + 3, tile.y() * TS + 3, 2, 2);
					}
				}
			}
		}

	}

	@Override
	public void drawGameReadyMessage(GraphicsContext g) {
		drawText(g, "READY!", Palette.YELLOW, arcadeFont(TS), t(11), t(21));
	}

	@Override
	public void drawGameOverMessage(GraphicsContext g) {
		drawText(g, "GAME  OVER", Palette.RED, arcadeFont(TS), t(9), t(21));
	}
}