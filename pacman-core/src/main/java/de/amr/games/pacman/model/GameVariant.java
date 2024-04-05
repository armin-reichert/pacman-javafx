/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.lib.EnumMethods;

import java.io.File;

import static de.amr.games.pacman.model.GameModel.FPS;

/**
 * Game variants that can be played.
 *
 * @author Armin Reichert
 */
public enum GameVariant implements EnumMethods<GameVariant> {
    MS_PACMAN {

        /**
         * These numbers are from a conversation with user "damselindis" on Reddit. I am not sure if they are correct.
         *
         * @see <a href="https://www.reddit.com/r/Pacman/comments/12q4ny3/is_anyone_able_to_explain_the_ai_behind_the/">Reddit</a>
         * @see <a href=" https://github.com/armin-reichert/pacman-basic/blob/main/doc/mspacman-details-reddit-user-damselindis.md">GitHub</a>
         */
        private static final int[][] HUNTING_DURATIONS = {
            {7 * FPS, 20 * FPS, 1, 1037 * FPS, 1, 1037 * FPS, 1, -1}, // Levels 1-4
            {5 * FPS, 20 * FPS, 1, 1037 * FPS, 1, 1037 * FPS, 1, -1}, // Levels 5+
        };

        public String pacName() {
            return "Ms. Pac-Man";
        }

        public String ghostName(byte id) {
            return switch(id) {
                case GameModel.RED_GHOST -> "Blinky";
                case GameModel.PINK_GHOST -> "Pinky";
                case GameModel.CYAN_GHOST -> "Inky";
                case GameModel.ORANGE_GHOST -> "Sue";
                default -> throw new IllegalGhostIDException(id);
            };
        }

        @Override
        public int[] huntingDurations(int levelNumber) {
            return HUNTING_DURATIONS[levelNumber <= 4 ? 0 : 1];
        }

        @Override
        public File highScoreFile() {
            return new File(System.getProperty("user.home"), "highscore-ms_pacman.xml");
        }
    },


    PACMAN {

        // Hunting duration (in ticks) of chase and scatter phases. See Pac-Man dossier.
        private static final int[][] HUNTING_DURATIONS = {
            {7 * FPS, 20 * FPS, 7 * FPS, 20 * FPS, 5 * FPS,   20 * FPS, 5 * FPS, -1}, // Level 1
            {7 * FPS, 20 * FPS, 7 * FPS, 20 * FPS, 5 * FPS, 1033 * FPS,       1, -1}, // Levels 2-4
            {5 * FPS, 20 * FPS, 5 * FPS, 20 * FPS, 5 * FPS, 1037 * FPS,       1, -1}, // Levels 5+
        };

        public String pacName() {
            return "Pac-Man";
        }

        public String ghostName(byte id) {
            return switch(id) {
                case GameModel.RED_GHOST -> "Blinky";
                case GameModel.PINK_GHOST -> "Pinky";
                case GameModel.CYAN_GHOST -> "Inky";
                case GameModel.ORANGE_GHOST -> "Clyde";
                default -> throw new IllegalGhostIDException(id);
            };
        }

        @Override
        public int[] huntingDurations(int levelNumber) {
            return switch (levelNumber) {
                case 1 -> HUNTING_DURATIONS[0];
                case 2, 3, 4 -> HUNTING_DURATIONS[1];
                default -> HUNTING_DURATIONS[2];
            };
        }

        @Override
        public File highScoreFile() {
            return new File(System.getProperty("user.home"), "highscore-pacman.xml");
        }
    };

    public abstract String pacName();

    public abstract String ghostName(byte id);

    public abstract int[] huntingDurations(int levelNumber);

    public abstract File highScoreFile();

    // Why does the default implementation returns NULL as soon as the enum classes have methods?
    @Override
    public GameVariant[] enumValues() {
        return new GameVariant[] {GameVariant.MS_PACMAN, GameVariant.PACMAN};
    }
}