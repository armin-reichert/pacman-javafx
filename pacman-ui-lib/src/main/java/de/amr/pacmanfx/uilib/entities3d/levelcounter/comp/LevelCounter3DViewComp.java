/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3d.levelcounter.comp;

import de.amr.basics.ui.ecs.GameEntityComp;
import de.amr.basics.ui.assets.DisposableGraphicsObject;
import javafx.scene.Group;

public class LevelCounter3DViewComp implements GameEntityComp, DisposableGraphicsObject {

    private Group root;

    public LevelCounter3DViewComp() {}

    public void setRoot(Group root) {
        this.root = root;
    }

    public Group root() {
        return root;
    }

    @Override
    public void dispose() {
        cleanupGroup(root, true);
    }

    public void build() {}
}
