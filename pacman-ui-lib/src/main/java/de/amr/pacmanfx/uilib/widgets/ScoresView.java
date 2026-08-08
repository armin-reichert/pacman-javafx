package de.amr.pacmanfx.uilib.widgets;

import de.amr.pacmanfx.core.entities.Score;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.uilib.entities3D.score.comp.ScoreViewComp;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static java.util.Objects.requireNonNull;

public class ScoresView {

    private final GridPane gridPane = new GridPane();

    private final Score leftScore;

    private final Score rightScore;

    public ScoresView(Score leftScore, Score rightScore) {
        this.leftScore = requireNonNull(leftScore);
        this.rightScore = requireNonNull(rightScore);

        gridPane.setHgap(5 * WorldMap.TS);

        final ScoreViewComp leftScoreView = leftScore.requireComp(ScoreViewComp.class);
        gridPane.add(leftScoreView.titleDisplay(),  0, 0);
        gridPane.add(leftScoreView.textDisplay(),   0, 1);

        final ScoreViewComp rightScoreView = rightScore.requireComp(ScoreViewComp.class);
        gridPane.add(rightScoreView.titleDisplay(), 1, 0);
        gridPane.add(rightScoreView.textDisplay(),  1, 1);

        leftScoreView.titleDisplay().setFill(Color.GHOSTWHITE);

        rightScoreView.titleDisplay().setFill(Color.GHOSTWHITE);
        rightScoreView.textDisplay().setFill(Color.YELLOW);
    }

    public void setFont(Font font) {
        final ScoreViewComp leftScoreView = leftScore.requireComp(ScoreViewComp.class);
        leftScoreView.titleDisplay().setFont(font);
        leftScoreView.textDisplay().setFont(font);

        final ScoreViewComp rightScoreView = rightScore.requireComp(ScoreViewComp.class);
        rightScoreView.titleDisplay().setFont(font);
        rightScoreView.textDisplay().setFont(font);
    }

    public GridPane root() {
        return gridPane;
    }

    public void showScore(int score, int levelNumber) {
        final ScoreViewComp leftScoreView = leftScore.requireComp(ScoreViewComp.class);
        leftScoreView.textDisplay().setFill(Color.YELLOW);
        leftScoreView.textDisplay().setText(String.format("%7d L%d", score, levelNumber));
    }

    public void showHighScore(int highScore, int highScoreLevelNumber) {
        final ScoreViewComp rightScoreView = rightScore.requireComp(ScoreViewComp.class);
        rightScoreView.textDisplay().setFill(Color.YELLOW);
        rightScoreView.textDisplay().setText(String.format("%7d L%d", highScore, highScoreLevelNumber));
    }

    public void showTextForScore(String text, Color color) {
        final ScoreViewComp leftScoreView = leftScore.requireComp(ScoreViewComp.class);
        leftScoreView.textDisplay().setFill(color);
        leftScoreView.textDisplay().setText(text);
    }
}
