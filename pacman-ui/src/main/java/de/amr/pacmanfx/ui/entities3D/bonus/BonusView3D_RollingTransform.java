/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.bonus;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.core.entities.world.WorldNavigationComp;

public class BonusView3D_RollingTransform {

    public static final int ANGLE_DELTA = 5;

    public BonusView3D_RollingTransform() {}

    public void update(Bonus bonus) {
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
