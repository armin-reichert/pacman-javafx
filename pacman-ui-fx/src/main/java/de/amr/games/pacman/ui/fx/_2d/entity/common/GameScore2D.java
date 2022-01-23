/*
MIT License

Copyright (c) 2021 Armin Reichert

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

import static de.amr.games.pacman.model.world.World.t;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 2D representation of the score or the high score.
 * 
 * @author Armin Reichert
 */
public class GameScore2D implements Renderable2D {

	public boolean showPoints = true;

	private final GameModel game;
	private final Rendering2D rendering;
	private final boolean showHighscore;

	private int x;
	private int y;
	private String title = "SCORE";
	private Color titleColor = Color.WHITE;
	private Color pointsColor = Color.YELLOW;

	public GameScore2D(String title, int x, int y, GameModel game, boolean showHighscore, Rendering2D rendering) {
		this.title = title;
		this.x = x;
		this.y = y;
		this.game = game;
		this.showHighscore = showHighscore;
		this.rendering = rendering;
	}

	@Override
	public void render(GraphicsContext g) {
		g.save();
		g.translate(x, y);
		g.setFont(rendering.getScoreFont());
		g.translate(0, 2);
		g.setFill(titleColor);
		g.fillText(title, 0, 0);
		g.translate(0, 1);
		if (showPoints) {
			g.setFill(pointsColor);
			g.translate(0, t(1));
			g.fillText(String.format("%7d", showHighscore ? game.hiscorePoints : game.score), 0, 0);
			g.setFill(Color.LIGHTGRAY);
			g.fillText(String.format("L%d", showHighscore ? game.hiscoreLevel : game.levelNumber), t(8), 0);
		}
		g.restore();
	}
}