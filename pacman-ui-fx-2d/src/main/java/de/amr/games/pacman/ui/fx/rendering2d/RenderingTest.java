package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.world.Tiles;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class RenderingTest extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent(), 800, 600));
        stage.show();
    }

    Parent createContent() {
        var canvas = new Canvas(800, 600);
        var g = canvas.getGraphicsContext2D();
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        draw(g);
        return new BorderPane(canvas);
    }

    void draw(GraphicsContext g) {
        double TS = 50;
        double r = TS / 2.0;

        g.setStroke(Color.RED);
        g.setLineWidth(3);
        g.setFill(Color.YELLOW);

        g.moveTo(200, 200);
        g.beginPath();
        g.lineTo(250, 200);
        corner(g, 250, 200, Tiles.CORNER_NE);
        corner(g, 250, 200, Tiles.CORNER_NE);
        g.moveTo(250, 200);
        g.lineTo(200, 200);
        corner(g, 200, 200, Tiles.CORNER_SW);
        corner(g, 200, 200, Tiles.CORNER_NW);
        //g.closePath();
        g.stroke();
        //g.fill();
    }

    void corner(GraphicsContext g, double tx, double ty, byte type) {
        double TS = 50;
        double r = 25; // TS/2
        switch (type) {
            case Tiles.CORNER_SE -> g.arc(tx + TS, ty + TS, r, r, -90, 90); // x,y = tile.x, tile.y
            case Tiles.CORNER_NE -> g.arc(tx, ty + TS, r, r, 0, 90);   // x,y = tile.x, tile.y + TS
            case Tiles.CORNER_NW -> g.arc(tx + TS, ty + TS, r, r, 90, 90);  // x, y = tile.x + TS, tile.y + TS
            case Tiles.CORNER_SW -> g.arc(tx + TS, ty + TS, r, r, 180, 90); // x, y = tile.x + TS, tile.y + TS
        }
    }
}
