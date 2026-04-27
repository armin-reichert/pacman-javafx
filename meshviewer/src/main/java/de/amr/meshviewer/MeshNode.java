/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.meshviewer;

import javafx.scene.shape.MeshView;

public final class MeshNode extends NavigationTreeNode {
    public final String meshName;
    public final MeshView meshView;

    public MeshNode(String meshName, MeshView meshView) {
        this.meshName = meshName;
        this.meshView = meshView;
    }
}
