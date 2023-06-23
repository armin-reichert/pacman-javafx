package de.amr.games.pacman.ui.fx.scene2d;

import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * @author Armin Reichert
 */
public class HelpButton extends Pane {

    private final ImageView icon = new ImageView();

    public HelpButton() {
        getChildren().add(icon);
        setCursor(Cursor.HAND);
    }

    public void setImage(Image image, double size) {
        icon.setImage(image);
        icon.setFitHeight(size);
        icon.setFitWidth(size);
        setMaxSize(size, size);
    }
}
