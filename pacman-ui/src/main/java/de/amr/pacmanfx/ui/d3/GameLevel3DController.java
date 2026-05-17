package de.amr.pacmanfx.ui.d3;

import de.amr.basics.fsm.State;
import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.ui.d3.animation.WallColorFlashingAnimation;
import de.amr.pacmanfx.ui.d3.entities.Maze3D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.uilib.model3D.ghost.Ghost3D;
import de.amr.pacmanfx.uilib.model3D.pac.Pac3D;
import de.amr.pacmanfx.uilib.model3D.world.Bonus3D;
import de.amr.pacmanfx.uilib.model3D.world.Energizer3D;

import static de.amr.pacmanfx.model.CanonicalGameState.*;
import static java.util.Objects.requireNonNull;

public class GameLevel3DController {

    public void handleGameStateChange(GameLevel3D level3D, GameStateChangeEvent event) {
        requireNonNull(event);
        final State<Game> gameState = event.newState();
        if (STARTING_GAME_OR_LEVEL.matches(gameState)) {
            onStartingGame(level3D);
        } else if (LEVEL_PLAYING.matches(gameState)) {
            onHuntingStart(level3D);
        } else if (PACMAN_DYING.matches(gameState)) {
            onPacManDying(level3D, gameState);
        } else if (EATING_GHOST.matches(gameState)) {
            onEatingGhost(level3D);
        } else if (LEVEL_COMPLETE.matches(gameState)) {
            onLevelComplete(level3D);
        } else if (GAME_OVER.matches(gameState)) {
            onGameOver(level3D);
        }
    }

    /**
     * Handles bonus activation: updates 3D representation and plays sound.
     */
    public void onBonusActivated(GameLevel3D level3D, BonusActivatedEvent e) {
        level3D.addOrReplaceBonus3D(e.bonus());
        level3D.optSoundEffects().ifPresent(GameSoundEffects::playBonusActiveSound);
    }

    /**
     * Handles bonus eaten: shows eaten animation and plays sound.
     */
    public void onBonusEaten(GameLevel3D level3D, BonusEatenEvent ignored) {
        level3D.entities().first(Bonus3D.class).ifPresent(Bonus3D::lookEaten);
        level3D.optSoundEffects().ifPresent(GameSoundEffects::playBonusEatenSound);
    }

    /**
     * Handles bonus expiration: expires 3D bonus and plays sound.
     */
    public void onBonusExpired(GameLevel3D level3D, BonusExpiredEvent ignoredEvent) {
        level3D.entities().first(Bonus3D.class).ifPresent(Bonus3D::lookExpired);
        level3D.optSoundEffects().ifPresent(GameSoundEffects::playBonusExpiredSound);
    }

    /**
     * Shows the "READY!" message when the game continues.
     */
    public void onGameContinues(GameLevel3D level3D, GameContinuedEvent ignoredEvent) {
        level3D.messageManager().showMessage(MessageManager3D.MessageType.READY);
    }

    /**
     * Plays game ready sound unless in demo or test mode.
     */
    public void onGameStarts(GameLevel3D level3D, GameStartedEvent event) {
        final Game game = event.game();
        final State<Game> state = game.flow().state();
        final boolean silent = game.isDemoLevelRunning() || state instanceof TestState;
        if (!silent) {
            level3D.optSoundEffects().ifPresent(GameSoundEffects::playGameReadySound);
        }
        level3D.messageManager().showMessage(MessageManager3D.MessageType.READY);
    }

    /**
     * Plays sound when a ghost is eaten.
     */
    public void onGhostEaten(GameLevel3D level3D, GhostEatenEvent ignoredEvent) {
        level3D.optSoundEffects().ifPresent(GameSoundEffects::playGhostEatenSound);
    }

