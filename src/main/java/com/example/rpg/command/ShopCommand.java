package com.example.rpg.command;

import com.example.rpg.facade.ShopGuiFacade;
import com.example.rpg.service.ShopService;
import com.example.rpg.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * SHOPコマンドの受付を担当するクラス。
 *
 * <p>コマンドは入力受付のみを担当し、GUI表示や売却処理の詳細はFacadeへ委譲する。</p>
 */

public class ShopCommand implements CommandExecutor {

    /**
     * SHOP GUI FACADE
     */
    private final ShopGuiFacade shopGuiFacade;
    /**
     * SHOP業務処理クラス
     */
    private final ShopService shopService;

    /**
     * SHOPコマンドを生成する。
     *
     * @param shopGuiFacade SHOP GUI Facade
     * @param shopService   SHOP業務処理クラス
     */
    public ShopCommand(ShopGuiFacade shopGuiFacade, ShopService shopService) {
        this.shopGuiFacade = shopGuiFacade;
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
            return true;
        }

        if (args.length == 0) {
            shopGuiFacade.openCategory(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("sell")) {
            return shopService.sellHandItem(player);
        }

        player.sendMessage(MessageUtil.yellow("使用方法： /shop または /shop sell"));
        return true;
    }
}