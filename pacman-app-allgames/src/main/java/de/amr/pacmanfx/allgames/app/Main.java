/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.allgames.app;

import javafx.application.Application;
import org.tinylog.Logger;

public class Main {
    public static void main(String[] args) {
        boolean noBuilder = args.length > 0 && "no_builder".equals(args[0]);
        Logger.info("Creating application {} builder", noBuilder ? "without" : "using");
        if (noBuilder) {
            Application.launch(PacManAllGamesNoBuilderApp.class, args);
        } else {
            Application.launch(PacManAllGamesApp.class, args);
        }
    }
}