package de.amr.pacmanfx.ui.d3.animation.energizer;

import de.amr.basics.Disposable;
import de.amr.basics.math.Vector3f;
import de.amr.pacmanfx.uilib.model3D.animation.EnergizerParticle3D;

import java.util.HashSet;
import java.util.Set;

import static de.amr.basics.math.RandomNumberSupport.randomInt;
import static java.util.Objects.requireNonNull;

public class SwirlAnimation3D implements Disposable {

    private final SwirlConfig config;
    private final Vector3f baseCenter;
    private final Set<EnergizerParticle3D> particles = new HashSet<>();

    public SwirlAnimation3D(SwirlConfig config, Vector3f baseCenter) {
        this.config = requireNonNull(config);
        this.baseCenter = requireNonNull(baseCenter);
    }

    @Override
    public void dispose() {
        particles.clear();
    }

    public void addParticle(EnergizerParticle3D particle) {
        requireNonNull(particle);
        particle.setAngle(Math.toRadians(randomInt(0, 360)));
        particle.setVelocity(new Vector3f(0, 0, -config.upwardsSpeed()));
        particles.add(particle);
    }

    public void update() {
        for (var p : particles) {
            updateParticle(p);
        }
    }

    private void updateParticle(EnergizerParticle3D particle) {
        particle.move();
        final Vector3f pos = particle.pos();
        if (pos.z() < -config.height()) {
            // reached top of swirl: wrap to base z
            particle.setPosition(new Vector3f(pos.x(), pos.y(), baseCenter.z() - particle.shape().getRadius()));
        }
        rotateOnSurface(particle);
    }

    private void rotateOnSurface(EnergizerParticle3D particle) {
        particle.setAngle(particle.angle() + config.rotationSpeed());
        if (particle.angle() > Math.TAU) {
            particle.setAngle(particle.angle() - Math.TAU);
        }
        final var pos = new Vector3f(
            baseCenter.x() + config.radius() * Math.cos(particle.angle()),
            baseCenter.y() + config.radius() * Math.sin(particle.angle()),
            particle.pos().z()
        );
        particle.setPosition(pos);
    }
}
