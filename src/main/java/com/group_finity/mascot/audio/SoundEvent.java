package com.group_finity.mascot.audio;

import java.nio.file.Path;
import java.util.List;

/** Immutable pack data. Volume is linear 0..1; cooldown and loop safety duration use milliseconds. */
public record SoundEvent(String id, Category category, List<Path> variations, double volume,
        int cooldown, int maxConcurrent, boolean loop, int priority, int maxDurationMs) {
    public enum Category { MOVEMENT, COMBAT, WEAPON, VFX, CHARACTER, UI }
    public SoundEvent {
        variations = List.copyOf(variations);
        if (!id.matches("[A-Za-z0-9_.-]{1,100}") || variations.isEmpty() || variations.size()>16 ||
                !Double.isFinite(volume) || volume<0 || volume>1 || cooldown<0 || cooldown>10000 ||
                maxConcurrent<1 || maxConcurrent>12 || priority<0 || priority>100 ||
                maxDurationMs<50 || maxDurationMs>5000) throw new IllegalArgumentException("Invalid sound event: "+id);
    }
}
