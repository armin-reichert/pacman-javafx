/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import javafx.beans.property.BooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Provides convenience methods for building context menus and handles closing of the menu when any action is executed.
 */
public class GameUI_ContextMenu extends ContextMenu {

    private GameUI ui;

    public GameUI_ContextMenu() {}

    public void setUI(GameUI ui) {
        this.ui = requireNonNull(ui);
    }

    public List<MenuItem> itemsCopy() {
        return new ArrayList<>(getItems());
    }

    public void clear() {
        getItems().clear();
    }

    public <M extends MenuItem> M add(M item) {
        if (item.getOnAction() != null) {
            item.setOnAction(wrapActionHandler(item.getOnAction()));
        }
        getItems().add(item);
        return item;
    }

    public void addAll(Collection<MenuItem> items) {
        items.forEach(getItems()::add);
    }

    public MenuItem addTitleItem(String itemText) {
        final PreferencesManager prefs = ui.userPrefs();
        final Font font = prefs.getFont("context_menu.title.font");
        final Color fillColor = prefs.getColor("context_menu.title.fill");
        final var text = new Text(itemText);
        text.setFont(font);
        text.setFill(fillColor);
        text.getStyleClass().add("custom-menu-title");
        final var item = new CustomMenuItem(text, false);
        getItems().add(item);
        return item;
    }

    public MenuItem addLocalizedActionItem(Runnable action, String globalAssetsKey, Object... args) {
        var actionItem = new MenuItem(ui.translate(globalAssetsKey, args));
        actionItem.setOnAction(e -> action.run());
        return add(actionItem);
    }

    public MenuItem addLocalizedActionItem(GameAction action, String globalAssetsKey, Object... args) {
        var actionItem = new MenuItem(ui.translate(globalAssetsKey, args));
        actionItem.setOnAction(e -> action.executeIfEnabled(ui));
        return add(actionItem);
    }

    public MenuItem addLocalizedTitleItem(String globalAssetsKey, Object... args) {
        return addTitleItem(ui.translate(globalAssetsKey, args));
    }

    public CheckMenuItem addLocalizedCheckBox(BooleanProperty selectionProperty, String globalAssetsKey, Object... args) {
        var checkMenuItem = new CheckMenuItem(ui.translate(globalAssetsKey, args));
        checkMenuItem.selectedProperty().bindBidirectional(selectionProperty);
        return add(checkMenuItem);
    }

    public RadioMenuItem addLocalizedRadioButton(String globalAssetsKey, Object... args) {
        return add(new RadioMenuItem(ui.translate(globalAssetsKey, args)));
    }

    public void addSeparator() {
        getItems().add(new SeparatorMenuItem());
    }

    private EventHandler<ActionEvent> wrapActionHandler(EventHandler<ActionEvent> actionHandler) {
        return actionEvent -> {
            actionHandler.handle(actionEvent);
            hide();
        };
    }
}