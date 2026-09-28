package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.math.Vector2f;
import de.amr.basics.ui.entities.hud.livescounter.LivesCounter;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.ghost.GhostState;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.world.House;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.ui.GameSystems3D;
import de.amr.pacmanfx.ui.entities3D.house.comp.House3DViewComp;
import de.amr.pacmanfx.ui.entities3D.house.system.House3DSystem;
import de.amr.pacmanfx.ui.entities3D.livescounter.system.LivesCounter3DViewSystem;

import java.util.Set;

public class GameLevel3DUpdateController {

    private static final Set<GhostState> GHOST_STATES_WITH_ACCESS_TO_HOUSE = Set.of(
        GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE);

    private static final Set<GhostState> GHOST_STATES_REQUIRING_HOUSE_LIGHTING = Set.of(
        GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE);

    public static void update3DSceneEntities(GameContext game, GameLevel3D level3D) {
        updateLivesCounter3D(game.session().hud().livesCounter());
        updateHouse3D(level3D);
        updatePac3D(level3D);
        updateGhosts3D(level3D);
        updateBonus3D(level3D);
    }

    private static void updatePac3D(GameLevel3D level3D) {
        final GameLevel level = level3D.level();
        final Pac pac = level.entitySet().pac();
        final GameSystems3D.PacSystems3D systems3D = GameSystems3D.reqSystem(GameSystems3D.PacSystems3D.class);

        systems3D.transform().update(pac, level);
        systems3D.animation().updateAnimations(pac);
        systems3D.animation().updatePowerLight(pac);
    }

    private static void updateLivesCounter3D(LivesCounter livesCounter) {
        // Lives counter shapes follow Pac location
        final LivesCounter3DViewSystem livesCounter3DViewSystem = GameSystems3D.reqSystem(LivesCounter3DViewSystem.class);
        livesCounter3DViewSystem.update(livesCounter);
    }

    private static void updateGhosts3D(GameLevel3D level3D) {
        final GameLevel level = level3D.level();
        final var ghostSystems3D = GameSystems3D.reqSystem(GameSystems3D.GhostSystems3D.class);
        level.entitySet().ghosts().forEach(ghost -> {
            ghostSystems3D.movement().update(ghost);
            ghostSystems3D.appearance().update(ghost);
        });
    }

    private static void updateHouse3D(GameLevel3D level3D) {
        final GameLevel level = level3D.level();
        final House house = level.entitySet().entities().theOne(House.class);

        boolean accessRequested = level.entitySet().ghostsInAnyOfStates(GHOST_STATES_WITH_ACCESS_TO_HOUSE)
            .filter(ghost -> house.isDoorAt(ghost.pos().tile()))
            .anyMatch(GameEntity::isVisible);

        boolean ghostNearHouseDoor = level.entitySet().ghostsInAnyOfStates(GHOST_STATES_REQUIRING_HOUSE_LIGHTING)
            .filter(ghost -> ghostIsNearHouseDoor(house, ghost))
            .anyMatch(GameEntity::isVisible);

        final var houseSystem3D = GameSystems3D.reqSystem(House3DSystem.class);
        houseSystem3D.updateLight(house, ghostNearHouseDoor);
        houseSystem3D.update(house, accessRequested);
    }

    private static boolean ghostIsNearHouseDoor(House house, Ghost ghost) {
        final House3DViewComp view3D = house.reqComp(House3DViewComp.class);
        final Vector2f houseEntryPos = house.floorplan().entryPosition();
        return ghost.pos().asVector2f().euclideanDist(houseEntryPos) <= view3D.doorSensitivity();
    }

    private static void updateBonus3D(GameLevel3D level3D) {
        final GameLevel level = level3D.level();
        final Bonus bonus = level.entitySet().entities().anyOfTypeOrNull(Bonus.class);
        final GameSystems3D.BonusSystems3D systems3D = GameSystems3D.reqSystem(GameSystems3D.BonusSystems3D.class);

        if (bonus != null) {
            level3D.ensureBonus3DViewAddedToSceneGraph(bonus);
            switch (bonus.state().enumValue()) {
                case EDIBLE -> systems3D.view3D().lookEdible(bonus);
                case EATEN  -> systems3D.view3D().lookEaten(bonus, level3D.animationManager().registry());
                case INACTIVE -> {}
            }
            systems3D.movement().update(bonus);
        }
    }
}
