/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D;

import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import static de.amr.pacmanfx.Globals.TS;

/**
 * Displays score and high score in 3D play scene.
 */
public class Scores3D extends GridPane {

    private final Text textDisplayLeft;
    private final Text textDisplayRight;

    public Scores3D(String titleTextLeft, String titleTextRight, Font font) {
        var titleDisplayLeft = new Text(titleTextLeft);
        titleDisplayLeft.setFill(Color.GHOSTWHITE);
        titleDisplayLeft.setFont(font);

        textDisplayLeft = new Text();
        textDisplayLeft.setFont(font);

        var titleDisplayRight = new Text(titleTextRight);
        titleDisplayRight.setFill(Color.GHOSTWHITE);
        titleDisplayRight.setFont(font);

        textDisplayRight = new Text();
        textDisplayRight.setFill(Color.YELLOW);
        textDisplayRight.setFont(font);

        setHgap(5 * TS);
        add(titleDisplayLeft, 0,0);
        add(textDisplayLeft, 0,1);
        add(titleDisplayRight,1,0);
        add(textDisplayRight, 1,1);
    }

    public void showScore(int score, int levelNumber) {
        textDisplayLeft.setFill(Color.YELLOW);
        textDisplayLeft.setText(String.format("%7d L%d", score, levelNumber));
    }

    public void showHighScore(int highScore, int highScoreLevelNumber) {
        textDisplayRight.setFill(Color.YELLOW);
        textDisplayRight.setText(String.format("%7d L%d", highScore, highScoreLevelNumber));
    }

    public void showTextForScore(String text, Color color) {
        textDisplayLeft.setFill(color);
        textDisplayLeft.setText(text);
    }
}