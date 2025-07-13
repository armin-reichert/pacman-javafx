/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.MapSelectionMode;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GameVariant;
import de.amr.pacmanfx.ui.PacManGames_UIConfig;
import de.amr.pacmanfx.ui._2d.ArcadePalette;
import de.amr.pacmanfx.ui._2d.SpriteGameRenderer;
import de.amr.pacmanfx.uilib.widgets.OptionMenu;
import de.amr.pacmanfx.uilib.widgets.OptionMenuEntry;
import de.amr.pacmanfx.uilib.widgets.OptionMenuStyle;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameModel.createGhost;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameModel.createPac;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.*;
import static de.amr.pacmanfx.ui.GameUI.theUI;
import static de.amr.pacmanfx.ui.input.Keyboard.nude;
import static de.amr.pacmanfx.uilib.widgets.OptionMenuStyle.DEFAULT_OPTION_MENU_STYLE;
import static java.util.Objects.requireNonNull;

public class PacManXXL_Common_StartPageMenu extends OptionMenu {

    public static class MenuState {
        String gameVariant;
        boolean play3D;
        boolean cutScenesEnabled;
        MapSelectionMode mapOrder;
    }

    private static class ChaseAnimation {
        private final GraphicsContext ctx;
        private final Pac pac;
        private final Ghost[] ghosts;
        private SpriteGameRenderer renderer;
        private boolean chasingGhosts;
        private boolean running;

        ChaseAnimation(Canvas canvas) {
            ctx = canvas.getGraphicsContext2D();
            pac = createPac(null);
            ghosts = new Ghost[] {
                createGhost(null, RED_GHOST_SHADOW),
                createGhost(null, PINK_GHOST_SPEEDY),
                createGhost(null, CYAN_GHOST_BASHFUL),
                createGhost(null, ORANGE_GHOST_POKEY)
            };
        }

        void start() {
            running = true;
        }

        void reset() {
            chasingGhosts = false;

            pac.setX(42 * TS);
            pac.setMoveAndWishDir(Direction.LEFT);
            pac.setSpeed(1.0f);
            pac.setVisible(true);

            for (Ghost ghost : ghosts) {
                ghost.setX(46 * TS + ghost.personality() * 18);
                ghost.setMoveAndWishDir(Direction.LEFT);
                ghost.setSpeed(1.05f);
                ghost.setVisible(true);
            }
        }

