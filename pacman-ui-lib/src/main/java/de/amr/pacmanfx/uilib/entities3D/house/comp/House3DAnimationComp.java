package de.amr.pacmanfx.uilib.entities3D.house.comp;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.entities3D.house.House3DAnimationID;

public class House3DAnimationComp implements GameEntityComponent, Disposable {

    private boolean lightOn;
    private boolean accessRequested;
    private final ManagedAnimation doorsMeltingAnimation;

    public House3DAnimationComp(AnimationRegistry animationRegistry) {
        doorsMeltingAnimation = new ManagedAnimation("House Doors Melting");
        animationRegistry.register(House3DAnimationID.HOUSE_DOORS_MELTING, doorsMeltingAnimation);
    }

    public ManagedAnimation doorsMeltingAnimation() {
        return doorsMeltingAnimation;
    }

    public boolean lightOn() {
        return lightOn;
    }

    public void setLightOn(boolean lightOn) {
        this.lightOn = lightOn;
    }

    public boolean accessRequested() {
        return accessRequested;
    }

    public void setAccessRequested(boolean accessRequested) {
        this.accessRequested = accessRequested;
    }

    @Override
    public void dispose() {
        doorsMeltingAnimation.dispose();
    }

    @Override
    public void reset() {
    }
}
