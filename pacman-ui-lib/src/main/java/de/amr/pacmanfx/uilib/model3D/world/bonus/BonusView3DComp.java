package de.amr.pacmanfx.uilib.model3D.world.bonus;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.model3D.animation.BonusEatenAnimation3D;
import de.amr.pacmanfx.uilib.model3D.animation.BonusRollingTransform;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

import static java.util.Objects.requireNonNull;

public class BonusView3DComp implements GameEntityComponent, DisposableGraphicsObject {

    private final Group root;
    private final Box shape3D;

    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Translate translate = new Translate();

    private final BonusRollingTransform rollingTransform;
    private final BonusEatenAnimation3D eatenAnimation;

    private PhongMaterial symbolTexture;
    private PhongMaterial pointsTexture;

    private final ImageView symbolImageView;
    private final ImageView pointsImageView;

    private final double symbolWidth;
    private final double pointsWidth;

    public BonusView3DComp(
        Image symbolImage, double symbolWidth,
        Image pointsImage, double pointsWidth)
    {
        root = new Group();

        shape3D = new Box(symbolWidth, 8, 8);

        Group rollingGroup = new Group(shape3D);
        rollingGroup.getTransforms().addAll(rotateX, rotateY);

        root.getChildren().add(rollingGroup);
        root.getTransforms().add(translate);

        this.symbolWidth = symbolWidth;
        symbolImageView = new ImageView(requireNonNull(symbolImage));
        symbolImageView.setPreserveRatio(true);
        symbolImageView.setFitWidth(symbolWidth);
        symbolTexture = new PhongMaterial(Color.GHOSTWHITE, symbolImageView.getImage(), null, null, null);

        this.pointsWidth = pointsWidth;
        pointsImageView = new ImageView(requireNonNull(pointsImage));
        pointsImageView.setPreserveRatio(true);
        pointsImageView.setFitWidth(pointsWidth);
        pointsTexture = new PhongMaterial(Color.GHOSTWHITE, pointsImageView.getImage(), null, null, null);

        rollingTransform = new BonusRollingTransform();

        eatenAnimation = new BonusEatenAnimation3D(shape3D);
    }

    @Override
    public void dispose() {
        cleanupShape3D(shape3D);
        symbolTexture = null;
        pointsTexture = null;
    }

    public Group root() {
        return root;
    }

    public Box box3D() {
        return shape3D;
    }

    public Translate translate() {
        return translate;
    }

    public Rotate rotateX() {
        return rotateX;
    }

    public Rotate rotateY() {
        return rotateY;
    }

    public BonusEatenAnimation3D eatenAnimation() {
        return eatenAnimation;
    }

    public BonusRollingTransform rollingTransform() {
        return rollingTransform;
    }

    public double symbolWidth() {
        return symbolWidth;
    }

    public double pointsWidth() {
        return pointsWidth;
    }

    public ImageView symbolImageView() {
        return symbolImageView;
    }

    public ImageView pointsImageView() {
        return pointsImageView;
    }

    public PhongMaterial symbolTexture() {
        return symbolTexture;
    }

    public PhongMaterial pointsTexture() {
        return pointsTexture;
    }

    @Override
    public void reset() {}
}
