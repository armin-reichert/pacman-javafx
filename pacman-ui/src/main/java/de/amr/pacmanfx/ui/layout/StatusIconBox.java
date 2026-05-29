/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.layout;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.ui.GameUIConstants;
import de.amr.pacmanfx.uilib.rendering.ArcadePalette;
import de.amr.pacmanfx.uilib.widgets.FontAwesomeIcon;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
//import org.kordamp.ikonli.Ikon;
//import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
//import org.kordamp.ikonli.javafx.FontIcon;

import java.util.stream.Stream;

public class StatusIconBox extends HBox implements Disposable {

    private static final Color STATUS_ICON_COLOR = ArcadePalette.ARCADE_WHITE;

    private static final int ICON_SIZE = 24;
    private static final int ICON_SPACING = 5;
    private static final int PADDING = 10;

    private static final Font TOOLTIP_FONT = Font.font("Sans", 16);

    private final FontAwesomeIcon iconMuted;
    private final FontAwesomeIcon icon3D;
    private final FontAwesomeIcon iconAutopilot;
    private final FontAwesomeIcon iconImmune;
    private final FontAwesomeIcon iconCheated;

    public StatusIconBox() {
        iconMuted     = createIcon(FontAwesomeIcon.Symbol.DEAF, STATUS_ICON_COLOR, "Muted");
        icon3D        = createIcon(FontAwesomeIcon.Symbol.CUBES, STATUS_ICON_COLOR, "3D Mode");
        iconAutopilot = createIcon(FontAwesomeIcon.Symbol.TAXI, STATUS_ICON_COLOR, "Autopilot");
        iconImmune    = createIcon(FontAwesomeIcon.Symbol.USER_SECRET, STATUS_ICON_COLOR, "Immunity");
        iconCheated   = createIcon(FontAwesomeIcon.Symbol.FLAG, Color.RED, "Cheater");

        getChildren().setAll(iconsInOrder().toList());

        long count = iconsInOrder().count();
        setMaxHeight(ICON_SIZE + 2 * PADDING);
        setMaxWidth((ICON_SIZE + ICON_SPACING) * count - ICON_SPACING + 2 * PADDING);

        setPadding(new Insets(PADDING));
        setSpacing(ICON_SPACING);

        //setBorder(Border.stroke(Color.RED));

        // "autopilot", "cheated" and "immune" icon visibilities are bound to properties of current game model!
        iconMuted.visibleProperty().bind(GameUIConstants.PROPERTY_MUTED);
        icon3D   .visibleProperty().bind(GameUIConstants.PROPERTY_3D_ENABLED);
    }

    public FontAwesomeIcon iconAutopilot() {
        return iconAutopilot;
    }

    public FontAwesomeIcon iconCheated() {
        return iconCheated;
    }

    public FontAwesomeIcon iconImmune() {
        return iconImmune;
    }

    public FontAwesomeIcon iconMuted() {
        return iconMuted;
    }

    public FontAwesomeIcon icon3D() {
        return icon3D;
    }

    public Stream<FontAwesomeIcon> iconsInOrder() {
        return Stream.of(iconMuted, icon3D, iconAutopilot, iconImmune, iconCheated);
    }

    @Override
    public void dispose() {
        visibleProperty().unbind();
        iconsInOrder().forEach(icon -> {
            icon.visibleProperty().unbind();
            icon.visibleProperty().removeListener(this::rearrangeIcons);
        });
    }

    private FontAwesomeIcon createIcon(FontAwesomeIcon.Symbol symbol, Color color, String tooltipText) {
        final FontAwesomeIcon icon = FontAwesomeIcon.of(symbol, ICON_SIZE, color);
        icon.setVisible(false);
        icon.visibleProperty().addListener(this::rearrangeIcons);

        final Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setFont(TOOLTIP_FONT);
        tooltip.setShowDelay(Duration.millis(250));
        Tooltip.install(icon, tooltip);

        return icon;
    }

    // keep box compact, show visible items only
    private void rearrangeIcons(ObservableValue<? extends Boolean> property, boolean wasVisible, boolean isVisible) {
        getChildren().setAll(iconsInOrder().filter(Node::isVisible).toList());
    }
}