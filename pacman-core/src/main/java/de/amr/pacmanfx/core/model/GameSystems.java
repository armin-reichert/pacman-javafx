package de.amr.pacmanfx.core.model;

import de.amr.pacmanfx.core.model.systems.common.MovementSystem;
import de.amr.pacmanfx.core.model.systems.common.RandomWorldMovementSystem;
import de.amr.pacmanfx.core.model.systems.ghost.*;
import de.amr.pacmanfx.core.model.systems.pac.PacDigestionSystem;
import de.amr.pacmanfx.core.model.systems.pac.PacPowerSystem;
import de.amr.pacmanfx.core.model.systems.common.WorldMovementSystem;

public final class GameSystems {

    public final MovementSystem movementSystem =  new MovementSystem();
    public final WorldMovementSystem worldMovementSystem = new WorldMovementSystem();

    public final PacPowerSystem pacPowerSystem = new PacPowerSystem();
    public final PacDigestionSystem pacDigestionSystem = new PacDigestionSystem();

    public final RandomWorldMovementSystem randomWorldMovementSystem = new RandomWorldMovementSystem();

    public final GhostStateMachine ghostStateMachine = new GhostStateMachine();

    public final GhostHuntingStrategy pokeyHuntingStrategy = new PokeyHuntingStrategy();
    public final GhostHuntingStrategy bashfulHuntingStrategy = new BashfulHuntingStrategy();
    public final GhostHuntingStrategy shadowHuntingStrategy = new ShadowHuntingStrategy();
    public final GhostHuntingStrategy speedyHuntingStrategy = new SpeedyHuntingStrategy();

    public GhostHuntingStrategy ghostHuntingStrategy(byte personality) {
        return switch (personality) {
            case GameModel.RED_GHOST_SHADOW -> shadowHuntingStrategy;
            case GameModel.PINK_GHOST_SPEEDY -> speedyHuntingStrategy;
            case GameModel.CYAN_GHOST_BASHFUL -> bashfulHuntingStrategy;
            case GameModel.ORANGE_GHOST_POKEY -> pokeyHuntingStrategy;
            default -> throw new IllegalArgumentException("Unknown personality: " + personality);
        };
    }
}
