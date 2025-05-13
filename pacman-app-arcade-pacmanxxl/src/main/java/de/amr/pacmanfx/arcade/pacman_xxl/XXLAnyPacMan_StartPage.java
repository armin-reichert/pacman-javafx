/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.ArcadePacMan_GhostAnimations;
import de.amr.pacmanfx.arcade.ArcadePacMan_PacAnimations;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GhostAnimations;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_PacAnimations;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.model.MapSelectionMode;
import de.amr.pacmanfx.model.actors.Animations;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostAnimations;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GameUIConfig;
import de.amr.pacmanfx.ui.StartPage;
import de.amr.pacmanfx.ui._2d.GameRenderer;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.input.Keyboard;
import de.amr.pacmanfx.uilib.widgets.Flyer;
import de.amr.pacmanfx.uilib.widgets.OptionMenu;
import de.amr.pacmanfx.uilib.widgets.OptionMenuEntry;
import de.amr.pacmanfx.uilib.widgets.OptionMenuStyle;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.THE_GAME_CONTROLLER;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.arcade.ArcadePacMan_GameModel.*;
import static de.amr.pacmanfx.ui.PacManGamesEnv.*;
import static java.util.Objects.requireNonNull;

/**
 * Displays an option menu where the game variant to be played and other options can be set.
 */
public class XXLAnyPacMan_StartPage implements StartPage {

    private static class GameOptionMenu extends OptionMenu {

        // State
        private GameVariant gameVariant = GameVariant.PACMAN_XXL;
        private boolean play3D;
        private boolean cutScenesEnabled;
        private MapSelectionMode mapOrder;

        // Animation
        private Pac pac;
        private Ghost[] ghosts = new Ghost[4];
        private GameRenderer renderer;
        private boolean chasingGhosts;

        // Entries
        private final OptionMenuEntry<GameVariant> entryGameVariant = new OptionMenuEntry<>("GAME VARIANT",
            GameVariant.PACMAN_XXL, GameVariant.MS_PACMAN_XXL) {

            @Override
            protected void onValueChanged(int index) {
                gameVariant = selectedValue();
                setActorAnimationVariant(gameVariant);
                logMenuState();
            }

            @Override
            public String selectedValueText() {
                return switch (gameVariant) {
                    case PACMAN_XXL -> "PAC-MAN";
                    case MS_PACMAN_XXL -> "MS.PAC-MAN";
                    default -> "";
                };
            }
        };

        private final OptionMenuEntry<Boolean> entryPlay3D = new OptionMenuEntry<>("SCENE DISPLAY", true, false) {

            @Override
            protected void onValueChanged(int index) {
                play3D = selectedValue();
                PY_3D_ENABLED.set(play3D);
                logMenuState();
            }

            @Override
            public String selectedValueText() {
                return play3D ? "3D" : "2D";
            }
        };

        private final OptionMenuEntry<Boolean> entryCutScenesEnabled = new OptionMenuEntry<>("CUTSCENES", true, false) {

            @Override
            protected void onValueChanged(int index) {
                cutScenesEnabled = selectedValue();
                logMenuState();
            }

            @Override
            public String selectedValueText() {
                return cutScenesEnabled ? "ON" : "OFF";
            }
        };

        private final OptionMenuEntry<MapSelectionMode> entryMapOrder = new OptionMenuEntry<>("MAP ORDER",
            MapSelectionMode.CUSTOM_MAPS_FIRST, MapSelectionMode.ALL_RANDOM) {

            @Override
            protected void onValueChanged(int index) {
                if (enabled) {
                    mapOrder = selectedValue();
                }
                logMenuState();
            }

            @Override
            public String selectedValueText() {
                if (!enabled) {
                    return "NO CUSTOM MAPS!";
                }
                return switch (mapOrder) {
                    case CUSTOM_MAPS_FIRST -> "CUSTOM MAPS FIRST";
                    case ALL_RANDOM -> "RANDOM ORDER";
                    case NO_CUSTOM_MAPS -> "NO CUSTOM MAPS";
                };
            }
        };


