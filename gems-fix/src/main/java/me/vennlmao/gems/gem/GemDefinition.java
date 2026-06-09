package me.vennlmao.gems.gem;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;

import java.util.List;

public record GemDefinition(
    String id,
    String displayName,
    List<String> lore,
    Material material,
    int customModelData,
    double damage,
    int cooldownSeconds,
    int manaCost,
    Sound sound,
    float soundPitch,
    Particle particle,
    int particleCount,
    double aoeRadius,
    List<String> abilities,
    GemStats stats
) {
    public record GemStats(
        int burnTicks,
        double healAmount,
        int slowAmplifier,
        int slowDuration,
        int stunDuration,
        int resistanceAmplifier,
        int resistanceDuration,
        double knockbackStrength,
        int speedAmplifier,
        int speedDuration,
        int jumpAmplifier,
        int jumpDuration,
        double launchMultiplier,
        int witherAmplifier,
        int witherDuration,
        int blindDuration,
        int invisDuration,
        int freezeDuration,
        int iceArmorDuration,
        int poisonAmplifier,
        int poisonDuration,
        int weaknessAmplifier,
        int weaknessDuration,
        double lifeStealPercent,
        int strengthAmplifier,
        int strengthDuration,
        double teleportRange,
        double explosionPower
    ) {}
}
