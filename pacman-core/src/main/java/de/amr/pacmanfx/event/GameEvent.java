/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.event;

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
    HuntingPhaseStartedEvent,
    IntermissionStartedEvent,
    LevelCreatedEvent,
    LevelStartedEvent,
    PacDeadEvent,
    PacDyingEvent,
    PacEatsFoodEvent,
    PacGetsPowerEvent,
    PacLostPowerEvent,
    PacStartsLosingPowerEvent,
    SpecialScoreReachedEvent,
    StopAllSoundsEvent,
    UnspecifiedChangeEvent
{}
