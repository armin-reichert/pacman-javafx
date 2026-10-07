/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.levelcounter.comp;

import de.amr.basics.ui.assets.DisposableGraphicsObject;
import javafx.scene.Group;

public class LevelCounterView3D implements DisposableGraphicsObject {

    private Group root;

    public LevelCounterView3D() {}

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