    /**
     * Handles Pac eating food: updates 3D food and plays munching sound (with rate limiting).
     */
    public void onPacEatsFood(GameLevel3D level3D, PacEatsFoodEvent gameEvent, long tick) {
        if (gameEvent.allPellets()) {
            level3D.removeAllPellets3D();
        } else {
            level3D.eatFoodAtTile(gameEvent.pac().tile());
            level3D.optSoundEffects().ifPresent(sfx -> sfx.playPacMunchingSound(tick));
        }
    }

    /**
     * Handles Pac gaining power: stops siren, starts power animation/sound.
     */
    public void onPacGetsPower(GameLevel3D level3D, PacGetsPowerEvent event) {
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

    /**
     * Handles Pac losing power: stops power animation/sound.
     */
    public void onPacLostPower(GameLevel3D level3D, PacLostPowerEvent ignoredEvent) {
        level3D.entities().unique(Pac3D.class).setPowerMode(false);
        level3D.animationRegistry().animation(GameLevel3D.AnimationID.WALL_COLOR_FLASHING).stop();
        level3D.optSoundEffects().ifPresent(GameSoundEffects::stopPacPowerSound);
    }

    public void onSpecialScoreReached(GameLevel3D level3D, SpecialScoreEvent ignoredEvent) {
        level3D.optSoundEffects().ifPresent(GameSoundEffects::playExtraLifeSound);
    }

    // Private state-specific handlers

    private void onStartingGame(GameLevel3D level3D) {
        level3D.entities().all().forEach(entity -> entity.init(level3D.level()));
    }

    private void onHuntingStart(GameLevel3D level3D) {
        level3D.entities().unique(Pac3D.class).init(level3D.level());
        level3D.entities().all(Ghost3D.class).forEach(ghost3D -> ghost3D.init(level3D.level()));
        level3D.entities().all(Energizer3D.class).forEach(Energizer3D::startPumping);
        level3D.animationRegistry().animation(GameLevel3D.AnimationID.ENERGIZER_PARTICLES_MOVEMENT).playFromStart();
        level3D.animationRegistry().animation(GameLevel3D.AnimationID.GHOST_LIGHT).playFromStart();
    }

    private void onPacManDying(GameLevel3D level3D, State<Game> gameState) {
        final Pac3D pac3D = level3D.entities().unique(Pac3D.class);
        gameState.lock();

        level3D.optSoundEffects().ifPresent(GameSoundEffects::stopAll);

        // Do not stop all animations!
        level3D.animationRegistry().animation(GameLevel3D.AnimationID.GHOST_LIGHT).stop();
        level3D.animationRegistry().animation(GameLevel3D.AnimationID.WALL_COLOR_FLASHING, WallColorFlashingAnimation.class).stop();
        level3D.entities().all(Ghost3D.class).forEach(Ghost3D::stopAllAnimations);
        level3D.entities().first(Bonus3D.class).ifPresent(Bonus3D::lookExpired);

        level3D.createPacDyingAnimationSequence(pac3D, gameState).play();
    }

    private void onEatingGhost(GameLevel3D level3D) {
        final GameLevel level = level3D.level();
        //TODO rethink this mess
        level.game().simulationStep().ghostsKilled.forEach(killedGhost -> {
            final int killIndex = level.energizerVictims().indexOf(killedGhost);
            final Ghost3D ghost3D = level3D.ghost3D(killedGhost.personality()).orElseThrow();
            final double riseHeight = (killIndex + 1) * 12;
            level3D.replaceGhost3DByAnimatedNumberBox(ghost3D, riseHeight, level3D.uiConfig().killedGhostPointsImage(killIndex));
        });
    }

    private void onLevelComplete(GameLevel3D level3D) {
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

    private void onGameOver(GameLevel3D level3D) {
        level3D.animationRegistry().animation(GameLevel3D.AnimationID.GHOST_LIGHT).stop();
        level3D.cleanupFoodAndParticles();
        level3D.entities().first(Bonus3D.class).ifPresent(Bonus3D::lookExpired);
        level3D.optSoundEffects().ifPresent(GameSoundEffects::playGameOverSound);
    }
}
