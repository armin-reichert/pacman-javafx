/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import java.io.File;

public class GameConstants {

    private GameConstants() {}

    /**
     * Directory under which the user specific files are stored.
     * <p>Default: <code>$HOME/.pacmanfx</code> (Unix) or <code>%USERPROFILE%\.pacmanfx</code> (MS Windows)</p>
     */
    public static final File USER_HOME_DIR = new File(System.getProperty("user.home"), ".pacmanfx");

    /**
     * Directory where custom maps are stored (default: <code>&lt;home_dir&gt;/maps</code>).
     */
    public static final File CUSTOM_MAP_DIR = new File(USER_HOME_DIR, "maps");

    // Simulation speed

    public static int SIMULATION_FPS = 60;

    public static float SIM_STEP_MESSAGE_SEC = 0.75f;

    public static final int SIM_SPEED_DELTA = 2;

    public static final int SIM_SPEED_MIN = 5;

    public static final int SIM_SPEED_MAX = 300;

}
