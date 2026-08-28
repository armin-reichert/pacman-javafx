package de.amr.pacmanfx.core.ecs.systems;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.level.GameLevelEntitySet;

import java.util.List;

public class LifetimeSystem {

    public void update(GameLevelEntitySet entitySet) {
        final List<GameEntity> all = entitySet.all().toList(); // avoid CME!
        all.forEach(entity -> entity.optLifetime().ifPresent(lifetime -> {
            lifetime.becomeOlder();
            if (lifetime.ticksRemaining() <= 0) {
                entitySet.remove(entity);
            }
        }));
    }
}
