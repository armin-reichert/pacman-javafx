package experiments;

import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.world.Obstacle;
import de.amr.pacmanfx.model.world.ObstacleSegment;
import de.amr.pacmanfx.model.world.TerrainTile;
import de.amr.pacmanfx.uilib.Ufx;
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
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static java.lang.Math.TAU;

public class ExperimentsApp extends Application {

    private static final double WALL_WIDTH = 5;
    private static final double WALL_HEIGHT = 10;
    private static final PhongMaterial WALL_MATERIAL = Ufx.defaultPhongMaterial(Color.BROWN);

    private static final int SCENE_SIZE_X = 800, SCENE_SIZE_Y = 600;
    private static final double MAZE_SIZE_X = 100 * 8, MAZE_SIZE_Y = 160 * 8, MAZE_SIZE_Z = 2 * 8;

    private final Group content = new Group();
    private Transition cycling;
    private Obstacle obstacle;

    @Override
    public void start(Stage primaryStage) {
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
            wallNorth.setMaterial(Ufx.defaultPhongMaterial(Color.RED));
            content.getChildren().add(wallNorth);
        }
        {
            Box wallSouth = new Box(MAZE_SIZE_X, 5, MAZE_SIZE_Z);
            wallSouth.setTranslateX(ox + wallSouth.getWidth() * 0.5);
            wallSouth.setTranslateY(oy + MAZE_SIZE_Y + wallSouth.getHeight() * 0.5);
            wallSouth.setMaterial(Ufx.defaultPhongMaterial(Color.GREEN));
            content.getChildren().add(wallSouth);
        }
        {
            Box wallWest = new Box(5, MAZE_SIZE_Y, MAZE_SIZE_Z);
            wallWest.setTranslateX(ox + wallWest.getWidth() * 0.5);
            wallWest.setTranslateY(oy + wallWest.getHeight() * 0.5);
            wallWest.setMaterial(Ufx.defaultPhongMaterial(Color.BLUE));
            content.getChildren().add(wallWest);
        }
        {
            Box wallEast = new Box(5, MAZE_SIZE_Y, MAZE_SIZE_Z);
            wallEast.setTranslateX(ox + MAZE_SIZE_X + wallEast.getWidth() * 0.5);
            wallEast.setTranslateY(oy + wallEast.getHeight() * 0.5);
            wallEast.setMaterial(Ufx.defaultPhongMaterial(Color.PINK));
            content.getChildren().add(wallEast);
        }

        obstacle = new Obstacle(new Vector2i(0, 15*TS));
        extendObstacle(-4, 4, true, TerrainTile.ARC_NW.$);
        extendObstacle(0, 3*8, true, TerrainTile.WALL_V.$);
        extendObstacle(-4, 4, false, TerrainTile.ARC_SE.$);
        extendObstacle(-8, 0, false, TerrainTile.WALL_H.$);
        extendObstacle(-4, 4, true, TerrainTile.ARC_NW.$);
        extendObstacle(4, 4, true, TerrainTile.ARC_SW.$);
        extendObstacle(4*8, 0, true, TerrainTile.WALL_H.$);
        extendObstacle(4, -4, true, TerrainTile.ARC_SE.$);
        extendObstacle(-4, -4, true, TerrainTile.ARC_NE.$);
        extendObstacle(-8, 0, true, TerrainTile.WALL_H.$);
        extendObstacle(-4, -4, false, TerrainTile.ARC_SW.$);
        extendObstacle(0, -3*8, false, TerrainTile.WALL_V.$);
        extendObstacle(-4, -4, true, TerrainTile.ARC_NE.$);

