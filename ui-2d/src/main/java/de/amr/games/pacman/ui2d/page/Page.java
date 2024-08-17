package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.ui2d.ActionHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * @author Armin Reichert
 */
public abstract class Page extends Scene {

    public static final Font CONTEXT_MENU_TITLE_FONT = Font.font("Dialog", FontWeight.BLACK, 14);
    public static final Color CONTEXT_MENU_TITLE_BACKGROUND = Color.CORNFLOWERBLUE; // "Kornblumenblau, sind die Augen der Frauen beim Weine..."

    public static MenuItem menuTitleItem(String titleText) {
        var text = new Text(titleText);
        text.setFont(CONTEXT_MENU_TITLE_FONT);
        text.setFill(CONTEXT_MENU_TITLE_BACKGROUND);
        return new CustomMenuItem(text);
    }

    protected Page(Parent root) {
        super(root);
    }

    public abstract Pane rootPane();

    public void onSelected() {}

    public void setSize(double width, double height) {}

    public void onContextMenuRequested(ContextMenuEvent e) {}

    public void onMouseClicked(MouseEvent e) {}

    public void handleKeyboardInput(ActionHandler handler) {}
}