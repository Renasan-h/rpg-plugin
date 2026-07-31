package com.example.rpg.bank.command;

import com.example.rpg.bank.exception.InsufficientBankBalanceException;
import com.example.rpg.bank.facade.BankFacade;
import com.example.rpg.common.message.MessageUtil;
import com.example.rpg.money.exception.InsufficientMoneyException;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Stream;

/**
 * 銀行機能を操作するコマンドです。
 *
 * <p>
 * 以下のサブコマンドを提供します。
 * </p>
 *
 * <ul>
 *     <li>/bank</li>
 *     <li>/bank balance</li>
 *     <li>/bank deposit &lt;amount&gt;</li>
 *     <li>/bank withdraw &lt;amount&gt;</li>
 *     <li>/bank transfer &lt;player&gt; &lt;amount&gt;</li>
 * </ul>
 */
public class BankCommand implements CommandExecutor, TabCompleter {

    /**
     * 銀行基本操作権限
     */
    private static final String BANK_USE_PERMISSION =
            "rpg.bank.use";

    /**
     * 銀行送金権限
     */
    private static final String BANK_TRANSFER_PERMISSION =
            "rpg.bank.transfer";

    /**
     * 銀行Facade
     */
    private final BankFacade bankFacade;

    /**
     * BankCommandを生成します。
     *
     * @param bankFacade 銀行Facade
     * @throws NullPointerException bankFacadeがnullの場合
     */
    public BankCommand(final BankFacade bankFacade) {
        this.bankFacade = Objects.requireNonNull(
                bankFacade,
                "bankFacade must not be null"
        );
    }

