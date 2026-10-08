/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.bonus.anim;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.math.Direction;
import de.amr.pacmanfx.core.entities.world.WorldNavigationComp;
import de.amr.pacmanfx.ui.entities3D.bonus.comp.BonusView3D;

public class BonusRollingTransform {

    public static final int ANGLE_DELTA = 5;

    public BonusRollingTransform() {}

    public void update(GameEntity bonus) {
        bonus.optComponent(WorldNavigationComp.class).ifPresent(worldNavigation -> {
            final BonusView3D comp3D = bonus.assertComponent(BonusView3D.class);
            final Direction moveDir = worldNavigation.moveDir();

            switch (moveDir) {
                case UP -> {
                    addRotX(comp3D, -ANGLE_DELTA);
                    comp3D.rotateY().setAngle(0);
                }
                case DOWN  -> {
                    addRotX(comp3D, ANGLE_DELTA);
                    comp3D.rotateY().setAngle(0);
                }
                case LEFT  -> {
                    comp3D.rotateX().setAngle(0);
                    addRotY(comp3D, ANGLE_DELTA);
                }
                case RIGHT -> {
                    comp3D.rotateX().setAngle(0);
                    addRotY(comp3D, -ANGLE_DELTA);
                }
            }
        });
    }

    private void addRotX(BonusView3D view3D, double delta) {
        view3D.rotateX().setAngle(normalize(view3D.rotateX().getAngle() + delta));
    }

    private void addRotY(BonusView3D view3D, double delta) {
        view3D.rotateY().setAngle(normalize(view3D.rotateY().getAngle() + delta));
    }

    private double normalize(double angle) {
        double na = angle % 360;
        return na < 0 ? na + 360 : na;
    }
}
