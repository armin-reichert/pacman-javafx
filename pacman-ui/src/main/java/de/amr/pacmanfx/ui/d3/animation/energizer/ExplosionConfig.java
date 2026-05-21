package de.amr.pacmanfx.ui.d3.animation.energizer;

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
