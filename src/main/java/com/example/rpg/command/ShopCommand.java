package com.example.rpg.command;

import com.example.rpg.facade.ShopFacade;
import com.example.rpg.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * SHOPコマンドの受付を担当するクラス。
 *
 * <p>コマンドは入力受付のみを担当し、GUI表示や売却処理の詳細はFacadeへ委譲する。</p>
 */

public class ShopCommand implements CommandExecutor {

    /**
     * SHOP機能の入口。
     */
    private final ShopFacade shopFacade;

    /**
     * SHOPコマンドを生成する。
     *
     * @param shopFacade SHOP Facade
     */
    public ShopCommand(ShopFacade shopFacade) {
        this.shopFacade = shopFacade;
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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageUtil.red("このコマンドはプレイヤーのみ実行できます。"));
            return true;
        }

        if (args.length == 0) {
            shopFacade.openShop(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("sell")) {
            return shopFacade.sellHandItem(player);
        }

        player.sendMessage(MessageUtil.yellow("使用方法： /shop または /shop sell"));
        return true;
    }
}