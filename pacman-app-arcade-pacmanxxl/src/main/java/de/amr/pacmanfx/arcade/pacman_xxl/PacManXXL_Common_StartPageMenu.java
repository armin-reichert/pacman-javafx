/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.pacman.model.actors.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.MapSelectionMode;
import de.amr.pacmanfx.model.StandardGameVariant;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.api.ArcadePalette;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.widgets.OptionMenu;
import de.amr.pacmanfx.uilib.widgets.OptionMenuEntry;
import de.amr.pacmanfx.uilib.widgets.OptionMenuStyle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.List;
import java.util.stream.IntStream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.ui.api.GameUI.PROPERTY_3D_ENABLED;
import static de.amr.pacmanfx.uilib.widgets.OptionMenuStyle.DEFAULT_OPTION_MENU_STYLE;
import static java.util.Objects.requireNonNull;

public class PacManXXL_Common_StartPageMenu extends OptionMenu {

    public static class MenuState {
        String gameVariant;
        boolean play3D;
        boolean cutScenesEnabled;
        MapSelectionMode mapOrder;
    }

    private class ChaseAnimation {
        private Pac pac;
        private List<Ghost> ghosts;
        private ActorRenderer actorRenderer;
        private boolean ghostsChased;
        private boolean running;

        public void init(GameUI_Config uiConfig) {
            requireNonNull(uiConfig);

            pac = ArcadePacMan_ActorFactory.createPacMan();
            pac.setAnimationManager(uiConfig.createPacAnimations());

            ghosts = List.of(
                uiConfig.createAnimatedGhost(RED_GHOST_SHADOW),
                uiConfig.createAnimatedGhost(PINK_GHOST_SPEEDY),
                uiConfig.createAnimatedGhost(CYAN_GHOST_BASHFUL),
                uiConfig.createAnimatedGhost(ORANGE_GHOST_POKEY)
            );

            actorRenderer = uiConfig.createActorRenderer(renderer.ctx().getCanvas());
            reset();
            start();
        }

        public void start() {
            running = true;
            pac.playAnimation(CommonAnimationID.ANIM_PAC_MUNCHING);
            ghosts.forEach(ghost -> ghost.playAnimation(CommonAnimationID.ANIM_GHOST_NORMAL));
        }

        public void reset() {
            pac.setX(42 * TS);
            pac.setMoveDir(Direction.LEFT);
            pac.setWishDir(Direction.LEFT);
            pac.setSpeed(1.0f);
            pac.setVisible(true);

            for (Ghost ghost : ghosts) {
                ghost.setX(46 * TS + ghost.personality() * 18);
                ghost.setMoveDir(Direction.LEFT);
                ghost.setWishDir(Direction.LEFT);
                ghost.setSpeed(1.05f);
                ghost.setVisible(true);
            }
            ghostsChased = false;
        }

