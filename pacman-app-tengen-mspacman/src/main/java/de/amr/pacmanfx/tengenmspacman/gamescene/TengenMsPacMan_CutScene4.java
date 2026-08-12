/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
import de.amr.pacmanfx.core.ecs.systems.SpriteAnimSystem;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.Clapperboard;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacManSoundID;
import de.amr.pacmanfx.tengenmspacman.entities.clapperboard.TengenMsPacMan_ClapperboardStateSystem;
import de.amr.pacmanfx.tengenmspacman.flow.TengenMsPacMan_GameState;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.input.JoypadButton;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;
import de.amr.pacmanfx.ui.sound.SoundID;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static de.amr.basics.math.RandomNumberSupport.randomInt;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantConfig.*;

public class TengenMsPacMan_CutScene4 extends AbstractGameScene2D {

    public static final int TICK_CLAP = 2;
    public static final int TICK_EXPIRES = 1512;
    public static final Set<Integer> TICKS_JUNIOR_SPAWNED = Set.of(
        904, 968, 1032, 1096, 1160, 1224, 1288, 1352
    );

    private static final int LEFT_BORDER = TS;
    private static final int RIGHT_BORDER = TS * (NES_SCREEN_TILES.x() - 2);

    private static final int LOWER_LANE = TS * 21; // TODO not sure

    private Pac pacMan;
    private Pac msPacMan;
    private List<Pac> juniors;
    private List<Long> juniorSpawnTicks;
    private Clapperboard clapperboard;

    public TengenMsPacMan_CutScene4(GameAppContext app) {
        super(app);
        unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        unscaledHeightProperty().set(NES_SCREEN_HEIGHT);
    }

    public Pac pacMan() {
        return pacMan;
    }

    public Pac msPacMan() {
        return msPacMan;
    }

    public List<Pac> juniors() {
        return Collections.unmodifiableList(juniors);
    }

    public Clapperboard clapperboard() {
        return clapperboard;
    }

    @Override
    public void onActivate() {
        // Quit cut scene when "START" button on "joypad" is pressed
        final GameAction quitAction = app().commonActions().gameFlowActions().actionLetGameStateExpire();
        actionBindings().bindActionToKeyCombination(quitAction, input().joypad().keyForButton(JoypadButton.START));

        createActors();
    }
    
    @Override
    public void onDeactivate() {
        stopMusic();
    }

    @Override
    public void onTick(GameContext game) {
        final long tick = gameState().timer().tickCount();
        if (tick == TICK_CLAP) {
            clapperboard.show();
            TengenMsPacMan_ClapperboardStateSystem.startFlapAnimation(clapperboard);
            playMusic();
        }
        else if (tick == TICK_EXPIRES) {
            game.session().gameFlow().enterState(game, TengenMsPacMan_GameState.GAME_PREPARATION.state());

        }
        TengenMsPacMan_ClapperboardStateSystem.update(clapperboard);
        playCutScene(game, tick);
    }

    private void playMusic() {
        app().ui().sounds().play(PacManGameSoundID.INTERMISSION_4);
    }

    private void stopMusic() {
        app().ui().sounds().stop(PacManGameSoundID.INTERMISSION_4);
    }

    private void createActors() {
        final var actorFactory = TengenMsPacMan_ActorFactory.instance();
        final GameVariantRenderConfig renderConfig = app().gameVariants().currentGameVariant().config().renderConfig();
        final SpriteAnimationContainer spriteAnimations = app().ui().sprites().animations();

        clapperboard = new Clapperboard("4", "THE END");
        clapperboard.pos().set(tilesPx(3), tilesPx(10));

        msPacMan = actorFactory.createMsPacMan();
        msPacMan.spriteAnim().setAnimations(renderConfig.createPacAnimations(spriteAnimations));

        pacMan = actorFactory.createPacMan();
        pacMan.spriteAnim().setAnimations(renderConfig.createPacAnimations(spriteAnimations));

        juniors = new ArrayList<>();
        juniorSpawnTicks = new ArrayList<>();
    }

    private void letActorsMove(GameContext game, long tick) {
        final MovementSystem motor = game.systems().motor();
        motor.move(pacMan);
        motor.move(msPacMan);
        for (int i = 0; i < juniors.size(); ++i) {
            updateJunior(game, tick, i);
        }
    }

