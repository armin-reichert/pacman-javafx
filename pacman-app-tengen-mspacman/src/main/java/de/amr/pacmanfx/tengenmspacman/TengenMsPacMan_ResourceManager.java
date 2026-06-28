/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.uilib.assets.ResourceManager;

public class TengenMsPacMan_ResourceManager implements ResourceManager {

    private static class LazyThreadSafeSingletonHolder {
        static final TengenMsPacMan_ResourceManager SINGLETON = new TengenMsPacMan_ResourceManager();
    }

    public static TengenMsPacMan_ResourceManager instance() {
        return LazyThreadSafeSingletonHolder.SINGLETON;
    }

    @Override
    public Class<?> resourceRootClass() {
        return getClass();
    }
}
