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
package de.amr.games.pacman.ui.fx._2d.entity.common;

import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.Rendering2D_PacMan;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 2D representation of the score / high score.
 * 
 * @author Armin Reichert
 */
public class GameScore2D extends GameEntity2D {

	public boolean showHighscore = false;
	public boolean showPoints = true;
	public String title = "SCORE";
	public Color titleColor = Color.WHITE;
	public Color pointsColor = Color.WHITE;

	public GameScore2D(GameModel game) {
		super(game);
	}

	@Override
	public void render(GraphicsContext g) {
		String pointsText = "%02d"
				.formatted(showHighscore ? game.scoreManager().hiscore().points : game.scoreManager().score().points);
		String levelText = "L" + (showHighscore ? game.scoreManager().hiscore().levelNumber : game.level.number);
		if (!showHighscore && !showPoints) {
			pointsText = "00";
			levelText = "";
		}
		g.setFont(Rendering2D_PacMan.get().getArcadeFont());
		g.setFill(titleColor);
		g.fillText(title, x, y);
		g.setFill(pointsColor);
		g.fillText("%7s".formatted(pointsText), x, y + t(1));
		g.setFill(Color.LIGHTGRAY);
		g.fillText(levelText, x + t(8), y + t(1));
	}
}