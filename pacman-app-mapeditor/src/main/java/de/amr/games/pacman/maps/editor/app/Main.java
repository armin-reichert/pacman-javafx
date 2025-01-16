package de.amr.games.pacman.maps.editor.app;

import static javafx.application.Application.launch;

public class Main {
    public static void main(String[] args) {
        try {
            launch(TileMapEditorApp.class, args);
        } catch (Exception x) {
            x.printStackTrace(System.err);
        }
    }
}