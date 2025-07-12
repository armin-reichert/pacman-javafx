/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.uilib.Ufx;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import static de.amr.pacmanfx.ui.GameUIContext.theAssets;

public class InfoBoxReadmeFirst extends InfoBox {

    @Override
    public void init() {
        super.init();

        var readmeText = new Text();
        readmeText.setText(theAssets().text("infobox.readme.content"));
        readmeText.setFont(Font.font("Serif", 16));
        readmeText.setFill(Color.WHITE);

        var pane = new BorderPane();
        pane.setBorder(Ufx.border(Color.TRANSPARENT, 5));

        var buttonPane = new HBox();
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.setPadding(new Insets(10, 0, 0, 0));

        var btnGotIt = new Button(theAssets().text("infobox.readme.got_it"));
        buttonPane.getChildren().add(btnGotIt);
        btnGotIt.setOnAction(e -> {
            dashboard.removeInfoBox(DashboardID.README);
        });

        pane.setCenter(readmeText);
        pane.setBottom(buttonPane);
        addRow(pane);
    }
}