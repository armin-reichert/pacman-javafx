/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.bonus;

import de.amr.pacmanfx.core.entities.bonus.Bonus;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.scene.shape.Box;

public class Bonus3DViewSystem {

    public static void update(Bonus bonus, AnimationRegistry animationRegistry) {
        switch (bonus.bonusState()) {
            case EDIBLE -> lookEdible(bonus);
            case EATEN -> lookEaten(bonus, animationRegistry);
            case INACTIVE -> {}
        }
    }

    public static void lookEdible(Bonus bonus) {
        final Bonus3DViewComp view3D = bonus.requireComponent(Bonus3DViewComp.class);
        final Box shape3D = view3D.box3D();

        shape3D.setVisible(true);
        shape3D.setWidth(view3D.symbolWidth());
        shape3D.setMaterial(view3D.symbolTexture());
    }

    public static void lookEaten(Bonus bonus, AnimationRegistry animations) {
        final Bonus3DViewComp view3D = bonus.requireComponent(Bonus3DViewComp.class);
        final Box shape3D = view3D.box3D();

        shape3D.setVisible(true);
        shape3D.setWidth(view3D.pointsWidth());
        shape3D.setMaterial(view3D.pointsTexture());

        // restore neutral orientation
        view3D.rotateX().setAngle(0);
        view3D.rotateY().setAngle(0);

        // Rotate around x-axis
        animations.animation(Bonus3DAnimationID.BONUS_EATEN).playFromStart();
    }

    public static void lookExpired(Bonus bonus, AnimationRegistry animations) {
        final Bonus3DViewComp view3D = bonus.requireComponent(Bonus3DViewComp.class);
        final Box shape3D = view3D.box3D();

        shape3D.setVisible(false);

        animations.optAnimation(Bonus3DAnimationID.BONUS_EDIBLE).ifPresent(ManagedAnimation::stop);
        animations.optAnimation(Bonus3DAnimationID.BONUS_EATEN).ifPresent(ManagedAnimation::stop);
    }
}
