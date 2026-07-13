/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.pacmanfx.ui.action.core.GameActionContext;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class DS_ReadmeFirst extends GameDashboardSection {

    private Runnable removeFromDashboardAction;

    public DS_ReadmeFirst() {
        super(DashboardID.README);
        setId("ds-readme-first");
    }

    public void setRemoveFromDashboardAction(Runnable action) {
        this.removeFromDashboardAction = action;
    }

    @Override
    public void setGameActionContext(GameActionContext actionContext) {
        final var readmeText = new Text();
        readmeText.setText(actionContext.ui().translations().translate("infobox.readme.content"));

        final var pane = new BorderPane();
        pane.setBorder(Ufx.border(Color.TRANSPARENT, 5));

        final var buttonPane = new HBox();
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.setPadding(new Insets(10, 0, 0, 0));

        final var btnGotIt = new Button(actionContext.ui().translations().translate("infobox.readme.got_it"));
        buttonPane.getChildren().add(btnGotIt);
        btnGotIt.setOnAction(_ -> {
            if (removeFromDashboardAction != null) removeFromDashboardAction.run();
        });

        pane.setCenter(readmeText);
        pane.setBottom(buttonPane);
        addRow(pane);
    }
}