/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.levelcounter.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import de.amr.pacmanfx.uilib.DisposableGraphicsObject;
import javafx.scene.Group;

public class LevelCounter3DViewComp implements GameEntityComponent, DisposableGraphicsObject {

    private Group root;

    public LevelCounter3DViewComp() {}

    public void setRoot(Group root) {
        this.root = root;
    }

    public Group root() {
        return root;
    }

    @Override
    public void reset() {}

    @Override
    public void dispose() {
        cleanupGroup(root, true);
    }

    public void build() {}
}
