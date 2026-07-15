package com.example.rpg.shop.menu;

import com.example.rpg.common.message.MessageUtil;
import com.example.rpg.item.ItemBuilder;
import com.example.rpg.shop.dto.ShopCategoryDto;
import com.example.rpg.shop.dto.ShopDto;
import com.example.rpg.shop.dto.ShopItemDto;
import com.example.rpg.shop.menu.holder.CategoryMenuHolder;
import com.example.rpg.shop.menu.holder.ItemMenuHolder;
import com.example.rpg.shop.menu.pdc.ShopPdcKeys;
import com.example.rpg.shop.repository.interfaces.IShopRepository;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * SHOP GUIの生成と表示を担当するMenu
 *
 * <p>
 * Inventory生成、アイコン生成、Lore生成のみを担当し、
 * クリック処理や購入処理は担当しない。
 * </p>
 */
public class ShopMenu {

    /**
     * Minecraftのインベントリ1行分のスロット数
     */
    private static final int INVENTORY_ROW_SIZE = 9;

    /**
     * 作成可能なインベントリの最小サイズ
     */
    private static final int MIN_INVENTORY_SIZE = 9;

    /**
     * 作成可能なインベントリの最大サイズ
     */
    private static final int MAX_INVENTORY_SIZE = 54;

    /**
     * 空きスロットを装飾するアイテム
     */
    private static final Material DECORATION_MATERIAL =
            Material.GRAY_STAINED_GLASS_PANE;

    /**
     * 商品一覧画面タイトルの区切り
     */
    private static final String ITEM_TITLE_SEPARATOR =
            " <gray>-</gray> ";
    /**
     * ページ操作領域として予約する行数
     */
    private static final int NAVIGATION_ROW_SIZE = 9;
    /**
     * 前ページボタンの最終行内オフセット
     */
    private static final int PREVIOUS_PAGE_OFFSET = 0;
    /**
     * ページ情報の最終行内オフセット
     */
    private static final int PAGE_INDICATOR_OFFSET = 4;
    /**
     * 次ページボタンの最終行内オフセット。
     */
    private static final int NEXT_PAGE_OFFSET = 8;
    /**
     * カテゴリ一覧へ戻るボタンの最終行内オフセット。
     */
    private static final int BACK_CATEGORY_OFFSET = 3;
    /**
     * SHOP設定情報の取得元。
     */
    private final IShopRepository shopRepository;

    /**
     * SHOP GUIで使用するPDCキー。
     */
    private final ShopPdcKeys pdcKeys;

    /**
     * RPGアイテム生成Builder
     */
    private final ItemBuilder itemBuilder;

    /**
     * ShopMenuを生成する。
     *
     * @param shopRepository SHOP設定Repository
     * @param pdcKeys        SHOP GUI用PDCキー
     * @param itemBuilder    RPGアイテム生成Builder
     */
    public ShopMenu(
            final IShopRepository shopRepository,
            final ShopPdcKeys pdcKeys,
            final ItemBuilder itemBuilder
    ) {
        this.shopRepository = shopRepository;
        this.pdcKeys = pdcKeys;
        this.itemBuilder = itemBuilder;
    }

    /**
     * SHOPカテゴリ一覧画面を開く。
     *
     * @param player 表示対象プレイヤー
     */
    public void openShopCategory(Player player) {
        final ShopDto shop = shopRepository.getShopDto();
        final int inventorySize = normalizeInventorySize(shop.getSize());

        final Inventory inventory = Bukkit.createInventory(
                new CategoryMenuHolder(), inventorySize, MessageUtil.mm(shop.getTitle()));

        fillDecoration(inventory);
        placeCategoryIcons(inventory);
        player.openInventory(inventory);
    }

    /**
     * 指定カテゴリの商品一覧画面の1ページ目を開く。
     *
     * @param player   表示対象プレイヤー
     * @param category 表示対象カテゴリ
     */
    public void openShopItemByCategory(
            Player player,
            ShopCategoryDto category
    ) {
        openShopItemByCategory(player, category, 0);
    }

