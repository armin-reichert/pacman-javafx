package de.amr.games.pacman.model.actors;

import de.amr.games.pacman.model.GameLevel;

public interface ActorSpeedControl {
    void setActorBaseSpeed(GameLevel level);
    float ghostAttackSpeed(GameLevel level, Ghost ghost);
    float ghostFrightenedSpeed(GameLevel level, Ghost ghost);
    float ghostSpeedInsideHouse(GameLevel level, Ghost ghost);
    float ghostSpeedReturningToHouse(GameLevel level, Ghost ghost);
    float ghostTunnelSpeed(GameLevel level, Ghost ghost);

    float pacNormalSpeed(GameLevel level);
    float pacPowerSpeed(GameLevel level);
}
