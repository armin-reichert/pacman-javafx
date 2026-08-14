/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.level;


import de.amr.basics.QuerySet;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

// This is just an experimental class for a general entity set with cache
public class GameLevelEntitySet extends QuerySet<GameEntity> {

    private Pac cachedPac;
    private List<Ghost> cachedGhosts;
    private Bonus cachedBonus;

    private void maybeInvalidateCache(GameEntity entity) {
        if (entity instanceof Pac) cachedPac = null;
        if (entity instanceof Ghost) cachedGhosts = null;
        if (entity instanceof Bonus) cachedBonus = null;
    }

    public void add(GameEntity entity) {
        super.add(entity);
        maybeInvalidateCache(entity);
    }

    public void remove(GameEntity entity) {
        super.remove(entity);
        maybeInvalidateCache(entity);
    }

    public Pac pac() {
        if (cachedPac == null) {
            cachedPac = theOne(Pac.class);
        }
        return cachedPac;
    }

    public List<Ghost> ghosts() {
        if (cachedGhosts == null) {
            cachedGhosts = List.copyOf(selectAllOfType(Ghost.class)
                .sorted(Comparator.comparing(Ghost::personality)).toList());
        }
        return cachedGhosts;
    }

    public Optional<Bonus> optBonus() {
        if (cachedBonus == null) {
            cachedBonus = anyOfType(Bonus.class);
        }
        return Optional.ofNullable(cachedBonus);
    }
}
