/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.HuntingPhase;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;

import static de.amr.games.pacman.Globals.*;

public class TengenMsPacMan_ActorFactory {

    static Pac createMsPacMan() {
        var msPacMan = new Pac("Ms. Pac-Man");
        msPacMan.reset();
        return msPacMan;
    }

    static Pac createPacMan() {
        var msPacMan = new Pac("Pac-Man");
        msPacMan.reset();
        return msPacMan;
    }

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
                return level.pac().tile();
            }
        };
    }

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
                return level.pac().tilesAhead(4, false);
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
                return level.pac().tilesAhead(2, false).scaled(2).minus(level.ghost(RED_GHOST_ID).tile());
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
                return tile().euclideanDist(level.pac().tile()) < 8 ? level.ghostScatterTile(id()) : level.pac().tile();
            }
        };
    }
}
