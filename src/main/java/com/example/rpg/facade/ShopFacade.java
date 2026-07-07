package com.example.rpg.facade;

import com.example.rpg.dto.ShopCategoryDto;
import com.example.rpg.dto.ShopItemDto;
import com.example.rpg.menu.ShopMenu;
import com.example.rpg.menu.holder.CategoryMenuHolder;
import com.example.rpg.menu.holder.ItemMenuHolder;
import com.example.rpg.repository.ShopRepository;
import com.example.rpg.service.ShopService;
import org.bukkit.Material;
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
    private final ShopRepository shopRepository;

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
            ShopRepository shopRepository,
            ShopMenu shopMenu,
            ShopService shopService) {
        this.shopMenu = shopMenu;
        this.shopService = shopService;
        this.shopRepository = shopRepository;
    }

    /**
     * SHOPカテゴリー一覧画面を表示する。
     *
     * @param player 操作プレイヤー
     */
    public void openShop(Player player) {
        // GUIはFacadeが管理するため、Commandから直接Menuを呼ばない
        shopMenu.open(player);
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
     * SHOP GUIクリックイベントを処理する。
     *
     * <p>InventoryClickEventはBukkitの画面操作に強く依存するため、
     * ServiceではなくFacadeで受け取る。</p>
     *
     * @param event インベントリクリックイベント
     */
    public void handleClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getClickedInventory() == null) {
            return;
        }

        InventoryHolder holder = event.getView().getTopInventory().getHolder();

        if (holder instanceof CategoryMenuHolder) {
            event.setCancelled(true);
            handleCategoryClick(player, event);
        }

        if (holder instanceof ItemMenuHolder itemMenuHolder) {
            event.setCancelled(true);
            handleItemClick(player, event, itemMenuHolder);
        }
    }

    /**
     * カテゴリ一覧GUIのクリックを処理する。
     *
     * @param player 操作プレイヤー
     * @param event  インベントリクリックイベント
     */
    private void handleCategoryClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        ShopCategoryDto category = shopRepository.findCategoryBySlot(event.getRawSlot());

        if (category == null) {
            return;
        }

        shopMenu.openCategory(player, category);
    }

    /**
     * 商品一覧GUIのクリックを処理する。
     *
     * @param player 操作プレイヤー
     * @param event  インベントリクリックイベント
     * @param holder 商品一覧Holder
     */
    private void handleItemClick(Player player, InventoryClickEvent event, ItemMenuHolder holder) {
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        ShopCategoryDto category = shopRepository.getShop()
                .getCategories()
                .get(holder.getCategoryId());

        if (category == null) {
            return;
        }

        ShopItemDto item = shopRepository.findItemBySlot(category.getId(), event.getRawSlot());

        shopService.buy(player, item);
    }
}
