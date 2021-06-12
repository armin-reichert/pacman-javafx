package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import de.amr.games.pacman.model.common.PacManGameModel;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_Assets;
import javafx.scene.Group;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class ScoreNotReally3D extends Group {

	private final Text txtScoreTitle;
	private final Text txtScore;
	private final Text txtHiscoreTitle;
	private final Text txtHiscore;

	public ScoreNotReally3D() {
		Font font = Rendering2D_Assets.ARCADE_FONT;

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
		grid.setHgap(4 * TS);
		grid.setTranslateY(-2 * TS);
		grid.setTranslateZ(-2 * TS);
		grid.add(txtScoreTitle, 0, 0);
		grid.add(txtScore, 0, 1);
		grid.add(txtHiscoreTitle, 1, 0);
		grid.add(txtHiscore, 1, 1);

		getChildren().add(grid);
	}

	public void update(PacManGameModel game, boolean showHighscoreOnly) {
		txtScore.setText(String.format("%07d L%d", game.score(), game.level().number));
		txtHiscore.setText(String.format("%07d L%d", game.hiscorePoints(), game.hiscoreLevel()));
		if (showHighscoreOnly) {
			txtScore.setVisible(false);
		} else {
			txtScore.setVisible(true);
		}
	}
}