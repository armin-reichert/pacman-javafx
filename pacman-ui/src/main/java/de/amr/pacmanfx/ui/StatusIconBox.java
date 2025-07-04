/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

import static de.amr.pacmanfx.ui.PacManGames.theSound;
import static de.amr.pacmanfx.ui.PacManGames_UI.*;

public class StatusIconBox extends HBox {

    public StatusIconBox(PacManGames_UI ui) {
        var iconMuted = FontIcon.of(FontAwesomeSolid.DEAF, STATUS_ICON_SIZE, STATUS_ICON_COLOR);
        iconMuted.visibleProperty().bind(theSound().mutedProperty());

        var icon3D = FontIcon.of(FontAwesomeSolid.CUBES, STATUS_ICON_SIZE, STATUS_ICON_COLOR);
        icon3D.visibleProperty().bind(PY_3D_ENABLED);

        var iconAutopilot = FontIcon.of(FontAwesomeSolid.TAXI, STATUS_ICON_SIZE, STATUS_ICON_COLOR);
        iconAutopilot.visibleProperty().bind(PY_USING_AUTOPILOT);

        var iconImmune = FontIcon.of(FontAwesomeSolid.USER_SECRET, STATUS_ICON_SIZE, STATUS_ICON_COLOR);
        iconImmune.visibleProperty().bind(PY_IMMUNITY);

        final List<FontIcon> icons = List.of(iconMuted, icon3D, iconAutopilot, iconImmune);
        getChildren().addAll(icons);
        setMaxHeight(STATUS_ICON_SIZE);
        setMaxWidth(STATUS_ICON_SIZE * 4);
        setPadding(new Insets(STATUS_ICON_PADDING));
        setSpacing(STATUS_ICON_SPACING);
        visibleProperty().bind(Bindings.createBooleanBinding
            (() -> (ui.currentView() == ui.startPagesView() || ui.currentView() == ui.gameView()),
                ui.currentViewProperty()));
        // keep box compact, show visible items only
        ChangeListener<? super Boolean> iconVisibilityChangeHandler = (py, ov, nv) ->
            getChildren().setAll(icons.stream().filter(Node::isVisible).toList());
        icons.forEach(icon -> icon.visibleProperty().addListener(iconVisibilityChangeHandler));
    }
}
