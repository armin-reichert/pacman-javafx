package de.amr.pacmanfx.uilib.entities3D.ghost.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import de.amr.pacmanfx.uilib.entities3D.ghost_old.GhostAppearance;
import javafx.scene.Group;

public class Ghost3DViewComp implements GameEntityComponent {

    // Root node containing all variants
    private Group root;

    // Currently active variant
    private GhostAppearance activeVariant;

    public Group root() {
        return root;
    }

    public void setRoot(Group root) {
        this.root = root;
    }

    public GhostAppearance activeVariant() {
        return activeVariant;
    }

    public void setActiveVariant(GhostAppearance activeVariant) {
        this.activeVariant = activeVariant;
    }

    @Override
    public void reset() {
    }
}
