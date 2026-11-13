/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.score;

import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.basics.Tile.TS;
import static java.util.Objects.requireNonNull;

public class ScoresView {

    private final GridPane gridPane = new GridPane();

    private final ScoreView leftScoreView;

    private final ScoreView rightScoreView;

    public ScoresView(ScoreView leftScoreView, ScoreView rightScoreView) {
        this.leftScoreView = requireNonNull(leftScoreView);
        this.rightScoreView = requireNonNull(rightScoreView);

        gridPane.setHgap(5 * TS);

        gridPane.add(leftScoreView.titleDisplay(),  0, 0);
        gridPane.add(leftScoreView.textDisplay(),   0, 1);

        gridPane.add(rightScoreView.titleDisplay(), 1, 0);
        gridPane.add(rightScoreView.textDisplay(),  1, 1);

        leftScoreView.titleDisplay().setFill(Color.GHOSTWHITE);

        rightScoreView.titleDisplay().setFill(Color.GHOSTWHITE);
        rightScoreView.textDisplay().setFill(Color.YELLOW);
    }

    public void setFont(Font font) {
        leftScoreView.titleDisplay().setFont(font);
        leftScoreView.textDisplay().setFont(font);

        rightScoreView.titleDisplay().setFont(font);
        rightScoreView.textDisplay().setFont(font);
    }

    public GridPane root() {
        return gridPane;
    }

    public void showScore(int score, int levelNumber) {
        leftScoreView.textDisplay().setFill(Color.YELLOW);
        leftScoreView.textDisplay().setText(String.format("%7d L%d", score, levelNumber));
    }

    public void showHighScore(int highScore, int highScoreLevelNumber) {
        rightScoreView.textDisplay().setFill(Color.YELLOW);
        rightScoreView.textDisplay().setText(String.format("%7d L%d", highScore, highScoreLevelNumber));
    }

    public void showTextForScore(String text, Color color) {
        leftScoreView.textDisplay().setFill(color);
        leftScoreView.textDisplay().setText(text);
    }
}
