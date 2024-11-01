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
            "Welcome to the Pleasuredome!",
            "",
            "While this dashboard is open, ",
            "Pac-Man is steered by pressing",
            "    CTRL + [steering key]",
            "instead of the key alone.",
            "",
            "This dashboard may also",
            "interfere with other keys.",
            "If you encounter such an issue,",
            "just shut me down! (F1, Alt+B)",
            "",
            "Relax (and do it)!"
    };

    @Override
    public void init(GameContext context) {
        super.init(context);
        Text readmeText = new Text();
        readmeText.setText(String.join("\n", LINES));
        readmeText.setFont(Font.font("Serif", 20));
        readmeText.setFill(Color.WHITE);
        Pane pane = new BorderPane(readmeText);
        //pane.setBackground(Ufx.coloredBackground(Color.WHITE));
        pane.setBorder(Ufx.border(Color.TRANSPARENT, 10));
        addRow(pane);
    }
}