        GameOptionMenu() {
            super(42, 36, 6, 20);

            addEntry(entryGameVariant);
            addEntry(entryPlay3D);
            addEntry(entryCutScenesEnabled);
            addEntry(entryMapOrder);

            ResourceManager rm = this::getClass;
            setStyle(new OptionMenuStyle(
                rm.loadFont("fonts/emulogic.ttf", 3 * TS),
                rm.loadFont("fonts/emulogic.ttf", TS),
                OptionMenu.DEFAULT_STYLE.backgroundFill(),
                OptionMenu.DEFAULT_STYLE.borderStroke(),
                Color.RED, // DEFAULT_STYLE.titleTextFill(),
                OptionMenu.DEFAULT_STYLE.entryTextFill(),
                OptionMenu.DEFAULT_STYLE.entryValueFill(),
                OptionMenu.DEFAULT_STYLE.entryValueDisabledFill(),
                OptionMenu.DEFAULT_STYLE.hintTextFill(),
                OptionMenu.DEFAULT_STYLE.entrySelectedSound(),
                OptionMenu.DEFAULT_STYLE.valueSelectedSound()
            ));
            setTitle("Pac-Man XXL");
            setCommandTexts(
                "SELECT OPTIONS WITH UP AND DOWN",
                "PRESS SPACE TO CHANGE VALUE",
                "PRESS E TO OPEN EDITOR",
                "PRESS ENTER TO START"
            );

            resetActorAnimation();
            setActorAnimationVariant(GameVariant.PACMAN_XXL);
        }

        private void initState() {
            GameModel game = THE_GAME_CONTROLLER.game(gameVariant);
            game.mapSelector().loadAllMaps(game);
            boolean customMapsExist = !game.mapSelector().customMaps().isEmpty();

            entryGameVariant.selectValue(gameVariant);
            setPlay3D(PY_3D_ENABLED.get());
            setCutScenesEnabled(game.cutScenesEnabledProperty().get());
            setMapOrder(game.mapSelector().mapSelectionMode(), customMapsExist);

            resetActorAnimation();
            setActorAnimationVariant(gameVariant);

            logMenuState();
            Logger.info("Option menu initialized");
        }

        private void setPlay3D(boolean play3D) {
            this.play3D = play3D;
            entryPlay3D.selectValue(play3D);
        }

        private void setCutScenesEnabled(boolean cutScenesEnabled) {
            this.cutScenesEnabled = cutScenesEnabled;
            entryCutScenesEnabled.selectValue(cutScenesEnabled);
        }

        private void setMapOrder(MapSelectionMode mapOrder, boolean customMapsExist) {
            this.mapOrder = requireNonNull(mapOrder);
            entryMapOrder.selectValue(mapOrder);
            entryMapOrder.setEnabled(customMapsExist);
        }

        private void logMenuState() {
            Logger.info("gameVariant={} play3D={} cutScenesEnabled={} mapOrder={}", gameVariant, play3D, cutScenesEnabled, mapOrder);
        }

        @Override
        protected void handleKeyPress(KeyEvent e) {
            if (Keyboard.naked(KeyCode.E).match(e)) {
                theUI().showEditorView();
            } else if (Keyboard.naked(KeyCode.ENTER).match(e)) {
                startGame();
            } else {
                super.handleKeyPress(e);
            }
        }

        @Override
        protected void animationStep() {
            updateActorAnimation();
            draw();
        }

        @Override
        public void draw() {
            super.draw();
            drawActorAnimation(scalingProperty().get());
        }

        private void startGame() {
            if (gameVariant == GameVariant.PACMAN_XXL || gameVariant == GameVariant.MS_PACMAN_XXL) {
                GameModel game = THE_GAME_CONTROLLER.game(gameVariant);
                game.cutScenesEnabledProperty().set(cutScenesEnabled);
                game.mapSelector().setMapSelectionMode(mapOrder);
                game.mapSelector().loadAllMaps(game);
                theUI().selectGameVariant(gameVariant);
            } else {
                Logger.error("Game variant {} is not allowed for XXL game", gameVariant);
            }
        }

        private void setActorAnimationVariant(GameVariant gameVariant) {
            GameUIConfig config = theUIConfig().configuration(gameVariant);
            renderer = config.createRenderer(canvas);

            switch (gameVariant) {
                case PACMAN_XXL -> pac.setAnimations(new ArcadePacMan_PacAnimations(config.spriteSheet()));
                case MS_PACMAN_XXL -> pac.setAnimations(new ArcadeMsPacMan_PacAnimations(config.spriteSheet()));
            }
            pac.selectAnimation(Animations.ANY_PAC_MUNCHING);
            pac.startAnimation();

            for (Ghost ghost : ghosts) {
                if (ghost.animations().isEmpty()) {
                    switch (gameVariant) {
                        case PACMAN_XXL ->
                            ghost.setAnimations(new ArcadePacMan_GhostAnimations(config.spriteSheet(), ghost.personality()));
                        case MS_PACMAN_XXL ->
                            ghost.setAnimations(new ArcadeMsPacMan_GhostAnimations(config.spriteSheet(), ghost.personality()));
                    }
                    ghost.selectAnimation(GhostAnimations.ANIM_GHOST_NORMAL);
                    ghost.startAnimation();
                }
            }
        }

