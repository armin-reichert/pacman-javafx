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
package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.common.world.World.TS;

import de.amr.games.pacman.model.common.GameModel;
import javafx.scene.Group;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Displays the score and high score.
 * 
 * @author Armin Reichert
 */
public class Scores3D extends Group {

	public final Text txtScoreTitle;
	public final Text txtScore;
	public final Text txtHiscoreTitle;
	public final Text txtHiscore;

	private Color titleColor = Color.GHOSTWHITE;
	private Color scoreColor = Color.YELLOW;
	private Font font;
	private boolean computeScoreText = true;

	public Scores3D() {
		txtScoreTitle = new Text("SCORE");
		txtScore = new Text();
		txtHiscoreTitle = new Text("HIGH SCORE");
		txtHiscore = new Text();
		GridPane grid = new GridPane();
		grid.setHgap(5 * TS);
		grid.setTranslateY(-2 * TS);
		grid.setTranslateZ(-2 * TS);
		grid.add(txtScoreTitle, 0, 0);
		grid.add(txtScore, 0, 1);
		grid.add(txtHiscoreTitle, 1, 0);
		grid.add(txtHiscore, 1, 1);
		getChildren().add(grid);
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public void setComputeScoreText(boolean state) {
		this.computeScoreText = state;
	}

	public void update(GameModel game) {
		int gamePoints = game.scores.gameScore.points;
		int gameLevelNumber = game.level.number;
		int highscorePoints = game.scores.highScore.points;
		int highscoreLevelNumber = game.scores.highScore.levelNumber;

		txtScoreTitle.setFill(titleColor);
		txtScoreTitle.setFont(font);
		if (computeScoreText) {
			txtScore.setText(String.format("%7d L%d", gamePoints, gameLevelNumber));
			txtScore.setFill(Color.YELLOW);
		}
		txtScore.setFont(font);

		txtHiscoreTitle.setFill(titleColor);
		txtHiscoreTitle.setFont(font);
		txtHiscore.setFill(scoreColor);
		txtHiscore.setFont(font);
		txtHiscore.setText(String.format("%7d L%d", highscorePoints, highscoreLevelNumber));
	}
}