package de.amr.games.pacman.ui.fx.scenes.common._3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.function.Supplier;

import de.amr.games.pacman.model.common.AbstractGameModel;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class ScoreNotReally3D implements Supplier<Node> {

	private final Group root;
	private final Text txtScoreTitle;
	private final Text txtScore;
	private final Text txtHiscoreTitle;
	private final Text txtHiscore;
	private boolean hiscoreOnly;

	public ScoreNotReally3D() {
		hiscoreOnly = false;
		Font font = Assets3D.ARCADE_FONT;

		txtScoreTitle = new Text("SCORE");
		txtScoreTitle.setFill(Color.WHITE);
		txtScoreTitle.setFont(font);

		txtScore = new Text();
		txtScore.setFill(Color.YELLOW);
		txtScore.setFont(font);

		txtHiscoreTitle = new Text("HI SCORE");
		txtHiscoreTitle.setFill(Color.WHITE);
		txtHiscoreTitle.setFont(font);

		txtHiscore = new Text();
		txtHiscore.setFill(Color.YELLOW);
		txtHiscore.setFont(font);

		GridPane grid = new GridPane();
		grid.setHgap(4 * TS);
		grid.setTranslateY(-2 * TS);
		grid.setTranslateZ(-2 * TS);
		grid.getChildren().clear();
		grid.add(txtScoreTitle, 0, 0);
		grid.add(txtScore, 0, 1);
		grid.add(txtHiscoreTitle, 1, 0);
		grid.add(txtHiscore, 1, 1);

		root = new Group(grid);
	}

	public void setHiscoreOnly(boolean hiscoreOnly) {
		this.hiscoreOnly = hiscoreOnly;
	}

	@Override
	public Node get() {
		return root;
	}

	public void update(AbstractGameModel game) {
		txtScore.setText(String.format("%07d L%d", game.score, game.currentLevelNumber));
		txtHiscore.setText(String.format("%07d L%d", game.highscorePoints, game.highscoreLevel));
		if (hiscoreOnly) {
			txtScore.setVisible(false);
		} else {
			txtScore.setVisible(true);
		}
	}
}