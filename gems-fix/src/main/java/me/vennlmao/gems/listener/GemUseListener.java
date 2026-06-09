package me.vennlmao.gems.listener;

import me.vennlmao.gems.GemsPlugin;
import me.vennlmao.gems.gem.AbilityExecutor;
import me.vennlmao.gems.gem.GemDefinition;
import me.vennlmao.gems.manager.CooldownManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Optional;

public class GemUseListener implements Listener {

    private final GemsPlugin plugin;
    private final CooldownManager cooldowns;
    private final AbilityExecutor executor;

    public GemUseListener(GemsPlugin plugin) {
        this.plugin = plugin;
        this.cooldowns = new CooldownManager();
        this.executor = new AbilityExecutor(plugin);
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR
            && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        Optional<GemDefinition> opt = plugin.registry().fromItem(player.getInventory().getItemInMainHand());
        if (opt.isEmpty()) return;

        event.setCancelled(true);
        GemDefinition def = opt.get();

        if (cooldowns.isOnCooldown(player, def.id())) {
            long remaining = (cooldowns.getRemainingMillis(player, def.id()) / 1000) + 1;
            player.sendMessage("\u00a7cHồi chiêu còn \u00a7f" + remaining + "s \u00a7ccho \u00a7r" + def.displayName());
            return;
        }

        if (!plugin.mana().consumeMana(player, def.manaCost())) {
            player.sendMessage("\u00a7cKhông đủ năng lượng! Cần \u00a7f" + def.manaCost() + " \u00a7cmana.");
            return;
        }

        cooldowns.set(player, def.id(), def.cooldownSeconds());
        executor.execute(player, def);
    }
}
