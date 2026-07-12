package com.example.rpg.service;

import com.example.rpg.constants.ShopServiceConst;
import com.example.rpg.dto.ShopItemDto;
import com.example.rpg.repository.interfaces.IMoneyRepository;
import com.example.rpg.repository.interfaces.IShopPurchaseRepository;
import com.example.rpg.repository.interfaces.IShopRepository;
import com.example.rpg.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * SHOPに関連する業務処理を担当するService。
 *
 * <p>購入・売却・COMMAND商品実行・購入履歴更新など、
 * SHOPユースケースに関する処理を集約する。</p>
 *
 * <p>GUIイベントやInventory生成は担当しない。
 * ServiceをBukkit GUIから切り離すことで、将来的なNPCショップやAPI連携にも対応しやすくする。</p>
 */
public class ShopService {

    /**
     * SHOP商品情報の取得元。
     */
    private final IShopRepository shopRepository;

    /**
     * 購入回数管理Service。
     */
    private final IShopPurchaseRepository shopPurchaseRepository;

    /**
     * 所持金管理Service。
     */
    private final IMoneyRepository moneyRepository;

    /**
     * ShopServiceを生成する。
     *
     * @param shopRepository         SHOP定義Repository
     * @param moneyRepository        所持金Repository
     * @param shopPurchaseRepository 購入履歴Repository
     */
    public ShopService(
            IShopRepository shopRepository,
            IMoneyRepository moneyRepository,
            IShopPurchaseRepository shopPurchaseRepository
    ) {
        this.shopRepository = shopRepository;
        this.moneyRepository = moneyRepository;
        this.shopPurchaseRepository = shopPurchaseRepository;
    }

    /**
     * 商品購入処理を実行する。
     *
     * <p>購入可否の確認を決済前に完了させることで、
     * 途中失敗時の返金処理を不要にする。</p>
     *
     * @param player   購入者
     * @param shopItem 購入対象商品
     */
    public void buy(Player player, ShopItemDto shopItem) {
        if (!hasInventorySpace(player, shopItem)) {
            player.sendMessage(MessageUtil.red("インベントリに空きがありません。"));
            return;
        }

        if (hasReachedPurchaseLimit(player, shopItem)) {
            sendLimitReachedMessage(player, shopItem);
            return;
        }

        if (!withdrawMoney(player, shopItem)) {
            sendInsufficientMoneyMessage(player, shopItem);
            return;
        }

        deliverProduct(player, shopItem);
        recordPurchase(player, shopItem);
        sendPurchaseCompletedMessage(player, shopItem);

    }

    /**
     * 購入商品を受け取るためのインベントリ空きがあるか判定する。
     *
     * <p>COMMAND商品はインベントリへ追加しないため、空きスロット確認を不要とする。</p>
     *
     * @param player   購入者
     * @param shopItem 購入対象商品
     * @return 購入可能な空きがある場合true
     */
    private boolean hasInventorySpace(Player player, ShopItemDto shopItem) {
        return shopItem.isCommandItem() || player.getInventory().firstEmpty() != -1;
    }

    /**
     * 購入上限に到達しているか判定する。
     *
     * @param player   購入者
     * @param shopItem 購入対象商品
     * @return 購入上限に到達している場合true
     */
    private boolean hasReachedPurchaseLimit(Player player, ShopItemDto shopItem) {
        if (shopItem.getLimit() < 0) {
            return false;
        }

        int currentCount = shopPurchaseRepository.findPurchaseCount(
                player.getUniqueId(),
                shopItem.getId()
        );

        return currentCount >= shopItem.getLimit();
    }

    /**
     * 購入上限到達メッセージを送信する。
     *
     * @param player   購入者
     * @param shopItem 購入対象商品
     */
    private void sendLimitReachedMessage(Player player, ShopItemDto shopItem) {
        int currentCount = shopPurchaseRepository.findPurchaseCount(
                player.getUniqueId(),
                shopItem.getId()
        );

        player.sendMessage(MessageUtil.red("この商品は購入上限に達しています。"));
        player.sendMessage(MessageUtil.mm(
                "<gray>購入済み: </gray><yellow>" + currentCount + "</yellow>" +
                        "<gray> / 上限: </gray><yellow>" + shopItem.getLimit() + "</yellow>"
        ));
    }

    /**
     * 商品代金を所持金から差し引く。
     *
     * @param player   購入者
     * @param shopItem 購入対象商品
     * @return 支払いに成功した場合true
     */
    private boolean withdrawMoney(Player player, ShopItemDto shopItem) {
        return moneyRepository.subtractMoney(player.getUniqueId(), shopItem.getPrice());
    }

    /**
     * 所持金不足メッセージを送信する。
     *
     * @param player   購入者
     * @param shopItem 購入対象商品
     */
    private void sendInsufficientMoneyMessage(Player player, ShopItemDto shopItem) {
        int currentMoney = moneyRepository.findMoney(player.getUniqueId());

        player.sendMessage(MessageUtil.red("所持金が足りません。"));
        player.sendMessage(MessageUtil.mm(
                "<gray>必要: </gray><gold>" + shopItem.getPrice() + "G</gold>" +
                        "<gray> / 現在: </gray><gold>" + currentMoney + "G</gold>"
        ));
    }

