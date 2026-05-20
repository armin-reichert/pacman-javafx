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

    private final int newEntryCount;
    private final Queue<T> entrySet = new ArrayDeque<>();
    private final Supplier<T> entryFactory;
    private final Consumer<T> onEntryRecycle;

    public Pool(int initialSize, int newEntryCount, Supplier<T> entryFactory, Consumer<T> onEntryRecycle) {
        this.newEntryCount = newEntryCount;
        this.entryFactory = entryFactory;
        this.onEntryRecycle = onEntryRecycle;
        addNewEntries(initialSize);
    }

    public T requestEntry() {
        if (entrySet.isEmpty()) {
            addNewEntries(newEntryCount);
        }
        return entrySet.poll();
    }

    public void recycleEntry(T entry) {
        onEntryRecycle.accept(entry);
        entrySet.offer(entry);
    }

    public void dispose() {
        for (T entry : entrySet) {
            if (entry instanceof Disposable disposable) {
                disposable.dispose();
            }
        }
        entrySet.clear();
    }

    private void addNewEntries(int count) {
        for (int i = 0; i < count; ++i) {
            entrySet.add(entryFactory.get());
        }
        Logger.info("Particle pool increased by {}! Pool size={}", count, entrySet.size());
    }
}
