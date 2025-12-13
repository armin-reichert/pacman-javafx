package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.uilib.assets.UIPreferences;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Collection;

import static java.util.Objects.requireNonNull;

public class GameUI_ContextMenu extends ContextMenu {

    private final GameUI ui;

    public GameUI_ContextMenu(GameUI ui) {
        this.ui = requireNonNull(ui);
    }

    public void clear() {
        getItems().clear();
    }

    public <M extends MenuItem> M add(M item) {
        if (item.getOnAction() != null) {
            item.setOnAction(menuClosingHandler(item.getOnAction()));
        }
        getItems().add(item);
        return item;
    }

    public void addAll(Collection<MenuItem> items) {
        items.forEach(getItems()::add);
    }

    public MenuItem addTitleItem(String itemText) {
        final UIPreferences prefs = ui.preferences();
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

    public MenuItem addLocalizedItem(String globalAssetsKey, Object... args) {
        return add(new MenuItem(ui.globalAssets().translated(globalAssetsKey, args)));
    }

    public MenuItem addLocalizedTitleItem(String globalAssetsKey, Object... args) {
        return addTitleItem(ui.globalAssets().translated(globalAssetsKey, args));
    }

    public CheckMenuItem addLocalizedCheckBoxItem(String globalAssetsKey, Object... args) {
        return add(new CheckMenuItem(ui.globalAssets().translated(globalAssetsKey, args)));
    }

    public void addSeparator() {
        getItems().add(new SeparatorMenuItem());
    }

    private EventHandler<ActionEvent> menuClosingHandler(EventHandler<ActionEvent> actionHandler) {
        return actionEvent -> {
            hide();
            actionHandler.handle(actionEvent);
        };
    }

}
