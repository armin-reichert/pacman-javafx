/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3;

import de.amr.basics.fsm.State;
import de.amr.basics.math.RandomNumberSupport;
import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUIConstants;
import de.amr.pacmanfx.ui.d3.animation.PlaySceneFadeInAnimation;
import de.amr.pacmanfx.ui.d3.animation.WallColorFlashingAnimation;
import de.amr.pacmanfx.ui.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.d3.entities.Maze3D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.uilib.model3D.ghost.Ghost3D;
import de.amr.pacmanfx.uilib.model3D.pac.Pac3D;
import de.amr.pacmanfx.uilib.model3D.world.Bonus3D;
import de.amr.pacmanfx.uilib.model3D.world.Energizer3D;
import javafx.util.Duration;

import java.util.List;

import static de.amr.pacmanfx.model.CanonicalGameState.*;
import static java.util.Objects.requireNonNull;

public class PlayScene3DGameEventHandler extends GameScene.DefaultGameEventHandler {

    public PlayScene3DGameEventHandler(PlayScene3D playScene3D) {
        super(playScene3D);
    }

    @Override
    public PlayScene3D gameScene() {
        return (PlayScene3D) super.gameScene();
    }

    // TODO: remove (only used by GameState.TESTING_CUT_SCENES)
    @Override
    public void onGenericChange(GenericChangeEvent event) {
        ui().forceGameSceneUpdate();
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent event) {
        requireNonNull(event);
        final State<Game> newGameState = event.newState();

        //TODO ugly
        if (newGameState instanceof TestState) {
            handleTestState();
        }
        else if (STARTING_GAME_OR_LEVEL.matches(newGameState)) {
            onStartingGame();
        }
        else if (LEVEL_PLAYING.matches(newGameState)) {
            onHuntingStart();
        }
        else if (PACMAN_DYING.matches(newGameState)) {
            onPacManDying(newGameState);
        }
        else if (EATING_GHOST.matches(newGameState)) {
            onEatingGhost();
        }
        else if (LEVEL_COMPLETE.matches(newGameState)) {
            onLevelComplete();
        }
        else if (GAME_OVER.matches(newGameState)) {
            onGameOver();
        }
    }

    @Override
    public void onBonusActivated(BonusActivatedEvent e) {
        assertLevel3D().addOrReplaceBonus3D(e.bonus());
        assertLevel3D().optSoundEffects().ifPresent(GameSoundEffects::playBonusActiveSound);
    }

    @Override
    public void onBonusEaten(BonusEatenEvent ignored) {
        assertLevel3D().entities().first(Bonus3D.class).ifPresent(Bonus3D::lookEaten);
        assertLevel3D().optSoundEffects().ifPresent(GameSoundEffects::playBonusEatenSound);
    }

    @Override
    public void onBonusExpired(BonusExpiredEvent ignoredEvent) {
        assertLevel3D().entities().first(Bonus3D.class).ifPresent(Bonus3D::lookExpired);
        assertLevel3D().optSoundEffects().ifPresent(GameSoundEffects::playBonusExpiredSound);
    }

    @Override
    public void onGameContinued(GameContinuedEvent ignoredEvent) {
        assertLevel3D().messageManager().showMessage(MessageManager3D.MessageType.READY);
    }

    @Override
    public void onGameStarted(GameStartedEvent event) {
        final Game game = event.game();
        final State<Game> state = game.flow().state();
        final boolean silent = game.isDemoLevelRunning() || state instanceof TestState;
        if (!silent) {
            assertLevel3D().optSoundEffects().ifPresent(GameSoundEffects::playGameReadySound);
        }
        assertLevel3D().messageManager().showMessage(MessageManager3D.MessageType.READY);
    }

    @Override
    public void onGhostEaten(GhostEatenEvent ignoredEvent) {
        assertLevel3D().optSoundEffects().ifPresent(GameSoundEffects::playGhostEatenSound);
    }

    @Override
    public void onLevelCreated(LevelCreatedEvent event) {
        gameScene().replaceGameLevel3D(event.level());
    }

    @Override
    public void onLevelStarted(LevelStartedEvent event) {
        final GameLevel level = event.level();
        final State<Game> state = level.game().flow().state();
        if (state instanceof TestState) {
            gameScene().replaceGameLevel3D(level);
            assertLevel3D().entities().all(Energizer3D.class).forEach(Energizer3D::startPumping);
            assertLevel3D().messageManager().showMessage(MessageManager3D.MessageType.TEST, level.number());
        }
        assertLevel3D().entities().all().forEach(e -> e.init(level));
        gameScene().replaceActionBindings(level);
        new PlaySceneFadeInAnimation(Duration.seconds(3), gameScene()).play();
    }

    @Override
    public void onPacEatsFood(PacEatsFoodEvent gameEvent) {
        if (gameEvent.allPellets()) {
            assertLevel3D().removeAllPellets3D();
        } else {
            final long tick = ui().gameContext().clock().tickCount();
            assertLevel3D().eatFoodAtTile(gameEvent.pac().tile());
            assertLevel3D().optSoundEffects().ifPresent(sfx -> sfx.playPacMunchingSound(tick));
        }
    }

