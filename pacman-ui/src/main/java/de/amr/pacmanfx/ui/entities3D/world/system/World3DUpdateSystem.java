package de.amr.pacmanfx.ui.entities3D.world.system;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.math.Vector2f;
import de.amr.basics.ui.animation.AnimationRegistry;
import de.amr.basics.ui.entities.hud.livescounter.LivesCounter;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.ghost.GhostState;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.world.House;
import de.amr.pacmanfx.core.level.GameLevelEntitySet;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.ui.GameSystems3D;
import de.amr.pacmanfx.ui.entities3D.house.comp.House3DViewComp;
import de.amr.pacmanfx.ui.entities3D.house.system.House3DSystem;
import de.amr.pacmanfx.ui.entities3D.livescounter.system.LivesCounter3DViewSystem;

import java.util.Set;

public class World3DUpdateSystem {

    private static final Set<GhostState> GHOST_STATES_WITH_ACCESS_TO_HOUSE = Set.of(
        GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE);

    private static final Set<GhostState> GHOST_STATES_REQUIRING_HOUSE_LIGHTING = Set.of(
        GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE);

    public void updateEntities(GameContext game, WorldMap worldMap, AnimationRegistry animationRegistry) {
        final GameSession session = game.session();
        final GameLevelEntitySet entitySet = session.level().entitySet();

        updateLivesCounter3D(session.hud().livesCounter());
        updateHouse3D(entitySet.entities().theOne(House.class), entitySet);
        updatePac3D(entitySet.pac(), worldMap);
        updateGhosts3D(entitySet);
        updateBonus3D(entitySet.entities().anyOfTypeOrNull(Bonus.class), animationRegistry);
    }

    private void updatePac3D(Pac pac, WorldMap worldMap) {
        final GameSystems3D.PacSystems3D pacSystems3D = GameSystems3D.reqSystem(GameSystems3D.PacSystems3D.class);
        pacSystems3D.transform().update(pac, worldMap);
        pacSystems3D.animation().updateAnimations(pac);
        pacSystems3D.animation().updatePowerLight(pac);
    }

    private void updateLivesCounter3D(LivesCounter livesCounter) {
        // Lives counter shapes follow Pac location
        final LivesCounter3DViewSystem livesCounter3DViewSystem = GameSystems3D.reqSystem(LivesCounter3DViewSystem.class);
        livesCounter3DViewSystem.update(livesCounter);
    }

    private void updateGhosts3D(GameLevelEntitySet entitySet) {
        final var ghostSystems3D = GameSystems3D.reqSystem(GameSystems3D.GhostSystems3D.class);
        entitySet.ghosts().forEach(ghost -> {
            ghostSystems3D.movement().update(ghost);
            ghostSystems3D.appearance().update(ghost);
        });
    }

    private void updateHouse3D(House house, GameLevelEntitySet entitySet) {
        boolean accessRequested = entitySet.ghostsInAnyOfStates(GHOST_STATES_WITH_ACCESS_TO_HOUSE)
            .filter(ghost -> house.isDoorAt(ghost.pos().tile()))
            .anyMatch(GameEntity::isVisible);

        boolean ghostNearHouseDoor = entitySet.ghostsInAnyOfStates(GHOST_STATES_REQUIRING_HOUSE_LIGHTING)
            .filter(ghost -> ghostIsNearHouseDoor(house, ghost))
            .anyMatch(GameEntity::isVisible);

        final var houseSystem3D = GameSystems3D.reqSystem(House3DSystem.class);
        houseSystem3D.updateLight(house, ghostNearHouseDoor);
        houseSystem3D.update(house, accessRequested);
    }

    private boolean ghostIsNearHouseDoor(House house, Ghost ghost) {
        final House3DViewComp view3D = house.reqComp(House3DViewComp.class);
        final Vector2f houseEntryPos = house.floorplan().entryPosition();
        return ghost.pos().asVector2f().euclideanDist(houseEntryPos) <= view3D.doorSensitivity();
    }

    private void updateBonus3D(Bonus bonus, AnimationRegistry animationRegistry) {
        if (bonus != null) {
            final GameSystems3D.BonusSystems3D bonusSystems3D = GameSystems3D.reqSystem(GameSystems3D.BonusSystems3D.class);
            bonusSystems3D.view3D().update(bonus, animationRegistry);
            switch (bonus.state().enumValue()) {
                case EDIBLE -> bonusSystems3D.view3D().lookEdible(bonus);
                case EATEN  -> bonusSystems3D.view3D().lookEaten(bonus, animationRegistry);
                case INACTIVE -> {}
            }
            bonusSystems3D.movement().update(bonus);
        }
    }
}
