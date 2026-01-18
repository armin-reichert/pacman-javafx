/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class DashboardSectionReadmeFirst extends DashboardSection {

    public DashboardSectionReadmeFirst(Dashboard dashboard) {
        super(dashboard);
    }

    @Override
    public void init(GameUI ui) {
        final var readmeText = new Text();
        readmeText.setText(ui.translated("infobox.readme.content"));
        readmeText.setFont(Font.font("Serif", 16));
        readmeText.setFill(Color.WHITE);

        final var pane = new BorderPane();
        pane.setBorder(Ufx.border(Color.TRANSPARENT, 5));

        final var buttonPane = new HBox();
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.setPadding(new Insets(10, 0, 0, 0));

        final var btnGotIt = new Button(ui.translated("infobox.readme.got_it"));
        buttonPane.getChildren().add(btnGotIt);
        btnGotIt.setOnAction(e -> dashboard.removeInfoBox(CommonDashboardID.README));

        pane.setCenter(readmeText);
        pane.setBottom(buttonPane);
        addRow(pane);
    }
}