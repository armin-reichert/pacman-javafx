/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.HuntingPhase;
import de.amr.games.pacman.model.actors.Ghost;

import static de.amr.games.pacman.Globals.*;

public interface ArcadeMsPacMan_GhostFactory {
    /**
     * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase. Some say,
     * the original intention had been to randomize the scatter target of *all* ghosts but because of a bug,
     * only the scatter target of Blinky and Pinky would have been affected. Who knows?
     */
    static Ghost createRedGhost() {
        return new Ghost(RED_GHOST_ID, "Blinky") {
            @Override
            public void hunt() {
                float speed = level.speedControl().ghostAttackSpeed(level, this);
                if (level.huntingTimer().phaseIndex() == 0) {
                    roam(speed);
                } else {
                    boolean chase = level.huntingTimer().phase() == HuntingPhase.CHASING || cruiseElroy() > 0;
                    Vector2i targetTile = chase ? chasingTargetTile() : level.ghostScatterTile(id());
                    followTarget(targetTile, speed);
                }
            }
            @Override
            public Vector2i chasingTargetTile() {
                // Blinky (red ghost) attacks Pac-Man directly
                return level.pac().tile();
            }
        };
    }

    /** @see <a href="http://www.donhodges.com/pacman_pinky_explanation.htm">Overflow bug explanation</a>. */
    static Ghost createPinkGhost() {
        return new Ghost(PINK_GHOST_ID, "Pinky") {
            @Override
            public void hunt() {
                float speed = level.speedControl().ghostAttackSpeed(level, this);
                if (level.huntingTimer().phaseIndex() == 0) {
                    roam(speed);
                } else {
                    boolean chase = level.huntingTimer().phase() == HuntingPhase.CHASING;
                    Vector2i targetTile = chase ? chasingTargetTile() : level.ghostScatterTile(id());
                    followTarget(targetTile, speed);
                }
            }
            @Override
            public Vector2i chasingTargetTile() {
                // Pinky (pink ghost) ambushes Pac-Man
                return level.pac().tilesAhead(4, true);
            }
        };
    }

    static Ghost createCyanGhost() {
        return new Ghost(CYAN_GHOST_ID, "Inky") {
            @Override
            public void hunt() {
                float speed = level.speedControl().ghostAttackSpeed(level, this);
                boolean chase = level.huntingTimer().phase() == HuntingPhase.CHASING;
                Vector2i targetTile = chase ? chasingTargetTile() : level.ghostScatterTile(id());
                followTarget(targetTile, speed);
            }
            @Override
            public Vector2i chasingTargetTile() {
                // Inky (cyan ghost) attacks from opposite side as Blinky
                return level.pac().tilesAhead(2, true).scaled(2).minus(level.ghost(RED_GHOST_ID).tile());
            }
        };
    }

    static Ghost createOrangeGhost() {
        return new Ghost(ORANGE_GHOST_ID, "Sue") {
            @Override
            public void hunt() {
                float speed = level.speedControl().ghostAttackSpeed(level, this);
                boolean chase = level.huntingTimer().phase() == HuntingPhase.CHASING;
                Vector2i targetTile = chase ? chasingTargetTile() : level.ghostScatterTile(id());
                followTarget(targetTile, speed);
            }
            @Override
            public Vector2i chasingTargetTile() {
                // Attacks directly or retreats towards scatter target if Pac is near
                return tile().euclideanDist(level.pac().tile()) < 8 ? level.ghostScatterTile(id()) : level.pac().tile();
            }
        };
    }

}
