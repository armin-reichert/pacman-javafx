/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.ui2d.GameActionProvider;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.tinylog.Logger;

/**
 * @author Armin Reichert
 */
public interface Page extends GameActionProvider {

    Font CONTEXT_MENU_TITLE_FONT = Font.font("Dialog", FontWeight.BLACK, 14);
    Color CONTEXT_MENU_TITLE_BACKGROUND = Color.CORNFLOWERBLUE; // "Kornblumenblau, sind die Augen der Frauen beim Weine..."

    static MenuItem menuTitleItem(String titleText) {
        var text = new Text(titleText);
        text.setFont(CONTEXT_MENU_TITLE_FONT);
        text.setFill(CONTEXT_MENU_TITLE_BACKGROUND);
        return new CustomMenuItem(text);
    }

    Pane rootPane();

    void onPageSelected();

    void setSize(double width, double height);

    default void handleContextMenuRequest(ContextMenuEvent e) {
        Logger.info("Context menu requested for page " + this);
        if (e.isKeyboardTrigger()) {
            Logger.info("Keyboard trigger detected, event={}", e);
        }
    }
}