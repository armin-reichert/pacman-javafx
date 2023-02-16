/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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

import java.util.List;
import java.util.Optional;

import de.amr.games.pacman.lib.anim.EntityAnimationByDirection;
import de.amr.games.pacman.lib.anim.EntityAnimationMap;
import de.amr.games.pacman.lib.anim.SingleEntityAnimation;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.World;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Common interface for Pac-Man and Ms. Pac-Man (spritesheet based) rendering.
 * 
 * @author Armin Reichert
 */
public interface Rendering2D {

	class Palette {
		public static final Color RED = Color.rgb(255, 0, 0);
		public static final Color YELLOW = Color.YELLOW;
		public static final Color PINK = Color.rgb(252, 181, 255);
		public static final Color CYAN = Color.rgb(0, 255, 255);
		public static final Color ORANGE = Color.rgb(251, 190, 88);
		public static final Color BLUE = Color.rgb(33, 33, 255);
		public static final Color PALE = Color.rgb(222, 222, 255);
		public static final Color ROSE = Color.rgb(252, 187, 179);
	}

	Font arcadeFont(double size);

	Color ghostColor(int ghostID);

	Color mazeBackgroundColor(int mazeNumber);

	Color mazeFoodColor(int mazeNumber);

	Color mazeTopColor(int mazeNumber);

	Color mazeSideColor(int mazeNumber);

	Color ghostHouseDoorColor();

	// Sprites, images

	Rectangle2D lifeSymbolRegion();

	Rectangle2D bonusSymbolRegion(int symbol);

	Image bonusSymbolImage(int symbol);

	Rectangle2D bonusValueRegion(int symbol);

	Image bonusValueImage(int symbol);

	// Animations

	EntityAnimationMap<AnimKeys> createPacAnimations(Pac pac);

	EntityAnimationByDirection createPacMunchingAnimation(Pac pac);

	SingleEntityAnimation<Rectangle2D> createPacDyingAnimation();

	EntityAnimationMap<AnimKeys> createGhostAnimations(Ghost ghost);

	EntityAnimationByDirection createGhostColorAnimation(Ghost ghost);

	SingleEntityAnimation<Rectangle2D> createGhostBlueAnimation();

	SingleEntityAnimation<Rectangle2D> createGhostFlashingAnimation();

	EntityAnimationByDirection createGhostEyesAnimation(Ghost ghost);

	SingleEntityAnimation<Boolean> createMazeFlashingAnimation();

	// Drawing

	void drawText(GraphicsContext g, String text, Color color, Font font, double x, double y);

	void drawPac(GraphicsContext g, Pac pac);

	void drawGhost(GraphicsContext g, Ghost ghost);

	void drawGhostFacingRight(GraphicsContext g, int id, int x, int y);

	void drawBonus(GraphicsContext g, Bonus bonus);

	void drawCopyright(GraphicsContext g, int tileY);

	void drawLevelCounter(GraphicsContext g, Optional<Integer> levelNumber, List<Byte> levelCounter);

	void drawLivesCounter(GraphicsContext g, int numLivesDisplayed);

	void drawScore(GraphicsContext g, int points, int levelNumber, String title, Color color, double x, double y);

	void drawCredit(GraphicsContext g, int credit);

	void drawEmptyMaze(GraphicsContext g, int x, int y, int mazeNumber, boolean flash);

	void drawMaze(GraphicsContext g, int x, int y, int mazeNumber, World world, boolean energizersHidden);

	void drawGameReadyMessage(GraphicsContext g);

	void drawGameOverMessage(GraphicsContext g);
}