        Vector2i currentPoint = obstacle.startPoint();
        Vector2i endPoint = null;
        for (int i = 0; i < obstacle.numSegments(); ++i) {
            ObstacleSegment segment = obstacle.segment(i);
            ObstacleSegment prevSegment = i > 0 ? obstacle.segment(i - 1) : obstacle.segment(obstacle.numSegments() - 1);
            ObstacleSegment nextSegment = i < obstacle.numSegments() - 1 ? obstacle.segment(i + 1) : obstacle.segment(0);
            Logger.info("Current point: {} segment: {}", currentPoint, segment);
            endPoint = currentPoint.plus(segment.vector());
            if (segment.isVerticalLine()) {
                addVerticalWall(segment, currentPoint, endPoint);
            }
            else if (segment.isHorizontalLine()) {
                addHorizontalWall(segment, currentPoint, endPoint);
            }
            else if (segment.isRoundedNWCorner()) {
                endPoint = currentPoint.plus(segment.ccw() ? Vector2i.of(-HTS, HTS) : Vector2i.of(HTS, -HTS));
                addCorner(segment, prevSegment, nextSegment, currentPoint, endPoint);
            }
            else if (segment.isRoundedSWCorner()) {
                endPoint = currentPoint.plus(segment.ccw() ? Vector2i.of(HTS, HTS) : Vector2i.of(-HTS, -HTS));
                addCorner(segment, prevSegment, nextSegment, currentPoint, endPoint);
            }
            else if (segment.isRoundedSECorner()) {
                endPoint = currentPoint.plus(segment.ccw() ? Vector2i.of(HTS, -HTS) : Vector2i.of(-HTS, HTS));
                addCorner(segment, prevSegment, nextSegment, currentPoint, endPoint);
            }
            else if (segment.isRoundedNECorner()) {
                endPoint = currentPoint.plus(segment.ccw() ? Vector2i.of(-HTS, -HTS) : Vector2i.of(HTS, HTS));
                addCorner(segment, prevSegment, nextSegment, currentPoint, endPoint);
            }
            currentPoint = endPoint;
        }
        Logger.info("End point: {}", endPoint);
    }

    private void addCorner(ObstacleSegment segment, ObstacleSegment prevSegment, ObstacleSegment nextSegment, Vector2i start, Vector2i end) {
        if (segment.isNWCorner() && prevSegment.isNECorner()) {
            Vector2i center = start.plus(0, HTS);
            addCornerCylinder(center);
        }
        else if (segment.isSECorner() && nextSegment.isNECorner()) {
            Vector2i center = start.plus(0, -HTS);
            addCornerCylinder(center);
        }
        else if (segment.isNWCorner() && nextSegment.isSWCorner()) {
            Vector2i center = start.plus(0, HTS);
            addCornerCylinder(center);
        }
    }

    private void addCornerCylinder(Vector2i center) {
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

    private void addVerticalWall(ObstacleSegment segment, Vector2i startPoint, Vector2i endPoint) {
        Vector2f middlePoint = startPoint.midpoint(endPoint);
        Box wall = new Box();
        wall.setMaterial(WALL_MATERIAL);
        wall.setTranslateX(middlePoint.x());
        wall.setTranslateY(middlePoint.y());
        wall.setWidth(WALL_WIDTH);
        wall.setHeight(endPoint.minus(startPoint).length());
        wall.setDepth(WALL_HEIGHT);
        content.getChildren().add(wall);
    }

    private void addHorizontalWall(ObstacleSegment segment, Vector2i startPoint, Vector2i endPoint) {
        Vector2f middlePoint = startPoint.midpoint(endPoint);
        Box wall = new Box();
        wall.setMaterial(WALL_MATERIAL);
        wall.setTranslateX(middlePoint.x());
        wall.setTranslateY(middlePoint.y());
        wall.setWidth(endPoint.minus(startPoint).length());
        wall.setHeight(WALL_WIDTH);
        wall.setDepth(WALL_HEIGHT);
        content.getChildren().add(wall);
    }

    private void extendObstacle(int dx, int dy, boolean ccw, byte content) {
        obstacle.addSegment(new Vector2i(dx, dy), ccw, content);
    }

    private void createBallCyclingAroundCylinder() {
        int ballRadius = 20;
        Sphere ball = new Sphere(ballRadius);
        double ox = 0, oy = 0, oz = 0;
        ball.setTranslateX(ox);
        ball.setTranslateY(oy);
        ball.setTranslateZ(oz);
        ball.setMaterial(Ufx.defaultPhongMaterial(Color.BLUE));
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