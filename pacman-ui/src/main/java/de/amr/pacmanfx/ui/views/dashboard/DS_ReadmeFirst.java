/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class DS_ReadmeFirst extends DashboardSection {

    private static final Font TEXT_FONT = Font.font("Sans", 14);

    public DS_ReadmeFirst(Dashboard dashboard) {
        super(dashboard);
    }

    @Override
    public void connect(Game game) {
        final var readmeText = new Text();
        readmeText.setText(game.ui().translations().translate("infobox.readme.content"));
        readmeText.setFont(TEXT_FONT);
        readmeText.setFill(Color.WHITE);

        final var pane = new BorderPane();
        pane.setBorder(Ufx.border(Color.TRANSPARENT, 5));

        final var buttonPane = new HBox();
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.setPadding(new Insets(10, 0, 0, 0));

        final var btnGotIt = new Button(game.ui().translations().translate("infobox.readme.got_it"));
        buttonPane.getChildren().add(btnGotIt);
        btnGotIt.setOnAction(_ -> dashboard.removeSection(DashboardID.README));

        pane.setCenter(readmeText);
        pane.setBottom(buttonPane);
        addRow(pane);
    }
}