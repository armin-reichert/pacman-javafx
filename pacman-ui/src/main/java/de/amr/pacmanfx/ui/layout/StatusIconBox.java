/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.ui.ArcadePalette;
import de.amr.pacmanfx.ui.GameUI;
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

    private static final int ICON_SIZE = 24;
    private static final int ICON_SPACING = 5;
    private static final int PADDING = 10;

    private static final Font TOOLTIP_FONT = Font.font("Sans", 16);

    private final FontIcon iconMuted;
    private final FontIcon icon3D;
    private final FontIcon iconAutopilot;
    private final FontIcon iconImmune;
    private final FontIcon iconCheated;

    public StatusIconBox() {
        iconMuted     = createIcon(FontAwesomeSolid.DEAF, STATUS_ICON_COLOR, "Muted");
        icon3D        = createIcon(FontAwesomeSolid.CUBES, STATUS_ICON_COLOR, "3D Mode");
        iconAutopilot = createIcon(FontAwesomeSolid.TAXI, STATUS_ICON_COLOR, "Autopilot");
        iconImmune    = createIcon(FontAwesomeSolid.USER_SECRET, STATUS_ICON_COLOR, "Immunity");
        iconCheated   = createIcon(FontAwesomeSolid.FLAG, Color.RED, "Cheater");

        getChildren().setAll(icons().toList());

        long count = icons().count();
        setMaxHeight(ICON_SIZE + 2 * PADDING);
        setMaxWidth((ICON_SIZE + ICON_SPACING) * count - ICON_SPACING + 2 * PADDING);

        setPadding(new Insets(PADDING));
        setSpacing(ICON_SPACING);

        iconMuted.visibleProperty().bind(GameUI.PROPERTY_MUTED);
        icon3D   .visibleProperty().bind(GameUI.PROPERTY_3D_ENABLED);
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
        icons().forEach(icon -> {
            icon.visibleProperty().unbind();
            icon.visibleProperty().removeListener(this::handleVisibilityChange);
        });
    }

    private FontIcon createIcon(Ikon iconType, Color color, String tooltipText) {
        FontIcon icon = FontIcon.of(iconType, ICON_SIZE, color);
        icon.setVisible(false);
        icon.visibleProperty().addListener(this::handleVisibilityChange);
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setFont(TOOLTIP_FONT);
        tooltip.setShowDelay(Duration.millis(250));
        Tooltip.install(icon, tooltip);
        return icon;
    }

    private void handleVisibilityChange(ObservableValue<? extends Boolean> property, boolean wasVisible, boolean isVisible) {
        // keep box compact, show visible items only
        getChildren().setAll(icons().filter(Node::isVisible).toList());
    }

    public Stream<FontIcon> icons() {
        return Stream.of(iconMuted, icon3D, iconAutopilot, iconImmune, iconCheated);
    }
}