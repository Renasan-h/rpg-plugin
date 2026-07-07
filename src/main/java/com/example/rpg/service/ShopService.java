package com.example.rpg.service;

import com.example.rpg.constants.ShopServiceConst;
import com.example.rpg.dto.ShopItemDto;
import com.example.rpg.repository.ShopRepository;
import com.example.rpg.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * SHOPの購入・売却などの業務処理を担当するService。
 *
 * <p>GUIの生成は {@code ShopMenu}、コマンドやイベントからの入口はFacadeが担当する。
 * このServiceは、購入可否判定・所持金操作・購入回数制限・売却処理に責務を限定する。</p>
 */
public class ShopService {

    /**
     * SHOP商品情報の取得元。
     */
    private final ShopRepository shopRepository;

    /**
     * 購入回数管理Service。
     */
    private final ShopPurchaseService shopPurchaseService;

    /**
     * 所持金管理Service。
     */
    private final MoneyService moneyService;

    /**
     * コンストラクタ。
     *
     * @param shopRepository      SHOP商品Repository
     * @param shopPurchaseService 購入回数管理Service
     * @param moneyService        所持金管理Service
     */
    public ShopService(
            ShopRepository shopRepository,
            ShopPurchaseService shopPurchaseService,
            MoneyService moneyService
    ) {
        this.shopRepository = shopRepository;
        this.shopPurchaseService = shopPurchaseService;
        this.moneyService = moneyService;
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
            int currentCount = shopPurchaseService.getPurchaseCount(player.getUniqueId(), shopItem.getId());

            if (currentCount >= shopItem.getLimit()) {
                player.sendMessage(MessageUtil.red("この商品は購入上限に達しています。"));
                player.sendMessage(MessageUtil.mm(
                        "<gray>購入済み: </gray><yellow>" + currentCount + "</yellow>" +
                                "<gray> / 上限: </gray><yellow>" + shopItem.getLimit() + "</yellow>"
                ));
                return;
            }
        }

        if (!moneyService.removeMoney(player.getUniqueId(), shopItem.getPrice())) {
            int currentMoney = moneyService.getMoney(player.getUniqueId());

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

        shopPurchaseService.addPurchaseCount(player.getUniqueId(), shopItem.getId());

        player.sendMessage(MessageUtil.mm(
                "<yellow>購入しました: </yellow>" + shopItem.getName() +
                        " <gray>x" + shopItem.getAmount() + "</gray>" +
                        " <gray>-" + shopItem.getPrice() + "G</gray>"
        ));
    }

    /**
     * 手に持っているアイテムを1個売却する。
     *
     * @param player 売却者
     * @return コマンド処理完了ならtrue
     */
    public boolean sellHandItem(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType() == Material.AIR) {
            player.sendMessage(MessageUtil.red("売却するアイテムを手に持ってください。"));
            return true;
        }

        ShopItemDto shopItem = shopRepository.findSellableItem(itemInHand.getType());

        if (shopItem == null) {
            player.sendMessage(MessageUtil.red("このアイテムは売却できません。"));
            return true;
        }

        int sellAmount = 1;
        int totalPrice = shopItem.getSellPrice() * sellAmount;

        itemInHand.setAmount(itemInHand.getAmount() - sellAmount);
        moneyService.addMoney(player.getUniqueId(), totalPrice);

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