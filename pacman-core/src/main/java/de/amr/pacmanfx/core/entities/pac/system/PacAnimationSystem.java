package de.amr.pacmanfx.core.entities.pac.system;

import de.amr.basics.Named;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.pac.comp.PacAnimationComp;
import de.amr.pacmanfx.core.entities.pac.comp.PacStateComp;
import de.amr.pacmanfx.core.rules.GameRules;

import static java.util.Objects.requireNonNull;

public class PacAnimationSystem {

    private final ActorSpriteAnimController animController;

    public PacAnimationSystem(ActorSpriteAnimController animController) {
        this.animController = requireNonNull(animController);
    }

    public void update(Pac pac, GameRules rules) {
        requireNonNull(pac);

        final PacStateComp state = pac.state();
        final PacAnimationComp animation = pac.animation();

        if (animation.isLocked()) {
            return;
        }

        switch (state.enumValue()) {
            case SLEEPING -> {
                animation.setAnimationID(rules.initialPacAnimationID());
                animation.setStopped(true);
            }
            case ACTIVE -> {
                animation.setAnimationID(CommonSpriteAnimationID.PAC_MOUTH_MOVING);
                animation.setStopped(!pac.worldNavigation().info().moved);
            }
        }

        animController.select(pac, animation.animationID());
        if (animation.isStopped()) {
            animController.stopSelected(pac);
        } else {
            animController.playSelected(pac);
        }
    }

    public void lockAnimation(Pac pac, boolean locked) {
        requireNonNull(pac);
        if (locked) {
            pac.animation().setLocked(true);
            animController.stopSelected(pac);
        } else {
            pac.animation().setLocked(false);
        }
    }

    public void selectDyingAnimation(Pac pac) {
        requireNonNull(pac);

        final Named id = CommonSpriteAnimationID.PAC_DYING;
        final PacAnimationComp animation = pac.animation();
        animation.setAnimationID(id);
        animation.setLocked(false);
        animation.setStopped(true);

        animController.select(pac, id);
        animController.setAnimationFrame(pac, id, 0);
    }

    public void startDyingAnimation(Pac pac) {
        requireNonNull(pac);

        final PacAnimationComp animation = pac.animation();
        animation.setStopped(false);
    }
}