        public void update() {
            if (!running) return;

            if (ghosts.getLast().x() < -4 * TS && !ghostsChased) {
                ghostsChased = true;
                pac.setMoveDir(pac.moveDir().opposite());
                pac.setWishDir(pac.moveDir().opposite());
                pac.setX(-36 * TS);
                for (Ghost ghost : ghosts) {
                    ghost.setVisible(true);
                    ghost.setX(pac.x() + 22 * TS + ghost.personality() * 18);
                    ghost.setMoveDir(ghost.moveDir().opposite());
                    ghost.setWishDir(ghost.moveDir().opposite());
                    ghost.setSpeed(0.58f);
                    ghost.playAnimation(CommonAnimationID.ANIM_GHOST_FRIGHTENED);
                }
            }
            else if (pac.x() > 56 * TS && ghostsChased) {
                ghostsChased = false;
                pac.setMoveDir(Direction.LEFT);
                pac.setWishDir(Direction.LEFT);
                pac.setX(42 * TS);
                for (Ghost ghost : ghosts) {
                    ghost.setVisible(true);
                    ghost.setMoveDir(Direction.LEFT);
                    ghost.setWishDir(Direction.LEFT);
                    ghost.setX(46 * TS + ghost.personality() * 2 * TS);
                    ghost.setSpeed(1.05f);
                    ghost.playAnimation(CommonAnimationID.ANIM_GHOST_NORMAL);
                }
            }
            else if (ghostsChased) {
                IntStream.range(0, 4).forEach(i -> {
                    if (Math.abs(pac.x() - ghosts.get(i).x()) < 1) {
                        ghosts.get(i).selectAnimationAt(CommonAnimationID.ANIM_GHOST_NUMBER, i);
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

        public void draw() {
            actorRenderer.setScaling(scaling());
            actorRenderer.setImageSmoothing(true);
            actorRenderer.ctx().save();
            actorRenderer.ctx().translate(0, TS(23.5f) * scaling());
            ghosts.forEach(actorRenderer::drawActor);
            actorRenderer.drawActor(pac);
            actorRenderer.ctx().restore();
        }
    }

    // Entries

    private final OptionMenuEntry<String> entryGameVariant = new OptionMenuEntry<>("GAME VARIANT",
        "PACMAN_XXL", "MS_PACMAN_XXL") {

        @Override
        protected void onValueChanged(int index) {
            String gameVariant = selectedValue();
            if (StandardGameVariant.PACMAN_XXL.name().equals(gameVariant)) {
                Logger.info("Loading assets for game variant {}", gameVariant);
                ui.config(gameVariant).loadAssets();
            }
            else if (StandardGameVariant.MS_PACMAN_XXL.name().equals(gameVariant)) {
                Logger.info("Loading assets for game variant {}", gameVariant);
                ui.config(gameVariant).loadAssets();
            }
            chaseAnimation.init(ui.config(gameVariant));
            state.gameVariant = gameVariant;
            ui.updateTitle();
            logState();
        }

        @Override
        public String selectedValueText() {
            return switch (state.gameVariant) {
                case "PACMAN_XXL" -> "PAC-MAN XXL";
                case "MS_PACMAN_XXL" -> "MS.PAC-MAN XXL";
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
            selectedGame().setCutScenesEnabled(state.cutScenesEnabled);
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
    private final MenuState state = new MenuState();
    private final ChaseAnimation chaseAnimation = new ChaseAnimation();
    private final Timeline animationTimer;

    public PacManXXL_Common_StartPageMenu(GameUI ui) {
        super(42, 36, 6, 20);

        this.ui = requireNonNull(ui);

        state.gameVariant = "PACMAN_XXL";
        state.play3D = false;
        state.cutScenesEnabled = true;
        state.mapOrder = MapSelectionMode.CUSTOM_MAPS_FIRST;

        var style = new OptionMenuStyle(
            Font.font(ui.assets().font_PacFontGood.getFamily(), 32),
            ui.assets().font_Arcade_8,
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

        final int freq = 60;
        animationTimer = new Timeline(freq,
            new KeyFrame(Duration.seconds(1.0 / freq), e -> {
                draw();
                chaseAnimation.update();
                chaseAnimation.draw();
            }));
        animationTimer.setCycleCount(Animation.INDEFINITE);
    }

    public void startAnimation() {
        animationTimer.play();
    }

    public void stopAnimation() {
        animationTimer.stop();
    }

    @Override
    protected void drawUsageInfo() {
        final GraphicsContext ctx = renderer.ctx();
        final Color normal = style.hintTextFill(), bright = style.entryValueFill();

        ctx.setFont(style.textFont());

        double y = TS(numTilesY() - 8);
        ctx.setFill(normal);
        ctx.fillText("SELECT OPTIONS WITH", TS(6), y);
        ctx.setFill(bright);
        ctx.fillText("UP", TS(26), y);
        ctx.setFill(normal);
        ctx.fillText("AND", TS(29), y);
        ctx.setFill(bright);
        ctx.fillText("DOWN", TS(33), y);

        y += TS(2);
        ctx.setFill(normal);
        ctx.fillText("PRESS", TS(8), y);
        ctx.setFill(bright);
        ctx.fillText("SPACE", TS(14), y);
        ctx.setFill(normal);
        ctx.fillText("TO CHANGE VALUE", TS(20), y);

        y += TS(2);
        ctx.setFill(normal);
        ctx.fillText("PRESS", TS(10), y);
        ctx.setFill(bright);
        ctx.fillText("E", TS(16), y);
        ctx.setFill(normal);
        ctx.fillText("TO OPEN EDITOR", TS(18), y);

        y += TS(2);
        ctx.setFill(normal);
        ctx.fillText("PRESS", TS(11), y);
        ctx.setFill(bright);
        ctx.fillText("ENTER", TS(17), y);
        ctx.setFill(normal);
        ctx.fillText("TO START", TS(23), y);
    }

    public void syncMenuState() {
        final Game game = selectedGame();
        final var mapSelector = (PacManXXL_Common_MapSelector) game.mapSelector();
        mapSelector.loadAllMapPrototypes();
        final boolean customMapsExist = !mapSelector.customMapPrototypes().isEmpty();

        state.play3D = PROPERTY_3D_ENABLED.get();
        state.cutScenesEnabled = game.cutScenesEnabled();
        state.mapOrder = mapSelector.selectionMode();

        entryGameVariant.selectValue(state.gameVariant);
        entryPlay3D.selectValue(state.play3D);
        entryCutScenesEnabled.selectValue(state.cutScenesEnabled);
        entryMapOrder.selectValue(state.mapOrder);
        entryMapOrder.setEnabled(customMapsExist);

        chaseAnimation.init(ui.config(state.gameVariant));
        chaseAnimation.reset();

        logState();
    }

    @Override
    protected void handleKeyPress(KeyEvent e) {
        ui.startPagesView().pauseTimer();
        switch (e.getCode()) {
            case E -> ui.showEditorView();
            case ENTER -> startGame();
            default -> super.handleKeyPress(e);
        }
    }

    public MenuState state() { return state; }

    private void logState() {
        Logger.info("Menu state: gameVariant={} play3D={} cutScenesEnabled={} mapOrder={}",
            state.gameVariant, state.play3D, state.cutScenesEnabled, state.mapOrder);
    }

    private Game selectedGame() {
        return THE_GAME_BOX.game(state.gameVariant);
    }

    private void startGame() {
        final Game game = selectedGame();
        final var mapSelector = (PacManXXL_Common_MapSelector) game.mapSelector();
        mapSelector.setSelectionMode(state.mapOrder);
        mapSelector.loadAllMapPrototypes();
        game.setCutScenesEnabled(state.cutScenesEnabled);
        ui.selectGameVariant(state.gameVariant);
        ui.soundManager().playVoice(SoundID.VOICE_EXPLAIN, 0);
    }
}