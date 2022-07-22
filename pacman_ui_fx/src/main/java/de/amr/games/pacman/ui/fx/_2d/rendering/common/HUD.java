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

import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Score;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class HUD {

	public final DoubleProperty widthPy = new SimpleDoubleProperty();
	public final DoubleProperty heightPy = new SimpleDoubleProperty();
	public final DoubleProperty scalingPy = new SimpleDoubleProperty();

	public boolean creditVisible;
	private Font font;

	public HUD() {
		font = Ufx.font("fonts/emulogic.ttf", 8);
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public void draw(GraphicsContext g, GameModel game) {
		var scaling = scalingPy.get();
		g.save();
		g.scale(scaling, scaling);
		drawScore(g, game.scores.gameScore, TS, TS, font);
		drawScore(g, game.scores.highScore, 16 * TS, TS, font);
		if (creditVisible) {
			fillText(g, "CREDIT  %d".formatted(game.getCredit()), Color.WHITE, font, t(2), t(36) - 1);
		}
		g.restore();
	}

	private void drawScore(GraphicsContext g, Score score, double x, double y, Font font) {
		if (score.visible) {
			fillText(g, score.title, Color.WHITE, font, x, y);
			var pointsText = score.showContent ? "%02d".formatted(score.points) : "00";
			fillText(g, "%7s".formatted(pointsText), Color.WHITE, font, x, y + TS + 1);
			var levelText = score.showContent ? "L" + score.levelNumber : "";
			fillText(g, levelText, Color.LIGHTGRAY, font, x + t(8), y + TS + 1);
		}
	}

	private void fillText(GraphicsContext g, String text, Color color, Font font, double x, double y) {
		g.setFont(font);
		g.setFill(color);
		g.fillText(text, x, y);
	}
}