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

import de.amr.games.pacman.model.common.actors.Score;
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
	public boolean showContent = true;
	public String title;
	public Color titleColor = Color.WHITE;
	public Color pointsColor = Color.WHITE;
	public Color levelTextColor = Color.LIGHTGRAY;

	public GameScore2D(Score score, String title) {
		this.score = score;
		this.title = title;
	}

	public void render(GraphicsContext g, Rendering2D r2D) {
		if (!score.visible) {
			return;
		}
		String pointsText = showContent ? "%02d".formatted(score.points) : "00";
		String levelText = showContent ? "L" + score.levelNumber : "";
		g.setFont(r2D.getArcadeFont());
		g.setFill(titleColor);
		g.fillText(title, score.position.x, score.position.y);
		g.setFill(pointsColor);
		g.fillText("%7s".formatted(pointsText), score.position.x, score.position.y + t(1));
		g.setFill(levelTextColor);
		g.fillText(levelText, score.position.x + t(8), score.position.y + t(1));
	}
}