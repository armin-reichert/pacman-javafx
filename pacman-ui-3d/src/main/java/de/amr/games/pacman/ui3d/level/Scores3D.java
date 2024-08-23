/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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

    private final Text txtLeft;
    private final Text txtRight;

    public Scores3D(String leftTitle, String rightTitle) {
        var txtLeftTitle = new Text(leftTitle);
        txtLeftTitle.setFill(Color.GHOSTWHITE);
        txtLeftTitle.fontProperty().bind(fontPy);

        txtLeft = new Text();
        txtLeft.fontProperty().bind(fontPy);

        var txtRightTitle = new Text(rightTitle);
        txtRightTitle.setFill(Color.GHOSTWHITE);
        txtRightTitle.fontProperty().bind(fontPy);

        txtRight = new Text();
        txtRight.setFill(Color.YELLOW);
        txtRight.fontProperty().bind(fontPy);

        setHgap(5 * TS);
        add(txtLeftTitle,  0,0);
        add(txtLeft,       0,1);
        add(txtRightTitle,1,0);
        add(txtRight,     1,1);
    }

    public void showScore(int score, int levelNumber) {
        txtLeft.setFill(Color.YELLOW);
        txtLeft.setText(String.format("%7d L%d", score, levelNumber));
    }
    public void showHighScore(int highScore, int highScoreLevelNumber) {
        txtLeft.setFill(Color.YELLOW);
        txtRight.setText(String.format("%7d L%d", highScore, highScoreLevelNumber));
    }

    public void showTextAsScore(String text, Color color) {
        txtLeft.setText(text);
        txtLeft.setFill(color);
    }
}