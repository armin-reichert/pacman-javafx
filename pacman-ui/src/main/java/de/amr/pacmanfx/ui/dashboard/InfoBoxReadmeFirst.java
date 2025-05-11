/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.uilib.Ufx;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import static de.amr.pacmanfx.ui.Globals.THE_ASSETS;

public class InfoBoxReadmeFirst extends InfoBox {

    @Override
    public void init() {
        super.init();
        Text readmeText = new Text();
        readmeText.setText(THE_ASSETS.text("infobox.readme.content"));
        readmeText.setFont(Font.font("Serif", 16));
        readmeText.setFill(Color.WHITE);
        Pane pane = new BorderPane(readmeText);
        pane.setBorder(Ufx.border(Color.TRANSPARENT, 5));
        addRow(pane);
    }
}