        private void resetActorAnimation() {
            chasingGhosts = false;

            pac = createPac();
            pac.setPosX(42 * TS);
            pac.setMoveAndWishDir(Direction.LEFT);
            pac.setSpeed(1.0f);
            pac.setVisible(true);

            ghosts = new Ghost[] {
                createRedGhost(),
                createPinkGhost(),
                createCyanGhost(),
                createOrangeGhost()
            };
            for (Ghost ghost : ghosts) {
                ghost.setPosX(46 * TS + ghost.personality() * 2 * TS);
                ghost.setMoveAndWishDir(Direction.LEFT);
                ghost.setSpeed(1.05f);
                ghost.setVisible(true);
            }
        }

        private void updateActorAnimation() {
            if (ghosts[3].posX() < -4 * TS && !chasingGhosts) {
                chasingGhosts = true;
                pac.setMoveAndWishDir(pac.moveDir().opposite());
                pac.setPosX(-36 * TS);
                for (Ghost ghost : ghosts) {
                    ghost.setVisible(true);
                    ghost.setPosX(pac.posX() + 22 * TS + ghost.personality() * 2.5f * TS);
                    ghost.setMoveAndWishDir(ghost.moveDir().opposite());
                    ghost.setSpeed(0.58f);
                    ghost.selectAnimation(GhostAnimations.ANIM_GHOST_FRIGHTENED);
                    ghost.startAnimation();
                }
            }
            else if (pac.posX() > 56 * TS && chasingGhosts) {
                chasingGhosts = false;
                pac.setMoveAndWishDir(Direction.LEFT);
                pac.setPosX(42 * TS);
                for (Ghost ghost : ghosts) {
                    ghost.setVisible(true);
                    ghost.setMoveAndWishDir(Direction.LEFT);
                    ghost.setPosX(46 * TS + ghost.personality() * 2 * TS);
                    ghost.setSpeed(1.05f);
                    ghost.selectAnimation(GhostAnimations.ANIM_GHOST_NORMAL);
                    ghost.startAnimation();
                }
            }
            else if (chasingGhosts) {
                for (int i = 0; i < 4; ++i) {
                    if (Math.abs(pac.posX() - ghosts[i].posX()) < 1) {
                        ghosts[i].selectAnimation(GhostAnimations.ANIM_GHOST_NUMBER, i);
                        if (i > 0) {
                            ghosts[i-1].setVisible(false);
                        }
                    }
                }
            }
            pac.move();
            for (Ghost ghost : ghosts) {
                ghost.move();
            }
        }

        private void drawActorAnimation(float scaling) {
            g.save();
            g.translate(0, 23.5 * TS * scaling);
            g.setImageSmoothing(false);
            renderer.setScaling(scaling);
            for (Ghost ghost : ghosts) { renderer.drawActor(ghost); }
            renderer.drawActor(pac);
            g.restore();
        }
    }

    private final StackPane root = new StackPane();
    private final GameOptionMenu menu;

    public XXLAnyPacMan_StartPage() {
        ResourceManager rm = this::getClass;
        Flyer flyer = new Flyer(rm.loadImage("graphics/pacman_xxl_startpage.jpg"));
        flyer.setPageLayout(0, Flyer.LayoutMode.FILL);
        flyer.selectPage(0);

        menu = new GameOptionMenu();
        // scale menu to take 90% of start page height
        menu.scalingProperty().bind(root.heightProperty().multiply(0.9).divide(menu.numTilesY() * TS));
        menu.soundEnabledProperty().bind(theSound().mutedProperty().not());

        root.setBackground(Background.fill(Color.BLACK));
        root.getChildren().addAll(flyer, menu.root());
    }

    @Override
    public void onEnter() {
        menu.initState();
        menu.startAnimation();
    }

    @Override
    public GameVariant currentGameVariant() {
        return menu.gameVariant;
    }

    @Override
    public Region layoutRoot() {
        return root;
    }

    @Override
    public void requestFocus() {
        menu.requestFocus();
    }

    @Override
    public void onExit() {
        menu.stopAnimation();
    }
}