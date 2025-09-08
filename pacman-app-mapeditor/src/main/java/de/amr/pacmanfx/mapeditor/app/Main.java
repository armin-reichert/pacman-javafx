/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.app;

import org.tinylog.Logger;

import static javafx.application.Application.launch;

public class Main {
    public static void main(String[] args) {
        try {
            launch(TileMapEditorApp.class, args);
        } catch (Exception x) {
            Logger.trace(x);
            x.printStackTrace(System.err);
        }
    }
}