/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.basics.QuerySet;
import de.amr.pacmanfx.ui.entities3D.bonus.system.Bonus3DUpdateSystem;
import de.amr.pacmanfx.ui.entities3D.ghost.system.Ghost3DAppearanceSystem;
import de.amr.pacmanfx.ui.entities3D.ghost.system.Ghost3DMovementSystem;
import de.amr.pacmanfx.ui.entities3D.house.system.House3DSystem;
import de.amr.pacmanfx.ui.entities3D.levelcounter.system.LevelCounter3DViewSystem;
import de.amr.pacmanfx.ui.entities3D.livescounter.system.LivesCounterView3DSystem;
import de.amr.pacmanfx.ui.entities3D.pac.system.Pac3DAnimationSystem;
import de.amr.pacmanfx.ui.entities3D.pac.system.Pac3DTransformSystem;
import de.amr.pacmanfx.ui.entities3D.world.system.World3DUpdateSystem;

public class GameSystems3D {

    private static class SingletonHolder {
        static final GameSystems3D SINGLETON = new GameSystems3D();
    }

    public static GameSystems3D instance() {
        return SingletonHolder.SINGLETON;
    }

    public static <T> T reqSystem(Class<T> systemClass) {
        return instance().systems.theOne(systemClass);
    }

    // Systems

    public record PacSystems3D(Pac3DTransformSystem transform, Pac3DAnimationSystem animation) {
        public PacSystems3D() {
            this(new Pac3DTransformSystem(), new Pac3DAnimationSystem());
        }
    }

    public record GhostSystems3D(Ghost3DMovementSystem movement, Ghost3DAppearanceSystem appearance) {
        public GhostSystems3D() {
            this(new Ghost3DMovementSystem(), new Ghost3DAppearanceSystem());
        }
    }

    private final QuerySet<Object> systems = new QuerySet<>();

    public GameSystems3D() {
        systems.add(new Bonus3DUpdateSystem());
        systems.add(new LevelCounter3DViewSystem());
        systems.add(new LivesCounterView3DSystem());
        systems.add(new GhostSystems3D());
        systems.add(new House3DSystem());
        systems.add(new PacSystems3D());
        systems.add(new World3DUpdateSystem());
    }
}