    /**
     * 指定カテゴリの商品一覧画面を開く。
     *
     * @param player   表示対象プレイヤー
     * @param category 表示対象カテゴリ
     * @param page     表示ページ番号
     */
    public void openShopItemByCategory(
            final Player player,
            final ShopCategoryDto category,
            final int page
    ) {
        final ShopDto shop = shopRepository.getShopDto();
        final int inventorySize = normalizeInventorySize(shop.getSize());
        final List<ShopItemDto> items = findDisplayableItems(player, category);
        final int pageSize = calculatePageSize(inventorySize);
        final int totalPages = calculateTotalPages(items.size(), pageSize);
        final int normalizedPage = normalizePage(page, totalPages);
        final List<ShopItemDto> pageItems = extractPageItems(items, normalizedPage, pageSize);

        final ItemMenuHolder holder = new ItemMenuHolder(
                category.getId(),
                normalizedPage
        );


        Inventory inventory = Bukkit.createInventory(
                holder,
                inventorySize,
                createItemMenuTitle(
                        shop,
                        category,
                        normalizedPage,
                        totalPages
                )
        );

        fillDecoration(inventory);
        placePageItems(
                inventory,
                pageItems,
                category.getId()
        );
        placeNavigationItems(
                inventory,
                category.getId(),
                normalizedPage,
                totalPages
        );

        player.openInventory(inventory);
    }

    /**
     * カテゴリアイコンをGUIへ配置する。
     *
     * @param inventory 配置対象Inventory
     */
    private void placeCategoryIcons(final Inventory inventory) {
        for (ShopCategoryDto category : shopRepository.findCategories()) {
            if (isInvalidSlot(inventory, category.getSlot())) {
                continue;
            }

            inventory.setItem(
                    category.getSlot(),
                    createCategoryIcon(category)
            );
        }
    }

    /**
     * カテゴリアイコンを生成する。
     *
     * @param category カテゴリ定義
     * @return カテゴリアイコン
     */
    private ItemStack createCategoryIcon(
            final ShopCategoryDto category
    ) {
        final ItemStack icon = new ItemStack(category.getIcon());
        final ItemMeta meta = icon.getItemMeta();

        meta.displayName(MessageUtil.mm(category.getName()));
        meta.lore(createCategoryLore(category));
        // 武器や防具をカテゴリ用アイコンとして使用した場合でも、
        // 攻撃力や防具値などの標準属性を表示しない。
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        final PersistentDataContainer pdc =
                meta.getPersistentDataContainer();
        // 表示スロットではなく、カテゴリIDで遷移先を判定するため、
        // GUIアイコン自身へカテゴリIDを保持する。
        pdc.set(pdcKeys.getShopCategoryKey(),
                PersistentDataType.STRING,
                category.getId()
        );

        icon.setItemMeta(meta);
        return icon;
    }

    /**
     * カテゴリアイコンのLoreを生成する。
     *
     * @param category カテゴリ定義
     * @return 表示用Lore
     */
    private List<Component> createCategoryLore(
            final ShopCategoryDto category
    ) {
        final int itemCount = shopRepository.findShopItems(category.getId()).size();

        return List.of(Component.empty(),
                MessageUtil.mm("<gray>商品数: </gray><yellow>"
                        + itemCount
                        + "</yellow>"),
                MessageUtil.mm("<green>クリックして商品を見る</green>")
        );
    }

    /**
     * 商品をプレイヤーへ表示できるか権限判定を行う。
     *
     * @param player 表示対象プレイヤー
     * @param item   商品定義
     * @return 表示可能な場合true
     */
    private boolean canDisplayItem(
            final Player player,
            final ShopItemDto item
    ) {
        return item.getPermission().isEmpty()
                || player.hasPermission(item.getPermission());
    }

