/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.animation;

import de.amr.pacmanfx.uilib.model3D.world.Bonus3D;

public class BonusRollingThroughWorldAnimation3D {

    public static final int ANGLE_DELTA = 5;
    private final Bonus3D bonus3D;

    public BonusRollingThroughWorldAnimation3D(Bonus3D bonus3D) {
        this.bonus3D = bonus3D;
    }

    public void update() {
        switch (bonus3D.bonus().moveDir()) {
            case UP -> {
                bonus3D.rotateX().setAngle(normalize(bonus3D.rotateX().getAngle() - ANGLE_DELTA));
                bonus3D.rotateY().setAngle(0);
            }
            case DOWN  -> {
                bonus3D.rotateX().setAngle(normalize(bonus3D.rotateX().getAngle() + ANGLE_DELTA));
                bonus3D.rotateY().setAngle(0);
            }
            case LEFT  -> {
                bonus3D.rotateX().setAngle(0);
                bonus3D.rotateY().setAngle(normalize(bonus3D.rotateY().getAngle() + ANGLE_DELTA));
            }
            case RIGHT -> {
                bonus3D.rotateX().setAngle(0);
                bonus3D.rotateY().setAngle(normalize(bonus3D.rotateY().getAngle() - ANGLE_DELTA));
            }
        }
    }

    private double normalize(double angle) {
        double na = angle % 360;
        return na < 0 ? na + 360 : na;
    }
}