        void update() {
            if (!running) return;

            if (ghosts[3].x() < -4 * TS && !chasingGhosts) {
                chasingGhosts = true;
                pac.setMoveAndWishDir(pac.moveDir().opposite());
                pac.setX(-36 * TS);
                for (Ghost ghost : ghosts) {
                    ghost.setVisible(true);
                    ghost.setX(pac.x() + 22 * TS + ghost.personality() * 18);
                    ghost.setMoveAndWishDir(ghost.moveDir().opposite());
                    ghost.setSpeed(0.58f);
                    ghost.playAnimation(ANIM_GHOST_FRIGHTENED);
                }
            }
            else if (pac.x() > 56 * TS && chasingGhosts) {
                chasingGhosts = false;
                pac.setMoveAndWishDir(Direction.LEFT);
                pac.setX(42 * TS);
                for (Ghost ghost : ghosts) {
                    ghost.setVisible(true);
                    ghost.setMoveAndWishDir(Direction.LEFT);
                    ghost.setX(46 * TS + ghost.personality() * 2 * TS);
                    ghost.setSpeed(1.05f);
                    ghost.playAnimation(ANIM_GHOST_NORMAL);
                }
            }
            else if (chasingGhosts) {
                for (int i = 0; i < 4; ++i) {
                    if (Math.abs(pac.x() - ghosts[i].x()) < 1) {
                        ghosts[i].selectAnimation(ANIM_GHOST_NUMBER, i);
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

        void draw(float scaling) {
            if (!running) return;
            if (scaling == 0) return;

            ctx.save();
            ctx.translate(0, 23.5 * TS * scaling);
            ctx.setImageSmoothing(false);
            renderer.setScaling(scaling);
            for (Ghost ghost : ghosts) { renderer.drawActor(ghost); }
            renderer.drawActor(pac);
            ctx.restore();
        }

        void setGameVariant(String gameVariant) {
            final PacManGames_UIConfig config = theUI().uiConfig(gameVariant);
            renderer = (SpriteGameRenderer) config.createGameRenderer(ctx.getCanvas());
            pac.setAnimations(config.createPacAnimations(pac));
            pac.playAnimation(ANIM_PAC_MUNCHING);
            for (Ghost ghost : ghosts) {
                if (ghost.animationMap().isEmpty()) {
                    ghost.setAnimations(config.createGhostAnimations(ghost));
                    ghost.playAnimation(ANIM_GHOST_NORMAL);
                }
            }
            start();
        }
    }

    // Entries

    private final OptionMenuEntry<String> entryGameVariant = new OptionMenuEntry<>("GAME VARIANT",
        "PACMAN_XXL", "MS_PACMAN_XXL") {

        @Override
        protected void onValueChanged(int index) {
            String gameVariant = selectedValue();
            if (GameVariant.PACMAN_XXL.name().equals(gameVariant)) {
                Logger.info("Loading assets for game variant {}", gameVariant);
                theUI().uiConfig(gameVariant).storeAssets(theUI().theAssets());
            }
            else if (GameVariant.MS_PACMAN_XXL.name().equals(gameVariant)) {
                Logger.info("Loading assets for game variant {}", gameVariant);
                theUI().uiConfig(gameVariant).storeAssets(theUI().theAssets());
            }
            chaseAnimation.setGameVariant(gameVariant);
            state.gameVariant = gameVariant;
            logState();
        }

        @Override
        public String selectedValueText() {
            return switch (state.gameVariant) {
                case "PACMAN_XXL" -> "PAC-MAN";
                case "MS_PACMAN_XXL" -> "MS.PAC-MAN";
                default -> "";
            };
        }
    };

    private final OptionMenuEntry<Boolean> entryPlay3D = new OptionMenuEntry<>("SCENE DISPLAY", true, false) {

        @Override
        protected void onValueChanged(int index) {
            state.play3D = selectedValue();
            theUI().property3DEnabled().set(state.play3D);
            logState();
        }

        @Override
        public String selectedValueText() {
            return state.play3D ? "3D" : "2D";
        }
    };

    private final OptionMenuEntry<Boolean> entryCutScenesEnabled = new OptionMenuEntry<>("CUTSCENES", true, false) {

        @Override
        protected void onValueChanged(int index) {
            state.cutScenesEnabled = selectedValue();
            logState();
        }

        @Override
        public String selectedValueText() {
            return state.cutScenesEnabled ? "ON" : "OFF";
        }
    };

    private final OptionMenuEntry<MapSelectionMode> entryMapOrder = new OptionMenuEntry<>("MAP ORDER",
        MapSelectionMode.CUSTOM_MAPS_FIRST, MapSelectionMode.ALL_RANDOM, MapSelectionMode.NO_CUSTOM_MAPS)
    {
        @Override
        protected void onValueChanged(int index) {
            if (enabled) {
                state.mapOrder = selectedValue();
            }
            logState();
        }

        @Override
        public String selectedValueText() {
            if (!enabled) {
                return "NO CUSTOM MAPS!";
            }
            return switch (state.mapOrder) {
                case CUSTOM_MAPS_FIRST -> "CUSTOM MAPS FIRST";
                case ALL_RANDOM -> "RANDOM ORDER";
                case NO_CUSTOM_MAPS -> "NO CUSTOM MAPS";
            };
        }
    };

    private final GameContext gameContext;
    private final MenuState state = new MenuState();
    private final ChaseAnimation chaseAnimation;

    public PacManXXL_Common_StartPageMenu(GameContext gameContext) {
        super(42, 36, 6, 20);
        this.gameContext = requireNonNull(gameContext);

        state.gameVariant = "PACMAN_XXL";
        state.play3D = false;
        state.cutScenesEnabled = true;
        state.mapOrder = MapSelectionMode.CUSTOM_MAPS_FIRST;

        var style = new OptionMenuStyle(
            theUI().theAssets().font("font.pacfontgood", 32),
            theUI().theAssets().arcadeFont(8),
            DEFAULT_OPTION_MENU_STYLE.backgroundFill(),
            DEFAULT_OPTION_MENU_STYLE.borderStroke(),
            ArcadePalette.ARCADE_RED,
            ArcadePalette.ARCADE_YELLOW,
            ArcadePalette.ARCADE_WHITE,
            DEFAULT_OPTION_MENU_STYLE.entryValueDisabledFill(),
            ArcadePalette.ARCADE_YELLOW,
            DEFAULT_OPTION_MENU_STYLE.entrySelectedSound(),
            DEFAULT_OPTION_MENU_STYLE.valueSelectedSound()
        );
        setStyle(style);
        setTitle("Pac-Man XXL");
        addEntry(entryGameVariant);
        addEntry(entryPlay3D);
        addEntry(entryCutScenesEnabled);
        addEntry(entryMapOrder);
        setCommandTexts(
            "SELECT OPTIONS WITH UP AND DOWN",
            "PRESS SPACE TO CHANGE VALUE",
            "PRESS E TO OPEN EDITOR",
            "PRESS ENTER TO START"
        );

        chaseAnimation = new ChaseAnimation(canvas);
        chaseAnimation.reset();
    }

    public void syncMenuState() {
        final GameModel game = gameContext.theGameController().game(state.gameVariant);
        final var mapSelector = (PacManXXL_Common_MapSelector) game.mapSelector();
        mapSelector.loadAllMaps();
        final boolean customMapsExist = !mapSelector.customMaps().isEmpty();

        state.play3D = theUI().property3DEnabled().get();
        state.cutScenesEnabled = game.cutScenesEnabled();
        state.mapOrder = mapSelector.mapSelectionMode();
        logState();

        entryGameVariant.selectValue(state.gameVariant);
        entryPlay3D.selectValue(state.play3D);
        entryCutScenesEnabled.selectValue(state.cutScenesEnabled);
        entryMapOrder.selectValue(state.mapOrder);
        entryMapOrder.setEnabled(customMapsExist);

        chaseAnimation.setGameVariant(state.gameVariant);
        chaseAnimation.reset();
    }

    @Override
    protected void handleKeyPress(KeyEvent e) {
        if (nude(KeyCode.E).match(e)) {
            theUI().showEditorView();
        } else if (nude(KeyCode.ENTER).match(e)) {
            startGame();
        } else {
            super.handleKeyPress(e);
        }
    }

    @Override
    protected void updateAnimation() {
        chaseAnimation.update();
    }

    @Override
    public void draw() {
        super.draw();
        chaseAnimation.draw(scalingPy.floatValue());
    }

    public MenuState state() { return state; }

    private void logState() {
        Logger.info("Menu state: gameVariant={} play3D={} cutScenesEnabled={} mapOrder={}",
            state.gameVariant, state.play3D, state.cutScenesEnabled, state.mapOrder);
    }

    private void startGame() {
        GameModel game = gameContext.theGameController().game(state.gameVariant);
        game.setCutScenesEnabled(state.cutScenesEnabled);
        var mapSelector = (PacManXXL_Common_MapSelector) game.mapSelector();
        mapSelector.setMapSelectionMode(state.mapOrder);
        mapSelector.loadAllMaps();
        theUI().selectGameVariant(state.gameVariant);
    }
}