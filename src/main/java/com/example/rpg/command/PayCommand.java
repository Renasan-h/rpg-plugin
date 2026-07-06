package com.example.rpg.command;

import com.example.rpg.service.MoneyService;
import com.example.rpg.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * プレイヤー間で送金を行うためのコマンド
 * TODO: 将来的には銀行か専用端末（スマホチックな何か）からのみ行える様子にする
 */
public class PayCommand implements CommandExecutor {

    private final MoneyService moneyService;

    public PayCommand(MoneyService moneyService) {
        this.moneyService = moneyService;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {

        if (!(sender instanceof Player fromPlayer)) {
            sender.sendMessage(MessageUtil.red("このコマンドはプレイヤーのみ実行できます。"));
            return true;
        }

        if (args.length != 2) {
            fromPlayer.sendMessage(MessageUtil.red("使い方： /pay <player> <amount>"));
            return true;
        }

        Player toPlayer = Bukkit.getPlayerExact(args[0]);

        if (toPlayer == null) {
            fromPlayer.sendMessage(MessageUtil.red("指定したプレイヤーが見つかりません。"));
            return true;
        }

        if (fromPlayer.getUniqueId().equals(toPlayer.getUniqueId())) {
            fromPlayer.sendMessage(MessageUtil.red("自分自身には送金できません。"));
            return true;
        }

        int amount;

        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            fromPlayer.sendMessage(MessageUtil.red("金額は数値で入力してください。"));
            return true;
        }

        if (amount < 0) {
            fromPlayer.sendMessage(MessageUtil.red("金額は1以上を指定してください。"));
            return true;
        }

        boolean success = moneyService.removeMoney(fromPlayer.getUniqueId(), amount);

        if (!success) {
            fromPlayer.sendMessage(MessageUtil.red("所持金が足りません。"));
            return true;
        }

        moneyService.addMoney(toPlayer.getUniqueId(), amount);
        int senderMoney = moneyService.getMoney(fromPlayer.getUniqueId());

        fromPlayer.sendMessage(MessageUtil.mm("""
                <gold>%s</gold><yellow> に </yellow><gold>%dG</gold><yellow> 送金しました。</yellow>
                <gray>残高: %dG</gray>""".formatted(toPlayer.getName(), amount, senderMoney)
        ));

        return true;
    }
}
