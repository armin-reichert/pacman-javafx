package de.amr.basics.ecs.system;

import de.amr.basics.QuerySet;
import de.amr.basics.ecs.GameEntity;
import de.amr.basics.ecs.comp.Lifetime;

import java.util.List;

public class LifetimeSystem {

    public void update(QuerySet<GameEntity> entities) {
        final List<GameEntity> copy = entities.stream().toList();
        for (GameEntity entity : copy) {
            if (entity.hasComponent(Lifetime.class)) {
                entity.lifetime().becomeOlder();
                if (entity.lifetime().ends()) {
                    entities.remove(entity);
                }
            }
        }
    }
}
