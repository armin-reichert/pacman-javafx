/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.dashboard;

import de.amr.games.pacman.ui.GameContext;
import de.amr.games.pacman.uilib.Ufx;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


public class InfoBoxReadmeFirst extends InfoBox {

    @Override
    public void init(GameContext context) {
        super.init(context);
        Text readmeText = new Text();
        readmeText.setText(context.locText("infobox.readme.content"));
        readmeText.setFont(Font.font("Serif", 16));
        readmeText.setFill(Color.WHITE);
        Pane pane = new BorderPane(readmeText);
        pane.setBorder(Ufx.border(Color.TRANSPARENT, 5));
        addRow(pane);
    }
}
