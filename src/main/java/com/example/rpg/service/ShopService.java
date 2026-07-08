package com.example.rpg.service;

import com.example.rpg.constants.ShopServiceConst;
import com.example.rpg.dto.ShopItemDto;
import com.example.rpg.repository.interfaces.IMoneyRepository;
import com.example.rpg.repository.interfaces.IShopPurchaseRepository;
import com.example.rpg.repository.interfaces.IShopRepository;
import com.example.rpg.util.MessageUtil;
import org.bukkit.Material;
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
     * <p>購入制限・所持金不足・インベントリ空きチェックをまとめて扱う。
     * 購入可否判定は必ず決済前に行い、返金処理が必要にならない順序にしている。</p>
     *
     * @param player   購入者
     * @param shopItem 購入対象商品
     */
    public void buy(Player player, ShopItemDto shopItem) {
        if (player.getInventory().firstEmpty() == -1 && !shopItem.isCommandItem()) {
            player.sendMessage(MessageUtil.red("インベントリに空きがありません。"));
            return;
        }

        if (shopItem.getLimit() >= 0) {
            int currentCount = shopPurchaseRepository.findPurchaseCount(player.getUniqueId(), shopItem.getId());

            if (currentCount >= shopItem.getLimit()) {
                player.sendMessage(MessageUtil.red("この商品は購入上限に達しています。"));
                player.sendMessage(MessageUtil.mm(
                        "<gray>購入済み: </gray><yellow>" + currentCount + "</yellow>" +
                                "<gray> / 上限: </gray><yellow>" + shopItem.getLimit() + "</yellow>"
                ));
                return;
            }
        }

        if (!moneyRepository.subtractMoney(player.getUniqueId(), shopItem.getPrice())) {
            int currentMoney = moneyRepository.findMoney(player.getUniqueId());

            player.sendMessage(MessageUtil.red("所持金が足りません。"));
            player.sendMessage(MessageUtil.mm(
                    "<gray>必要: </gray><gold>" + shopItem.getPrice() + "G</gold>" +
                            "<gray> / 現在: </gray><gold>" + currentMoney + "G</gold>"
            ));
            return;
        }

        if (shopItem.isCommandItem()) {
            buyCommands(player, shopItem);
        } else {
            buyItem(player, shopItem);
        }

        shopPurchaseRepository.incrementPurchaseCount(player.getUniqueId(), shopItem.getId());

        player.sendMessage(MessageUtil.mm(
                "<yellow>購入しました: </yellow>" + shopItem.getName() +
                        " <gray>x" + shopItem.getAmount() + "</gray>" +
                        " <gray>-" + shopItem.getPrice() + "G</gray>"
        ));
    }

    /**
     * 手持ちアイテムを売却する。
     *
     * @param player 売却者
     * @return 売却処理結果
     */
    public boolean sellHandItem(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType() == Material.AIR) {
            player.sendMessage(MessageUtil.red("売却するアイテムを手に持ってください。"));
            return true;
        }

        ShopItemDto shopItem = shopRepository.findShopSellableItem(itemInHand.getType());

        if (shopItem == null) {
            player.sendMessage(MessageUtil.red("このアイテムは売却できません。"));
            return true;
        }

        int sellAmount = 1;
        int totalPrice = shopItem.getSellPrice() * sellAmount;

        itemInHand.setAmount(itemInHand.getAmount() - sellAmount);
        moneyRepository.addMoney(player.getUniqueId(), totalPrice);

        player.sendMessage(MessageUtil.mm(
                "<yellow>売却しました: </yellow>" +
                        shopItem.getName() +
                        " <gray>x" + sellAmount + "</gray>" +
                        " <gold>+" + totalPrice + "G</gold>"
        ));

        return true;
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