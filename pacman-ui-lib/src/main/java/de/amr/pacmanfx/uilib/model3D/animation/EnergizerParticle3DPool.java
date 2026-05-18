/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.animation;

import de.amr.basics.Disposable;
import org.tinylog.Logger;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Supplier;

public class EnergizerParticle3DPool implements Disposable {

    static final int AMOUNT_ADDED_WHEN_EMPTY = 100;

    private final Queue<EnergizerParticle3D> q = new ArrayDeque<>();
    private final Supplier<EnergizerParticle3D> particleFactory;

    public EnergizerParticle3DPool(int size, Supplier<EnergizerParticle3D> particleFactory) {
        this.particleFactory = particleFactory;
        addNewParticles(size);
    }

    public EnergizerParticle3D getParticle() {
        if (q.isEmpty()) {
            addNewParticles(AMOUNT_ADDED_WHEN_EMPTY);
        }
        return q.poll();
    }

    private void addNewParticles(int count) {
        for (int i = 0; i < count; ++i) {
            q.add(particleFactory.get());
        }
        Logger.info("Particle pool increased by {}! Pool size={}", count, q.size());
    }

    public void recycle(EnergizerParticle3D particle) {
        particle.reset();
        particle.shape().setVisible(false);
        q.offer(particle);
    }

    public void dispose() {
        for (EnergizerParticle3D energizerParticle3D : q) {
            energizerParticle3D.dispose();
        }
        q.clear();
    }
}
