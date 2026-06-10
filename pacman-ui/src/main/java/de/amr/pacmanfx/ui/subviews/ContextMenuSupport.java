/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.subviews;

import de.amr.pacmanfx.ui.game.GameGlobals;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.beans.property.BooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.text.Text;

public final class ContextMenuSupport {

    private ContextMenuSupport() {}
    
    public static <M extends MenuItem> M add(ContextMenu menu, M item) {
        if (item.getOnAction() != null) {
            item.setOnAction(wrapActionHandler(menu, item.getOnAction()));
        }
        menu.getItems().add(item);
        return item;
    }

    public static MenuItem addTitleItem(ContextMenu menu, String itemText) {
        final var text = new Text(itemText);
        text.setFont(GameGlobals.CONTEXT_MENU_DEFAULT_TITLE_FONT);
        text.setFill(GameGlobals.CONTEXT_MENU_DEFAULT_TITLE_COLOR);
        text.getStyleClass().add("custom-menu-title");
        final var item = new CustomMenuItem(text, false);
        menu.getItems().add(item);
        return item;
    }

    public static MenuItem addLocalizedActionItem(ContextMenu menu, TranslationManager translator, Runnable action, String globalAssetsKey, Object... args) {
        var actionItem = new MenuItem(translator.translate(globalAssetsKey, args));
        actionItem.setOnAction(_ -> action.run());
        return add(menu, actionItem);
    }

    public static MenuItem addLocalizedActionItem(ContextMenu menu, Game game, TranslationManager translator, GameAction action, String globalAssetsKey, Object... args) {
        var actionItem = new MenuItem(translator.translate(globalAssetsKey, args));
        actionItem.setOnAction(_ -> action.execute(game));
        return add(menu, actionItem);
    }

    public static MenuItem addLocalizedTitleItem(ContextMenu menu, TranslationManager translator, String globalAssetsKey, Object... args) {
        return addTitleItem(menu, translator.translate(globalAssetsKey, args));
    }

    public static CheckMenuItem addLocalizedCheckBox(ContextMenu menu, TranslationManager translator, BooleanProperty selectionProperty, String globalAssetsKey, Object... args) {
        var checkMenuItem = new CheckMenuItem(translator.translate(globalAssetsKey, args));
        checkMenuItem.selectedProperty().bindBidirectional(selectionProperty);
        return add(menu, checkMenuItem);
    }

    public static RadioMenuItem addLocalizedRadioButton(ContextMenu menu, TranslationManager translator, String globalAssetsKey, Object... args) {
        return add(menu, new RadioMenuItem(translator.translate(globalAssetsKey, args)));
    }

    public static void addSeparator(ContextMenu menu) {
        menu.getItems().add(new SeparatorMenuItem());
    }

    public static EventHandler<ActionEvent> wrapActionHandler(ContextMenu menu, EventHandler<ActionEvent> actionHandler) {
        return actionEvent -> {
            actionHandler.handle(actionEvent);
            menu.hide();
        };
    }
}