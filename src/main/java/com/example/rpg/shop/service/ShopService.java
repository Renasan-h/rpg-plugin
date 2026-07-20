package com.example.rpg.shop.service;

import com.example.rpg.common.message.MessageUtil;
import com.example.rpg.item.dto.ItemDto;
import com.example.rpg.item.factory.interfaces.IItemFactory;
import com.example.rpg.item.repository.interfaces.IItemRepository;
import com.example.rpg.item.service.ItemPdcService;
import com.example.rpg.repository.interfaces.IMoneyRepository;
import com.example.rpg.shop.constants.ShopServiceConst;
import com.example.rpg.shop.dto.ShopItemDto;
import com.example.rpg.shop.repository.interfaces.IShopPurchaseRepository;
import com.example.rpg.shop.repository.interfaces.IShopRepository;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * SHOPに関連する業務処理を担当するService
 *
 * <p>購入・売却・COMMAND商品実行・購入履歴更新など、
 * SHOPユースケースに関する処理を集約する。</p>
 *
 * <p>GUIイベントやInventory生成は担当しない。
 * ServiceをBukkit GUIから切り離すことで、将来的なNPCショップやAPI連携にも対応しやすくする。</p>
 */
public class ShopService {
    /**
     * RPGショップ商品情報Repository
     */
    private final IShopRepository shopRepository;

    /**
     * ショップ購入回数管理Repository
     */
    private final IShopPurchaseRepository shopPurchaseRepository;

    /**
     * RPG所持金管理Repository
     */
    private final IMoneyRepository moneyRepository;

    /**
     * RPGアイテム定義Repository
     */
    private final IItemRepository itemRepository;

    /**
     * ItemPersistentDataContainer操作Service
     */
    private final ItemPdcService itemPdcService;

    /**
     * RPGアイテム生成Builder。
     */
    private final IItemFactory itemFactory;

    /**
     * ShopServiceを生成する。
     *
     * @param shopRepository         SHOP定義Repository
     * @param moneyRepository        所持金Repository
     * @param shopPurchaseRepository 購入履歴Repository
     * @param itemPdcService         ItemPdc操作用サービス
     * @param itemFactory            RPGアイテム生成Factory
     * @param itemRepository         RPGアイテム定義Repository
     */
    public ShopService(
            final IShopRepository shopRepository,
            final IMoneyRepository moneyRepository,
            final IShopPurchaseRepository shopPurchaseRepository,
            final ItemPdcService itemPdcService,
            final IItemFactory itemFactory,
            final IItemRepository itemRepository
    ) {
        this.shopRepository = shopRepository;
        this.moneyRepository = moneyRepository;
        this.shopPurchaseRepository = shopPurchaseRepository;
        this.itemPdcService = itemPdcService;
        this.itemFactory = itemFactory;
        this.itemRepository = itemRepository;
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
        player.sendMessage(
                MessageUtil.mm("<yellow>購入しました: </yellow>")
                        .append(getItemDisplayName(shopItem))
                        .append(MessageUtil.mm(
                                " <gray>x" + shopItem.getAmount() + "</gray>"))
                        .append(MessageUtil.mm(
                                " <gray>-" + shopItem.getPrice() + "G</gray>"))
        );
    }

    /**
     * ITEM商品を購入者のインベントリへ追加する。
     *
     * @param player   購入者
     * @param shopItem 商品定義
     */
    private void buyItem(Player player, ShopItemDto shopItem) {
        ItemStack itemStack = itemFactory.create(shopItem.getItemId(), shopItem.getAmount());

        player.getInventory().addItem(itemStack);
    }

    /**
     * 手に持っているアイテムを1個売却する。
     *
     * @param player 売却するプレイヤー
     */
    public boolean sellHandItem(final Player player) {
        return sellHandItem(player, 1);
    }

    /**
     * 手に持っているアイテムを指定個数売却する。
     *
     * @param player 売却するプレイヤー
     */
    public boolean sellHandItem(final Player player, int amount) {
        if (amount <= 0 || amount > 64) {
            player.sendMessage(MessageUtil.red("売却個数を1~64(1stack)の範囲で指定してくだい。"));
            return false;
        }

        final ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (isEmptyHand(itemInHand)) {
            sendEmptyHandMessage(player);
            return false;
        }

        if (hasInsufficientItemAmount(itemInHand, amount)) {
            sendInsufficientItemAmountMessage(player, amount);
            return false;
        }

        final ShopItemDto shopItem = findSellableItem(itemInHand);

        if (shopItem == null) {
            sendCannotSellMessage(player);
            return false;
        }

        final int totalPrice = calculateSellPrice(shopItem, amount);

        removeSoldItem(itemInHand, amount);
        depositMoney(player, totalPrice);
        sendSellCompletedMessage(player, shopItem, amount, totalPrice);

        return true;
    }

