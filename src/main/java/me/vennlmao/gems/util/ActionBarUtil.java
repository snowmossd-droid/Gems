package me.vennlmao.gems.util;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public final class ActionBarUtil {

    private ActionBarUtil() {}

    public static void send(Player player, String message) {
        player.sendActionBar(Component.text(message));
    }
}
