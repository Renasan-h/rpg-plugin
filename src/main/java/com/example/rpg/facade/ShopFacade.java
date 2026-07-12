package com.example.rpg.facade;

import com.example.rpg.dto.ShopCategoryDto;
import com.example.rpg.dto.ShopItemDto;
import com.example.rpg.menu.ShopMenu;
import com.example.rpg.menu.holder.CategoryMenuHolder;
import com.example.rpg.menu.holder.ItemMenuHolder;
import com.example.rpg.repository.interfaces.IShopRepository;
import com.example.rpg.service.ShopService;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * SHOP機能のFacade。
 * <p>
 * Command・Listenerからの入口を集約し、
 * GUIとServiceの責務を分離する。
 */
public class ShopFacade {
    /**
     * SHOP定義を取得するRepository。
     */
    private final IShopRepository shopRepository;

    /**
     * SHOP GUI生成クラス
     */
    private final ShopMenu shopMenu;
    /**
     * SHOP サービスクラス
     */
    private final ShopService shopService;

    /**
     * コンストラクタ
     *
     * @param shopMenu    SHOP画面管理
     * @param shopService 購入・売却処理
     */
    public ShopFacade(
            IShopRepository shopRepository,
            ShopMenu shopMenu,
            ShopService shopService) {
        this.shopRepository = shopRepository;
        this.shopMenu = shopMenu;
        this.shopService = shopService;
    }

    /**
     * SHOPカテゴリー一覧画面を表示する。
     *
     * @param player 操作プレイヤー
     */
    public void openShop(Player player) {
        // GUIはFacadeが管理するため、Commandから直接Menuを呼ばない
        shopMenu.openShopCategory(player);
    }

    /**
     * 手持ちアイテムを売却する。
     *
     * @param player 操作プレイヤー
     * @return コマンド処理結果
     */
    public boolean sellHandItem(Player player) {
        return shopService.sellHandItem(player);
    }

    /**
     * ショップGUIのクリックイベントを処理します。
     *
     * @param event クリックイベント
     */
    public void handleClick(final InventoryClickEvent event) {
        final InventoryHolder holder =
                event.getView().getTopInventory().getHolder();

        if (!isShopMenu(holder)) {
            return;
        }

        // SHOP画面を開いている間は、Shiftクリックなどによる
        // プレイヤーイベントからGUIへのアイテム移動も禁止する。
        event.setCancelled(true);

        if (!isTopInventoryClick(event)) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (holder instanceof CategoryMenuHolder) {
            handleCategoryClick(player, event);
            return;
        }

        if (holder instanceof ItemMenuHolder itemMenuHolder) {
            handleItemClick(player, event, itemMenuHolder);
        }
    }

    /**
     * SHOP機能が管理するGUIか判定する。
     *
     * @param holder 表示中インベントリのHolder
     * @return SHOP GUIの場合true
     */
    private boolean isShopMenu(final InventoryHolder holder) {
        return holder instanceof CategoryMenuHolder
                || holder instanceof ItemMenuHolder;
    }

    /**
     * 上側のSHOP GUIがクリックされたか判定する。
     *
     * @param event インベントリクリックイベント
     * @return SHOP GUI内のクリックの場合true
     */
    private boolean isTopInventoryClick(final InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            return false;
        }

        return event.getRawSlot()
                < event.getView().getTopInventory().getSize();
    }

    /**
     * カテゴリ画面のクリックを処理します。
     *
     * @param player 操作プレイヤー
     * @param event  クリックイベント
     */
    private void handleCategoryClick(
            final Player player,
            final InventoryClickEvent event
    ) {
        if (isEmptySlot(event.getCurrentItem())) {
            return;
        }

        final ShopCategoryDto category =
                shopRepository.findShopCategoryBySlot(event.getRawSlot());

        if (category == null) {
            return;
        }

        shopMenu.openShopItemByCategory(player, category);
    }

    /**
     * 商品一覧画面のクリックを処理する。
     *
     * @param player 操作プレイヤー
     * @param event  インベントリクリックイベント
     * @param holder 商品一覧GUIのHolder
     */
    private void handleItemClick(
            final Player player,
            final InventoryClickEvent event,
            final ItemMenuHolder holder
    ) {
        if (isEmptySlot(event.getCurrentItem())) {
            return;
        }

        final ShopItemDto shopItem = shopRepository.findShopItemBySlot(
                holder.getCategoryId(),
                event.getRawSlot()
        );

        if (shopItem == null) {
            return;
        }

        shopService.buy(player, shopItem);
    }

    /**
     * クリックされたスロットが空か判定する。
     *
     * @param clickedItem クリックされたアイテム
     * @return nullまたはAIRの場合true
     */
    private boolean isEmptySlot(final ItemStack clickedItem) {
        return clickedItem == null || clickedItem.getType().isAir();
    }
}