    @Override
    public void onPacGetsPower(PacGetsPowerEvent event) {
        final GameLevel3D level3D = assertLevel3D();
        final Pac3D pac3D = level3D.entities().unique(Pac3D.class);
        final Game game = event.game();
        level3D.optSoundEffects().ifPresent(GameSoundEffects::stopSiren);
        if (!game.isLevelCompleted()) {
            pac3D.setPowerMode(true);
            level3D.animationRegistry()
                .animation(GameLevel3D.AnimationID.WALL_COLOR_FLASHING)
                .playFromStart();
            level3D.optSoundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
        }
    }

    @Override
    public void onPacLostPower(PacLostPowerEvent ignoredEvent) {
        final GameLevel3D level3D = assertLevel3D();
        level3D.entities().unique(Pac3D.class).setPowerMode(false);
        level3D.animationRegistry().animation(GameLevel3D.AnimationID.WALL_COLOR_FLASHING).stop();
        level3D.optSoundEffects().ifPresent(GameSoundEffects::stopPacPowerSound);
    }

    @Override
    public void onSpecialScore(SpecialScoreEvent ignoredEvent) {
        assertLevel3D().optSoundEffects().ifPresent(GameSoundEffects::playExtraLifeSound);
    }

    // Private state-specific handlers

    private void onStartingGame() {
        GameLevel3D level3D = assertLevel3D();
        level3D.entities().all().forEach(entity -> entity.init(level3D.level()));
    }

    private void onHuntingStart() {
        GameLevel3D level3D = assertLevel3D();
        level3D.entities().unique(Pac3D.class).init(level3D.level());
        level3D.entities().all(Ghost3D.class).forEach(ghost3D -> ghost3D.init(level3D.level()));
        level3D.entities().all(Energizer3D.class).forEach(Energizer3D::startPumping);
        level3D.animationRegistry().animation(GameLevel3D.AnimationID.ENERGIZER_PARTICLES_MOVEMENT).playFromStart();
        level3D.animationRegistry().animation(GameLevel3D.AnimationID.GHOST_LIGHT).playFromStart();
    }

    private void onPacManDying(State<Game> dyingState) {
        GameLevel3D level3D = assertLevel3D();
        final Pac3D pac3D = level3D.entities().unique(Pac3D.class);

        level3D.optSoundEffects().ifPresent(GameSoundEffects::stopAll);

        // Do not stop all animations!
        level3D.animationRegistry().animation(GameLevel3D.AnimationID.GHOST_LIGHT).stop();
        level3D.animationRegistry().animation(GameLevel3D.AnimationID.WALL_COLOR_FLASHING, WallColorFlashingAnimation.class).stop();
        level3D.entities().all(Ghost3D.class).forEach(Ghost3D::stopAllAnimations);
        level3D.entities().first(Bonus3D.class).ifPresent(Bonus3D::lookExpired);

        dyingState.lock();
        level3D.createPacDyingAnimationSeq(pac3D, dyingState::expire).play();
    }

    private void onEatingGhost() {
        final GameLevel3D level3D = assertLevel3D();
        final List<Ghost> currentlyKilledGhosts = level3D.level().game().simulationStep().ghostsKilled;
        currentlyKilledGhosts.forEach(ghost -> {
            final int killedIndex = level3D.level().energizerVictims().indexOf(ghost);
            level3D.playHideGhostShowPointsAnimation(ghost.personality(), killedIndex);
        });
    }

    private void onLevelComplete() {
        final GameLevel3D level3D = assertLevel3D();
        final State<Game> gameState = level3D.level().game().flow().state();

        final Maze3D maze3D = level3D.entities().unique(Maze3D.class);
        maze3D.house().hideDoors();

        level3D.optSoundEffects().ifPresent(GameSoundEffects::stopAll);
        level3D.animationRegistry().stopAllAnimations();
        level3D.cleanupFoodAndParticles();
        level3D.entities().first(Bonus3D.class).ifPresent(Bonus3D::lookExpired);
        level3D.messageManager().hideMessage();
        level3D.playLevelEndAnimation(maze3D, gameState);
    }

    private void onGameOver() {
        GameLevel3D level3D = assertLevel3D();
        if (!level3D.level().isDemoLevel() && RandomNumberSupport.chance(0.25)) {
            gameScene().showRandomGameOverMessage();
        }
        level3D.animationRegistry().animation(GameLevel3D.AnimationID.GHOST_LIGHT).stop();
        level3D.cleanupFoodAndParticles();
        level3D.entities().first(Bonus3D.class).ifPresent(Bonus3D::lookExpired);
        level3D.optSoundEffects().ifPresent(GameSoundEffects::playGameOverSound);
    }

    private void handleTestState() {
        gameScene().optGameLevel3D().ifPresent(level3D -> {
            gameScene().replaceGameLevel3D(level3D.level());
            level3D.messageManager().showMessage(MessageManager3D.MessageType.TEST, level3D.level().number());
            GameUIConstants.PROPERTY_3D_PERSPECTIVE_ID.set(PerspectiveID.TOTAL);
        });
    }

    private GameLevel3D assertLevel3D() {
        return gameScene().optGameLevel3D().orElseThrow();
    }
}
