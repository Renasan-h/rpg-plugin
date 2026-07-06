package com.example.rpg.command;

import com.example.rpg.util.MessageUtil;
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
                sender.sendMessage(MessageUtil.red("権限がありません。"));
                return false;
            }

            plugin.reloadConfig();

            sender.sendMessage(MessageUtil.green("RpgPlugin の config.yml を再読み込みしました。"));
            return true;
        }

        sender.sendMessage(MessageUtil.red("使い方： /rpg reload"));
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
