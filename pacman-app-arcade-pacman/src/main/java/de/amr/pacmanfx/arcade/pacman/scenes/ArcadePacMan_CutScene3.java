/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimContainer;
import de.amr.pacmanfx.game.GameVariant;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.CutScene;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

/**
 * Third cut scene in Arcade Pac-Man game:<br>
 * Red ghost in damaged dress chases Pac-Man from right to left over the screen.
 * After they have disappeared, a naked, shaking ghost runs from left over the screen.
 */
public class ArcadePacMan_CutScene3 extends CutScene {

    public static final int TICK_ANIMATION_START      = 120;
    public static final int TICK_BLINKY_RUNNING_NAKED = TICK_ANIMATION_START + 400;
    public static final int TICK_ANIMATION_ENDS       = TICK_ANIMATION_START + 700;

    public int sceneTick;

    private Pac pacMan;
    private Ghost blinky;

    public ArcadePacMan_CutScene3(GameAppContext app) {
        super(app);
        setComp(CanvasRenderingComp.class, new CanvasRenderingComp());
    }

    @Override
    public void onActivate() {
        final GameVariant variant = app().gameVariants().currentGameVariant();
        final GameVariantRenderConfig renderConfig = variant.uiConfig().renderConfig();
        final SpriteAnimContainer animContainer    = variant.spriteAnimContainer();
        final ActorSpriteAnimController animController  = variant.config().systems().actorSpriteAnimController();
        final var actorFactory = ArcadePacMan_ActorFactory.instance();

        pacMan = actorFactory.createPacMan();
        pacMan.spriteAnim().setSpriteAnimations(renderConfig.createPacAnimations(animContainer));

        blinky = renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.RED_GHOST_SHADOW);

        entities().clear();
        entities().addAll(pacMan, blinky);

        sceneTick = -1;
    }

    @Override
    public void onTick(GameContext game) {
        ++sceneTick;
        if (sceneTick < TICK_ANIMATION_START) {
            return;
        }

        final GameSystems sys = game.variant().systems();

        switch (sceneTick) {
            case TICK_ANIMATION_START      -> startAnimation(sys);
            case TICK_BLINKY_RUNNING_NAKED -> startBlinkyRunningNaked(sys);
            case TICK_ANIMATION_ENDS       -> game().state().triggerTimeout();
        }

        sys.motor().move(pacMan);
        sys.motor().move(blinky);
    }

    private void startAnimation(GameSystems systems) {
        soundManager().play(PacManGameSoundID.INTERMISSION_3, 2);
        startBlinkyChasingPacMan(systems);
    }

    private void startBlinkyRunningNaked(GameSystems systems) {
        systems.navigator().placeAtTile(blinky, -1, 20);
        systems.navigator().setMoveDir(blinky, Direction.RIGHT);
        systems.navigator().setWishDir(blinky, Direction.RIGHT);

        systems.actorSpriteAnimController().select(blinky, CommonSpriteAnimationID.BLINKY_NAKED);
        systems.actorSpriteAnimController().playSelected(blinky);
    }

    private void startBlinkyChasingPacMan(GameSystems systems) {
        pacMan.show();

        systems.navigator().placeAtTile(pacMan, 29, 20);
        systems.navigator().setMoveDir(pacMan, Direction.LEFT);
        systems.navigator().setMoveDirSpeed(pacMan, 1.25f);

        systems.actorSpriteAnimController().select(pacMan, CommonSpriteAnimationID.PAC_MOUTH_MOVING);
        systems.actorSpriteAnimController().playSelected(pacMan);

        blinky.show();

        systems.navigator().placeAtTile(blinky, 35, 20);
        systems.navigator().setMoveDir(blinky, Direction.LEFT);
        systems.navigator().setWishDir(blinky, Direction.LEFT);
        systems.navigator().setMoveDirSpeed(blinky, 1.25f);

        systems.actorSpriteAnimController().select(blinky, CommonSpriteAnimationID.BLINKY_PATCHED);
        systems.actorSpriteAnimController().playSelected(blinky);
    }
}