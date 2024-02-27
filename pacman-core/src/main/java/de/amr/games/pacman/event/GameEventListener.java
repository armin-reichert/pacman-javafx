/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.event;

/**
 * Implemented by classes that listen to game events.
 *
 * @author Armin Reichert
 */
public interface GameEventListener {

    /**
     * Called when a game event is received.
     *
     * @param event a game event
     */
    default void onGameEvent(GameEvent event) {
        switch (event.type) {
            case BONUS_ACTIVATED -> onBonusActivated(event);
            case BONUS_EATEN -> onBonusEaten(event);
            case BONUS_EXPIRED -> onBonusExpired(event);
            case CREDIT_ADDED -> onCreditAdded(event);
            case EXTRA_LIFE_WON -> onExtraLifeWon(event);
            case GAME_STATE_CHANGED -> onGameStateChange((GameStateChangeEvent) event);
            case GHOST_EATEN -> onGhostEaten(event);
            case GHOST_ENTERS_HOUSE -> onGhostEntersHouse(event);
            case GHOST_STARTS_RETURNING_HOME -> onGhostStartsReturningHome(event);
            case HUNTING_PHASE_STARTED -> onHuntingPhaseStarted(event);
            case LEVEL_CREATED -> onLevelCreated(event);
            case LEVEL_STARTED -> onLevelStarted(event);
            case PAC_DIED -> onPacDied(event);
            case PAC_FOUND_FOOD -> onPacFoundFood(event);
            case PAC_GETS_POWER -> onPacGetsPower(event);
            case PAC_LOST_POWER -> onPacLostPower(event);
            case PAC_STARTS_LOSING_POWER -> onPacStartsLosingPower(event);
            case INTERMISSION_STARTED -> onIntermissionStarted(event);
            case STOP_ALL_SOUNDS -> onStopAllSounds(event);
            case UNSPECIFIED_CHANGE -> onUnspecifiedChange(event);
            default -> throw new IllegalArgumentException("Unknown event type: " + event);
        }
    }

    default void onCreditAdded(GameEvent e) {
    }

    default void onExtraLifeWon(GameEvent e) {
    }

    default void onBonusActivated(GameEvent e) {
    }

    default void onBonusEaten(GameEvent e) {
    }

    default void onBonusExpired(GameEvent e) {
    }

    default void onGameStateChange(GameStateChangeEvent e) {
    }

    default void onGhostEaten(GameEvent e) {
    }

    default void onGhostEntersHouse(GameEvent e) {
    }

    default void onGhostStartsReturningHome(GameEvent e) {
    }

    default void onHuntingPhaseStarted(GameEvent e) {
    }

    default void onIntermissionStarted(GameEvent e) {
    }

    default void onLevelCreated(GameEvent e) {
    }

    default void onLevelStarted(GameEvent e) {
    }

    default void onPacDied(GameEvent e) {
    }

    default void onPacFoundFood(GameEvent e) {
    }

    default void onPacGetsExtraLife(GameEvent e) {
    }

    default void onPacGetsPower(GameEvent e) {
    }

    default void onPacLostPower(GameEvent e) {
    }

    default void onPacStartsLosingPower(GameEvent e) {
    }

    default void onStopAllSounds(GameEvent e) {
    }

    default void onUnspecifiedChange(GameEvent e) {
    }
}