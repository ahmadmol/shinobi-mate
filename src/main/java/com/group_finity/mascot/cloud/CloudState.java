package com.group_finity.mascot.cloud;

import java.util.List;
import java.util.random.RandomGenerator;

/** Per-instance state and monotonic clock; no Swing or timer threads required. */
public final class CloudState {
    public enum Visibility { HIDDEN, HOVER_VISIBLE, PINNED }
    public static final long MESSAGE_INTERVAL_MILLIS = 6_000;
    public static final long FADE_MILLIS = 180;
    private final List<String> messages;
    private final RandomGenerator random;
    private Visibility visibility = Visibility.HIDDEN;
    private int index = -1;
    private long nextMessageAt;
    private long lastUpdate;
    private float opacity;
    private boolean changing;
    private boolean suppressHover;
    private long intervalMillis=MESSAGE_INTERVAL_MILLIS;
    public void setIntervalMillis(long value,long now) {
        value=Math.max(1000,Math.min(120000,value));
        if(value!=intervalMillis){intervalMillis=value;nextMessageAt=now+value;}
    }

    public CloudState(List<String> messages, RandomGenerator random) {
        this.messages = List.copyOf(messages);
        this.random = random;
    }

    public boolean isEnabled() { return messages.size() >= 2; }
    public Visibility visibility() { return visibility; }
    public float opacity() { return opacity; }
    public String message() { return index < 0 ? "" : messages.get(index); }

    public void mouseEnter(long now) {
        if (isEnabled() && !suppressHover && visibility == Visibility.HIDDEN) show(Visibility.HOVER_VISIBLE, now);
    }

    public void mouseExit() {
        suppressHover = false;
        if (visibility == Visibility.HOVER_VISIBLE) visibility = Visibility.HIDDEN;
    }

    public void click(long now) {
        if (!isEnabled()) return;
        if (visibility == Visibility.PINNED) {
            visibility = Visibility.HIDDEN;
            suppressHover = true; // Remain hidden until a real exit/re-enter.
            changing = false;
        } else {
            if (visibility == Visibility.HIDDEN) show(Visibility.PINNED, now);
            visibility = Visibility.PINNED;
        }
    }

    private void show(Visibility target, long now) {
        visibility = target;
        if (index < 0) selectNext();
        nextMessageAt = now + intervalMillis;
        lastUpdate = now;
        changing = false;
    }

    private void selectNext() {
        int next = random.nextInt(messages.size() - (index < 0 ? 0 : 1));
        if (index >= 0 && next >= index) next++;
        index = next;
    }

    public void update(long now) {
        float step = Math.max(0, now - lastUpdate) / (float) FADE_MILLIS;
        lastUpdate = now;
        if (visibility != Visibility.HIDDEN && now >= nextMessageAt) changing = true;
        if (visibility == Visibility.HIDDEN || changing) {
            opacity = Math.max(0, opacity - step);
            if (changing && opacity == 0 && visibility != Visibility.HIDDEN) {
                selectNext();
                changing = false;
                // Preserve the six-second cadence, including the short transition.
                nextMessageAt = now + intervalMillis - FADE_MILLIS;
            }
        } else {
            opacity = Math.min(1, opacity + step);
        }
    }
}
