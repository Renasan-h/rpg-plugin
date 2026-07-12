package com.example.rpg.facade;

import com.example.rpg.dto.ShopCategoryDto;
import com.example.rpg.dto.ShopItemDto;
import com.example.rpg.menu.ShopMenu;
import com.example.rpg.menu.ShopMenuAction;
import com.example.rpg.menu.holder.CategoryMenuHolder;
import com.example.rpg.menu.holder.ItemMenuHolder;
import com.example.rpg.menu.pdc.ShopPdcKeys;
import com.example.rpg.repository.interfaces.IShopRepository;
import com.example.rpg.service.ShopService;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * SHOP GUIイベントの処理入口を担当するFacade。
 *
 * <p>ListenerクラスをBukkitイベントの受け口に限定し、
 * GUI判定・画面遷移・商品クリック処理をこのクラスへ集約する。</p>
 */
public class ShopFacade {

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
     * SHOP GUIで使用するPDCキー。
     */
    private final ShopPdcKeys pdcKeys;

    /**
     * SHOP GUI Facadeを生成する。
     *
     * @param shopRepository SHOP定義Repository
     * @param shopMenu       SHOP GUI描画クラス
     * @param shopService    SHOP業務処理Service
     * @param pdcKeys        SHOP GUI用PDCキー
     */
    public ShopFacade(
            final IShopRepository shopRepository,
            final ShopMenu shopMenu,
            final ShopService shopService,
            final ShopPdcKeys pdcKeys
    ) {
        this.shopRepository = shopRepository;
        this.shopMenu = shopMenu;
        this.shopService = shopService;
        this.pdcKeys = pdcKeys;
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
     * カテゴリ一覧GUIのクリックを処理する。
     *
     * @param player 操作プレイヤー
     * @param event  クリックイベント
     */
    private void handleCategoryClick(Player player, InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();

        if (isEmptyItem(clickedItem)) {
            return;
        }

        final String categoryId = findCategoryId(clickedItem);

        if (categoryId == null) {
            return;
        }

        ShopCategoryDto category = shopRepository.findShopCategoryById(categoryId);

        if (category == null) {
            return;
        }

        shopMenu.openShopItemByCategory(player, category);
    }

    /**
     * 商品一覧GUIのクリックを処理する。
     *
     * @param player 操作プレイヤー
     * @param event  クリックイベント
     * @param holder 商品一覧GUI Holder
     */
    private void handleItemClick(
            final Player player,
            final InventoryClickEvent event,
            final ItemMenuHolder holder
    ) {
        final ItemStack clickedItem = event.getCurrentItem();

        if (isEmptyItem(clickedItem)) {
            return;
        }

        if (handlePageAction(player, clickedItem, holder)) {
            return;
        }

        final String itemId = findItemId(clickedItem);

        if (itemId == null) {
            return;
        }

        final ShopItemDto item =
                shopRepository.findShopItemById(itemId);

        if (item == null) {
            return;
        }

        shopService.buy(player, item);
    }

    /**
     * 指定ページの商品一覧画面を表示する。
     *
     * @param player 操作プレイヤー
     * @param holder 現在の画面情報
     * @param page   遷移先ページ
     */
    private void openItemPage(
            final Player player,
            final ItemMenuHolder holder,
            final int page
    ) {
        final ShopCategoryDto category =
                shopRepository.findShopCategoryById(
                        holder.getCategoryId()
                );

        if (category == null) {
            return;
        }

        shopMenu.openShopItemByCategory(
                player,
                category,
                page
        );
    }

    /**
     * PDCに保存されたページ操作を処理する。
     *
     * @param player      操作プレイヤー
     * @param clickedItem クリックされたアイテム
     * @param holder      現在の商品画面Holder
     * @return ページ操作を処理した場合true
     */
    private boolean handlePageAction(
            final Player player,
            final ItemStack clickedItem,
            final ItemMenuHolder holder
    ) {
        final String actionName = findAction(clickedItem);

        if (actionName == null) {
            return false;
        }

        final Integer targetPage = findPage(clickedItem);

        if (targetPage == null) {
            return true;
        }

        final ShopMenuAction action;

        try {
            action = ShopMenuAction.valueOf(actionName);
        } catch (IllegalArgumentException exception) {
            return true;
        }

        switch (action) {
            case PREVIOUS_PAGE, NEXT_PAGE -> openItemPage(player, holder, targetPage);
        }

        return true;
    }

    /**
     * GUIアイテムからカテゴリIDを取得する。
     *
     * @param itemStack GUIアイテム
     * @return カテゴリID。存在しない場合はnull
     */
    private String findCategoryId(final ItemStack itemStack) {
        return findString(
                itemStack,
                pdcKeys.getShopCategoryKey()
        );
    }

    /**
     * GUIアイテムから商品IDを取得する。
     *
     * @param itemStack GUIアイテム
     * @return 商品ID。存在しない場合はnull
     */
    private String findItemId(final ItemStack itemStack) {
        return findString(
                itemStack,
                pdcKeys.getShopItemKey()
        );
    }

    /**
     * GUIアイテムからアクションを取得する。
     *
     * @param itemStack GUIアイテム
     * @return アクション名。存在しない場合はnull
     */
    private String findAction(final ItemStack itemStack) {
        return findString(
                itemStack,
                pdcKeys.getActionKey()
        );
    }

    /**
     * GUIアイテムからページ番号を取得する。
     *
     * @param itemStack GUIアイテム
     * @return ページ番号。存在しない場合はnull
     */
    private Integer findPage(final ItemStack itemStack) {
        final ItemMeta meta = itemStack.getItemMeta();

        return meta.getPersistentDataContainer().get(
                pdcKeys.getPageKey(),
                PersistentDataType.INTEGER
        );
    }

    /**
     * GUIアイテムのPDCから文字列を取得する。
     *
     * @param itemStack GUIアイテム
     * @param key       取得対象キー
     * @return 保存値。存在しない場合はnull
     */
    private String findString(
            final ItemStack itemStack,
            final org.bukkit.NamespacedKey key
    ) {
        final ItemMeta meta = itemStack.getItemMeta();

        return meta.getPersistentDataContainer().get(
                key,
                PersistentDataType.STRING
        );
    }

    /**
     * アイテムが空か判定する。
     *
     * @param itemStack 対象アイテム
     * @return nullまたはAIRの場合true
     */
    private boolean isEmptyItem(final ItemStack itemStack) {
        return itemStack == null || itemStack.getType().isAir();
    }
}