    /**
     * 商品アイコンを生成する。
     *
     * @param item       商品定義
     * @param categoryId 商品が所属するカテゴリID
     * @return 商品アイコン
     */
    private ItemStack createShopItem(
            final ShopItemDto item,
            final String categoryId
    ) {
        final ItemStack itemStack = itemBuilder.build(item.getItemId(), item.getAmount());
        final ItemMeta meta = itemStack.getItemMeta();

        meta.lore(createItemLore(meta, item));

        final PersistentDataContainer pdc =
                meta.getPersistentDataContainer();

        pdc.set(
                pdcKeys.getShopCategoryKey(),
                PersistentDataType.STRING,
                categoryId
        );

        // 商品配置スロットが変更されても購入対象を特定できるよう、
        // 商品IDをGUIアイテム自身へ保存する。
        pdc.set(
                pdcKeys.getShopItemKey(),
                PersistentDataType.STRING,
                item.getId()
        );

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    /**
     * RPGアイテム本来のLoreへSHOP表示情報を追加する。
     *
     * @param meta ItemBuilderが生成したItemMeta
     * @param item SHOP商品定義
     * @return SHOP表示用Lore
     */
    private List<Component> createItemLore(final ItemMeta meta, final ShopItemDto item) {
        final List<Component> lore = new ArrayList<>();
        final List<Component> itemLore = meta.lore();

        if (itemLore != null && !itemLore.isEmpty()) {
            lore.addAll(itemLore);
        }

        lore.add(Component.empty());
        lore.add(MessageUtil.mm(
                "<yellow>価格: "
                        + item.getPrice()
                        + "G</yellow>"
        ));

        if (item.isSellable()) {
            lore.add(MessageUtil.mm(
                    "<gray>売却: "
                            + item.getSellPrice()
                            + "G</gray>"
            ));
        }

        lore.add(MessageUtil.mm(
                "<green>クリックして購入</green>"
        ));

        return List.copyOf(lore);
    }

    /**
     * 商品一覧画面のタイトルを生成する。
     *
     * @param shop        SHOP定義
     * @param category    カテゴリ定義
     * @param currentPage 現在ページ
     * @param totalPages  総ページ数
     * @return 商品一覧画面タイトル
     */
    private Component createItemMenuTitle(
            final ShopDto shop,
            final ShopCategoryDto category,
            final int currentPage,
            final int totalPages
    ) {
        return MessageUtil.mm(
                shop.getTitle()
                        + ITEM_TITLE_SEPARATOR
                        + category.getName()
                        + " <gray>("
                        + (currentPage + 1)
                        + "/"
                        + totalPages
                        + ")</gray>"
        );
    }

    /**
     * GUIの空きスロットを装飾する。
     *
     * @param inventory 装飾対象Inventory
     */
    private void fillDecoration(final Inventory inventory) {
        final ItemStack decoration = createDecorationItem();

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            // 商品やカテゴリで後から上書きする前提で、
            // 先に全スロットへ背景アイテムを配置する。
            inventory.setItem(slot, decoration);
        }
    }

    /**
     * 空きスロット用の装飾アイテムを生成する。
     *
     * @return 装飾アイテム
     */
    private ItemStack createDecorationItem() {
        final ItemStack decoration = new ItemStack(DECORATION_MATERIAL);

        final ItemMeta meta = decoration.getItemMeta();

        // アイテム名を空で設定し、装飾クリック時に不要な文字を表示しない
        meta.displayName(Component.empty());
        meta.setHideTooltip(true);

        decoration.setItemMeta(meta);

        return decoration;
    }

    /**
     * スロット番号がInventoryの範囲外(0未満 または inventory size以上)か判定する。
     *
     * @param inventory 対象Inventory
     * @param slot      スロット番号
     * @return 有効な場合true
     */
    private boolean isInvalidSlot(
            final Inventory inventory,
            final int slot
    ) {
        return slot < 0 || slot >= inventory.getSize();
    }

    /**
     * 設定されたGUIサイズを作成可能な値へ正規化する。
     *
     * @param configuredSize 設定されたサイズ
     * @return 9の倍数かつ9～54の範囲のサイズ
     */
    private int normalizeInventorySize(final int configuredSize) {
        final int boundedSize = Math.clamp(
                configuredSize, MIN_INVENTORY_SIZE, MAX_INVENTORY_SIZE);

        final int remainder = boundedSize % INVENTORY_ROW_SIZE;

        if (remainder == 0) {
            return boundedSize;
        }

        // Bukkitは9の倍数以外のチェストGUIを作成できないため、
        // 設定値を切り上げ最も近い有効サイズへ補正する
        return Math.min(boundedSize + INVENTORY_ROW_SIZE - remainder,
                MAX_INVENTORY_SIZE);
    }

