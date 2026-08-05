package de.amr.pacmanfx.uilib.entities3D.ghost.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import javafx.scene.Group;
import javafx.scene.Node;

public class Ghost3DViewComp implements GameEntityComponent {

    // Colored ghost mesh/material
    private Node coloredGhost;

    // Blue frightened ghost mesh/material
    private Node blueGhost;

    // Flashing ghost mesh/material
    private Node flashingGhost;

    // Number ghost (points)
    private Node numberGhost;

    // Root node containing all variants
    private Group root;

    // Currently active variant
    private Ghost3DVariant activeVariant;


    public Node coloredGhost() {
        return coloredGhost;
    }

    public void setColoredGhost(Node coloredGhost) {
        this.coloredGhost = coloredGhost;
    }

    public Node blueGhost() {
        return blueGhost;
    }

    public void setBlueGhost(Node blueGhost) {
        this.blueGhost = blueGhost;
    }

    public Node flashingGhost() {
        return flashingGhost;
    }

    public void setFlashingGhost(Node flashingGhost) {
        this.flashingGhost = flashingGhost;
    }

    public Node numberGhost() {
        return numberGhost;
    }

    public void setNumberGhost(Node numberGhost) {
        this.numberGhost = numberGhost;
    }

    public Group root() {
        return root;
    }

    public void setRoot(Group root) {
        this.root = root;
    }

    public Ghost3DVariant activeVariant() {
        return activeVariant;
    }

    public void setActiveVariant(Ghost3DVariant activeVariant) {
        this.activeVariant = activeVariant;
    }

    @Override
    public void reset() {
    }
}
