package com.example.rpg.shop.command;

import com.example.rpg.common.message.MessageUtil;
import com.example.rpg.shop.facade.ShopFacade;
import com.example.rpg.shop.service.ShopService;
import com.example.rpg.util.RpgUtil;
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
 * SHOPコマンドの受付を担当するクラス。
 *
 * <p>コマンドは入力受付のみを担当し、GUI表示や売却処理の詳細はFacadeへ委譲する。</p>
 */

public class ShopCommand implements CommandExecutor, TabCompleter {

    /**
     * SHOP GUI FACADE
     */
    private final ShopFacade shopFacade;
    /**
     * SHOP業務処理クラス
     */
    private final ShopService shopService;

    /**
     * SHOPコマンドを生成する。
     *
     * @param shopFacade  SHOP GUI Facade
     * @param shopService SHOP業務処理クラス
     */
    public ShopCommand(ShopFacade shopFacade, ShopService shopService) {
        this.shopFacade = shopFacade;
        this.shopService = shopService;
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
            shopFacade.openCategory(player);
            return true;
        }

        // 手に持っているアイテムを指定個数販売する
        if (args[0].equalsIgnoreCase("sell")) {
            if (args.length == 2) {
                int amount = RpgUtil.getIntOrDefault(args[1]);
                if (amount < 0) return false;
                shopService.sellHandItem(player, amount);
                return true;
            }
            return shopService.sellHandItem(player);
        }

        player.sendMessage(MessageUtil.yellow("使用方法： /shop または /shop sell amount"));
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
            return filter(List.of("sell"), args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("sell")) {
            return List.of("1", "5", "10", "64");
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