    /**
     * 表示可能な商品一覧を取得する。
     *
     * @param player   表示対象プレイヤー
     * @param category 表示対象カテゴリ
     * @return 表示可能商品一覧
     */
    private List<ShopItemDto> findDisplayableItems(
            final Player player,
            final ShopCategoryDto category
    ) {
        return shopRepository.findShopItems(category.getId())
                .stream()
                .filter(item -> canDisplayItem(player, item))
                .toList();
    }

    /**
     * 現在ページの商品を抽出する。
     *
     * @param items    全商品
     * @param page     ページ番号
     * @param pageSize 1ページの表示件数
     * @return 現在ページの商品
     */
    private List<ShopItemDto> extractPageItems(
            final List<ShopItemDto> items,
            final int page,
            final int pageSize
    ) {
        final int fromIndex = page * pageSize;
        final int toIndex = Math.min(
                fromIndex + pageSize,
                items.size()
        );

        if (fromIndex >= items.size()) {
            return List.of();
        }

        return List.copyOf(items.subList(fromIndex, toIndex));
    }

    /**
     * 現在ページの商品をGUIへ配置する。
     *
     * @param inventory  配置対象Inventory
     * @param pageItems  現在ページの商品
     * @param categoryId 表示中カテゴリID
     */
    private void placePageItems(
            final Inventory inventory,
            final List<ShopItemDto> pageItems,
            final String categoryId
    ) {
        IntStream.range(0, pageItems.size())
                .forEach(slot -> inventory.setItem(
                        slot,
                        createShopItem(pageItems.get(slot),
                                categoryId)
                ));
    }

    /**
     * ページ操作アイテムを配置する。
     *
     * @param inventory   配置対象Inventory
     * @param categoryId  表示中カテゴリID
     * @param currentPage 現在ページ
     * @param totalPages  総ページ数
     */
    private void placeNavigationItems(
            final Inventory inventory,
            final String categoryId,
            final int currentPage,
            final int totalPages
    ) {
        inventory.setItem(
                getBackCategorySlot(inventory.getSize()),
                createActionItem(
                        Material.BARRIER,
                        "<red>カテゴリ一覧へ戻る</red>",
                        ShopMenuAction.BACK_CATEGORY
                )
        );

        inventory.setItem(
                getPageIndicatorSlot(inventory.getSize()),
                createPageIndicator(currentPage, totalPages)
        );

        if (currentPage > 0) {
            inventory.setItem(
                    getPreviousPageSlot(inventory.getSize()),
                    createNavigationItem(
                            Material.ARROW,
                            "<yellow>前のページ</yellow>",
                            categoryId,
                            currentPage - 1,
                            ShopMenuAction.PREVIOUS_PAGE
                    )
            );
        }

        if (currentPage + 1 < totalPages) {
            inventory.setItem(
                    getNextPageSlot(inventory.getSize()),
                    createNavigationItem(
                            Material.ARROW,
                            "<yellow>次のページ</yellow>",
                            categoryId,
                            currentPage + 1,
                            ShopMenuAction.NEXT_PAGE
                    )
            );
        }
    }

