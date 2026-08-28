package de.amr.pacmanfx.core.ecs.systems;

import de.amr.pacmanfx.core.ecs.comp.LifetimeComp;
import de.amr.pacmanfx.core.level.GameLevelEntitySet;

public class LifetimeSystem {

    public void update(GameLevelEntitySet entitySet) {
        final var list = entitySet.allWith(LifetimeComp.class).toList();
        list.forEach(entity -> {
            entity.lifetime().becomeOlder();
            if (entity.lifetime().ends()) {
                entitySet.remove(entity);
            }
        });
    }
}
