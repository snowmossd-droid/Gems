package me.vennlmao.gems.manager;

import me.vennlmao.gems.GemsPlugin;
import me.vennlmao.gems.gem.GemDefinition;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class ConfigManager {

    private final GemsPlugin plugin;
    private FileConfiguration gemsConfig;

    public ConfigManager(GemsPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        File gemsFile = new File(plugin.getDataFolder(), "gems.yml");
        gemsConfig = YamlConfiguration.loadConfiguration(gemsFile);
    }

    public int getManaRegenInterval() {
        return plugin.getConfig().getInt("settings.mana-regen-interval", 40);
    }

    public int getManaRegenAmount() {
        return plugin.getConfig().getInt("settings.mana-regen-amount", 5);
    }

    public int getMaxMana() {
        return plugin.getConfig().getInt("settings.max-mana", 100);
    }

    public int getActionBarInterval() {
        return plugin.getConfig().getInt("settings.action-bar-update-interval", 10);
    }

    public int getComboWindowSeconds() {
        return plugin.getConfig().getInt("settings.combo-window-seconds", 3);
    }

    public Map<String, GemDefinition> loadGems() {
        Map<String, GemDefinition> result = new LinkedHashMap<>();
        ConfigurationSection gemsSection = gemsConfig.getConfigurationSection("gems");
        if (gemsSection == null) return result;

        for (String id : gemsSection.getKeys(false)) {
            ConfigurationSection sec = gemsSection.getConfigurationSection(id);
            if (sec == null) continue;
            try {
                GemDefinition def = parseGem(id, sec);
                result.put(id, def);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load gem '" + id + "': " + e.getMessage());
            }
        }
        return result;
    }

    private GemDefinition parseGem(String id, ConfigurationSection s) {
        String displayName = colorize(s.getString("display-name", id));
        List<String> lore = s.getStringList("lore").stream().map(this::colorize).toList();
        Material material = parseMaterial(s.getString("material", "DIAMOND"));
        int cmd = s.getInt("custom-model-data", 1000);
        double damage = s.getDouble("damage", 50);
        int cooldown = s.getInt("cooldown", 5);
        int manaCost = s.getInt("mana-cost", 15);
        Sound sound = parseSound(s.getString("sound", "ENTITY_PLAYER_ATTACK_SWEEP"));
        float soundPitch = (float) s.getDouble("sound-pitch", 1.0);
        Particle particle = parseParticle(s.getString("particle", "FLAME"));
        int particleCount = s.getInt("particle-count", 40);
        double aoeRadius = s.getDouble("aoe-radius", 5.0);
        List<String> abilities = s.getStringList("abilities");

        GemDefinition.GemStats stats = new GemDefinition.GemStats(
            s.getInt("burn-ticks", 0),
            s.getDouble("heal-amount", 0),
            s.getInt("slow-amplifier", 0),
            s.getInt("slow-duration", 0),
            s.getInt("stun-duration", 0),
            s.getInt("resistance-amplifier", 0),
            s.getInt("resistance-duration", 0),
            s.getDouble("knockback-strength", 1.5),
            s.getInt("speed-amplifier", 0),
            s.getInt("speed-duration", 0),
            s.getInt("jump-amplifier", 0),
            s.getInt("jump-duration", 0),
            s.getDouble("launch-multiplier", 1.0),
            s.getInt("wither-amplifier", 0),
            s.getInt("wither-duration", 0),
            s.getInt("blind-duration", 0),
            s.getInt("invis-duration", 0),
            s.getInt("freeze-duration", 0),
            s.getInt("ice-armor-duration", 0),
            s.getInt("poison-amplifier", 0),
            s.getInt("poison-duration", 0),
            s.getInt("weakness-amplifier", 0),
            s.getInt("weakness-duration", 0),
            s.getDouble("lifesteal-percent", 0),
            s.getInt("strength-amplifier", 0),
            s.getInt("strength-duration", 0),
            s.getDouble("teleport-range", 0),
            s.getDouble("explosion-power", 0)
        );

        return new GemDefinition(id, displayName, lore, material, cmd,
            damage, cooldown, manaCost, sound, soundPitch,
            particle, particleCount, aoeRadius, abilities, stats);
    }

    private String colorize(String s) {
        return s == null ? "" : s.replace("&", "\u00a7");
    }

    private Material parseMaterial(String name) {
        Material m = Material.getMaterial(name.toUpperCase());
        return m != null ? m : Material.DIAMOND;
    }

    private Sound parseSound(String name) {
        try { return Sound.valueOf(name.toUpperCase()); }
        catch (Exception e) { return Sound.ENTITY_PLAYER_ATTACK_SWEEP; }
    }

    private Particle parseParticle(String name) {
        try { return Particle.valueOf(name.toUpperCase()); }
        catch (Exception e) { return Particle.FLAME; }
    }
}