    /**
     * 銀行コマンドを実行します。
     *
     * @param sender  コマンド送信者
     * @param command 実行コマンド
     * @param label   使用されたコマンド名
     * @param args    コマンド引数
     * @return コマンドを処理した場合true
     */
    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String @NotNull [] args
    ) {
        if (!sender.hasPermission(BANK_USE_PERMISSION)) {
            sender.sendMessage(MessageUtil.red(
                    "銀行コマンドを使用する権限がありません。"
            ));
        }

        final Player player = requirePlayer(sender);

        if (player == null) {
            return true;
        }

        player.sendMessage(MessageUtil.red("args.length: " + args.length));
        if (args.length == 0) {
            return showBalance(player);
        }

        return switch (args[0].toLowerCase(Locale.ROOT)) {
            case "balance" -> executeBalance(
                    player,
                    args
            );
            case "deposit" -> executeDeposit(
                    player,
                    args
            );
            case "withdraw" -> executeWithdraw(
                    player,
                    args
            );
            case "transfer" -> executeTransfer(
                    player,
                    args
            );
            default -> showUsage(player);
        };
    }

    /**
     * Tab補完候補を返します。
     *
     * @param sender  コマンド送信者
     * @param command 対象コマンド
     * @param alias   使用されたコマンド名
     * @param args    入力中の引数
     * @return 補完候補
     */
    @Override
    public List<String> onTabComplete(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String alias,
            @NotNull final String[] args
    ) {
        if (!sender.hasPermission(BANK_USE_PERMISSION)) {
            return List.of();
        }

        if (args.length == 1) {
            return filterByPrefix(
                    Stream.of(
                            "balance",
                            "deposit",
                            "withdraw",
                            "transfer"
                    ),
                    args[0]
            );
        }

        if (args.length == 2) {
            if (isAmountCommand(args[0])) {
                return filterByPrefix(
                        Stream.of(
                                "100",
                                "1000",
                                "10000"
                        ),
                        args[1]
                );
            }

            if (args[0].equalsIgnoreCase("transfer")
                    && sender.hasPermission(BANK_TRANSFER_PERMISSION)) {
                return findKnownPlayerNames(
                        sender,
                        args[1]
                );
            }
        }

        if (args.length == 3
                && args[0].equalsIgnoreCase("transfer")) {
            return filterByPrefix(
                    Stream.of(
                            "100",
                            "1000",
                            "10000"
                    ),
                    args[2]
            );
        }

        return List.of();
    }

    /**
     * balanceサブコマンドを実行します。
     *
     * @param player 実行プレイヤー
     * @param args   コマンド引数
     * @return 処理済みを表すtrue
     */
    private boolean executeBalance(
            final Player player,
            final String[] args
    ) {
        if (args.length != 1) {
            return showUsage(player);
        }

        return showBalance(player);
    }

    /**
     * プレイヤーの銀行残高を表示します。
     *
     * @param player 対象プレイヤー
     * @return 処理済みを表すtrue
     */
    private boolean showBalance(final Player player) {
        final int balance =
                bankFacade.getBalance(
                        player.getUniqueId()
                );

        player.sendMessage(MessageUtil.mm(
                "<gold>銀行残高：</gold>"
                        + "<yellow>"
                        + formatAmount(balance)
                        + "G</yellow>"
        ));

        return true;
    }

    /**
     * 入金コマンドを実行します。
     *
     * @param player 実行プレイヤー
     * @param args   コマンド引数
     * @return 処理済みを表すtrue
     */
    private boolean executeDeposit(
            final Player player,
            final String[] args
    ) {
        if (args.length != 2) {
            player.sendMessage(MessageUtil.red(
                    "使い方: /bank deposit <amount>"
            ));
            return true;
        }

        final Integer amount =
                parsePositiveAmount(
                        player,
                        args[1]
                );

        if (amount == null) {
            return true;
        }

        final int afterBalance;

        try {
            afterBalance = bankFacade.deposit(
                    player.getUniqueId(),
                    amount
            );
        } catch (final InsufficientMoneyException exception) {
            player.sendMessage(MessageUtil.red(
                    "手持ち所持金が不足しています。"
            ));
            return true;
        } catch (final ArithmeticException exception) {
            player.sendMessage(MessageUtil.red(
                    "銀行残高の上限を超えるため入金できません。"
            ));
            return true;
        }

        player.sendMessage(MessageUtil.mm(
                "<gold>"
                        + formatAmount(amount)
                        + "G</gold>"
                        + "<yellow>を銀行へ入金しました。</yellow>"
                        + " <gray>銀行残高: "
                        + formatAmount(afterBalance)
                        + "G</gray>"
        ));

        return true;
    }

    /**
     * 出金コマンドを実行します。
     *
     * @param player 実行プレイヤー
     * @param args   コマンド引数
     * @return 処理済みを表すtrue
     */
    private boolean executeWithdraw(
            final Player player,
            final String[] args
    ) {
        if (args.length != 2) {
            player.sendMessage(MessageUtil.red(
                    "使い方: /bank withdraw <amount>"
            ));
            return true;
        }

        final Integer amount =
                parsePositiveAmount(
                        player,
                        args[1]
                );

        if (amount == null) {
            return true;
        }

        final int afterBalance;

        try {
            afterBalance = bankFacade.withdraw(
                    player.getUniqueId(),
                    amount
            );
        } catch (final InsufficientBankBalanceException exception) {
            player.sendMessage(MessageUtil.red(
                    "銀行残高が不足しています。"
            ));
            return true;
        } catch (final ArithmeticException exception) {
            player.sendMessage(MessageUtil.red(
                    "手持ち所持金の上限を超えるため出金できません。"
            ));
            return true;
        }

        player.sendMessage(MessageUtil.mm(
                "<gold>"
                        + formatAmount(amount)
                        + "G</gold>"
                        + "<yellow>を銀行から出金しました。</yellow>"
                        + " <gray>銀行残高: "
                        + formatAmount(afterBalance)
                        + "G</gray>"
        ));

        return true;
    }

    /**
     * 銀行間送金コマンドを実行します。
     *
     * @param player 実行プレイヤー
     * @param args   コマンド引数
     * @return 処理済みを表すtrue
     */
    private boolean executeTransfer(
            final Player player,
            final String[] args
    ) {
        if (!player.hasPermission(BANK_TRANSFER_PERMISSION)) {
            player.sendMessage(MessageUtil.red(
                    "銀行送金を使用する権限がありません。"
            ));
            return true;
        }

        if (args.length != 3) {
            player.sendMessage(MessageUtil.red(
                    "使い方: /bank transfer <player> <amount>"
            ));
            return true;
        }

        final OfflinePlayer receiver =
                findKnownPlayer(
                        player,
                        args[1]
                );

        if (receiver == null) {
            return true;
        }

        final Integer amount =
                parsePositiveAmount(
                        player,
                        args[2]
                );

        if (amount == null) {
            return true;
        }

        final UUID senderId =
                player.getUniqueId();

        final UUID receiverId =
                receiver.getUniqueId();

        if (senderId.equals(receiverId)) {
            player.sendMessage(MessageUtil.red(
                    "自分自身には送金できません。"
            ));
            return true;
        }

        try {
            bankFacade.transfer(
                    senderId,
                    receiverId,
                    amount
            );
        } catch (final InsufficientBankBalanceException exception) {
            player.sendMessage(MessageUtil.red(
                    "銀行残高が不足しています。"
            ));
            return true;
        } catch (final ArithmeticException exception) {
            player.sendMessage(MessageUtil.red(
                    "送金先の銀行残高が上限を超えるため送金できません。"
            ));
            return true;
        }

        final String receiverName =
                Objects.requireNonNullElse(
                        receiver.getName(),
                        receiverId.toString()
                );

        player.sendMessage(MessageUtil.mm(
                "<gold>"
                        + receiverName
                        + "</gold>"
                        + "<yellow>へ</yellow>"
                        + "<gold>"
                        + formatAmount(amount)
                        + "G</gold>"
                        + "<yellow>送金しました。</yellow>"
        ));

        /*
         * オンライン中のプレイヤーには受取メッセージを通知する。
         * オフラインの場合も銀行残高自体は更新される。
         */
        final Player onlineReceiver =
                receiver.getPlayer();

        if (onlineReceiver != null) {
            onlineReceiver.sendMessage(MessageUtil.mm(
                    "<gold>"
                            + player.getName()
                            + "</gold>"
                            + "<yellow>から銀行へ</yellow>"
                            + "<gold>"
                            + formatAmount(amount)
                            + "G</gold>"
                            + "<yellow>送金されました。</yellow>"
            ));
        }

        return true;
    }

    /**
     * コマンド送信者がプレイヤーであることを確認します。
     *
     * @param sender コマンド送信者
     * @return プレイヤー。コンソールの場合null
     */
    private Player requirePlayer(
            final CommandSender sender
    ) {
        if (sender instanceof Player player) {
            return player;
        }

        sender.sendMessage(MessageUtil.red(
                "このコマンドはプレイヤーのみ使用できます。"
        ));

        return null;
    }

    /**
     * 過去にサーバーへ参加したことがあるプレイヤーを検索します。
     *
     * <p>
     * オンラインプレイヤーだけでなく、
     * オフラインプレイヤーへの送金にも対応します。
     * </p>
     *
     * @param sender     エラーメッセージ送信先
     * @param playerName プレイヤー名
     * @return 対象プレイヤー。見つからない場合null
     */
    private OfflinePlayer findKnownPlayer(
            final CommandSender sender,
            final String playerName
    ) {
        final Player onlinePlayer =
                Bukkit.getPlayerExact(playerName);

        if (onlinePlayer != null) {
            return onlinePlayer;
        }

        final OfflinePlayer offlinePlayer =
                Arrays.stream(Bukkit.getOfflinePlayers())
                        .filter(player ->
                                player.getName() != null
                                        && player.getName()
                                        .equalsIgnoreCase(playerName)
                        )
                        .findFirst()
                        .orElse(null);

        if (offlinePlayer == null) {
            sender.sendMessage(MessageUtil.red(
                    "指定したプレイヤーが見つかりません。"
            ));
        }

        return offlinePlayer;
    }

    /**
     * 既知のプレイヤー名をTab補完候補として返します。
     *
     * @param sender コマンド送信者
     * @param prefix 入力中の文字列
     * @return プレイヤー名候補
     */
    private List<String> findKnownPlayerNames(
            final CommandSender sender,
            final String prefix
    ) {
        final String normalizedPrefix =
                prefix.toLowerCase(Locale.ROOT);

        final UUID senderId =
                sender instanceof Player player
                        ? player.getUniqueId()
                        : null;

        return Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(player -> player.getName() != null)
                .filter(player ->
                        senderId == null
                                || !senderId.equals(player.getUniqueId())
                )
                .map(OfflinePlayer::getName)
                .filter(Objects::nonNull)
                .filter(name ->
                        name.toLowerCase(Locale.ROOT)
                                .startsWith(normalizedPrefix)
                )
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    /**
     * 文字列を正の整数金額へ変換します。
     *
     * @param sender    エラーメッセージ送信先
     * @param rawAmount 金額文字列
     * @return 変換後の金額。変換できない場合null
     */
    private Integer parsePositiveAmount(
            final CommandSender sender,
            final String rawAmount
    ) {
        final int amount;

        try {
            amount = Integer.parseInt(rawAmount);
        } catch (final NumberFormatException exception) {
            sender.sendMessage(MessageUtil.red(
                    "金額は整数で指定してください。"
            ));
            return null;
        }

        if (amount <= 0) {
            sender.sendMessage(MessageUtil.red(
                    "金額は1以上を指定してください。"
            ));
            return null;
        }

        return amount;
    }

    /**
     * 金額を3桁区切りの文字列へ変換します。
     *
     * @param amount 金額
     * @return フォーマット済み金額
     */
    private String formatAmount(final int amount) {
        return NumberFormat
                .getIntegerInstance(Locale.JAPAN)
                .format(amount);
    }

    /**
     * 入金または出金コマンドか判定します。
     *
     * @param subCommand サブコマンド
     * @return 入金または出金の場合true
     */
    private boolean isAmountCommand(
            final String subCommand
    ) {
        return subCommand.equalsIgnoreCase("deposit")
                || subCommand.equalsIgnoreCase("withdraw");
    }

    /**
     * Stream内の候補を入力中の文字列で前方一致検索します。
     *
     * @param candidates 補完候補
     * @param prefix     入力中の文字列
     * @return 前方一致した補完候補
     */
    private List<String> filterByPrefix(
            final Stream<String> candidates,
            final String prefix
    ) {
        final String normalizedPrefix =
                prefix.toLowerCase(Locale.ROOT);

        return candidates
                .filter(candidate ->
                        candidate.toLowerCase(Locale.ROOT)
                                .startsWith(normalizedPrefix)
                )
                .toList();
    }

    /**
     * bankコマンドの使用方法を表示します。
     *
     * @param sender メッセージ送信先
     * @return 処理済みを表すtrue
     */
    private boolean showUsage(
            final CommandSender sender
    ) {
        sender.sendMessage(MessageUtil.mm(
                """
                        <gold>銀行コマンド</gold>
                        <yellow>/bank</yellow><gray> - 銀行残高を確認</gray>
                        <yellow>/bank balance</yellow><gray> - 銀行残高を確認</gray>
                        <yellow>/bank deposit <amount></yellow><gray> - 入金</gray>
                        <yellow>/bank withdraw <amount></yellow><gray> - 出金</gray>
                        <yellow>/bank transfer <player> <amount></yellow><gray> - 銀行送金</gray>
                        """
        ));

        return true;
    }
}