    /**
     * 購入商品をプレイヤーへ付与する。
     *
     * @param player   購入者
     * @param shopItem 購入対象商品
     */
    private void deliverProduct(Player player, ShopItemDto shopItem) {
        if (shopItem.isCommandItem()) {
            buyCommands(player, shopItem);
            return;
        }

        buyItem(player, shopItem);
    }

    /**
     * 購入履歴を記録する。
     *
     * <p>購入上限は再起動後も維持する必要があるため、
     * 商品付与完了後に永続化対象の購入回数を更新する。</p>
     *
     * @param player   購入者
     * @param shopItem 購入対象商品
     */
    private void recordPurchase(Player player, ShopItemDto shopItem) {
        shopPurchaseRepository.incrementPurchaseCount(player.getUniqueId(), shopItem.getId());
    }

    /**
     * 購入完了メッセージを送信する。
     *
     * @param player   購入者
     * @param shopItem 購入対象商品
     */
    private void sendPurchaseCompletedMessage(Player player, ShopItemDto shopItem) {
        player.sendMessage(MessageUtil.mm(
                "<yellow>購入しました: </yellow>" + shopItem.getName() +
                        " <gray>x" + shopItem.getAmount() + "</gray>" +
                        " <gray>-" + shopItem.getPrice() + "G</gray>"
        ));
    }

    /**
     * ITEM商品を購入者のインベントリへ追加する。
     *
     * @param player   購入者
     * @param shopItem 商品定義
     */
    private void buyItem(Player player, ShopItemDto shopItem) {
        ItemStack itemStack = new ItemStack(
                shopItem.getMaterial(),
                shopItem.getAmount()
        );

        player.getInventory().addItem(itemStack);
    }

    /**
     * 手に持っているアイテムを売却します。
     *
     * @param player 売却するプレイヤー
     */
    public void sellHandItem(final Player player) {
        if (isEmptyHand(player)) {
            sendCannotSellMessage(player);
            return;
        }

        final ItemStack item = player.getInventory().getItemInMainHand();
        final int sellPrice = calculateSellPrice(item);

        if (sellPrice <= 0) {
            sendCannotSellMessage(player);
            return;
        }

        removeSoldItem(player);
        depositMoney(player, sellPrice);
        sendSellCompletedMessage(player, item, sellPrice);
    }

    /**
     * プレイヤーが何も持っていないか判定します。
     *
     * @param player 対象プレイヤー
     * @return AIRの場合true
     */
    private boolean isEmptyHand(final Player player) {
        return player.getInventory().getItemInMainHand().getType().isAir();
    }

    /**
     * 売却価格を計算します。
     *
     * @param item 売却対象
     * @return 売却価格
     */
    private int calculateSellPrice(final ItemStack item) {
        return shopRepository.findSellPrice(item);
    }

    /**
     * 売却したアイテムを削除します。
     *
     * @param player 対象プレイヤー
     */
    private void removeSoldItem(final Player player) {

        // 売却済みアイテムが残ると複製できるため、
        // 通貨付与前に必ず削除する。
        player.getInventory().setItemInMainHand(null);
    }

    /**
     * 売却代金を付与します。
     *
     * @param player 対象プレイヤー
     * @param amount 金額
     */
    private void depositMoney(final Player player, final int amount) {
        moneyRepository.deposit(player.getUniqueId(), amount);
    }

    /**
     * 売却完了メッセージを送信します。
     *
     * @param player 対象プレイヤー
     * @param item   売却アイテム
     * @param price  売却価格
     */
    private void sendSellCompletedMessage(
            final Player player,
            final ItemStack item,
            final int price
    ) {
        player.sendMessage(
                MessageUtil.mm(
                        "<green>"
                                + item.getAmount()
                                + "個の"
                                + item.getType().name()
                                + " を "
                                + price
                                + "G で売却しました。"
                )
        );
    }

    /**
     * 売却できない場合のメッセージを送信します。
     *
     * @param player 対象プレイヤー
     */
    private void sendCannotSellMessage(final Player player) {
        player.sendMessage(
                MessageUtil.red(
                        "<red>このアイテムは売却できません。"
                )
        );
    }

    /**
     * COMMAND商品のコマンドをコンソール権限で実行する。
     *
     * @param player   購入者
     * @param shopItem 商品定義
     */
    private void buyCommands(Player player, ShopItemDto shopItem) {
        for (String command : shopItem.getCommands()) {
            String parsedCommand = command.replace(ShopServiceConst.REP_COMMAND_PLAYER, player.getName());

            player.getServer().dispatchCommand(
                    player.getServer().getConsoleSender(),
                    parsedCommand
            );
        }
    }
}