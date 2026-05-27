/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.animation;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.Validations;
import org.tinylog.Logger;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class Pool<T> implements Disposable {

    private final int itemIncrease;
    private final Queue<T> items = new ArrayDeque<>();
    private final Supplier<T> itemFactory;
    private final Consumer<T> onItemRecycled;

    public Pool(int initialSize, int itemIncrease, Supplier<T> itemFactory, Consumer<T> onItemRecycled) {
        this.itemIncrease = Validations.requireNonNegativeInt(itemIncrease);
        this.itemFactory = itemFactory;
        this.onItemRecycled = onItemRecycled;
        growBy(initialSize);
    }

    public T provideItem() {
        if (items.isEmpty()) {
            growBy(itemIncrease);
        }
        return items.poll();
    }

    public void recycle(T item) {
        requireNonNull(item);
        onItemRecycled.accept(item);
        items.offer(item);
    }

    public void recycle(Collection<T> itemsToRecycle) {
        requireNonNull(itemsToRecycle);
        for (T item : itemsToRecycle) recycle(item);
    }

    public void dispose() {
        for (T item : items) {
            if (item instanceof Disposable disposable) {
                disposable.dispose();
            }
        }
        items.clear();
    }

    private void growBy(int count) {
        for (int i = 0; i < count; ++i) {
            items.add(itemFactory.get());
        }
        Logger.info("Pool grown by {} items! New size: {}", count, items.size());
    }
}
