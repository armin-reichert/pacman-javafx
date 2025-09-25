/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.DefaultGameVariants;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.MapSelectionMode;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.ArcadePalette;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.widgets.OptionMenu;
import de.amr.pacmanfx.uilib.widgets.OptionMenuEntry;
import de.amr.pacmanfx.uilib.widgets.OptionMenuStyle;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.tinylog.Logger;

import java.util.List;
import java.util.stream.IntStream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameModel.createPac;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.*;
import static de.amr.pacmanfx.ui.api.GameUI_Properties.PROPERTY_3D_ENABLED;
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
        private Pac pac;
        private List<Ghost> ghosts;
        private ActorRenderer actorRenderer;
        private boolean chasingGhosts;
        private boolean running;

        ChaseAnimation(Canvas canvas) {
            ctx = canvas.getGraphicsContext2D();
        }

        void setGameConfig(GameUI_Config uiConfig) {
            createActors(uiConfig);
            actorRenderer = uiConfig.createActorRenderer(ctx.getCanvas());
            reset();
            start();
        }

        private void createActors(GameUI_Config uiConfig) {
            pac = createPac();
            pac.setAnimations(uiConfig.createPacAnimations());
            pac.playAnimation(ANIM_PAC_MUNCHING);
            ghosts = List.of(
                uiConfig.createGhost(RED_GHOST_SHADOW),
                uiConfig.createGhost(PINK_GHOST_SPEEDY),
                uiConfig.createGhost(CYAN_GHOST_BASHFUL),
                uiConfig.createGhost(ORANGE_GHOST_POKEY)
            );
            ghosts.forEach(Ghost::playAnimation);
        }

        void start() {
            running = true;
        }

        void reset() {
            chasingGhosts = false;
            if (pac != null) {
                pac.setX(42 * TS);
                pac.setMoveDir(Direction.LEFT);
                pac.setWishDir(Direction.LEFT);
                pac.setSpeed(1.0f);
                pac.setVisible(true);
            }
            if (ghosts != null) {
                for (Ghost ghost : ghosts) {
                    ghost.setX(46 * TS + ghost.personality() * 18);
                    ghost.setMoveDir(Direction.LEFT);
                    ghost.setWishDir(Direction.LEFT);
                    ghost.setSpeed(1.05f);
                    ghost.setVisible(true);
                }
            }
        }

        void update() {
            if (!running) return;

            if (ghosts.get(3).x() < -4 * TS && !chasingGhosts) {
                chasingGhosts = true;
                pac.setMoveDir(pac.moveDir().opposite());
                pac.setWishDir(pac.moveDir().opposite());
                pac.setX(-36 * TS);
                for (Ghost ghost : ghosts) {
                    ghost.setVisible(true);
                    ghost.setX(pac.x() + 22 * TS + ghost.personality() * 18);
                    ghost.setMoveDir(ghost.moveDir().opposite());
                    ghost.setWishDir(ghost.moveDir().opposite());
                    ghost.setSpeed(0.58f);
                    ghost.playAnimation(ANIM_GHOST_FRIGHTENED);
                }
            }
            else if (pac.x() > 56 * TS && chasingGhosts) {
                chasingGhosts = false;
                pac.setMoveDir(Direction.LEFT);
                pac.setWishDir(Direction.LEFT);
                pac.setX(42 * TS);
                for (Ghost ghost : ghosts) {
                    ghost.setVisible(true);
                    ghost.setMoveDir(Direction.LEFT);
                    ghost.setWishDir(Direction.LEFT);
                    ghost.setX(46 * TS + ghost.personality() * 2 * TS);
                    ghost.setSpeed(1.05f);
                    ghost.playAnimation(ANIM_GHOST_NORMAL);
                }
            }
            else if (chasingGhosts) {
                IntStream.range(0, 4).forEach(i -> {
                    if (Math.abs(pac.x() - ghosts.get(i).x()) < 1) {
                        ghosts.get(i).selectAnimationFrame(ANIM_GHOST_NUMBER, i);
                        if (i > 0) {
                            ghosts.get(i-1).setVisible(false);
                        }
                    }
                });
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
            actorRenderer.setScaling(scaling);
            ghosts.forEach(actorRenderer::drawActor);
            actorRenderer.drawActor(pac);
            ctx.restore();
        }

    }

    // Entries

    private final OptionMenuEntry<String> entryGameVariant = new OptionMenuEntry<>("GAME VARIANT",
        "PACMAN_XXL", "MS_PACMAN_XXL") {

        @Override
        protected void onValueChanged(int index) {
            String gameVariant = selectedValue();
            if (DefaultGameVariants.PACMAN_XXL.name().equals(gameVariant)) {
                Logger.info("Loading assets for game variant {}", gameVariant);
                ui.config(gameVariant).loadAssets();
            }
            else if (DefaultGameVariants.MS_PACMAN_XXL.name().equals(gameVariant)) {
                Logger.info("Loading assets for game variant {}", gameVariant);
                ui.config(gameVariant).loadAssets();
            }
            chaseAnimation.setGameConfig(ui.config(gameVariant));
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
            PROPERTY_3D_ENABLED.set(state.play3D);
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

    private final GameUI ui;
    private final GameContext gameContext;
    private final MenuState state = new MenuState();
    private final ChaseAnimation chaseAnimation;

    public PacManXXL_Common_StartPageMenu(GameUI ui) {
        super(42, 36, 6, 20);

        this.ui = requireNonNull(ui);
        this.gameContext = requireNonNull(ui.gameContext());

        state.gameVariant = "PACMAN_XXL";
        state.play3D = false;
        state.cutScenesEnabled = true;
        state.mapOrder = MapSelectionMode.CUSTOM_MAPS_FIRST;

        var style = new OptionMenuStyle(
            ui.assets().font("font.pacfontgood", 32),
            ui.assets().arcadeFont(8),
            Ufx.colorWithOpacity(DEFAULT_OPTION_MENU_STYLE.backgroundFill(), 0.5),
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
        final Game game = gameContext.gameController().game(state.gameVariant);
        final var mapSelector = (PacManXXL_Common_MapSelector) game.mapSelector();
        mapSelector.loadAllMaps();
        final boolean customMapsExist = !mapSelector.customMaps().isEmpty();

        state.play3D = PROPERTY_3D_ENABLED.get();
        state.cutScenesEnabled = game.cutScenesEnabled();
        state.mapOrder = mapSelector.selectionMode();
        logState();

        entryGameVariant.selectValue(state.gameVariant);
        entryPlay3D.selectValue(state.play3D);
        entryCutScenesEnabled.selectValue(state.cutScenesEnabled);
        entryMapOrder.selectValue(state.mapOrder);
        entryMapOrder.setEnabled(customMapsExist);

        chaseAnimation.setGameConfig(ui.config(state.gameVariant));
        chaseAnimation.reset();
    }

    @Override
    protected void handleKeyPress(KeyEvent e) {
        if (nude(KeyCode.E).match(e)) {
            ui.showEditorView();
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
        Game game = gameContext.gameController().game(state.gameVariant);
        game.setCutScenesEnabled(state.cutScenesEnabled);
        var mapSelector = (PacManXXL_Common_MapSelector) game.mapSelector();
        mapSelector.setSelectionMode(state.mapOrder);
        mapSelector.loadAllMaps();
        ui.selectGameVariant(state.gameVariant);
    }
}