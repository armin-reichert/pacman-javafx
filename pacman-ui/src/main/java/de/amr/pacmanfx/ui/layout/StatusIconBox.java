/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.ui.api.ArcadePalette;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.stream.Stream;

public class StatusIconBox extends HBox implements Disposable {

    private static final Color STATUS_ICON_COLOR = ArcadePalette.ARCADE_WHITE;
    private static final int STATUS_ICON_PADDING = 10;
    private static final int STATUS_ICON_SIZE    = 24;
    private static final int STATUS_ICON_SPACING = 5;

    private final FontIcon iconMuted;
    private final FontIcon icon3D;
    private final FontIcon iconAutopilot;
    private final FontIcon iconImmune;

    public StatusIconBox() {
        iconMuted = FontIcon.of(FontAwesomeSolid.DEAF, STATUS_ICON_SIZE, STATUS_ICON_COLOR);
        iconMuted.visibleProperty().addListener(this::handleIconVisibilityChange);

        icon3D = FontIcon.of(FontAwesomeSolid.CUBES, STATUS_ICON_SIZE, STATUS_ICON_COLOR);
        icon3D.visibleProperty().addListener(this::handleIconVisibilityChange);

        iconAutopilot = FontIcon.of(FontAwesomeSolid.TAXI, STATUS_ICON_SIZE, STATUS_ICON_COLOR);
        iconAutopilot.visibleProperty().addListener(this::handleIconVisibilityChange);

        iconImmune = FontIcon.of(FontAwesomeSolid.USER_SECRET, STATUS_ICON_SIZE, STATUS_ICON_COLOR);
        iconImmune.visibleProperty().addListener(this::handleIconVisibilityChange);

        getChildren().addAll(iconMuted, icon3D, iconAutopilot, iconImmune);

        setMaxHeight(STATUS_ICON_SIZE);
        setMaxWidth(STATUS_ICON_SIZE * 4);
        setPadding(new Insets(STATUS_ICON_PADDING));
        setSpacing(STATUS_ICON_SPACING);
    }

    public FontIcon iconAutopilot() {
        return iconAutopilot;
    }

    public FontIcon iconImmune() {
        return iconImmune;
    }

    public FontIcon iconMuted() {
        return iconMuted;
    }

    public FontIcon icon3D() {
        return icon3D;
    }

    @Override
    public void dispose() {
        visibleProperty().unbind();

        iconMuted.visibleProperty().unbind();
        iconMuted.visibleProperty().removeListener(this::handleIconVisibilityChange);

        icon3D.visibleProperty().unbind();
        icon3D.visibleProperty().removeListener(this::handleIconVisibilityChange);

        iconAutopilot.visibleProperty().unbind();
        iconAutopilot.visibleProperty().removeListener(this::handleIconVisibilityChange);

        iconImmune.visibleProperty().unbind();
        iconImmune.visibleProperty().removeListener(this::handleIconVisibilityChange);
    }

    private void handleIconVisibilityChange(ObservableValue<? extends Boolean> property, boolean oldVisible, boolean newVisible) {
        // keep box compact, show visible items only
        getChildren().setAll(Stream.of(iconMuted, icon3D, iconAutopilot, iconImmune).filter(Node::isVisible).toList());
    }
}
