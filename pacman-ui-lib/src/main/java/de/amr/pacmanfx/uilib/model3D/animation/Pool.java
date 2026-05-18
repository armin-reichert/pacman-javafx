/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.animation;

import de.amr.basics.Disposable;
import org.tinylog.Logger;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Pool<T> implements Disposable {

    static final int AMOUNT_ADDED_WHEN_EMPTY = 100;

    private final Queue<T> entryQ = new ArrayDeque<>();
    private final Supplier<T> constructor;
    private final Consumer<T> destructor;

    public Pool(int size, Supplier<T> entryConstructor, Consumer<T> entryDestructor) {
        this.constructor = entryConstructor;
        this.destructor = entryDestructor;
        addNewEntries(size);
    }

    public T getEntry() {
        if (entryQ.isEmpty()) {
            addNewEntries(AMOUNT_ADDED_WHEN_EMPTY);
        }
        return entryQ.poll();
    }

    private void addNewEntries(int count) {
        for (int i = 0; i < count; ++i) {
            entryQ.add(constructor.get());
        }
        Logger.info("Particle pool increased by {}! Pool size={}", count, entryQ.size());
    }

    public void recycleEntry(T entry) {
        destructor.accept(entry);
        entryQ.offer(entry);
    }

    public void dispose() {
        for (T entry : entryQ) {
            if (entry instanceof Disposable disposable) {
                disposable.dispose();
            }
        }
        entryQ.clear();
    }
}
