/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.model.GameLevel;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import static de.amr.games.pacman.lib.Globals.TS;
import static java.util.Objects.requireNonNull;

/**
 * Displays the score and high score.
 *
 * @author Armin Reichert
 */
public class Scores3D {

    private final Group root = new Group();
    private final Text txtScoreTitle;
    private final Text txtScore;
    private final Text txtHiscoreTitle;
    private final Text txtHiscore;
    private Color titleColor = Color.GHOSTWHITE;
    private Color scoreColor = Color.YELLOW;
    private Font font = Font.font("Courier", 12);
    private boolean pointsDisplayed = true;

    public Scores3D(Font font) {
        requireNonNull(font);

        this.font = font;
        txtScoreTitle = new Text("SCORE");
        txtScore = new Text();
        txtHiscoreTitle = new Text("HIGH SCORE");
        txtHiscore = new Text();
        GridPane grid = new GridPane();
        grid.setHgap(5 * TS);
        grid.add(txtScoreTitle, 0, 0);
        grid.add(txtScore, 0, 1);
        grid.add(txtHiscoreTitle, 1, 0);
        grid.add(txtHiscore, 1, 1);
        root.getChildren().add(grid);
    }

    public Node root() {
        return root;
    }

    public void setPosition(double x, double y, double z) {
        root.setTranslateX(x);
        root.setTranslateY(y);
        root.setTranslateZ(z);
    }

    public void setShowText(Color color, String text) {
        requireNonNull(color);

        txtScore.setFill(color);
        txtScore.setText(text);
        pointsDisplayed = false;
    }

    public void setShowPoints(boolean show) {
        this.pointsDisplayed = show;
    }

    public void update(GameLevel level) {
        requireNonNull(level);

        txtScoreTitle.setFill(titleColor);
        txtScoreTitle.setFont(font);
        if (pointsDisplayed) {
            txtScore.setFont(font);
            txtScore.setText(String.format("%7d L%d", level.game().score().points(), level.game().score().levelNumber()));
            txtScore.setFill(Color.YELLOW);
        }
        txtHiscoreTitle.setFill(titleColor);
        txtHiscoreTitle.setFont(font);
        txtHiscore.setFill(scoreColor);
        txtHiscore.setFont(font);
        txtHiscore
            .setText(String.format("%7d L%d", level.game().highScore().points(), level.game().highScore().levelNumber()));
    }
}