    private void playCutScene(GameContext game, long tick) {
        final WorldNavigationSystem navigator = game.systems().worldNavigator();
        final SpriteAnimSystem animSystem = game.systems().spriteAnim();

        letActorsMove(game, tick);

        if (tick == 130) {
            pacMan.pos().set(LEFT_BORDER, LOWER_LANE);
            pacMan.show();

            navigator.setMoveDir(pacMan, Direction.RIGHT);
            navigator.setSpeed(pacMan, 1f);

            animSystem.select(pacMan, TengenMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING);
            animSystem.playSelected(pacMan);

            msPacMan.pos().set(RIGHT_BORDER, LOWER_LANE);
            msPacMan.show();

            navigator.setMoveDir(msPacMan, Direction.LEFT);
            navigator.setSpeed(msPacMan, 1f);

            animSystem.select(msPacMan, CommonSpriteAnimationID.PAC_MUNCHING);
            animSystem.playSelected(msPacMan);
        }
        else if (tick == 230) {
            navigator.setSpeed(pacMan, 0);
            animSystem.stopSelected(pacMan);
            animSystem.resetSelected(pacMan);

            navigator.setSpeed(msPacMan, 0);
            animSystem.stopSelected(msPacMan);
            animSystem.resetSelected(msPacMan);
        }
        else if (tick == 400) {
            animSystem.select(pacMan, TengenMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING);
            animSystem.playSelected(pacMan);

            animSystem.select(msPacMan, CommonSpriteAnimationID.PAC_MUNCHING);
            animSystem.playSelected(msPacMan);
        }
        else if (tick == 520) {
            animSystem.select(pacMan, TengenMsPacMan_AnimationID.MR_PAC_MAN_WAVING_HAND);
            animSystem.select(msPacMan, TengenMsPacMan_AnimationID.MS_PAC_MAN_WAVING_HAND);
        }
        else if (tick == 527) {
            animSystem.playSelected(pacMan);
            animSystem.playSelected(msPacMan);
        }
        else if (tick == 648) {
            animSystem.select(pacMan, TengenMsPacMan_AnimationID.MR_PAC_MAN_TURNING_AWAY);
            animSystem.playSelected(pacMan);

            animSystem.select(msPacMan, TengenMsPacMan_AnimationID.MS_PAC_MAN_TURNING_AWAY);
            animSystem.playSelected(msPacMan);
        }
        else if (tick == 650) {
            navigator.setSpeed(pacMan, 1.5f); // TODO not sure
            navigator.setMoveDir(pacMan, Direction.UP);
            navigator.setSpeed(msPacMan, 1.5f); // TODO not sure
            navigator.setMoveDir(msPacMan, Direction.UP);
        }
        else if (tick == 720) {
            pacMan.hide();
            msPacMan.hide();
        }
        else if (TICKS_JUNIOR_SPAWNED.contains((int) tick)) {
            spawnJunior(game, tick);
        }
        else if (tick == 1500) {
            optSoundEffects().ifPresent(GameSoundEffects::stopAll);
        }
    }

    private void spawnJunior(GameContext game, long tick) {
        final var factory = TengenMsPacMan_ActorFactory.instance();
        final GameVariantRenderConfig renderConfig = app().gameVariants().currentGameVariant().config().renderConfig();
        final WorldNavigationSystem navigator = game.systems().worldNavigator();
        final SpriteAnimSystem animSystem = game.systems().spriteAnim();
        final SpriteAnimationContainer spriteAnimations = app().ui().sprites().animations();

        final Pac junior = factory.createPacMan();
        double randomX = 8 * TS + (8 * TS) * Math.random();
        junior.pos().set((float) randomX, unscaledHeight() - 4 * TS);
        junior.show();

        navigator.setMoveDir(junior, Direction.UP);
        navigator.setSpeed(junior, 2);

        animSystem.setAnimations(junior, renderConfig.createPacAnimations(spriteAnimations));
        animSystem.select(junior, TengenMsPacMan_AnimationID.ANIM_JUNIOR);

        juniors.add(junior);
        juniorSpawnTicks.add(tick);

        playRandomJuniorSound();

        Logger.info("Junior spawned at tick {}", tick);
    }

    private void playRandomJuniorSound() {
        final SoundID soundID = switch (randomInt(1, 3)) {
            case 1 -> TengenMsPacManSoundID.INTERMISSION_4_JUNIOR_1;
            case 2 -> TengenMsPacManSoundID.INTERMISSION_4_JUNIOR_2;
            default -> throw new IllegalArgumentException();
        };
        app().ui().sounds().playLoop(soundID);
    }

    private void updateJunior(GameContext game, long tick, int index) {
        final MovementSystem motor = game.systems().motor();
        final WorldNavigationSystem navigator = game.systems().worldNavigator();

        Pac junior = juniors.get(index);
        long creationTime = juniorSpawnTicks.get(index);
        long lifeTime = tick - creationTime;
        if (lifeTime> 0 && lifeTime % 10 == 0) {
            computeNewMoveDir(navigator, junior);
        }
        motor.move(junior);
        if (junior.pos().x() > unscaledWidth()) {
            junior.pos().setX(0);
        }
        if (junior.pos().x() < 0) {
            junior.pos().setX(unscaledWidth());
        }
    }

    private void computeNewMoveDir(WorldNavigationSystem navigator, Pac junior) {
        Direction oldMoveDir = junior.worldNavigation().moveDir();
        List<Direction> possibleDirs = new ArrayList<>(List.of(Direction.values()));
        possibleDirs.remove(oldMoveDir.opposite());
        List<Direction> dirsByMinCenterDist = possibleDirs.stream().sorted(
            (d1, d2) -> compareBySmallestDistToSceneCenter(junior, d1, d2)).toList();
        Direction bestDir = dirsByMinCenterDist.getFirst();
        Direction randomDir = possibleDirs.get(randomInt(0, possibleDirs.size()));
        boolean chooseBestDir = randomInt(0, 100) < 40;
        navigator.setMoveDir(junior, chooseBestDir ? bestDir : randomDir);
    }

    private int compareBySmallestDistToSceneCenter(Pac junior, Direction dir1, Direction dir2) {
        Vector2i tile = WorldNavigationSystem.computeTile(junior);
        Vector2f pos1 = tile.plus(dir1.vector()).scaled(TS).toVector2f();
        Vector2f pos2 = tile.plus(dir2.vector()).scaled(TS).toVector2f();
        Vector2f center = new Vector2f(0.5f * unscaledWidth(), 0.5f * unscaledHeight());
        return Double.compare(pos1.euclideanDist(center), pos2.euclideanDist(center));
    }
}