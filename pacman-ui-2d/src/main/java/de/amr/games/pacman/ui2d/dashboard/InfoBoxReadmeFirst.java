/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.util.Ufx;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class InfoBoxReadmeFirst extends InfoBox {

    static final String[] LINES = {
        "While this dashboard is open, steer Pac-Man",
        "by pressing Ctrl+[steering key].",
        "",
        "This dashboard might also interfere with",
        "other keys. If you encounter any such issue,",
        "close it with F1 or Alt+B.",
        "",
        "Relax (and do it)!"
    };

    @Override
    public void init(GameContext context) {
        super.init(context);
        Text readmeText = new Text();
        readmeText.setText(String.join("\n", LINES));
        readmeText.setFont(Font.font("Serif", 16));
        readmeText.setFill(Color.WHITE);
        Pane pane = new BorderPane(readmeText);
        pane.setBorder(Ufx.border(Color.TRANSPARENT, 5));
        addRow(pane);
    }
}
