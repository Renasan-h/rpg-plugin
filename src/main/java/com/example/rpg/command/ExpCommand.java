package com.example.rpg.command;

import com.example.rpg.common.message.MessageUtil;
import com.example.rpg.service.ExpService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 所持金確認コマンド実行クラス
 */
public class ExpCommand implements CommandExecutor {

    private final ExpService expService;

    public ExpCommand(ExpService expService) {
        this.expService = expService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // コマンドの実行者確認（プレイヤーかどうか）
        if (!(sender instanceof Player player)) {
            sender.sendMessage("このコマンドはプレイヤーのみ実行できます。");
            return true;
        }

        int exp = expService.getExp(player.getUniqueId());

        player.sendMessage(MessageUtil.mm("<yellow>現在の経験値：</yellow><gold>%d Exp</gold>".formatted(exp)));

        return true;
    }
}
