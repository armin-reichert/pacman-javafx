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
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

public class RenderingTest extends Application {

    Canvas canvas;

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(createContent(), 800, 400);
        canvas.widthProperty().bind(scene.widthProperty());
        canvas.heightProperty().bind(scene.heightProperty());
        stage.setScene(scene);
        stage.show();
    }

    Parent createContent() {
        canvas = new Canvas(800, 400);
        var g = canvas.getGraphicsContext2D();
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        draw(g);
        return new BorderPane(canvas);
    }

    void draw(GraphicsContext g) {
        g.setLineWidth(3);
        g.setFill(Color.YELLOW);
        g.setStroke(Color.RED);
        double rx=40,  ry = 25;

        g.beginPath();

        drawCross(g, 200, 200);

        // corner SE
        g.arc(200, 200, rx, ry, 270, 90);
        // corner NE
        g.arc(200, 200, rx, ry, 0, 90);
        // hline to left
        g.lineTo(150, 200-ry);

        drawCross(g, 150, 200);

        // corner nw
        g.arc(150, 200, rx, ry, 90, 90);
        // corner sw
        g.arc(150, 200, rx, ry, 180, 90);

        g.closePath();
        g.fill();
        g.stroke();
    }

    void drawCross(GraphicsContext g, double x, double y) {
        g.save();
        g.setStroke(Color.GRAY);
        g.setLineWidth(1);
        g.strokeLine(x-5,y-5,x+5,y+5);
        g.strokeLine(x-5,y+5,x+5,y-5);
        g.restore();
    }
}
