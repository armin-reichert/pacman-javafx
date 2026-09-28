/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.bonus.system;

import de.amr.basics.ui.animation.AnimationRegistry;
import de.amr.basics.ui.animation.ManagedAnimation;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.ui.entities3D.bonus.anim.Bonus3DAnimationID;
import de.amr.pacmanfx.ui.entities3D.bonus.comp.Bonus3DViewComp;
import javafx.scene.shape.Box;

public class Bonus3DViewSystem {

    public void update(Bonus bonus, AnimationRegistry animationRegistry) {
        switch (bonus.state().enumValue()) {
            case EDIBLE -> lookEdible(bonus);
            case EATEN  -> lookEaten(bonus, animationRegistry);
            case INACTIVE -> {}
        }
    }

    public void lookEdible(Bonus bonus) {
        final Bonus3DViewComp view3D = bonus.reqComp(Bonus3DViewComp.class);
        final Box shape3D = view3D.box3D();

        shape3D.setVisible(true);
        shape3D.setWidth(view3D.symbolWidth());
        shape3D.setMaterial(view3D.symbolTexture());
    }

    public void lookEaten(Bonus bonus, AnimationRegistry animationRegistry) {
        final Bonus3DViewComp view3D = bonus.reqComp(Bonus3DViewComp.class);
        final Box shape3D = view3D.box3D();

        shape3D.setVisible(true);
        shape3D.setWidth(view3D.pointsWidth());
        shape3D.setMaterial(view3D.pointsTexture());

        // restore neutral orientation
        view3D.rotateX().setAngle(0);
        view3D.rotateY().setAngle(0);

        // Rotate around x-axis
        animationRegistry.requireAnimation(Bonus3DAnimationID.BONUS_EATEN).playFromStart();
    }

    public void lookExpired(Bonus bonus, AnimationRegistry animationRegistry) {
        final Bonus3DViewComp view3D = bonus.reqComp(Bonus3DViewComp.class);
        final Box shape3D = view3D.box3D();

        shape3D.setVisible(false);

        animationRegistry.optAnimation(Bonus3DAnimationID.BONUS_EDIBLE).ifPresent(ManagedAnimation::stop);
        animationRegistry.optAnimation(Bonus3DAnimationID.BONUS_EATEN).ifPresent(ManagedAnimation::stop);
    }
}
