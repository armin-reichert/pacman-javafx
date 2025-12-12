/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.event;

import org.tinylog.Logger;

public interface GameEventListener {

    default void onGameEvent(GameEvent event) {
        switch (event.type()) {
            case BONUS_ACTIVATED -> onBonusActivated(event);
            case BONUS_EATEN -> onBonusEaten(event);
            case BONUS_EXPIRED -> onBonusExpires(event);
            case CREDIT_ADDED -> onCreditAdded(event);
            case GAME_CONTINUED -> onGameContinues(event);
            case GAME_STARTED -> onGameStarts(event);
            case GAME_STATE_CHANGED -> {
                var stateChangeEvent = (GameStateChangeEvent) event;
                onGameStateChange(stateChangeEvent);
            }
            case GHOST_EATEN -> onGhostEaten(event);
            case GHOST_ENTERS_HOUSE -> onGhostEntersHouse(event);
            case GHOST_STARTS_RETURNING_HOME -> onGhostStartsReturningHome(event);
            case HUNTING_PHASE_STARTED -> onHuntingPhaseStarted(event);
            case LEVEL_CREATED -> onLevelCreated(event);
            case LEVEL_STARTED -> onLevelStarts(event);
            case PAC_DEAD -> onPacDead(event);
            case PAC_DYING -> onPacDying(event);
            case PAC_FOUND_FOOD -> onPacFindsFood(event);
            case PAC_GETS_POWER -> onPacPowerBegins(event);
            case PAC_LOST_POWER -> onPacPowerEnds(event);
            case PAC_STARTS_LOSING_POWER -> onPacStartsLosingPower(event);
            case INTERMISSION_STARTED -> onIntermissionStarted(event);
            case SPECIAL_SCORE_REACHED -> onSpecialScoreReached(event);
            case STOP_ALL_SOUNDS -> onStopAllSounds(event);
            case UNSPECIFIED_CHANGE -> onUnspecifiedChange(event);
            default -> throw new IllegalArgumentException("Unknown event type: " + event);
        }
    }

    default void onGameStateChange(GameStateChangeEvent e) {
        String newStateName = e.newState().name();
        Logger.info("Enter new game state '{}'", newStateName);
    }

    default void onCreditAdded(GameEvent e) {}
    default void onSpecialScoreReached(GameEvent e) {}
    default void onBonusActivated(GameEvent e) {}
    default void onBonusEaten(GameEvent e) {}
    default void onBonusExpires(GameEvent e) {}
    default void onGameContinues(GameEvent e) {}
    default void onGameStarts(GameEvent e) {}
    default void onGhostEaten(GameEvent e) {}
    default void onGhostEntersHouse(GameEvent e) {}
    default void onGhostStartsReturningHome(GameEvent e) {}
    default void onHuntingPhaseStarted(GameEvent e) {}
    default void onIntermissionStarted(GameEvent e) {}
    default void onLevelCreated(GameEvent e) {}
    default void onLevelStarts(GameEvent e) {}
    default void onPacDead(GameEvent e) {}
    default void onPacDying(GameEvent e) {}
    default void onPacFindsFood(GameEvent e) {}
    default void onPacPowerBegins(GameEvent e) {}
    default void onPacPowerEnds(GameEvent e) {}
    default void onPacStartsLosingPower(GameEvent e) {}
    default void onStopAllSounds(GameEvent e) {}
    default void onUnspecifiedChange(GameEvent e) {}
}
