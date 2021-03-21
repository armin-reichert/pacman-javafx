package de.amr.games.pacman.ui.fx.common.scene3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.function.Supplier;

import de.amr.games.pacman.model.common.GameModel;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class ScoreNotReally3D implements Supplier<Node> {

	private final Group root;
	private final Text txtScore;
	private final Text txtHiscore;

	public ScoreNotReally3D() {
		Font font = Assets3D.ARCADE_FONT;

		Text txtScoreTitle = new Text("SCORE");
		txtScoreTitle.setFill(Color.WHITE);
		txtScoreTitle.setFont(font);

		txtScore = new Text();
		txtScore.setFill(Color.YELLOW);
		txtScore.setFont(font);

		Text txtHiscoreTitle = new Text("HI SCORE");
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

	@Override
	public Node get() {
		return root;
	}

	public void update(GameModel game) {
		txtScore.setText(String.format("%07d L%d", game.score, game.levelNumber));
		txtHiscore.setText(String.format("%07d L%d", game.highscorePoints, game.highscoreLevel));
	}
}