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
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.stream.Stream;

public class StatusIconBox extends HBox implements Disposable {

    private static final Color STATUS_ICON_COLOR = ArcadePalette.ARCADE_WHITE;
    private static final int STATUS_ICON_PADDING = 10;
    private static final int STATUS_ICON_SIZE    = 24;
    private static final int STATUS_ICON_SPACING = 5;

    private static final Font TOOLTIP_FONT = Font.font("Sans", 16);

    private final FontIcon iconMuted;
    private final FontIcon icon3D;
    private final FontIcon iconAutopilot;
    private final FontIcon iconImmune;
    private final FontIcon iconCheated;

    public StatusIconBox() {
        iconMuted     = createIcon(FontAwesomeSolid.DEAF, "Muted");
        icon3D        = createIcon(FontAwesomeSolid.CUBES, "3D Mode");
        iconAutopilot = createIcon(FontAwesomeSolid.TAXI, "Autopilot");
        iconImmune    = createIcon(FontAwesomeSolid.USER_SECRET, "Immunity");
        iconCheated   = createIcon(FontAwesomeSolid.FLAG, "Cheater");

        getChildren().setAll(iconMuted, icon3D, iconAutopilot, iconImmune, iconCheated);

        setMaxHeight(STATUS_ICON_SIZE);
        setMaxWidth(STATUS_ICON_SIZE * 4);
        setPadding(new Insets(STATUS_ICON_PADDING));
        setSpacing(STATUS_ICON_SPACING);
    }

    private FontIcon createIcon(Ikon iconType, String tooltipText) {
        FontIcon icon = FontIcon.of(iconType, STATUS_ICON_SIZE, STATUS_ICON_COLOR);
        icon.setVisible(false);
        icon.visibleProperty().addListener(this::handleIconVisibilityChange);
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setFont(TOOLTIP_FONT);
        tooltip.setShowDelay(Duration.millis(250));
        Tooltip.install(icon, tooltip);
        return icon;
    }

    public FontIcon iconAutopilot() {
        return iconAutopilot;
    }

    public FontIcon iconCheated() {
        return iconCheated;
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

        iconCheated.visibleProperty().unbind();
        iconCheated.visibleProperty().removeListener(this::handleIconVisibilityChange);

        iconImmune.visibleProperty().unbind();
        iconImmune.visibleProperty().removeListener(this::handleIconVisibilityChange);
    }

    private void handleIconVisibilityChange(ObservableValue<? extends Boolean> property, boolean oldVisible, boolean newVisible) {
        // keep box compact, show visible items only
        getChildren().setAll(Stream.of(iconMuted, icon3D, iconAutopilot, iconImmune, iconCheated).filter(Node::isVisible).toList());
    }
}
