package com.example.rpg.admin.command;

import com.example.rpg.admin.config.ReloadTarget;
import com.example.rpg.admin.service.ConfigurationReloadService;
import com.example.rpg.common.message.MessageUtil;
import com.example.rpg.shop.repository.interfaces.IShopPurchaseRepository;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class AdminCommand implements CommandExecutor, TabCompleter {

    /**
     * 管理者権限。
     */
    private static final String ADMIN_PERMISSION = "rpg.admin";

    /**
     * 購入履歴管理Repository
     */
    private final IShopPurchaseRepository shopPurchaseRepository;
    
    /**
     * 設定再読み込みService。
     */
    private final ConfigurationReloadService reloadService;

    /**
     * RPG管理コマンドを生成する。
     *
     * @param reloadService 設定再読み込みService
     */
    public AdminCommand(
            final ConfigurationReloadService reloadService,
            final IShopPurchaseRepository shopPurchaseRepository
    ) {
        this.reloadService = Objects.requireNonNull(
                reloadService,
                "reloadService must not be null"
        );
        this.shopPurchaseRepository = Objects.requireNonNull(
                shopPurchaseRepository,
                "shopPurchaseRepository must not be null"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(MessageUtil.red("権限がありません。"));
            return false;
        }

        if (args[0].equalsIgnoreCase("shopreset")) {
            return resetShopByPlayer(sender, args);
        }

        if (args[0].equalsIgnoreCase("reload")) {
            return reloadConfig(sender, args);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args) {

        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            return List.of();
        }

        if (args.length == 1) {
            return filterCandidates(
                    List.of("reload"),
                    args[0]
            );
        }

        if (args.length == 2
                && args[0].equalsIgnoreCase("reload")) {
            return filterCandidates(
                    ReloadTarget.commandNames(),
                    args[1]
            );
        }

        return List.of();
    }

    /**
     * 入力済み文字列に一致する補完候補を返す。
     *
     * @param candidates 補完候補
     * @param input      入力済み文字列
     * @return 一致する補完候補
     */
    private List<String> filterCandidates(
            final List<String> candidates,
            final String input
    ) {
        final String normalizedInput =
                input.toLowerCase(Locale.ROOT);

        return candidates.stream()
                .filter(candidate ->
                        candidate.startsWith(normalizedInput))
                .toList();
    }

    /**
     * 指定されたプレイヤーのSHOP購入制限をリセットする
     */
    private boolean resetShopByPlayer(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendShopResetUsage(sender);
            return false;
        }

        Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null) {
            sender.sendMessage(MessageUtil.red("指定したプレイヤーが見つかりません。"));
            return false;
        }

        shopPurchaseRepository.resetPlayer(target.getUniqueId());

        sender.sendMessage(MessageUtil.green(target.getName() + " のSHOP購入履歴をリセットしました。"));

        return true;
    }

    /**
     * 各種設定ファイルの再読み込みを行う
     *
     * @param sender Source of the command. For players tab-completing a command inside of a command block, this will be the player, not the command block.
     * @param args   The arguments passed to the command, including final partial argument to be completed
     * @return true if a valid command, otherwise false
     */
    private boolean reloadConfig(
            CommandSender sender,
            String[] args
    ) {
        final Optional<ReloadTarget> reloadTarget = parseReloadTarget(args);

        if (reloadTarget.isEmpty()) {
            sendReloadUsage(sender);
            return true;
        }

        try {
            reloadService.reload(reloadTarget.get());
            sender.sendMessage(
                    MessageUtil.green(
                            "設定を再読み込みしました: "
                                    + reloadTarget
                    )
            );
        } catch (RuntimeException ex) {
            sender.sendMessage(
                    MessageUtil.red(
                            "設定の再読み込みに失敗しました。"
                                    + " サーバーログを確認してください。"
                    )
            );

            /*
             * Bukkitへ例外を再送出するとコマンドエラーとして
             * スタックトレースが記録される。
             */
            throw ex;
        }

        return true;
    }

    /**
     * コマンド引数から再読み込み対象を取得する。
     *
     * <p>
     * {@code /rpg reload}は全件再読み込みとして扱う。
     * </p>
     *
     * @param args コマンド引数
     * @return 再読み込み対象
     */
    private Optional<ReloadTarget> parseReloadTarget(
            final String[] args
    ) {
        if (args.length == 1) {
            return Optional.of(ReloadTarget.ALL);
        }

        if (args.length != 2) {
            return Optional.empty();
        }

        return ReloadTarget.fromCommandName(args[1]);
    }

    /**
     * ショップ購入履歴リセットコマンドの使用方法を送信する。
     *
     * @param sender 送信先
     */
    private void sendShopResetUsage(CommandSender sender) {
        sender.sendMessage(MessageUtil.yellow("使用方法: /admin shopreset <player>"));
    }

    /**
     * 設定ファイル再読み込みコマンドの使用方法を送信する。
     *
     * @param sender 送信先
     */
    private void sendReloadUsage(final CommandSender sender) {
        sender.sendMessage(
                MessageUtil.red(
                        "使い方: /rpg reload "
                                + "[all|config|items|attributes"
                                + "|enchantments|effects|shop]"
                )
        );
    }
}