    /**
     * 手持ちアイテムが空か判定する。
     *
     * @param itemInHand 手持ちアイテム
     * @return AIRの場合true
     */
    private boolean isEmptyHand(final ItemStack itemInHand) {
        return itemInHand.getType().isAir();
    }

    /**
     * 手持ちアイテムの個数が売却個数未満か判定する。
     *
     * @param itemInHand 手持ちアイテム
     * @param amount     売却個数
     * @return 手持ちアイテムが売却個数未満ならtrue
     */
    private boolean hasInsufficientItemAmount(final ItemStack itemInHand, int amount) {
        return itemInHand.getAmount() < amount;
    }

    /**
     * 手持ちアイテムに対応する売却可能商品を取得する。
     *
     * @param itemInHand 手持ちアイテム
     * @return 売却可能商品。存在しない場合はnull
     */
    private ShopItemDto findSellableItem(final ItemStack itemInHand) {
        final String itemId = itemPdcService.getItemId(itemInHand);

        return shopRepository.findShopSellableItem(itemId);
    }

    /**
     * 売却価格を計算する。
     *
     * @param shopItem 売却対象の商品定義
     * @param amount   売却個数
     * @return 今回の売却金額
     */
    private int calculateSellPrice(final ShopItemDto shopItem, int amount) {
        return shopItem.getSellPrice() * amount;
    }

    /**
     * 売却した個数を手持ちアイテムから減らす。
     *
     * @param itemInHand 手持ちアイテム
     * @param amount     売却個数
     */
    private void removeSoldItem(final ItemStack itemInHand, int amount) {

        // ItemStack#setAmountは0以下になった場合にAIRとして扱われるため、
        // スタック全体を削除せず今回売却した個数だけ減算する。
        itemInHand.setAmount(itemInHand.getAmount() - amount);
    }

    /**
     * 売却代金を所持金へ加算する。
     *
     * @param player 売却したプレイヤー
     * @param amount 加算する金額
     */
    private void depositMoney(final Player player, final int amount) {
        moneyRepository.addMoney(player.getUniqueId(), amount);
    }

    /**
     * 売却完了メッセージを送信する。
     *
     * @param player   売却したプレイヤー
     * @param shopItem 売却した商品
     * @param amount   売却個数
     * @param price    売却金額
     */
    private void sendSellCompletedMessage(
            final Player player,
            final ShopItemDto shopItem,
            final int amount,
            final int price
    ) {
        player.sendMessage(
                MessageUtil.mm("<yellow>売却しました: </yellow>")
                        .append(getItemDisplayName(shopItem)
                                .append(MessageUtil.mm(" <gray>x" + amount + "</gray>"))
                                .append(MessageUtil.mm(" <gold>+" + price + "G</gold>"))
                        )
        );
    }

    /**
     * 手持ちアイテムがない場合のメッセージを送信する。
     *
     * @param player 対象プレイヤー
     */
    private void sendEmptyHandMessage(final Player player) {
        player.sendMessage(
                MessageUtil.red("売却するアイテムを手に持ってください。")
        );
    }

    /**
     * 手持ちアイテムがない場合のメッセージを送信する。
     *
     * @param player 対象プレイヤー
     */
    private void sendInsufficientItemAmountMessage(final Player player, int amount) {
        final int currentAmount = player.getInventory().getItemInMainHand().getAmount();
        player.sendMessage(MessageUtil.mm(
                "<red>手持ちアイテムの個数が不足しています。</red>"
                        + " <gray>指定: " + amount
                        + " / 所持: " + currentAmount + "</gray>"
        ));
    }

    /**
     * 売却対象として登録されていない場合のメッセージを送信する。
     *
     * @param player 対象プレイヤー
     */
    private void sendCannotSellMessage(final Player player) {
        player.sendMessage(
                MessageUtil.red("このアイテムは売却できません。")
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

    /**
     * SHOP商品のRPGアイテム表示名を取得する。
     *
     * @param shopItem SHOP商品定義
     * @return MiniMessageから変換した表示名
     * @throws IllegalStateException アイテム定義が存在しない場合
     */
    private Component getItemDisplayName(final ShopItemDto shopItem) {
        final ItemDto item = itemRepository.findById(shopItem.getItemId());

        if (item == null) {
            throw new IllegalArgumentException(
                    "Item definition not found: " + shopItem.getItemId()
            );
        }

        return MessageUtil.mm(item.getDisplayName());
    }
}