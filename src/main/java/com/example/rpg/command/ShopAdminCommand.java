package com.example.rpg.command;

import com.example.rpg.service.ShopPurchaseService;
import com.example.rpg.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class ShopAdminCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "rpg.shop.admin";

    private final ShopPurchaseService shopPurchaseService;

    public ShopAdminCommand(ShopPurchaseService shopPurchaseService) {
        this.shopPurchaseService = shopPurchaseService;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(MessageUtil.red("権限がありません。"));
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            return resetPlayer(sender, args);
        }

        sender.sendMessage(MessageUtil.red("不明なサブコマンドです。"));
        sendUsage(sender);
        return true;
    }

    private boolean resetPlayer(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(MessageUtil.yellow("使用方法: /shopadmin reset <player>"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null) {
            sender.sendMessage(MessageUtil.red("指定したプレイヤーが見つかりません。"));
            return true;
        }

        shopPurchaseService.resetPlayer(target.getUniqueId());

        sender.sendMessage(MessageUtil.green(target.getName() + " のSHOP購入履歴をリセットしました。"));

        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(MessageUtil.yellow("使用方法: /shopadmin reset <player>"));
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args
    ) {
        if (!sender.hasPermission(PERMISSION)) {
            return List.of();
        }

        if (args.length == 1) {
            return filter(List.of("reset"), args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .toList();
        }

        return List.of();
    }

    private List<String> filter(List<String> values, String input) {
        String lowerInput = input.toLowerCase(Locale.ROOT);

        return values.stream()
                .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(lowerInput))
                .toList();
    }
}