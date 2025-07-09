package de.amr.pacmanfx.uilib.model3D;

import javafx.scene.shape.Box;

public class Floor3D extends Box implements Destroyable {
    public Floor3D(double sizeX, double sizeY, double thickness, double padding) {
        super(sizeX + 2 * padding, sizeY, thickness);
        translateXProperty().bind(widthProperty().divide(2).subtract(padding));
        translateYProperty().bind(heightProperty().divide(2));
        translateZProperty().bind(depthProperty().divide(2));
    }

    @Override
    public void destroy() {
        translateXProperty().unbind();
        translateYProperty().unbind();
        translateZProperty().unbind();
        materialProperty().unbind();
    }
}
