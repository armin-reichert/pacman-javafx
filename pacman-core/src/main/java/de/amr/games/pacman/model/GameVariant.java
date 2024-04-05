/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.lib.EnumMethods;

/**
 * Game variants that can be played.
 *
 * @author Armin Reichert
 */
public enum GameVariant implements EnumMethods<GameVariant> {
    MS_PACMAN {

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
    },


    PACMAN {
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
    };

    public abstract String pacName();
    public abstract String ghostName(byte id);

    // Why does the default implementation returns NULL as soon as the enum classes have methods?
    @Override
    public GameVariant[] enumValues() {
        return new GameVariant[] {GameVariant.MS_PACMAN, GameVariant.PACMAN};
    }
}