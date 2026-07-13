package com.example.rpg.command;

import com.example.rpg.common.message.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Locale;

public class DevHelpCommand implements CommandExecutor, TabCompleter {

    public DevHelpCommand() {
    }

    /**
     * /shop コマンドを処理する。
     *
     * @param sender  実行者
     * @param command コマンド
     * @param label   実行ラベル
     * @param args    引数
     * @return コマンド処理結果
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageUtil.red("このコマンドはプレイヤーのみ実行できます。"));
            return false;
        }
        if (args.length == 0) {
            player.sendMessage(MessageUtil.yellow("使用方法： /devhelp hand amount"));
            return false;
        }

        if (args[0].equalsIgnoreCase("hand")) {
            if (args.length > 1) {
                if (args[1].equalsIgnoreCase("amount")) {
                    player.sendMessage(MessageUtil.green("手持ちアイテムamount：" + player.getInventory().getItemInMainHand().getAmount()));
                    return true;
                }
            }

        }

        player.sendMessage(MessageUtil.yellow("使用方法： /devhelp または /devhelp hand amount"));
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String @NonNull [] args
    ) {
        if (args.length == 1) {
            return filter(List.of("hand"), args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("hand")) {
            return filter(List.of("amount"), args[1]);
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
