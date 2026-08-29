/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.event;

import de.amr.pacmanfx.core.event.bonus.BonusActivatedEvent;
import de.amr.pacmanfx.core.event.bonus.BonusEatenEvent;
import de.amr.pacmanfx.core.event.bonus.BonusExpiredEvent;
import de.amr.pacmanfx.core.event.gameplay.*;
import de.amr.pacmanfx.core.event.ghost.GhostEatenEvent;
import de.amr.pacmanfx.core.event.ghost.GhostEntersHouseEvent;
import de.amr.pacmanfx.core.event.ghost.GhostStartsReturningHomeEvent;
import de.amr.pacmanfx.core.event.pac.*;

public sealed interface GameEvent permits
    BonusActivatedEvent,
    BonusEatenEvent,
    BonusExpiredEvent,
    CreditAddedEvent,
    GameContinuedEvent,
    GameStartedEvent,
    GameStateChangeEvent,
    GhostEatenEvent,
    GhostEntersHouseEvent,
    GhostStartsReturningHomeEvent,
    HighScoreAccessErrorEvent,
    HuntingPhaseStartedEvent,
    IntermissionStartedEvent,
    LevelCreatedEvent,
    LevelStartedEvent,
    PacDeadEvent,
    PacDyingEvent,
    PacEatsFoodEvent,
    PacPowerStartsEvent,
    PacPowerEndsEvent,
    PacPowerStartsFadingEvent,
    SpecialScoreEvent,
    StopAllSoundsEvent,
    GenericChangeEvent,
    TestStartedEvent
{}
