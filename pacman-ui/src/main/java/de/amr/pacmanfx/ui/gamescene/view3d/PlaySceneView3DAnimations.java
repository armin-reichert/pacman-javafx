/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.view3d;

import de.amr.basics.Disposable;
import de.amr.basics.ui.animation.AnimationRegistry;

public class PlaySceneView3DAnimations implements Disposable {

    private final AnimationRegistry registry = new AnimationRegistry();

    public PlaySceneView3DAnimations() {}

    public AnimationRegistry registry() {
        return registry;
    }

    @Override
    public void dispose() {
        registry.dispose();
    }
}
