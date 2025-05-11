package de.amr.pacmanfx.tilemap.editor.app;

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