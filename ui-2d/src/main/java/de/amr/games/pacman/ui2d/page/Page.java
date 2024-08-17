package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.ui2d.ActionHandler;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.tinylog.Logger;

/**
 * @author Armin Reichert
 */
public interface Page {

    Font CONTEXT_MENU_TITLE_FONT = Font.font("Dialog", FontWeight.BLACK, 14);
    Color CONTEXT_MENU_TITLE_BACKGROUND = Color.CORNFLOWERBLUE; // "Kornblumenblau, sind die Augen der Frauen beim Weine..."

    static MenuItem menuTitleItem(String titleText) {
        var text = new Text(titleText);
        text.setFont(CONTEXT_MENU_TITLE_FONT);
        text.setFill(CONTEXT_MENU_TITLE_BACKGROUND);
        return new CustomMenuItem(text);
    }

    Pane rootPane();

    void onSelected();

    void setSize(double width, double height);

    default void onContextMenuRequested(ContextMenuEvent e) {
        Logger.info("Context menu requested for page " + this);
    }

    default void onMouseClicked(MouseEvent e) {
        Logger.info("Context menu requested for page " + this);
    }

    void handleKeyboardInput(ActionHandler handler);
}