package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui.input.Keyboard;
import javafx.scene.input.KeyCombination;
import org.tinylog.Logger;

import java.util.*;

import static java.util.Map.entry;
import static java.util.Objects.requireNonNull;

public class ActionBindingMap {

    public static final ActionBindingMap EMPTY_MAP = new ActionBindingMap();

    private Keyboard keyboard;
    private final Map<KeyCombination, GameAction> actionBindings = new HashMap<>();

    public static Map.Entry<GameAction, Set<KeyCombination>> createActionBinding(GameAction action, KeyCombination... combinations) {
        requireNonNull(combinations);
        if (combinations.length == 0) {
            throw new IllegalArgumentException("No key combinations specified for action " + action);
        }
        for (KeyCombination combination: combinations) {
            if (combination == null) {
                throw new IllegalArgumentException("Found null value in key combinations for action " + action);
            }
        }
        var combinationSet = Set.of(combinations);
        return entry(action, combinationSet);
    }

    private ActionBindingMap() {}

    public ActionBindingMap(Keyboard keyboard) {
        this.keyboard = requireNonNull(keyboard);
    }

    public boolean isEmpty() {
        return actionBindings.isEmpty();
    }

    public Set<Map.Entry<KeyCombination, GameAction>> entrySet() {
        return actionBindings.entrySet();
    }

    public void updateActionBindings() {
        for (KeyCombination combination : actionBindings.keySet()) {
            keyboard.setBinding(combination, this);
        }
        Logger.info("Key bindings updated for {}", getClass().getSimpleName());
        actionBindings.entrySet().stream()
            // sort by string representation of key combination
            .sorted(Comparator.comparing(entry -> entry.getKey().toString()))
            .forEach(entry -> Logger.debug("%-20s: %s".formatted(entry.getKey(), entry.getValue().name())));
    }

    public void clearActionBindings() {
        for (KeyCombination combination : actionBindings.keySet()) {
            keyboard.removeBinding(combination, this);
        }
        Logger.info("Key bindings cleared for {}", getClass().getSimpleName());
    }

    public void bindActionToKeyCombination(GameAction action, KeyCombination combination) {
        requireNonNull(action);
        requireNonNull(combination);
        actionBindings.put(combination, action);
    }

    public void bindAction(GameAction gameAction, Map<GameAction, Set<KeyCombination>> bindings) {
        requireNonNull(gameAction);
        requireNonNull(bindings);
        if (bindings.values().stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Found null value in key bindings map");
        }
        if (bindings.containsKey(gameAction)) {
            for (KeyCombination combination : bindings.get(gameAction)) {
                actionBindings.put(combination, gameAction);
            }
        } else {
            Logger.error("No keyboard binding found for game action {}", gameAction);
        }
    }

    public Optional<GameAction> matchingAction() {
        return actionBindings.keySet().stream()
            .filter(keyboard::isMatching)
            .map(actionBindings::get)
            .findFirst();
    }

    public void runMatchingAction(PacManGames_UI ui) {
        matchingAction().ifPresent(action -> GameAction.executeIfEnabled(ui, action));
    }

    public void runMatchingActionOrElse(PacManGames_UI ui, Runnable defaultAction) {
        matchingAction().ifPresentOrElse(action -> GameAction.executeIfEnabled(ui, action), defaultAction);
    }
}
