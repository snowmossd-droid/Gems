package me.vennlmao.gems.manager;

import me.vennlmao.gems.GemsPlugin;
import me.vennlmao.gems.util.ActionBarUtil;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ManaManager {

    private final GemsPlugin plugin;
    private final Map<UUID, Integer> manaMap = new ConcurrentHashMap<>();

    public ManaManager(GemsPlugin plugin) {
        this.plugin = plugin;
    }

    public void startRegenTask() {
        int interval = plugin.cfg().getManaRegenInterval();
        int barInterval = plugin.cfg().getActionBarInterval();
        int regenAmount = plugin.cfg().getManaRegenAmount();
        int max = plugin.cfg().getMaxMana();

        plugin.scheduler().runGlobalTimer(() -> {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                int current = getMana(p);
                if (current < max) {
                    setMana(p, Math.min(max, current + regenAmount));
                }
            }
        }, interval, interval);

        plugin.scheduler().runGlobalTimer(() -> {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                ActionBarUtil.send(p, buildBar(p));
            }
        }, barInterval, barInterval);
    }

    private String buildBar(Player p) {
        int mana = getMana(p);
        int max = plugin.cfg().getMaxMana();
        int filled = (int) ((mana / (double) max) * 20);
        String bar = "\u00a79" + "\u2588".repeat(filled) + "\u00a78" + "\u2588".repeat(20 - filled);
        return "\u00a79\u26a1 " + bar + " \u00a7f" + mana + "/" + max;
    }

    public int getMana(Player p) {
        return manaMap.getOrDefault(p.getUniqueId(), plugin.cfg().getMaxMana());
    }

    public void setMana(Player p, int amount) {
        manaMap.put(p.getUniqueId(), Math.max(0, Math.min(plugin.cfg().getMaxMana(), amount)));
    }

    public boolean consumeMana(Player p, int cost) {
        int current = getMana(p);
        if (current < cost) return false;
        setMana(p, current - cost);
        return true;
    }

    public void remove(Player p) {
        manaMap.remove(p.getUniqueId());
    }

    public void cleanup() {
        manaMap.clear();
    }
}
