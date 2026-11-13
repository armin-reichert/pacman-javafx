package de.amr.pacmanfx.ui.entities3D.bonus;

import de.amr.basics.math.Vector2f;
import de.amr.basics.ui.animation.AnimationRegistry;
import de.amr.basics.ui.animation.ManagedAnimation;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.ui.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.GameVariantUIConfig;
import javafx.scene.shape.Box;

import static de.amr.basics.Tile.HTS;
import static java.util.Objects.requireNonNull;

public class BonusView3D_System {

    public BonusView3D_System() {}

    public void createBonusView3D(Bonus bonus, GameVariantUIConfig uiConfig, AnimationRegistry animationRegistry) {
        final Bonus3DSettings config = uiConfig.worldSettings().bonus();
        final GameVariantRenderConfig renderConfig = uiConfig.renderConfig();
        final BonusView3D view3D = new BonusView3D(
            renderConfig.createBonusSymbolImage(bonus.data().symbolCode()),
            config.symbolWidth(),
            renderConfig.createBonusPointsImage(bonus.data().symbolCode()),
            config.pointsWidth()
        );
        bonus.setComponent(BonusView3D.class, view3D);

        //TODO move elsewhere
        animationRegistry.register(Bonus3DAnimationID.BONUS_EATEN, view3D.eatenAnimation());
    }

    public void update(Bonus bonus, AnimationRegistry animationRegistry) {
        requireNonNull(bonus);
        requireNonNull(animationRegistry);

        if (!bonus.hasComponent(BonusView3D.class)) return;
        final BonusView3D view3D = bonus.assertComponent(BonusView3D.class);

        updateTransform(view3D, bonus);
        updateAnimation(view3D, bonus, animationRegistry);
    }

    //TODO Check if this can be integrated into update
    public void lookExpired(BonusView3D view3D, AnimationRegistry animationRegistry) {
        final Box shape3D = view3D.box3D();
        shape3D.setVisible(false);
        animationRegistry.optAnimation(Bonus3DAnimationID.BONUS_EDIBLE).ifPresent(ManagedAnimation::stop);
        animationRegistry.optAnimation(Bonus3DAnimationID.BONUS_EATEN).ifPresent(ManagedAnimation::stop);
    }

    private void updateTransform(BonusView3D view3D, Bonus bonus) {
        final Vector2f center = bonus.position().bodyCenter();

        view3D.translate().setX(center.x());
        view3D.translate().setY(center.y());
        view3D.translate().setZ(-HTS);

        view3D.rollingTransform().update(bonus);
    }

    private void updateAnimation(BonusView3D view3D, Bonus bonus, AnimationRegistry animationRegistry) {
        switch (bonus.state().enumValue()) {
            case EDIBLE -> lookEdible(view3D, animationRegistry);
            case EATEN  -> lookEaten(view3D, bonus, animationRegistry);
            case INACTIVE -> {}
        }
    }

    private void lookEdible(BonusView3D view3D, AnimationRegistry animationRegistry) {
        view3D.box3D().setVisible(true);
        view3D.box3D().setWidth(view3D.symbolWidth());
        view3D.box3D().setMaterial(view3D.symbolTexture());
        animationRegistry.optAnimation(Bonus3DAnimationID.BONUS_EDIBLE).ifPresent(ManagedAnimation::playOrContinue);
    }

    private void lookEaten(BonusView3D view3D, Bonus bonus, AnimationRegistry animationRegistry) {
        final Box shape3D = view3D.box3D();
        shape3D.setVisible(true);
        shape3D.setWidth(view3D.pointsWidth());
        shape3D.setMaterial(view3D.pointsTexture());

        // restore neutral orientation
        view3D.rotateX().setAngle(0);
        view3D.rotateY().setAngle(0);

        // Rotate around x-axis
        animationRegistry.requireAnimation(Bonus3DAnimationID.BONUS_EATEN).replay();
    }
}
