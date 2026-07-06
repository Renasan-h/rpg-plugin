package com.example.rpg.command;

import com.example.rpg.menu.ShopMenu;
import com.example.rpg.service.ShopService;
import com.example.rpg.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {

    private final ShopMenu menu;
    private final ShopService shopService;

    public ShopCommand(ShopMenu menu, ShopService shopService) {
        this.menu = menu;
        this.shopService = shopService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageUtil.red("このコマンドはプレイヤーのみ実行できます。"));
            return true;
        }

        if (args.length == 0) {
            menu.open(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("sell")) {
            return shopService.sellHandItem(player);
        }

        player.sendMessage(MessageUtil.yellow("使用方法： /shop または /shop sell"));
        return true;
    }
}