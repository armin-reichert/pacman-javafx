package de.amr.games.pacman.tilemapeditor;

import java.io.PrintWriter;

import static javafx.application.Application.launch;

public class Main {
    public static void main(String[] args) {
        try {
            launch(TileMapEditorApp.class, args);
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