    /**
     * ページ番号を必要としないGUI操作アイテムを生成する。
     *
     * @param material 表示Material
     * @param name     表示名
     * @param action   実行アクション
     * @return GUI操作アイテム
     */
    private ItemStack createActionItem(
            final Material material,
            final String name,
            final ShopMenuAction action
    ) {
        final ItemStack itemStack = new ItemStack(material);
        final ItemMeta meta = itemStack.getItemMeta();

        meta.displayName(MessageUtil.mm(name));
        meta.getPersistentDataContainer().set(
                pdcKeys.getActionKey(),
                PersistentDataType.STRING,
                action.name()
        );

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    /**
     * カテゴリ一覧へ戻るボタンのスロットを取得する。
     *
     * @param inventorySize GUIサイズ
     * @return 戻るボタンのスロット番号
     */
    private int getBackCategorySlot(final int inventorySize) {
        return inventorySize
                - NAVIGATION_ROW_SIZE
                + BACK_CATEGORY_OFFSET;
    }

    /**
     * ページ情報アイテムを生成する。
     *
     * @param currentPage 現在ページ
     * @param totalPages  総ページ数
     * @return ページ情報アイテム
     */
    private ItemStack createPageIndicator(
            final int currentPage,
            final int totalPages
    ) {
        final String name = "<gold>"
                + (currentPage + 1)
                + " / "
                + totalPages
                + " ページ</gold>";

        return createDisplayItem(
                Material.PAPER,
                name
        );
    }

    /**
     * 表示専用のGUIアイテムを生成する。
     *
     * <p>
     * ページ表示など、クリック時に処理を行わないアイテムに使用する。
     * </p>
     *
     * @param material 表示Material
     * @param name     表示名
     * @return 表示専用アイテム
     */
    private ItemStack createDisplayItem(
            final Material material,
            final String name
    ) {
        final ItemStack itemStack = new ItemStack(material);
        final ItemMeta meta = itemStack.getItemMeta();

        meta.displayName(MessageUtil.mm(name));

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    /**
     * ページ操作アイテムを生成する。
     *
     * @param material   表示Material
     * @param name       表示名
     * @param categoryId 表示中カテゴリID
     * @param targetPage 遷移先ページ
     * @param action     実行アクション
     * @return ページ操作アイテム
     */
    private ItemStack createNavigationItem(
            final Material material,
            final String name,
            final String categoryId,
            final int targetPage,
            final ShopMenuAction action
    ) {
        final ItemStack itemStack = new ItemStack(material);
        final ItemMeta meta = itemStack.getItemMeta();

        meta.displayName(MessageUtil.mm(name));

        final PersistentDataContainer pdc =
                meta.getPersistentDataContainer();

        pdc.set(
                pdcKeys.getShopCategoryKey(),
                PersistentDataType.STRING,
                categoryId
        );

        pdc.set(
                pdcKeys.getPageKey(),
                PersistentDataType.INTEGER,
                targetPage
        );

        pdc.set(
                pdcKeys.getActionKey(),
                PersistentDataType.STRING,
                action.name()
        );

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    /**
     * 商品表示領域のスロット数を計算する。
     *
     * @param inventorySize GUIサイズ
     * @return 1ページの表示件数
     */
    private int calculatePageSize(final int inventorySize) {
        return inventorySize - NAVIGATION_ROW_SIZE;
    }

    /**
     * 総ページ数を計算する。
     *
     * @param itemCount 商品数
     * @param pageSize  1ページの表示件数
     * @return 総ページ数
     */
    private int calculateTotalPages(
            final int itemCount,
            final int pageSize
    ) {
        return Math.max(
                1,
                (itemCount + pageSize - 1) / pageSize
        );
    }

    /**
     * ページ番号を有効範囲へ補正する。
     *
     * @param page       指定ページ
     * @param totalPages 総ページ数
     * @return 補正後ページ
     */
    private int normalizePage(
            final int page,
            final int totalPages
    ) {
        return Math.clamp(page, 0, totalPages - 1);
    }

    /**
     * 前ページボタンのスロットを取得する。
     *
     * @param inventorySize GUIサイズ
     * @return スロット番号
     */
    public int getPreviousPageSlot(final int inventorySize) {
        return inventorySize - NAVIGATION_ROW_SIZE + PREVIOUS_PAGE_OFFSET;
    }

    /**
     * ページ情報のスロットを取得する。
     *
     * @param inventorySize GUIサイズ
     * @return スロット番号
     */
    private int getPageIndicatorSlot(final int inventorySize) {
        return inventorySize
                - NAVIGATION_ROW_SIZE
                + PAGE_INDICATOR_OFFSET;
    }

    /**
     * 次ページボタンのスロットを取得する。
     *
     * @param inventorySize GUIサイズ
     * @return スロット番号
     */
    public int getNextPageSlot(final int inventorySize) {
        return inventorySize
                - NAVIGATION_ROW_SIZE
                + NEXT_PAGE_OFFSET;
    }

}