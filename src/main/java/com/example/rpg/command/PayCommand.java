package com.example.rpg.command;

import com.example.rpg.common.message.MessageUtil;
import com.example.rpg.money.event.MoneyChangeReason;
import com.example.rpg.money.exception.InsufficientMoneyException;
import com.example.rpg.money.service.MoneyService;
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

    /**
     * 所持金管理Service
     */
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
            return false;
        }

        if (args.length != 2) {
            fromPlayer.sendMessage(MessageUtil.red("使い方： /pay <player> <amount>"));
            return false;
        }

        Player toPlayer = Bukkit.getPlayerExact(args[0]);

        if (toPlayer == null) {
            fromPlayer.sendMessage(MessageUtil.red("指定したプレイヤーが見つかりません。"));
            return false;
        }

        if (fromPlayer.getUniqueId().equals(toPlayer.getUniqueId())) {
            fromPlayer.sendMessage(MessageUtil.red("自分自身には送金できません。"));
            return false;
        }

        int amount;

        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            fromPlayer.sendMessage(MessageUtil.red("金額は数値で入力してください。"));
            return false;
        }

        if (amount <= 0) {
            fromPlayer.sendMessage(MessageUtil.red("金額は1以上を指定してください。"));
            return false;
        }

        try {
            moneyService.removeMoney(fromPlayer.getUniqueId(), amount, MoneyChangeReason.PLAYER_PAYMENT_SENT);
        } catch (InsufficientMoneyException ex) {
            fromPlayer.sendMessage(MessageUtil.red("所持金が足りません。"));
            return false;
        }

        moneyService.addMoney(toPlayer.getUniqueId(), amount, MoneyChangeReason.PLAYER_PAYMENT_RECEIVED);
        int senderMoney = moneyService.getBalance(fromPlayer.getUniqueId());

        fromPlayer.sendMessage(MessageUtil.mm("""
                <gold>%s</gold><yellow> に </yellow><gold>%dG</gold><yellow> 送金しました。</yellow>
                <gray>残高: %dG</gray>""".formatted(toPlayer.getName(), amount, senderMoney)
        ));

        return true;
    }
}
