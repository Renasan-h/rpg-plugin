package com.example.rpg.command;

import com.example.rpg.common.message.MessageUtil;
import com.example.rpg.money.event.MoneyChangeReason;
import com.example.rpg.money.exception.InsufficientMoneyException;
import com.example.rpg.money.service.MoneyService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 所持金確認コマンド実行クラス
 */
public class MoneyCommand implements CommandExecutor, TabCompleter {

    /**
     * 所持金管理Service
     */
    private final MoneyService moneyService;

    public MoneyCommand(MoneyService moneyService) {
        this.moneyService = moneyService;
    }

    @Override
    public boolean onCommand(
            @NonNull CommandSender sender,
            @NonNull Command command,
            @NonNull String label,
            String[] args
    ) {
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


        sender.sendMessage(MessageUtil.mm(
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
            return Stream.of("add", "set", "remove")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3) {
            return List.of("1", "100", "1000");
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

        final int currentMoney = moneyService.getBalance(player.getUniqueId());

        final String formattedMoney = NumberFormat
                .getNumberInstance(Locale.ROOT)
                .format(currentMoney);

        player.sendMessage(MessageUtil.mm("<yellow>現在の所持金：</yellow><gold>" + formattedMoney + "G</gold>"));

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
            sender.sendMessage(MessageUtil.red("権限がありません。"));
            return false;
        }

        Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null) {
            sender.sendMessage(MessageUtil.red("指定したプレイヤーが見つかりません。"));
            return false;
        }

        int amount = parseMoney(sender, args[2], 1);
        if (amount < 0) {
            return false;
        }

        int total = moneyService.addMoney(target.getUniqueId(), amount, MoneyChangeReason.ADMIN_ADD);

        sender.sendMessage(MessageUtil.mm("""
                <gold>%s</gold><yellow> に </yellow><gold>%dG</gold><yellow> 追加しました。</yellow> <gray>残高: %dG</gray>""".formatted(target.getName(), amount, total)
        ));

        return true;
    }

    /**
     * 指定プレイヤーの所持金を設定します。
     *
     * @param sender コマンド送信者
     * @param args   コマンド引数
     * @return 処理済みの場合true
     */
    private boolean setMoney(
            final CommandSender sender,
            final String[] args
    ) {
        if (!hasAdminPermission(sender)) {
            return false;
        }

        final Player target =
                findOnlinePlayer(sender, args[1]);

        if (target == null) {
            return false;
        }

        final Integer amount =
                parseMoney(
                        sender,
                        args[2],
                        0
                );

        if (amount == null) {
            return false;
        }

        final int afterAmount =
                moneyService.set(
                        target.getUniqueId(),
                        amount,
                        MoneyChangeReason.ADMIN_SET
                );

        sender.sendMessage(MessageUtil.mm(
                "<gold>"
                        + target.getName()
                        + "</gold>"
                        + "<yellow> の所持金を </yellow>"
                        + "<gold>"
                        + afterAmount
                        + "G</gold>"
                        + "<yellow> に設定しました。</yellow>"
        ));

        target.sendMessage(MessageUtil.mm(
                "<yellow>所持金が </yellow>"
                        + "<gold>"
                        + afterAmount
                        + "G</gold>"
                        + "<yellow> に設定されました。</yellow>"
        ));

        return true;
    }

    /**
     * 指定プレイヤーの所持金を減算します。
     *
     * @param sender コマンド送信者
     * @param args   コマンド引数
     * @return 処理済みの場合true
     */
    private boolean removeMoney(
            final CommandSender sender,
            final String[] args
    ) {
        if (!hasAdminPermission(sender)) {
            return false;
        }

        final Player target =
                findOnlinePlayer(sender, args[1]);

        if (target == null) {
            return false;
        }

        /*
         * MoneyService.removeは正数のみ受け付けるため、
         * 管理者による減算額も1以上とする。
         */
        final Integer amount =
                parseMoney(
                        sender,
                        args[2],
                        1
                );

        if (amount == null) {
            return false;
        }

        final int afterAmount;

        try {
            afterAmount = moneyService.removeMoney(
                    target.getUniqueId(),
                    amount,
                    MoneyChangeReason.ADMIN_REMOVE
            );
        } catch (InsufficientMoneyException exception) {
            sender.sendMessage(MessageUtil.red(
                    "対象プレイヤーの所持金が足りません。"
            ));
            return false;
        }

        sender.sendMessage(MessageUtil.mm(
                "<gold>"
                        + target.getName()
                        + "</gold>"
                        + "<yellow> から </yellow>"
                        + "<gold>"
                        + amount
                        + "G</gold>"
                        + "<yellow> 減らしました。</yellow>"
                        + " <gray>残高: "
                        + afterAmount
                        + "G</gray>"
        ));

        target.sendMessage(MessageUtil.mm(
                "<gold>"
                        + amount
                        + "G</gold>"
                        + "<yellow> 減少しました。</yellow>"
                        + " <gray>残高: "
                        + afterAmount
                        + "G</gray>"
        ));

        return true;
    }

    /**
     * コマンド送信者が所持金管理権限を持っているか確認します。
     *
     * @param sender コマンド送信者
     * @return 権限を持っている場合true
     */
    private boolean hasAdminPermission(
            final CommandSender sender
    ) {
        if (sender.hasPermission("rpg.money.admin")) {
            return true;
        }
        sender.sendMessage(MessageUtil.red(
                "権限がありません。"
        ));
        return false;
    }

    /**
     * 指定されたオンラインプレイヤーを取得します。
     *
     * @param sender     エラーメッセージ送信先
     * @param playerName プレイヤー名
     * @return 対象プレイヤー。存在しない場合null
     */
    private Player findOnlinePlayer(
            final CommandSender sender,
            final String playerName
    ) {
        final Player target =
                Bukkit.getPlayerExact(playerName);

        if (target == null) {
            sender.sendMessage(MessageUtil.red(
                    "指定したプレイヤーが見つかりません。"
            ));
        }

        return target;
    }

    /**
     * 文字列の金額を整数へ変換し、最小値を検証します。
     *
     * @param sender        エラーメッセージ送信先
     * @param rawAmount     金額文字列
     * @param minimumAmount 許容する最小金額
     * @return 変換後の金額。変換または検証に失敗した場合null
     */
    private Integer parseMoney(
            final CommandSender sender,
            final String rawAmount,
            final int minimumAmount
    ) {
        final int amount;

        try {
            amount = Integer.parseInt(rawAmount);
        } catch (NumberFormatException exception) {
            sender.sendMessage(MessageUtil.red(
                    "金額は整数で指定してください。"
            ));
            return null;
        }

        if (amount < minimumAmount) {
            sender.sendMessage(MessageUtil.red(
                    "金額は"
                            + minimumAmount
                            + "以上を指定してください。"
            ));
            return null;
        }

        return amount;
    }

    /**
     * moneyコマンドの使用方法を表示します。
     *
     * @param sender コマンド送信者
     * @return 処理済みを表すtrue
     */
    private boolean showUsage(
            final CommandSender sender
    ) {
        sender.sendMessage(MessageUtil.mm(
                "<red>使い方: "
                        + "/money, "
                        + "/money [add, set, remove] "
                        + "<player> <amount>"
                        + "</red>"
        ));

        return true;
    }
}
