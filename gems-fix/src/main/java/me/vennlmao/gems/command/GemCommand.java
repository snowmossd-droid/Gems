package me.vennlmao.gems.command;

import me.vennlmao.gems.GemsPlugin;
import me.vennlmao.gems.gem.GemDefinition;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GemCommand implements CommandExecutor, TabCompleter {

    private final GemsPlugin plugin;

    public GemCommand(GemsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("gems.admin")) {
            sender.sendMessage("\u00a7cBạn không có quyền dùng lệnh này.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "give" -> {
                if (args.length < 3) {
                    sender.sendMessage("\u00a7eUsage: /gem give <player> <gem_id> [amount]");
                    return true;
                }
                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("\u00a7cKhông tìm thấy player: " + args[1]);
                    return true;
                }
                Optional<GemDefinition> opt = plugin.registry().get(args[2]);
                if (opt.isEmpty()) {
                    sender.sendMessage("\u00a7cKhông tìm thấy gem: " + args[2]);
                    return true;
                }
                int amount = args.length >= 4 ? parseInt(args[3], 1) : 1;
                for (int i = 0; i < amount; i++) {
                    target.getInventory().addItem(plugin.registry().buildItem(opt.get()));
                }
                sender.sendMessage("\u00a7aĐã cho \u00a7f" + target.getName() + " \u00a7ax" + amount
                    + " \u00a7r" + opt.get().displayName());
            }

            case "reload" -> {
                plugin.cfg().reload();
                plugin.registry().loadAll();
                sender.sendMessage("\u00a7aGems reloaded — " + plugin.registry().size() + " gems.");
            }

            case "list" -> {
                sender.sendMessage("\u00a79=== Danh sách Gems ===");
                for (GemDefinition def : plugin.registry().all()) {
                    sender.sendMessage("\u00a77- \u00a7r" + def.displayName()
                        + " \u00a78(" + def.id() + ")"
                        + " \u00a77DMG:" + (int) def.damage()
                        + " CD:" + def.cooldownSeconds() + "s"
                        + " Mana:" + def.manaCost());
                }
            }

            case "resendpack" -> {
                if (args.length < 2) {
                    sender.sendMessage("\u00a7eUsage: /gem resendpack <player|all>");
                    return true;
                }
                if (plugin.packListener() == null || !plugin.packServer().isReady()) {
                    sender.sendMessage("\u00a7cPack server chưa sẵn sàng!");
                    return true;
                }
                if (args[1].equalsIgnoreCase("all")) {
                    plugin.getServer().getOnlinePlayers()
                        .forEach(p -> plugin.packListener().resendPack(p));
                    sender.sendMessage("\u00a7aĐã gửi lại pack cho tất cả " + plugin.getServer().getOnlinePlayers().size() + " players.");
                } else {
                    Player target = plugin.getServer().getPlayer(args[1]);
                    if (target == null) { sender.sendMessage("\u00a7cKhông tìm thấy player!"); return true; }
                    plugin.packListener().resendPack(target);
                    sender.sendMessage("\u00a7aĐã gửi lại pack cho " + target.getName());
                }
            }

            case "packinfo" -> {
                if (!plugin.packServer().isReady()) {
                    sender.sendMessage("\u00a7cPack server chưa chạy.");
                    return true;
                }
                sender.sendMessage("\u00a79=== Pack Server Info ===");
                sender.sendMessage("\u00a7eURL: \u00a7f" + plugin.packServer().getPackUrl());
                sender.sendMessage("\u00a7eSHA1: \u00a7f" + bytesToHex(plugin.packServer().getPackSha1()));
            }

            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender s) {
        s.sendMessage("\u00a79=== Gems Commands ===");
        s.sendMessage("\u00a7e/gem give <player> <id> [amount]");
        s.sendMessage("\u00a7e/gem list");
        s.sendMessage("\u00a7e/gem reload");
        s.sendMessage("\u00a7e/gem resendpack <player|all>");
        s.sendMessage("\u00a7e/gem packinfo");
    }

    private int parseInt(String s, int fallback) {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return fallback; }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) return List.of("give", "list", "reload", "resendpack", "packinfo");
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("resendpack")) {
            List<String> names = new ArrayList<>(plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList());
            names.add(0, "all");
            return names;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return new ArrayList<>(plugin.registry().all().stream().map(GemDefinition::id).toList());
        }
        return List.of();
    }
}
