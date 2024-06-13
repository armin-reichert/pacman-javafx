/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.entity;

import javafx.beans.property.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import static de.amr.games.pacman.lib.Globals.TS;

/**
 * Displays the score and high score.
 *
 * @author Armin Reichert
 */
public class Scores3D extends GridPane {

    public ObjectProperty<Font> fontPy = new SimpleObjectProperty<>(this, "font", Font.font("Courier", 12));

    public BooleanProperty altTextShownPy = new SimpleBooleanProperty(this, "altTextShown", false) {
        @Override
        protected void invalidated() {
            txtScore.setText(textPy.get());
        }
    };

    public StringProperty textPy = new SimpleStringProperty(this, "text", "") {
        @Override
        protected void invalidated() {
            txtScore.setText(get());
        }
    };

    private final Text txtScore;
    private final Text txtHighScore;

    public Scores3D() {
        var txtScoreTitle = new Text("SCORE");
        txtScoreTitle.setFill(Color.GHOSTWHITE);
        txtScoreTitle.fontProperty().bind(fontPy);

        txtScore = new Text();
        txtScore.fontProperty().bind(fontPy);

        var txtHighScoreTitle = new Text("HIGH SCORE");
        txtHighScoreTitle.setFill(Color.GHOSTWHITE);
        txtHighScoreTitle.fontProperty().bind(fontPy);

        txtHighScore = new Text();
        txtHighScore.setFill(Color.YELLOW);
        txtHighScore.fontProperty().bind(fontPy);

        setHgap(5 * TS);
        add(txtScoreTitle,  0,0);
        add(txtScore,       0,1);
        add(txtHighScoreTitle,1,0);
        add(txtHighScore,     1,1);
    }

    public void showScores(int score, int levelNumber, int highScore, int highScoreLevelNumber) {
        altTextShownPy.set(false);
        txtScore.setFill(Color.YELLOW);
        txtScore.setText(String.format("%7d L%d", score, levelNumber));
        txtHighScore.setText(String.format("%7d L%d", highScore, highScoreLevelNumber));
    }

    public void showAlternativeText(String text, Color color) {
        altTextShownPy.set(true);
        textPy.set(text);
        txtScore.setFill(color);
    }
}