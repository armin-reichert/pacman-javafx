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

import de.amr.games.pacman.lib.Score;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 2D representation of the score / high score.
 * 
 * @author Armin Reichert
 */
public class GameScore2D {

	public final Score score;
	public double x, y;
	public boolean showPoints = true;
	public String title;
	public Color titleColor = Color.WHITE;
	public Color pointsColor = Color.WHITE;

	public GameScore2D(Score score, String title, double x, double y) {
		this.score = score;
		this.title = title;
		this.x = x;
		this.y = y;
	}

	public void render(GraphicsContext g, Rendering2D r2D) {
		String pointsText, levelText;
		if (!showPoints) {
			pointsText = "00";
			levelText = "";
		} else {
			pointsText = "%02d".formatted(score.points);
			levelText = "L" + score.levelNumber;
		}
		g.setFont(r2D.getArcadeFont());
		g.setFill(titleColor);
		g.fillText(title, x, y);
		g.setFill(pointsColor);
		g.fillText("%7s".formatted(pointsText), x, y + t(1));
		g.setFill(Color.LIGHTGRAY);
		g.fillText(levelText, x + t(8), y + t(1));
	}
}