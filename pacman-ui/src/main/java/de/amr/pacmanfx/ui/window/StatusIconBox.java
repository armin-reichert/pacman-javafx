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
import de.amr.pacmanfx.uilib.controls.FontAwesomeIcon;
import de.amr.pacmanfx.uilib.controls.FontAwesomeSymbol;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.stream.Stream;

public class StatusIconBox implements Disposable {

    private final StackPane rootPane = new StackPane();
    private final HBox hbox = new HBox();

    private final FontAwesomeIcon iconMuted;
    private final FontAwesomeIcon icon3D;
    private final FontAwesomeIcon iconAutopilot;
    private final FontAwesomeIcon iconImmune;
    private final FontAwesomeIcon iconCheated;

    public StatusIconBox() {
        iconMuted = createIcon(FontAwesomeSymbol.DEAF);
        icon3D = createIcon(FontAwesomeSymbol.CUBES);
        iconAutopilot = createIcon(FontAwesomeSymbol.TAXI);
        iconImmune = createIcon(FontAwesomeSymbol.USER_SECRET);

        iconCheated = createIcon(FontAwesomeSymbol.FLAG);
        iconCheated.setId("icon-cheated");

        rootPane.setId("status-icon-box");
        hbox.setId("status-icon-layout");

        hbox.getChildren().setAll(iconsInOrder().toList());
        rootPane.getChildren().add(hbox);

        // Without this the button fill the complete area
        rootPane.setPrefSize(StackPane.USE_COMPUTED_SIZE, StackPane.USE_COMPUTED_SIZE);
        rootPane.setMaxSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);
    }

    public Pane rootPane() {
        return rootPane;
    }

    public Stream<FontAwesomeIcon> iconsInOrder() {
        return Stream.of(iconMuted, icon3D, iconAutopilot, iconImmune, iconCheated);
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
            final GameModel gameModel = game.gameVariantRuntime(variantName).gameModel();
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

        iconMuted.visibleProperty().bind(game.ui().viewModel().mutedProperty);
        icon3D.visibleProperty().bind(game.ui().viewModel().common3D.view3DEnabledProperty);
    }

    @Override
    public void dispose() {
        rootPane.visibleProperty().unbind();
        iconsInOrder().forEach(iconNode -> {
            iconNode.visibleProperty().unbind();
            iconNode.visibleProperty().removeListener(this::rearrangeIcons);
        });
    }

    private FontAwesomeIcon createIcon(FontAwesomeSymbol symbol) {
        final FontAwesomeIcon icon = new FontAwesomeIcon(symbol);
        icon.fillProperty().set(Color.WHITE);
        icon.visibleProperty().addListener(this::rearrangeIcons);
        return icon;
    }

    private void setTooltip(FontAwesomeIcon icon, String text) {
        final Tooltip tooltip = new Tooltip(text);
        tooltip.setFont(Font.font("Sans", 16));
        tooltip.setShowDelay(Duration.millis(250));
        Tooltip.install(icon, tooltip);
    }

    // keep box compact, show visible items only
    private void rearrangeIcons(ObservableValue<? extends Boolean> property, boolean wasVisible, boolean isVisible) {
        hbox.getChildren().setAll(iconsInOrder().filter(Node::isVisible).toList());
    }
}