package de.amr.pacmanfx.core.model.component;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.Actor;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.world.TerrainLayer;

import static java.util.Objects.requireNonNull;

public class PacManWorldMovementPolicy implements WorldMovementPolicy {

    @Override
    public void reset() {
    }

    @Override
    public boolean canTurnBack(Actor actor) {
        final WorldMovement worldMovement = actor.assertComponent(WorldMovement.class);
        return worldMovement.isNewTileEntered();
    }

    @Override
    public boolean canAccessTile(GameContext gameContext, Actor actor, Vector2i tile) {
        requireNonNull(gameContext);
        requireNonNull(tile);

        final GameLevel level = gameContext.assertLevel();
        final TerrainLayer terrain = level.worldMap().terrainLayer();

        // Portal tiles are the only tiles outside the world that can be accessed
        if (terrain.outOfBounds(tile)) {
            return terrain.isTileInPortalSpace(tile);
        }
        if (terrain.optHouse().isPresent() && terrain.optHouse().get().contains(tile)) {
            return false; // Schieb ab, Alter!
        }
        return !terrain.isTileBlocked(tile);
    }
}
