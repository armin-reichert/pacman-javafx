/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.subviews;

import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Constants;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.beans.property.BooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.text.Text;

public interface ContextMenuSupport {

    static <M extends MenuItem> M add(ContextMenu menu, M item) {
        if (item.getOnAction() != null) {
            item.setOnAction(wrapActionHandler(menu, item.getOnAction()));
        }
        menu.getItems().add(item);
        return item;
    }

    static MenuItem addTitleItem(ContextMenu menu, String itemText) {
        final var text = new Text(itemText);
        text.setFont(GameUI_Constants.CONTEXT_MENU_DEFAULT_TITLE_FONT);
        text.setFill(GameUI_Constants.CONTEXT_MENU_DEFAULT_TITLE_COLOR);
        text.getStyleClass().add("custom-menu-title");
        final var item = new CustomMenuItem(text, false);
        menu.getItems().add(item);
        return item;
    }

    static MenuItem addLocalizedActionItem(ContextMenu menu, TranslationManager translator, Runnable action, String globalAssetsKey, Object... args) {
        var actionItem = new MenuItem(translator.translate(globalAssetsKey, args));
        actionItem.setOnAction(_ -> action.run());
        return add(menu, actionItem);
    }

    static MenuItem addLocalizedActionItem(ContextMenu menu, GameUI ui, TranslationManager translator, GameAction action, String globalAssetsKey, Object... args) {
        var actionItem = new MenuItem(translator.translate(globalAssetsKey, args));
        actionItem.setOnAction(_ -> action.executeIfEnabled(ui));
        return add(menu, actionItem);
    }

    static MenuItem addLocalizedTitleItem(ContextMenu menu, TranslationManager translator, String globalAssetsKey, Object... args) {
        return addTitleItem(menu, translator.translate(globalAssetsKey, args));
    }

    static CheckMenuItem addLocalizedCheckBox(ContextMenu menu, TranslationManager translator, BooleanProperty selectionProperty, String globalAssetsKey, Object... args) {
        var checkMenuItem = new CheckMenuItem(translator.translate(globalAssetsKey, args));
        checkMenuItem.selectedProperty().bindBidirectional(selectionProperty);
        return add(menu, checkMenuItem);
    }

    static RadioMenuItem addLocalizedRadioButton(ContextMenu menu, TranslationManager translator, String globalAssetsKey, Object... args) {
        return add(menu, new RadioMenuItem(translator.translate(globalAssetsKey, args)));
    }

    static void addSeparator(ContextMenu menu) {
        menu.getItems().add(new SeparatorMenuItem());
    }

    static EventHandler<ActionEvent> wrapActionHandler(ContextMenu menu, EventHandler<ActionEvent> actionHandler) {
        return actionEvent -> {
            actionHandler.handle(actionEvent);
            menu.hide();
        };
    }
}