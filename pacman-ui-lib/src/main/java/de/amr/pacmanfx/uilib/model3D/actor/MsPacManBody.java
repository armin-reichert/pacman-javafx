/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.actor;

import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import javafx.scene.Group;

public class MsPacManBody extends Group implements DisposableGraphicsObject {

    public MsPacManBody(Group body, Group femaleParts) {
        getChildren().addAll(body, femaleParts);
    }

    @Override
    public void dispose() {
        cleanupGroup(this, true);
    }
}
