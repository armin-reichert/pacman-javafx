package experiments;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.tilemap.Obstacle;
import de.amr.games.pacman.lib.tilemap.Tiles;
import de.amr.games.pacman.ui2d.lib.Ufx;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui2d.lib.Ufx.coloredMaterial;
import static de.amr.games.pacman.ui2d.lib.Ufx.wallpaperBackground;
import static java.lang.Math.TAU;

public class ExperimentsApp extends Application {

    private static final double WALL_WIDTH = 5;
    private static final double WALL_HEIGHT = 10;
    private static final PhongMaterial WALL_MATERIAL = coloredMaterial(Color.BROWN);

    private static final int SCENE_SIZE_X = 1200, SCENE_SIZE_Y = 800;
    private static final double MAZE_SIZE_X = 100 * 8, MAZE_SIZE_Y = 160 * 8, MAZE_SIZE_Z = 2 * 8;

    private final Group content = new Group();
    private Transition cycling;
    private Obstacle obstacle;

    @Override
    public void start(Stage primaryStage) throws Exception {
        //createBallCyclingAroundCylinder();
        createMaze(-MAZE_SIZE_X *0.5, -MAZE_SIZE_Y *0.5);

        Scene scene = new Scene(content, SCENE_SIZE_X, SCENE_SIZE_Y, true);
        scene.setFill(Color.grayRgb(30));

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setFieldOfView(40); // default: 30
        camera.setRotationAxis(Rotate.X_AXIS);
        camera.setRotate(45);
        camera.setTranslateY(250);
        camera.setTranslateZ(-100);
        scene.setCamera(camera);

        scene.setOnKeyPressed(e -> {
            Logger.info("Key '{}' pressed", e.getCode());
            if (cycling == null) return;
            if (e.getCode() == KeyCode.P) {
                if (cycling.getStatus() == Animation.Status.RUNNING) {
                    cycling.pause();
                } else {
                    cycling.play();
                }
            }
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("Experiments");
        primaryStage.show();
    }

    private void createMaze(double ox, double oy) {
        {
            Box wallNorth = new Box(MAZE_SIZE_X, 5, MAZE_SIZE_Z);
            wallNorth.setTranslateX(ox + wallNorth.getWidth() * 0.5);
            wallNorth.setTranslateY(oy + wallNorth.getHeight() * 0.5);
            wallNorth.setMaterial(coloredMaterial(Color.RED));
            content.getChildren().add(wallNorth);
        }
        {
            Box wallSouth = new Box(MAZE_SIZE_X, 5, MAZE_SIZE_Z);
            wallSouth.setTranslateX(ox + wallSouth.getWidth() * 0.5);
            wallSouth.setTranslateY(oy + MAZE_SIZE_Y + wallSouth.getHeight() * 0.5);
            wallSouth.setMaterial(coloredMaterial(Color.GREEN));
            content.getChildren().add(wallSouth);
        }
        {
            Box wallWest = new Box(5, MAZE_SIZE_Y, MAZE_SIZE_Z);
            wallWest.setTranslateX(ox + wallWest.getWidth() * 0.5);
            wallWest.setTranslateY(oy + wallWest.getHeight() * 0.5);
            wallWest.setMaterial(coloredMaterial(Color.BLUE));
            content.getChildren().add(wallWest);
        }
        {
            Box wallEast = new Box(5, MAZE_SIZE_Y, MAZE_SIZE_Z);
            wallEast.setTranslateX(ox + MAZE_SIZE_X + wallEast.getWidth() * 0.5);
            wallEast.setTranslateY(oy + wallEast.getHeight() * 0.5);
            wallEast.setMaterial(coloredMaterial(Color.PINK));
            content.getChildren().add(wallEast);
        }

        obstacle = new Obstacle(new Vector2f(0, 15*TS), false);
        extendObstacle(-4, 4, true, Tiles.CORNER_NW);
        extendObstacle(0, 3*8, true, Tiles.WALL_V);
        extendObstacle(-4, 4, false, Tiles.CORNER_SE);
        extendObstacle(-8, 0, false, Tiles.WALL_H);
        extendObstacle(-4, 4, true, Tiles.CORNER_NW);
        extendObstacle(4, 4, true, Tiles.CORNER_SW);
        extendObstacle(4*8, 0, true, Tiles.WALL_H);
        extendObstacle(4, -4, true, Tiles.CORNER_SE);
        extendObstacle(-4, -4, true, Tiles.CORNER_NE);
        extendObstacle(-8, 0, true, Tiles.WALL_H);
        extendObstacle(-4, -4, false, Tiles.CORNER_SW);
        extendObstacle(0, -3*8, false, Tiles.WALL_V);
        extendObstacle(-4, -4, true, Tiles.CORNER_NE);

        Vector2f currentPoint = obstacle.startPoint();
        Vector2f endPoint = null;
        for (int i = 0; i < obstacle.numSegments(); ++i) {
            Obstacle.Segment segment = obstacle.segment(i);
            Obstacle.Segment prevSegment = i > 0 ? obstacle.segment(i - 1) : obstacle.segment(obstacle.numSegments() - 1);
            Obstacle.Segment nextSegment = i < obstacle.numSegments() - 1 ? obstacle.segment(i + 1) : obstacle.segment(0);
            Logger.info("Current point: {} segment: {}", currentPoint, segment);
            endPoint = currentPoint.plus(segment.vector());
            if (segment.isVerticalLine()) {
                addVerticalWall(segment, currentPoint, endPoint);
            }
            else if (segment.isHorizontalLine()) {
                addHorizontalWall(segment, currentPoint, endPoint);
            }
            else if (segment.isRoundedNWCorner()) {
                endPoint = currentPoint.plus(segment.ccw() ? v2f(-HTS, HTS) : v2f(HTS, -HTS));
                addCorner(segment, prevSegment, nextSegment, currentPoint, endPoint);
            }
            else if (segment.isRoundedSWCorner()) {
                endPoint = currentPoint.plus(segment.ccw() ? v2f(HTS, HTS) : v2f(-HTS, -HTS));
                addCorner(segment, prevSegment, nextSegment, currentPoint, endPoint);
            }
            else if (segment.isRoundedSECorner()) {
                endPoint = currentPoint.plus(segment.ccw() ? v2f(HTS, -HTS) : v2f(-HTS, HTS));
                addCorner(segment, prevSegment, nextSegment, currentPoint, endPoint);
            }
            else if (segment.isRoundedNECorner()) {
                endPoint = currentPoint.plus(segment.ccw() ? v2f(-HTS, -HTS) : v2f(HTS, HTS));
                addCorner(segment, prevSegment, nextSegment, currentPoint, endPoint);
            }
            currentPoint = endPoint;
        }
        Logger.info("End point: {}", endPoint);
    }

    private void addCorner(Obstacle.Segment segment, Obstacle.Segment prevSegment, Obstacle.Segment nextSegment, Vector2f start, Vector2f end) {
        if (segment.isNWCorner() && prevSegment.isNECorner()) {
            Vector2f center = start.plus(0, HTS);
            addCornerCylinder(center);
        }
        else if (segment.isSECorner() && nextSegment.isNECorner()) {
            Vector2f center = start.plus(0, -HTS);
            addCornerCylinder(center);
        }
        else if (segment.isNWCorner() && nextSegment.isSWCorner()) {
            Vector2f center = start.plus(0, HTS);
            addCornerCylinder(center);
        }
    }

    private void addCornerCylinder(Vector2f center) {
        Cylinder cyl = new Cylinder();
        //cyl.setDrawMode(DrawMode.LINE);
        cyl.setMaterial(WALL_MATERIAL);
        cyl.setRadius(TS-1.5);
        cyl.setHeight(WALL_HEIGHT);
        cyl.setRotationAxis(Rotate.X_AXIS);
        cyl.setRotate(90);
        cyl.setTranslateX(center.x());
        cyl.setTranslateY(center.y());
        content.getChildren().add(cyl);
    }

    private void addVerticalWall(Obstacle.Segment segment, Vector2f startPoint, Vector2f endPoint) {
        Vector2f middlePoint = startPoint.plus(endPoint).scaled(0.5f);
        Box wall = new Box();
        wall.setMaterial(WALL_MATERIAL);
        wall.setTranslateX(middlePoint.x());
        wall.setTranslateY(middlePoint.y());
        wall.setWidth(WALL_WIDTH);
        wall.setHeight(endPoint.minus(startPoint).length());
        wall.setDepth(WALL_HEIGHT);
        content.getChildren().add(wall);
    }

    private void addHorizontalWall(Obstacle.Segment segment, Vector2f startPoint, Vector2f endPoint) {
        Vector2f middlePoint = startPoint.plus(endPoint).scaled(0.5f);
        Box wall = new Box();
        wall.setMaterial(WALL_MATERIAL);
        wall.setTranslateX(middlePoint.x());
        wall.setTranslateY(middlePoint.y());
        wall.setWidth(endPoint.minus(startPoint).length());
        wall.setHeight(WALL_WIDTH);
        wall.setDepth(WALL_HEIGHT);
        content.getChildren().add(wall);
    }

    private void extendObstacle(float dx, float dy, boolean ccw, byte content) {
        obstacle.addSegment(new Vector2f(dx, dy), ccw, content);
    }

    private void createBallCyclingAroundCylinder() {
        int ballRadius = 20;
        Sphere ball = new Sphere(ballRadius);
        double ox = 0, oy = 0, oz = 0;
        ball.setTranslateX(ox);
        ball.setTranslateY(oy);
        ball.setTranslateZ(oz);
        ball.setMaterial(coloredMaterial(Color.BLUE));
        content.getChildren().add(ball);

        cycling = new Transition() {
            {
                setCycleDuration(Duration.seconds(3));
                setInterpolator(Interpolator.LINEAR);
                setCycleCount(Animation.INDEFINITE);
            }
            @Override
            protected void interpolate(double t) {
                double x = Math.cos(TAU * t) * (50 - ballRadius);
                double y = Math.sin(TAU * t) * (50 - ballRadius);
                double z = Math.sin(TAU * t) * 100;
                ball.setTranslateX(ox + x);
                ball.setTranslateY(oy + y);
                ball.setTranslateZ(oz + z);
                Logger.debug("x={0.00} y={0.00} z={0.00}", x, y, z);
            }
        };
        cycling.playFromStart();

        Cylinder cylinder = new Cylinder(5, 300);
        cylinder.setRotationAxis(Rotate.X_AXIS);
        cylinder.setRotate(90);
        content.getChildren().add(cylinder);

    }


}