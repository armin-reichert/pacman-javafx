package de.amr.pacmanfx.uilib.model3D.animation;

import de.amr.basics.Disposable;
import de.amr.basics.math.Vector3f;

import java.util.HashSet;
import java.util.Set;

import static de.amr.basics.math.RandomNumberSupport.randomInt;
import static java.util.Objects.requireNonNull;

public class ParticlesSwirlAnimation implements Disposable {

    private final EnergizerParticlesAnimation3D.SwirlConfig config;
    private final Vector3f baseCenter;
    private final Set<EnergizerParticle3D> particles = new HashSet<>();

    public ParticlesSwirlAnimation(EnergizerParticlesAnimation3D.SwirlConfig config, Vector3f baseCenter) {
        this.config = requireNonNull(config);
        this.baseCenter = requireNonNull(baseCenter);
    }

    public void addParticle(EnergizerParticle3D particle) {
        particle.setAngle(Math.toRadians(randomInt(0, 360)));
        particle.setVelocity(new Vector3f(0, 0, -config.upwardsSpeed()));
        particles.add(particle);
    }

    public void update() {
        for (var p : particles) {
            updateStateInsideSwirl(p);
        }
    }

    @Override
    public void dispose() {
        for (var p : particles) {
            p.dispose();
        }
        particles.clear();
    }

    private void updateStateInsideSwirl(EnergizerParticle3D particle) {
        particle.move();
        final Vector3f pos = particle.position();
        if (pos.z() < -config.height()) {
            // reached top of swirl: move to bottom of floor
            particle.setPosition(new Vector3f(pos.x(), pos.y(), baseCenter.z() - 0.5 * particle.size()));
        }
        // Rotate on swirl surface
        particle.setAngle(particle.angle() + config.rotationSpeed());
        if (particle.angle() > Math.TAU) {
            particle.setAngle(particle.angle() - Math.TAU);
        }
        updateParticlePositionOnSwirlSurface(particle);
    }

    private void updateParticlePositionOnSwirlSurface(EnergizerParticle3D particle) {
        final var pos = new Vector3f(
            baseCenter.x() + config.radius() * Math.cos(particle.angle()),
            baseCenter.y() + config.radius() * Math.sin(particle.angle()),
            particle.position().z()
        );
        particle.setPosition(pos);
    }
}
