package de.amr.games.pacman.ui2d.rendering;

import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.ui2d.GameContext;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public interface GameWorldRenderer {

    DoubleProperty scalingProperty();
    ObjectProperty<Color> backgroundColorProperty();

    SpriteRenderer spriteRenderer();

    void drawWorld(GraphicsContext g, GameContext context, GameWorld world);
    void setFlashMode(boolean on);
    void setBlinkingOn(boolean on);
}
