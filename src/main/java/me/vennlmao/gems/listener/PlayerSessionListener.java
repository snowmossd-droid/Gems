package me.vennlmao.gems.listener;

import me.vennlmao.gems.GemsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerSessionListener implements Listener {

    private final GemsPlugin plugin;

    public PlayerSessionListener(GemsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.mana().remove(event.getPlayer());
    }
}
