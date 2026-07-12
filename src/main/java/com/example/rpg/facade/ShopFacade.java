package com.example.rpg.facade;

import com.example.rpg.menu.ShopMenu;
import com.example.rpg.menu.holder.CategoryMenuHolder;
import com.example.rpg.menu.holder.ItemMenuHolder;
import com.example.rpg.repository.ShopRepository;
import com.example.rpg.service.ShopService;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

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
    private final ShopRepository shopRepositoryImpl;

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
            ShopRepository shopRepositoryImpl,
            ShopMenu shopMenu,
            ShopService shopService) {
        this.shopMenu = shopMenu;
        this.shopService = shopService;
        this.shopRepositoryImpl = shopRepositoryImpl;
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

        if (!validateClick(event)) {
            return;
        }

        final InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof CategoryMenuHolder categoryHolder) {
            handleCategoryClick(event, categoryHolder);
            return;
        }

        if (holder instanceof ItemMenuHolder itemHolder) {
            handleItemClick(event, itemHolder);
        }
    }

    /**
     * ショップGUIのクリックイベントを検証します。
     *
     * @param event クリックイベント
     * @return 処理対象の場合true
     */
    private boolean validateClick(final InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) {
            return false;
        }

        if (event.getClickedInventory() == null) {
            return false;
        }

        if (event.getCurrentItem() == null) {
            return false;
        }

        // BukkitはAIRクリックでもイベントを発火するため、
        // 空スロットは購入対象外とする。
        if (event.getCurrentItem().getType().isAir()) {
            return false;
        }

        return event.getInventory().getHolder() instanceof ShopMenuHolder;
    }

    /**
     * カテゴリ画面のクリックを処理します。
     *
     * @param event          クリックイベント
     * @param categoryHolder カテゴリホルダー
     */
    private void handleCategoryClick(
            final InventoryClickEvent event,
            final CategoryMenuHolder categoryHolder
    ) {
        final Player player = (Player) event.getWhoClicked();

        event.setCancelled(true);

        shopMenu.openItemMenu(
                player,
                categoryHolder.getShopId(),
                event.getSlot()
        );
    }

    /**
     * 商品画面のクリックを処理します。
     *
     * @param event      クリックイベント
     * @param itemHolder 商品ホルダー
     */
    private void handleItemClick(
            final InventoryClickEvent event,
            final ItemMenuHolder itemHolder
    ) {
        event.setCancelled(true);

        handleActionClick(event, itemHolder);
    }

    /**
     * 商品画面のアクションを処理します。
     *
     * @param event      クリックイベント
     * @param itemHolder 商品ホルダー
     */
    private void handleActionClick(
            final InventoryClickEvent event,
            final ItemMenuHolder itemHolder
    ) {
        final Player player = (Player) event.getWhoClicked();

        switch (event.getClick()) {
            case LEFT -> shopService.buy(
                    player,
                    itemHolder.getShopId(),
                    itemHolder.getCategoryId(),
                    event.getSlot()
            );

            case RIGHT -> shopService.sellHandItem(player);

            default -> {
                // 左右クリック以外はショップ操作対象外とする。
            }
        }
    }
}
