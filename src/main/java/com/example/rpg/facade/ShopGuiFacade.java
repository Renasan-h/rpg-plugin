package com.example.rpg.facade;

import com.example.rpg.dto.ShopCategoryDto;
import com.example.rpg.dto.ShopItemDto;
import com.example.rpg.menu.ShopMenu;
import com.example.rpg.menu.holder.CategoryMenuHolder;
import com.example.rpg.menu.holder.ItemMenuHolder;
import com.example.rpg.repository.interfaces.IShopRepository;
import com.example.rpg.service.ShopService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * SHOP GUIイベントの処理入口を担当するFacade。
 *
 * <p>ListenerクラスをBukkitイベントの受け口に限定し、
 * GUI判定・画面遷移・商品クリック処理をこのクラスへ集約する。</p>
 */
public class ShopGuiFacade {

    /**
     * SHOP定義Repository。
     */
    private final IShopRepository shopRepository;

    /**
     * SHOP GUI描画クラス。
     */
    private final ShopMenu shopMenu;

    /**
     * SHOP業務処理Service。
     */
    private final ShopService shopService;

    /**
     * SHOP Listener Facadeを生成する。
     *
     * @param ShopRepository SHOP定義Repository
     * @param shopMenu       SHOP GUI描画クラス
     * @param shopService    SHOP業務処理Service
     */
    public ShopGuiFacade(
            IShopRepository shopRepository,
            ShopMenu shopMenu,
            ShopService shopService
    ) {
        this.shopRepository = shopRepository;
        this.shopMenu = shopMenu;
        this.shopService = shopService;
    }

    public void openCategory(Player player) {
        shopMenu.openShopCategory(player);
    }

    /**
     * SHOP GUIクリックイベントを処理する。
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

        // 下側プレイヤーインベントリはSHOP GUIではないので、処理対象外
        if (event.getRawSlot() >= event.getView().getTopInventory().getSize()) {
            return;
        }

        InventoryHolder holder = event.getView().getTopInventory().getHolder();

        if (holder instanceof CategoryMenuHolder) {
            event.setCancelled(true);
            handleCategoryClick(player, event);
            return;
        }

        if (holder instanceof ItemMenuHolder itemMenuHolder) {
            event.setCancelled(true);
            handleItemClick(player, event, itemMenuHolder);
        }
    }

    /**
     * カテゴリ一覧GUIのクリック処理
     *
     * @param player 操作プレイヤー
     * @param event  インベントリクリックイベント
     */
    private void handleCategoryClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        ShopCategoryDto category = shopRepository.findShopCategoryBySlot(event.getRawSlot());

        if (category == null) {
            return;
        }

        shopMenu.openShopItemByCategory(player, category);
    }

    /**
     * 商品一覧GUIのクリックを処理する。
     *
     * @param player 操作プレイヤー
     * @param event  インベントリクリックイベント
     * @param holder 商品一覧GUI Holder
     */
    private void handleItemClick(Player player, InventoryClickEvent event, ItemMenuHolder holder) {
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        ShopItemDto item = shopRepository.findShopItemBySlot(holder.getCategoryId(), event.getRawSlot());

        if (item == null) {
            return;
        }

        shopService.buy(player, item);
    }
}
