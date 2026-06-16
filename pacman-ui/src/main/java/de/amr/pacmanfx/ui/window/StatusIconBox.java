/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.window;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.model.GameCheats;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.GameViewManager;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import de.amr.pacmanfx.uilib.rendering.ArcadePalette;
import de.amr.pacmanfx.uilib.widgets.FontAwesomeIcon;
import javafx.beans.value.ChangeListener;
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

    private final Config config;

    private final HBox rootPane = new HBox();
    private final FontAwesomeIcon iconMuted;
    private final FontAwesomeIcon icon3D;
    private final FontAwesomeIcon iconAutopilot;
    private final FontAwesomeIcon iconImmune;
    private final FontAwesomeIcon iconCheated;

    public StatusIconBox() {
        this(DEFAULT_CONFIG);
    }
    
    public StatusIconBox(Config config) {
        this.config = requireNonNull(config);

        iconMuted = createIcon(FontAwesomeIcon.Symbol.DEAF, config.defaultIconColor());
        icon3D = createIcon(FontAwesomeIcon.Symbol.CUBES, config.defaultIconColor());
        iconAutopilot = createIcon(FontAwesomeIcon.Symbol.TAXI, config.defaultIconColor());
        iconImmune = createIcon(FontAwesomeIcon.Symbol.USER_SECRET, config.defaultIconColor());
        iconCheated = createIcon(FontAwesomeIcon.Symbol.FLAG, Color.RED);

        final int iconCount = (int) iconNodesInOrder().count();
        final int padding = config.iconPadding();
        final int spacing = config.iconSpacing();
        final int size = config.iconSize();

        rootPane.getChildren().setAll(iconNodesInOrder().toList());
        rootPane.setMaxHeight(size + 2 * padding);
        rootPane.setMaxWidth(size + (iconCount - 1) * spacing + 2 * padding);
        rootPane.setPadding(new Insets(padding));
        rootPane.setSpacing(spacing);
    }

    public Pane rootPane() {
        return rootPane;
    }

    public Stream<Node> iconNodesInOrder() {
        return Stream.of(iconMuted, icon3D, iconAutopilot, iconImmune, iconCheated).map(FontAwesomeIcon::node);
    }

    public void connect(Game game) {
        final TranslationManager translations = game.ui().translations();

        setTooltip(iconMuted, translations.translate("status_icon.muted"));
        setTooltip(icon3D, translations.translate("status_icon.3d"));
        setTooltip(iconAutopilot, translations.translate("status_icon.autopilot"));
        setTooltip(iconImmune, translations.translate("status_icon.immune"));
        setTooltip(iconCheated, translations.translate("status_icon.cheated"));

        final GameViewManager views = game.ui().views();
        // Hide status icon box in editor view
        rootPane().visibleProperty().bind(
                views.currentViewIDProperty().isEqualTo(GameViewID.GAMEPLAY)
            .or(views.currentViewIDProperty().isEqualTo(GameViewID.START_PAGES))
        );

        // Visibility of "autopilot", "cheated" and "immune" is bound to *current game model*'s cheat object!
        final ChangeListener<String> variantChangeHandler = (_, _, variantName) -> {
            final GameModel gameModel = game.gameVariant(variantName).gameModel();
            final GameCheats cheats = gameModel.cheats();

            iconAutopilot.visibleProperty().unbind();
            iconAutopilot.visibleProperty().bind(cheats.pacUsingAutopilotProperty());

            iconCheated.visibleProperty().unbind();
            iconCheated.visibleProperty().bind(cheats.cheatUsedProperty());

            iconImmune.visibleProperty().unbind();
            iconImmune.visibleProperty().bind(cheats.pacImmuneProperty());

            Logger.info("Icons autopilot, cheated and immune visibility bound to game model {}", gameModel);
        };

        game.gameVariantNameProperty().addListener(variantChangeHandler);

        iconMuted.visibleProperty().bind(game.ui().settings().mutedProperty());
        icon3D.visibleProperty().bind(game.ui().settings().d3().view3DEnabledProperty());
    }

    @Override
    public void dispose() {
        rootPane.visibleProperty().unbind();
        iconNodesInOrder().forEach(iconNode -> {
            iconNode.visibleProperty().unbind();
            iconNode.visibleProperty().removeListener(this::rearrangeIcons);
        });
    }

    private FontAwesomeIcon createIcon(FontAwesomeIcon.Symbol symbol, Color color) {
        final FontAwesomeIcon icon = new FontAwesomeIcon(symbol, config.iconSize());
        icon.fillProperty().set(color);
        icon.visibleProperty().addListener(this::rearrangeIcons);
        return icon;
    }

    private void setTooltip(FontAwesomeIcon icon, String text) {
        final Tooltip tooltip = new Tooltip(text);
        tooltip.setFont(config.tooltipFont);
        tooltip.setShowDelay(Duration.millis(250));
        Tooltip.install(icon.node(), tooltip);
    }

    // keep box compact, show visible items only
    private void rearrangeIcons(ObservableValue<? extends Boolean> property, boolean wasVisible, boolean isVisible) {
        rootPane.getChildren().setAll(iconNodesInOrder().filter(Node::isVisible).toList());
    }
}