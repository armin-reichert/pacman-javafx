/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.view;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.model.GameCheats;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.ui.Globals_GameUI;
import de.amr.pacmanfx.ui.game.Game;
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
import org.tinylog.Logger;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

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

    private final HBox rootPane = new HBox();
    private final FontAwesomeIcon iconMuted;
    private final FontAwesomeIcon icon3D;
    private final FontAwesomeIcon iconAutopilot;
    private final FontAwesomeIcon iconImmune;
    private final FontAwesomeIcon iconCheated;

    public StatusIconBox(Game game) {
        this(game, DEFAULT_CONFIG);
    }
    
    public StatusIconBox(Game game, Config config) {
        requireNonNull(game);
        requireNonNull(config);

        final TranslationManager translations = game.ui().translations();

        iconMuted = createIcon(config, FontAwesomeIcon.Symbol.DEAF, config.defaultIconColor(),
            translations.translate("status_icon.muted"));

        icon3D = createIcon(config, FontAwesomeIcon.Symbol.CUBES, config.defaultIconColor(),
            translations.translate("status_icon.3d"));

        iconAutopilot = createIcon(config, FontAwesomeIcon.Symbol.TAXI, config.defaultIconColor(),
            translations.translate("status_icon.autopilot"));

        iconImmune = createIcon(config, FontAwesomeIcon.Symbol.USER_SECRET, config.defaultIconColor(),
            translations.translate("status_icon.immune"));

        iconCheated = createIcon(config, FontAwesomeIcon.Symbol.FLAG, Color.RED,
            translations.translate("status_icon.cheated"));

        final int iconCount = (int) iconNodesInOrder().count();
        final int padding = config.iconPadding();
        final int spacing = config.iconSpacing();
        final int size = config.iconSize();

        rootPane.getChildren().setAll(iconNodesInOrder().toList());
        rootPane.setMaxHeight(size + 2 * padding);
        rootPane.setMaxWidth(size + (iconCount - 1) * spacing + 2 * padding);
        rootPane.setPadding(new Insets(padding));
        rootPane.setSpacing(spacing);

        // "autopilot", "cheated" and "immune" icon visibilities are dynamically bound to current game model's cheat object!
        iconMuted.visibleProperty().bind(Globals_GameUI.PROPERTY_MUTED);

        icon3D   .visibleProperty().bind(game.ui().globals3D().d3EnabledProperty);
    }

    public Pane rootPane() {
        return rootPane;
    }

    public Stream<Node> iconNodesInOrder() {
        return Stream.of(iconMuted, icon3D, iconAutopilot, iconImmune, iconCheated).map(FontAwesomeIcon::node);
    }

    public void bind(GameModel gameModel) {
        final GameCheats cheats = gameModel.cheats();

        iconAutopilot.visibleProperty().unbind();
        iconAutopilot.visibleProperty().bind(cheats.pacUsingAutopilotProperty());

        iconCheated.visibleProperty().unbind();
        iconCheated.visibleProperty().bind(cheats.cheatUsedProperty());

        iconImmune.visibleProperty().unbind();
        iconImmune.visibleProperty().bind(cheats.pacImmuneProperty());

        Logger.info("Icons autopilot, cheated and immune visibility bound to game model {}", gameModel);
    }

    @Override
    public void dispose() {
        rootPane.visibleProperty().unbind();
        iconNodesInOrder().forEach(iconNode -> {
            iconNode.visibleProperty().unbind();
            iconNode.visibleProperty().removeListener(this::rearrangeIcons);
        });
    }

    private FontAwesomeIcon createIcon(Config config, FontAwesomeIcon.Symbol symbol, Color color, String tooltipText) {
        final FontAwesomeIcon icon = new FontAwesomeIcon(symbol, config.iconSize());
        icon.fillProperty().set(color);
        icon.visibleProperty().addListener(this::rearrangeIcons);

        final Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setFont(config.tooltipFont);
        tooltip.setShowDelay(Duration.millis(250));
        Tooltip.install(icon.node(), tooltip);

        return icon;
    }

    // keep box compact, show visible items only
    private void rearrangeIcons(ObservableValue<? extends Boolean> property, boolean wasVisible, boolean isVisible) {
        rootPane.getChildren().setAll(iconNodesInOrder().filter(Node::isVisible).toList());
    }
}