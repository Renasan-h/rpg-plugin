package com.example.rpg.command;

import com.example.rpg.service.MoneyService;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 所持金確認コマンド実行クラス
 */
public class MoneyCommand implements CommandExecutor, TabCompleter {

    private final MiniMessage mm = MiniMessage.miniMessage();
    private final MoneyService moneyService;

    public MoneyCommand(MoneyService moneyService) {
        this.moneyService = moneyService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 所持金確認
        if (args.length == 0) {
            return showOwnMoney(sender);
        }

        if (args.length == 3) {
            // 所持金追加
            if (args[0].equalsIgnoreCase("add")) {
                return addMoney(sender, args);
            }

            // 所持金設定
            if (args[0].equalsIgnoreCase("set")) {
                return setMoney(sender, args);
            }

            // 所持金減算
            if (args[0].equalsIgnoreCase("remove")) {
                return removeMoney(sender, args);
            }
        }


        sender.sendMessage(mm.deserialize(
                "<red>使い方: /money, /money [add,set, remove] <player> <amount></red>"
        ));
        return true;
    }

    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args) {

        if (!sender.hasPermission("rpg.money.admin")) {
            return List.of();
        }
        if (args.length == 1) {
            return List.of("add", "set", "remove").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    /**
     * プレイヤーの所持金を確認する
     *
     * @param sender コマンド送信者情報
     * @return true:完了 false:未完了
     */
    private boolean showOwnMoney(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("コンソールからは /money add <player> <amount> を使用してください。");
            return false;
        }

        int money = moneyService.getMoney(player.getUniqueId());

        player.sendMessage(mm.deserialize("<yellow>現在の所持金：</yellow><gold>" + NumberFormat.getNumberInstance().format(money) + "G</gold>"));

        return true;
    }

    /**
     * 金額追加するコマンド（管理者用）
     *
     * @param sender コマンド送信者情報
     * @param args   パラメータ
     * @return true:実行完了 false: 未実行
     */
    private boolean addMoney(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rpg.money.admin")) {
            sender.sendMessage(mm.deserialize("<red>権限がありません。</red>"));
            return false;
        }

        Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null) {
            sender.sendMessage(mm.deserialize("<red>指定したプレイヤーが見つかりません。</red>"));
            return false;
        }

        int amount = parseMoney(sender, args[2], 1);
        if (amount < 0) {
            return false;
        }

        int total = moneyService.addMoney(target.getUniqueId(), amount);

        sender.sendMessage(mm.deserialize("""
                <gold>%s</gold><yellow> に </yellow><gold>%dG</gold><yellow> 追加しました。</yellow> <gray>残高: %dG</gray>""".formatted(target.getName(), amount, total)
        ));

        return true;
    }

    /**
     * 所持金設定コマンド（管理者）
     *
     * @param sender コマンド送信者情報
     * @param args   パラメータ
     * @return true:完了 false:未完了
     */
    private boolean setMoney(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rpg.money.admin")) {
            sender.sendMessage(mm.deserialize("<red>権限がありません。</red>"));
            return false;
        }

        Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null) {
            sender.sendMessage(mm.deserialize("<red>指定したプレイヤーが見つかりません。</red>"));
            return false;
        }

        int amount = parseMoney(sender, args[2], 0);
        if (amount < 0) {
            return false;
        }

        int total = moneyService.setMoney(target.getUniqueId(), amount);

        sender.sendMessage(mm.deserialize(
                "<gold>" + target.getName() + "</gold><yellow> の所持金を </yellow><gold>" + total + "G</gold><yellow> に設定しました。</yellow>"
        ));

        target.sendMessage(mm.deserialize(
                "<yellow>所持金が </yellow><gold>" + total + "G</gold><yellow> に設定されました。</yellow>"
        ));

        return true;
    }

    /**
     * 指定された金額をプレイヤーの所持金から減額する
     *
     * @param sender コマンド送信者情報
     * @param args   パラメータ
     * @return true:完了 false:未完了
     */
    private boolean removeMoney(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rpg.money.admin")) {
            sender.sendMessage(mm.deserialize("<red>権限がありません。</red>"));
            return false;
        }

        Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null) {
            sender.sendMessage(mm.deserialize("<red>指定したプレイヤーが見つかりません。</red>"));
            return false;
        }

        int amount = parseMoney(sender, args[2], 0);
        if (amount < 0) {
            return false;
        }

        boolean success = moneyService.removeMoney(target.getUniqueId(), amount);

        if (!success) {
            sender.sendMessage(mm.deserialize("<red>対象プレイヤーの所持金が足りません。</red>"));
            return false;
        }

        int total = moneyService.getMoney(target.getUniqueId());

        sender.sendMessage(mm.deserialize(
                "<gold>" + target.getName() + "</gold><yellow> から </yellow><gold>" + amount + "G</gold><yellow> 減らしました。</yellow> <gray>残高: " + total + "G</gray>"
        ));

        target.sendMessage(mm.deserialize(
                "<gold>" + amount + "G</gold><yellow> 減少しました。</yellow> <gray>残高: " + total + "G</gray>"
        ));

        return true;
    }

    /**
     * 指定された金額をstringからintへ変換し、条件を満たしているか確認する
     *
     * @param money 指定金額
     * @param min   最低金額
     * @return true: 成功 false: 失敗
     */
    private int parseMoney(CommandSender sender, String money, int min) {
        int amount;
        try {
            amount = Integer.parseInt(money);
        } catch (NumberFormatException e) {
            return -1;
        }

        if (amount < min) {
            sender.sendMessage(mm.deserialize("<red>金額は0以上を指定してください。</red>"));
            return -1;
        }

        return amount;
    }
}
