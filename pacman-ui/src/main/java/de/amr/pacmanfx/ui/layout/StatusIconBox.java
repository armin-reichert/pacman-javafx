/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.layout;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.ui.GameUIConstants;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import de.amr.pacmanfx.uilib.rendering.ArcadePalette;
import de.amr.pacmanfx.uilib.widgets.FontAwesomeIcon;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
//import org.kordamp.ikonli.Ikon;
//import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
//import org.kordamp.ikonli.javafx.FontIcon;

import java.util.stream.Stream;

public class StatusIconBox implements Disposable {

    public record Config(
        Color defaultIconColor,
        int iconSize,
        int iconSpacing,
        int iconPadding,
        Font tooltipFont
    ) {}
    
    public static final Config DEFAULT_CONFIG = new Config(
        ArcadePalette.ARCADE_WHITE,
        24,
        5,
        10,
        Font.font("Sans", 16)
    );

    private HBox rootPane = new HBox();
    private final FontAwesomeIcon iconMuted;
    private final FontAwesomeIcon icon3D;
    private final FontAwesomeIcon iconAutopilot;
    private final FontAwesomeIcon iconImmune;
    private final FontAwesomeIcon iconCheated;

    public StatusIconBox(TranslationManager translator) {
        this(translator, DEFAULT_CONFIG);
    }
    
    public StatusIconBox(TranslationManager translator, Config config) {
        iconMuted = createIcon(config, FontAwesomeIcon.Symbol.DEAF, config.defaultIconColor(),
            translator.translate("status_icon.muted"));

        icon3D = createIcon(config, FontAwesomeIcon.Symbol.CUBES, config.defaultIconColor(),
            translator.translate("status_icon.3d"));

        iconAutopilot = createIcon(config, FontAwesomeIcon.Symbol.TAXI, config.defaultIconColor(),
            translator.translate("status_icon.autopilot"));

        iconImmune = createIcon(config, FontAwesomeIcon.Symbol.USER_SECRET, config.defaultIconColor(),
            translator.translate("status_icon.immune"));

        iconCheated = createIcon(config, FontAwesomeIcon.Symbol.FLAG, Color.RED,
            translator.translate("status_icon.cheated"));


        final int count = (int) iconsInOrder().count();
        final int padding = config.iconPadding();
        final int spacing = config.iconSpacing();
        final int size = config.iconSize();

        rootPane.getChildren().setAll(iconsInOrder().toList());
        rootPane.setMaxHeight(size + 2 * padding);
        rootPane.setMaxWidth(size + (count - 1) * spacing + 2 * padding);
        rootPane.setPadding(new Insets(padding));
        rootPane.setSpacing(spacing);

        // "autopilot", "cheated" and "immune" icon visibilities are bound to properties of current game model!
        iconMuted.visibleProperty().bind(GameUIConstants.PROPERTY_MUTED);
        icon3D   .visibleProperty().bind(GameUIConstants.PROPERTY_3D_ENABLED);
    }

    public Pane rootPane() {
        return rootPane;
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
        rootPane.visibleProperty().unbind();
        iconsInOrder().forEach(icon -> {
            icon.visibleProperty().unbind();
            icon.visibleProperty().removeListener(this::rearrangeIcons);
        });
    }

    private FontAwesomeIcon createIcon(Config config, FontAwesomeIcon.Symbol symbol, Color color, String tooltipText) {
        final FontAwesomeIcon icon = FontAwesomeIcon.of(symbol, config.iconSize(), color);
        icon.setVisible(false);
        icon.visibleProperty().addListener(this::rearrangeIcons);

        final Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setFont(config.tooltipFont);
        tooltip.setShowDelay(Duration.millis(250));
        Tooltip.install(icon, tooltip);

        return icon;
    }

    // keep box compact, show visible items only
    private void rearrangeIcons(ObservableValue<? extends Boolean> property, boolean wasVisible, boolean isVisible) {
        rootPane.getChildren().setAll(iconsInOrder().filter(Node::isVisible).toList());
    }
}