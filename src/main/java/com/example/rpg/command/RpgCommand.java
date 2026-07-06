package com.example.rpg.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class RpgCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public RpgCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("rpg.admin")) {
                sender.sendMessage(mm.deserialize("<red>権限がありません。</red>"));
                return false;
            }

            plugin.reloadConfig();

            sender.sendMessage(mm.deserialize("<green>RpgPlugin の config.yml を再読み込みしました。</green>"));
            return true;
        }

        sender.sendMessage(mm.deserialize("<red>使い方： /rpg reload</red>"));
        return false;
    }

    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args) {

        if (!sender.hasPermission("rpg.admin")) {
            return List.of();
        }
        if (args.length == 1) {
            return List.of("reload").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}
