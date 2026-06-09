package me.vennlmao.gems.command;

import me.vennlmao.gems.GemsPlugin;
import me.vennlmao.gems.gem.GemDefinition;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class GemsCommand implements CommandExecutor {

    private final GemsPlugin plugin;

    public GemsCommand(GemsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Chỉ player mới dùng được lệnh này.");
            return true;
        }

        Optional<GemDefinition> opt = plugin.registry().fromItem(player.getInventory().getItemInMainHand());
        if (opt.isEmpty()) {
            player.sendMessage("\u00a7cBạn không cầm gem nào trong tay.");
            return true;
        }

        GemDefinition def = opt.get();
        int mana = plugin.mana().getMana(player);
        int max = plugin.cfg().getMaxMana();

        player.sendMessage("\u00a79=== " + def.displayName() + " \u00a79===");
        def.lore().forEach(player::sendMessage);
        player.sendMessage("\u00a79Mana hiện tại: \u00a7f" + mana + "/" + max);
        player.sendMessage("\u00a79Abilities: \u00a7f" + String.join(", ", def.abilities()));
        return true;
    }
}
