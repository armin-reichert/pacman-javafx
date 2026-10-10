/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine.action;

import de.amr.pacmanfx.core.Validations;

/**
 * Common base class for game actions.
 *
 * @param <C> type of action context
 */
public abstract class GameAction<C> {

    protected final String id;

    protected GameAction(String id) {
        this.id = Validations.requireValidIdentifier(id);
    }

    @Override
    public String toString() {
        return "GameAction{" + "id='" + id + '\'' + '}';
    }

    public final String id() {
        return id;
    }

    public final String resourceBundleKey() { return "action." + id; }

    public abstract void execute(C context);

    public boolean isEnabled(C context) { return true; }
}