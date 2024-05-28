/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import java.io.PrintWriter;

import static javafx.application.Application.launch;

/**
 * @author Armin Reichert
 */
public class Main {
    public static void main(String[] args) {
        try {
            launch(PacManGames3dApp.class, args);
        } catch (Throwable x) {
            try (var pw = new PrintWriter("oh_shit.txt")) {
                x.printStackTrace(pw);
            }
            catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }
}