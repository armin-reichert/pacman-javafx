package de.amr.pacmanfx.ui.entities3D.bonus.system;

import de.amr.basics.ui.animation.AnimationRegistry;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;

public record Bonus3DUpdateSystem(Bonus3DMovementSystem movement3D, Bonus3DAnimationSystem animation3D) {

    public Bonus3DUpdateSystem() {
        this(new Bonus3DMovementSystem(), new Bonus3DAnimationSystem());
    }

    public void update(Bonus bonus, AnimationRegistry animationRegistry) {
        movement3D.update(bonus);
        animation3D.update(bonus, animationRegistry);
    }
}
