/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.bonus.anim;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.WorldNavigationComp;
import de.amr.pacmanfx.uilib.entities3D.bonus.comp.Bonus3DViewComp;

public class BonusRollingTransform {

    public static final int ANGLE_DELTA = 5;

    public BonusRollingTransform() {}

    public void update(GameEntity bonus) {
        bonus.optComp(WorldNavigationComp.class).ifPresent(worldNavigation -> {
            final Bonus3DViewComp comp3D = bonus.reqComp(Bonus3DViewComp.class);
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

    private void addRotX(Bonus3DViewComp view3D, double delta) {
        view3D.rotateX().setAngle(normalize(view3D.rotateX().getAngle() + delta));
    }

    private void addRotY(Bonus3DViewComp view3D, double delta) {
        view3D.rotateY().setAngle(normalize(view3D.rotateY().getAngle() + delta));
    }

    private double normalize(double angle) {
        double na = angle % 360;
        return na < 0 ? na + 360 : na;
    }
}
