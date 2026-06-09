package me.vennlmao.gems.manager;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {

    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    public boolean isOnCooldown(Player player, String gemId) {
        Map<String, Long> playerMap = cooldowns.get(player.getUniqueId());
        if (playerMap == null) return false;
        Long end = playerMap.get(gemId);
        return end != null && System.currentTimeMillis() < end;
    }

    public long getRemainingMillis(Player player, String gemId) {
        Map<String, Long> playerMap = cooldowns.get(player.getUniqueId());
        if (playerMap == null) return 0;
        Long end = playerMap.get(gemId);
        if (end == null) return 0;
        return Math.max(0, end - System.currentTimeMillis());
    }

    public void set(Player player, String gemId, int seconds) {
        cooldowns
            .computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
            .put(gemId, System.currentTimeMillis() + seconds * 1000L);
    }

    public void clear(Player player) {
        cooldowns.remove(player.getUniqueId());
    }
}
