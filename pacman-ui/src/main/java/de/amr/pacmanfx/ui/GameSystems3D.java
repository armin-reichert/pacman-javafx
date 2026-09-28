/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.basics.QuerySet;
import de.amr.pacmanfx.ui.entities3D.levelcounter.system.LevelCounter3DViewSystem;
import de.amr.pacmanfx.ui.entities3D.livescounter.system.LivesCounter3DViewSystem;
import de.amr.pacmanfx.ui.entities3D.bonus.system.Bonus3DMovementSystem;
import de.amr.pacmanfx.ui.entities3D.bonus.system.Bonus3DViewSystem;
import de.amr.pacmanfx.ui.entities3D.ghost.system.Ghost3DAppearanceSystem;
import de.amr.pacmanfx.ui.entities3D.ghost.system.Ghost3DMovementSystem;
import de.amr.pacmanfx.ui.entities3D.house.system.House3DSystem;

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

    public record GhostSystems3D(Ghost3DMovementSystem movement, Ghost3DAppearanceSystem appearance) {
        public GhostSystems3D() {
            this(new Ghost3DMovementSystem(), new Ghost3DAppearanceSystem());
        }
    }

    public record BonusSystems3D(Bonus3DMovementSystem movement, Bonus3DViewSystem view3D) {
        public BonusSystems3D() {
            this(new Bonus3DMovementSystem(), new Bonus3DViewSystem());
        }
    }

    private final QuerySet<Object> systems = new QuerySet<>();

    public GameSystems3D() {
        systems.add(new BonusSystems3D());
        systems.add(new LevelCounter3DViewSystem());
        systems.add(new LivesCounter3DViewSystem());
        systems.add(new GhostSystems3D());
        systems.add(new House3DSystem());
    }

}
