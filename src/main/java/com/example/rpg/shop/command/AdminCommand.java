package com.example.rpg.shop.command;

import com.example.rpg.common.message.MessageUtil;
import com.example.rpg.shop.repository.interfaces.IShopPurchaseRepository;
import org.bukkit.Bukkit;
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

/**
 * adminコマンドの受付を担当するクラス。
 *
 * <p>コマンドは入力受付のみを担当し、詳細はFacadeへ委譲する。</p>
 */
public class AdminCommand implements CommandExecutor, TabCompleter {

    /**
     * コマンド実行権限
     */
    private static final String PERMISSION = "rpg.admin.shop";

    /**
     * 購入回数管理Service。
     */
    private final IShopPurchaseRepository shopPurchaseRepository;

    /**
     * コンストラクタ
     *
     * @param shopPurchaseRepository 購入履歴
     */
    public AdminCommand(IShopPurchaseRepository shopPurchaseRepository) {
        this.shopPurchaseRepository = shopPurchaseRepository;
    }

    /**
     * コマンド送信時の処理
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return isComplete true:完了 false:未完了
     */
    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String @NonNull [] args
    ) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(MessageUtil.red("権限がありません。"));
            return false;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return false;
        }

        if (args[0].equalsIgnoreCase("shopreset")) {
            return resetShopByPlayer(sender, args);
        }

        sender.sendMessage(MessageUtil.red("不明なサブコマンドです。"));
        sendUsage(sender);
        return true;
    }

    /**
     * 指定されたプレイヤーのSHOP購入制限をリセットする
     */
    private boolean resetShopByPlayer(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(MessageUtil.yellow("使用方法: /admin shopreset <player>"));
            return false;
        }

        Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null) {
            sender.sendMessage(MessageUtil.red("指定したプレイヤーが見つかりません。"));
            return false;
        }

        shopPurchaseRepository.resetPlayer(target.getUniqueId());

        sender.sendMessage(MessageUtil.green(target.getName() + " のSHOP購入履歴をリセットしました。"));

        return true;
    }

    /**
     * 使い方をプレイヤーに送信する
     *
     * @param sender コマンド送信者
     */
    private void sendUsage(CommandSender sender) {
        sender.sendMessage(MessageUtil.yellow("使用方法: /admin shopreset <player>"));
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String @NonNull [] args
    ) {
        if (!sender.hasPermission(PERMISSION)) {
            return List.of();
        }

        if (args.length == 1) {
            return filter(List.of("shopreset"), args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("shopreset")) {
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