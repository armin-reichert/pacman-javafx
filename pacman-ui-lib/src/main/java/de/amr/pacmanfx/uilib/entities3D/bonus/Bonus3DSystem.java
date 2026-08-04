/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.bonus;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.world.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.entities.bonus.Bonus;
import de.amr.pacmanfx.core.model.entities.bonus.BonusState;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.scene.shape.Box;

public class Bonus3DSystem {

    public static void lookEdible(GameEntity bonus) {
        final BonusView3DComp view3D = bonus.requireComponent(BonusView3DComp.class);
        final Box shape3D = view3D.box3D();

        shape3D.setVisible(true);
        shape3D.setWidth(view3D.symbolWidth());
        shape3D.setMaterial(view3D.symbolTexture());
    }

    public static void makeBonusLookEaten(GameEntity bonus, AnimationRegistry animations) {
        final BonusView3DComp view3D = bonus.requireComponent(BonusView3DComp.class);
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

    public static void makeBonusLookExpired(GameEntity bonus, AnimationRegistry animations) {
        final BonusView3DComp view3D = bonus.requireComponent(BonusView3DComp.class);
        final Box shape3D = view3D.box3D();

        shape3D.setVisible(false);

        animations.optAnimation(Bonus3DAnimationID.BONUS_EDIBLE).ifPresent(ManagedAnimation::stop);
        animations.optAnimation(Bonus3DAnimationID.BONUS_EATEN).ifPresent(ManagedAnimation::stop);
    }

    public static void update(GameContext gameContext, Bonus bonus) {
        switch (bonus.state()) {
            case INACTIVE, EATEN -> {}

            case EDIBLE -> {
                Bonus3DSystem.update3DTransform(bonus);

                //TODO move into some system
                final Vector2f center = WorldNavigationSystem.computeCenter(bonus);
                final WorldMap worldMap = gameContext.assertLevel().worldMap();
                boolean outsideWorld = center.x() < WorldMap.HTS || center.x() > worldMap.numCols() * WorldMap.TS - WorldMap.HTS;

                final BonusView3DComp view3D = bonus.requireComponent(BonusView3DComp.class);
                view3D.root().setVisible(bonus.state() == BonusState.EDIBLE && !outsideWorld);
                view3D.rollingTransform().update(bonus);
            }
        }
    }

    //TODO move elsewhere
    public static void update3DTransform(GameEntity bonus) {
        final BonusView3DComp view3D = bonus.requireComponent(BonusView3DComp.class);
        final Vector2f center = WorldNavigationSystem.computeCenter(bonus);

        view3D.translate().setX(center.x());
        view3D.translate().setY(center.y());
        view3D.translate().setZ(-WorldMap.HTS);
    }
}
