/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.basics.QuerySet;
import de.amr.pacmanfx.uilib.entities3d.bonus.system.Bonus3DMovementSystem;
import de.amr.pacmanfx.uilib.entities3d.bonus.system.Bonus3DViewSystem;

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

    public record BonusSystems3D(Bonus3DMovementSystem movement, Bonus3DViewSystem view3D) {
        public BonusSystems3D() {
            this(new Bonus3DMovementSystem(), new Bonus3DViewSystem());
        }
    }

    private final QuerySet<Object> systems = new QuerySet<>();

    public GameSystems3D() {
        systems.add(new BonusSystems3D());
    }


}
