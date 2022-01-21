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
package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

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
public class ScoreNotReally3D extends Group {

	private final Text txtScoreTitle;
	private final Text txtScore;
	private final Text txtHiscoreTitle;
	private final Text txtHiscore;

	public ScoreNotReally3D(Font font) {

		txtScoreTitle = new Text("SCORE");
		txtScoreTitle.setFill(Color.WHITE);
		txtScoreTitle.setFont(font);

		txtScore = new Text();
		txtScore.setFill(Color.YELLOW);
		txtScore.setFont(font);

		txtHiscoreTitle = new Text("HIGH SCORE");
		txtHiscoreTitle.setFill(Color.WHITE);
		txtHiscoreTitle.setFont(font);

		txtHiscore = new Text();
		txtHiscore.setFill(Color.YELLOW);
		txtHiscore.setFont(font);

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

	public void update(GameModel game, String scoreTextOverwrite) {
		txtScore.setText(String.format("%7d L%d", game.score, game.levelNumber));
		txtHiscore.setText(String.format("%7d L%d", game.hiscorePoints, game.hiscoreLevel));
		if (scoreTextOverwrite != null) {
			txtScore.setText(scoreTextOverwrite);
		}
	}
}