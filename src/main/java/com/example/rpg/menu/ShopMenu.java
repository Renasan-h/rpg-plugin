package com.example.rpg.menu;

import com.example.rpg.dto.ShopCategoryDto;
import com.example.rpg.dto.ShopDto;
import com.example.rpg.dto.ShopItemDto;
import com.example.rpg.menu.holder.CategoryMenuHolder;
import com.example.rpg.menu.holder.ItemMenuHolder;
import com.example.rpg.repository.interfaces.IShopRepository;
import com.example.rpg.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * SHOP GUIの生成と表示を担当するMenu。
 *
 * <p>
 * Inventory生成、アイコン生成、Lore生成のみを担当し、
 * クリック処理や購入処理は担当しない。
 * </p>
 */
public class ShopMenu {

    /**
     * Minecraftのインベントリ1行分のスロット数。
     */
    private static final int INVENTORY_ROW_SIZE = 9;

    /**
     * 作成可能なインベントリの最小サイズ。
     */
    private static final int MIN_INVENTORY_SIZE = 9;

    /**
     * 作成可能なインベントリの最大サイズ。
     */
    private static final int MAX_INVENTORY_SIZE = 54;

    /**
     * 空きスロットを装飾するアイテム。
     */
    private static final Material DECORATION_MATERIAL =
            Material.GRAY_STAINED_GLASS_PANE;

    /**
     * 商品一覧画面タイトルの区切り。
     */
    private static final String ITEM_TITLE_SEPARATOR =
            " <gray>-</gray> ";

    /**
     * SHOP設定情報の取得元。
     */
    private final IShopRepository shopRepository;

    /**
     * コンストラクタ。
     *
     * @param shopRepository SHOP設定Repository
     */
    public ShopMenu(IShopRepository shopRepository) {
        this.shopRepository = shopRepository;
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
     * 指定カテゴリの商品一覧画面を開く。
     *
     * @param player   表示対象プレイヤー
     * @param category 表示対象カテゴリ
     */
    public void openShopItemByCategory(
            Player player, ShopCategoryDto category
    ) {
        final ShopDto shop = shopRepository.getShopDto();
        final int inventorySize = normalizeInventorySize(shop.getSize());

        Inventory inventory = Bukkit.createInventory(
                new ItemMenuHolder(category.getId()),
                inventorySize,
                createItemMenuTitle(shop, category)
        );

        fillDecoration(inventory);
        placeShopItems(inventory, player, category);
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
     * 商品アイコンをGUIへ配置する。
     *
     * @param inventory 配置対象Inventory
     * @param player    表示対象プレイヤー
     * @param category  表示対象カテゴリ
     */
    private void placeShopItems(
            final Inventory inventory,
            final Player player,
            final ShopCategoryDto category
    ) {
        for (ShopItemDto item : shopRepository.findShopItems(category.getId())) {
            if (!canDisplayItem(player, item)) {
                continue;
            }

            if (isInvalidSlot(inventory, item.getSlot())) {
                continue;
            }

            inventory.setItem(item.getSlot(), createShopItem(item));
        }
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
     * @param item 商品定義
     * @return 商品アイコン
     */
    private ItemStack createShopItem(final ShopItemDto item) {
        final ItemStack itemStack = new ItemStack(item.getMaterial(), item.getAmount());
        final ItemMeta meta = itemStack.getItemMeta();

        meta.displayName(MessageUtil.mm(item.getDisplayName()));
        meta.lore(createItemLore(item));

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    /**
     * 商品Loreを生成する。
     *
     * @param item 商品定義
     * @return 表示用Lore
     */
    private List<Component> createItemLore(final ShopItemDto item) {
        final List<Component> lore = new ArrayList<>(
                item.getLore()
                        .stream()
                        .map(MessageUtil::mm)
                        .toList()
        );

        lore.add(Component.empty());
        lore.add(MessageUtil.mm("<green>クリックして購入</green>"));

        return List.copyOf(lore);
    }

    /**
     * 商品一覧画面のタイトルを生成する。
     *
     * @param shop     SHOP定義
     * @param category カテゴリ定義
     * @return 商品一覧画面タイトル
     */
    private Component createItemMenuTitle(
            final ShopDto shop,
            final ShopCategoryDto category
    ) {
        return MessageUtil.mm(shop.getTitle()
                + ITEM_TITLE_SEPARATOR + category.getName());
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

}