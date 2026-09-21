package de.amr.basics.ui.ecs.systems;

import de.amr.basics.QuerySet;
import de.amr.basics.ui.ecs.GameEntity;
import de.amr.basics.ui.ecs.comp.LifetimeComp;

import java.util.List;

public class LifetimeSystem {

    public void update(QuerySet<GameEntity> entities) {
        final List<GameEntity> copy = entities.all().toList();
        for (GameEntity entity : copy) {
            if (entity.hasComp(LifetimeComp.class)) {
                entity.lifetime().becomeOlder();
                if (entity.lifetime().ends()) {
                    entities.remove(entity);
                }
            }
        }
    }
}
