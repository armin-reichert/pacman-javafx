package de.amr.pacmanfx.uilib.model3D.animation;

import de.amr.basics.math.Vector3f;

public record ExplosionConfig(
    Vector3f gravity,
    int particleCount,
    float particleMeanRadius,
    float particleMinSpeedXY,
    float particleMaxSpeedXY,
    float particleMinSpeedZ,
    float particleMaxSpeedZ) {
}
