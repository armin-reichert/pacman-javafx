package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.basics.Disposable;
import de.amr.basics.ui.animation.AnimationRegistry;

public class GameSceneAnimations3DComp implements Disposable {

    private final AnimationRegistry registry = new AnimationRegistry();

    public GameSceneAnimations3DComp() {}

    public AnimationRegistry registry() {
        return registry;
    }

    @Override
    public void dispose() {
        registry.dispose();
    }
}
