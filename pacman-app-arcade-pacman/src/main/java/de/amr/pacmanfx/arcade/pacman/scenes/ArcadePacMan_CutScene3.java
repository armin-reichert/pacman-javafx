/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.CommonAnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.component.spriteanim.SpriteAnim;
import de.amr.pacmanfx.core.model.systems.common.MovementSystem;
import de.amr.pacmanfx.core.model.systems.common.WorldMovementSystem;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

import static de.amr.pacmanfx.core.model.GameModel.RED_GHOST_SHADOW;

/**
 * Third cut scene in Arcade Pac-Man game:<br>
 * Red ghost in damaged dress chases Pac-Man from right to left over the screen.
 * After they have disappeared, a naked, shaking ghost runs from left over the screen.
 */
public class ArcadePacMan_CutScene3 extends AbstractGameScene2D {

    public static final int TICK_ANIMATION_START      = 120;
    public static final int TICK_BLINKY_RUNNING_NAKED = TICK_ANIMATION_START + 400;
    public static final int TICK_ANIMATION_ENDS       = TICK_ANIMATION_START + 700;

    public int sceneTick;
    public Pac pacMan;
    public Ghost blinky;

    public ArcadePacMan_CutScene3(GameAppContext appContext) {
        super(appContext);
    }

    @Override
    public void onActivate() {
        final GameVariantRenderConfig renderConfig = appContext().variants().currentVariant().config().renderConfig();
        final SpriteAnimationContainer container = appContext().ui().sprites().animations();
        pacMan = ArcadePacMan_ActorFactory.createPacMan();
        pacMan.assertComponent(SpriteAnim.class).setAnimations(renderConfig.createPacAnimations(container));
        blinky = renderConfig.createAnimatedGhost(gameContext(), container, RED_GHOST_SHADOW);
        sceneTick = -1;
    }

    @Override
    public void onTick(GameContext gameContext) {
        ++sceneTick;
        if (sceneTick < TICK_ANIMATION_START) {
            return;
        }

        final MovementSystem motor = gameContext.systems().motor;
        final WorldMovementSystem navigator = gameContext.systems().navigator;

        switch (sceneTick) {
            case TICK_ANIMATION_START      -> startAnimation(navigator);
            case TICK_BLINKY_RUNNING_NAKED -> startBlinkyRunningNaked(navigator);
            case TICK_ANIMATION_ENDS       -> gameState().triggerTimeout();
        }
        motor.moveAccelerated(pacMan);
        motor.moveAccelerated(blinky);
    }

    private void startAnimation(WorldMovementSystem navigator) {
        appContext().ui().sounds().play(PacManGameSoundID.INTERMISSION_3, 2);
        startBlinkyChasingPacMan(navigator);
    }

    private void startBlinkyRunningNaked(WorldMovementSystem navigator) {
        navigator.placeAtTile(blinky, -1, 20);
        navigator.setMoveDir(blinky, Direction.RIGHT);
        navigator.setWishDir(blinky, Direction.RIGHT);
        blinky.assertComponent(SpriteAnim.class).animations().select(CommonAnimationID.BLINKY_NAKED);
        blinky.assertComponent(SpriteAnim.class).animations().playSelected();
    }

    private void startBlinkyChasingPacMan(WorldMovementSystem navigator) {
        navigator.placeAtTile(pacMan, 29, 20);
        navigator.setMoveDir(pacMan, Direction.LEFT);
        navigator.setSpeed(pacMan, 1.25f);

        pacMan.visibility().show();

        pacMan.assertComponent(SpriteAnim.class).animations().select(CommonAnimationID.PAC_MUNCHING);
        pacMan.assertComponent(SpriteAnim.class).animations().playSelected();

        navigator.placeAtTile(blinky, 35, 20);
        navigator.setMoveDir(blinky, Direction.LEFT);
        navigator.setWishDir(blinky, Direction.LEFT);
        navigator.setSpeed(blinky, 1.25f);

        blinky.visibility().show();

        blinky.assertComponent(SpriteAnim.class).animations().select(CommonAnimationID.BLINKY_PATCHED);
        blinky.assertComponent(SpriteAnim.class).animations().playSelected();
    }
}