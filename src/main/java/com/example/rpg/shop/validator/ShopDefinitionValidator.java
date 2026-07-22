package com.example.rpg.shop.validator;

import com.example.rpg.common.exception.InvalidDefinitionException;
import com.example.rpg.item.repository.interfaces.IItemRepository;
import com.example.rpg.shop.dto.ShopCategoryDto;
import com.example.rpg.shop.dto.ShopDto;
import com.example.rpg.shop.dto.ShopItemDto;
import org.bukkit.Material;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * SHOP設定全体の整合性を検証するValidator。
 *
 * <p>
 * SHOP単体の値だけでなく、SHOP商品が参照するRPGアイテム定義の
 * 存在確認も担当する。
 * </p>
 */
public final class ShopDefinitionValidator {

    /**
     * Minecraftインベントリの1行分のスロット数。
     */
    private static final int INVENTORY_ROW_SIZE = 9;

    /**
     * 作成可能な最小インベントリサイズ。
     */
    private static final int MIN_INVENTORY_SIZE = 9;

    /**
     * 作成可能な最大インベントリサイズ。
     */
    private static final int MAX_INVENTORY_SIZE = 54;

    /**
     * RPGアイテム定義Repository。
     */
    private final IItemRepository itemRepository;

    /**
     * Validatorを生成する。
     *
     * @param itemRepository RPGアイテム定義Repository
     */
    public ShopDefinitionValidator(
            final IItemRepository itemRepository
    ) {
        this.itemRepository = Objects.requireNonNull(
                itemRepository,
                "itemRepository must not be null"
        );
    }

    /**
     * SHOP定義全体を検証する。
     *
     * @param shop 検証対象SHOP定義
     */
    public void validate(final ShopDto shop) {
        Objects.requireNonNull(
                shop,
                "shop must not be null"
        );

        validateTitle(shop);
        validateInventorySize(shop);
        validateCategories(shop);
    }

    /**
     * SHOPタイトルを検証する。
     *
     * @param shop SHOP定義
     */
    private void validateTitle(final ShopDto shop) {
        if (shop.getTitle() == null || shop.getTitle().isBlank()) {
            throw invalidShop(
                    "title",
                    "Shop title must not be blank"
            );
        }
    }

    /**
     * SHOP GUIサイズを検証する。
     *
     * @param shop SHOP定義
     */
    private void validateInventorySize(
            final ShopDto shop
    ) {
        final int size = shop.getSize();

        if (size < MIN_INVENTORY_SIZE
                || size > MAX_INVENTORY_SIZE) {

            throw invalidShop(
                    "size",
                    "Shop inventory size must be between "
                            + MIN_INVENTORY_SIZE
                            + " and "
                            + MAX_INVENTORY_SIZE
                            + " / size="
                            + size
            );
        }

        if (size % INVENTORY_ROW_SIZE != 0) {
            throw invalidShop(
                    "size",
                    "Shop inventory size must be a multiple of "
                            + INVENTORY_ROW_SIZE
                            + " / size="
                            + size
            );
        }
    }

    /**
     * 全カテゴリを検証する。
     *
     * @param shop SHOP定義
     */
    private void validateCategories(
            final ShopDto shop
    ) {
        final Map<String, ShopCategoryDto> categories =
                Objects.requireNonNull(
                        shop.getCategories(),
                        "shop categories must not be null"
                );

        final Set<Integer> usedSlots = new HashSet<>();

        for (Map.Entry<String, ShopCategoryDto> entry : categories.entrySet()) {
            final String categoryKey = entry.getKey();
            final ShopCategoryDto category =
                    Objects.requireNonNull(
                            entry.getValue(),
                            "shop category must not be null"
                    );

            validateCategory(
                    shop,
                    categoryKey,
                    category,
                    usedSlots
            );
        }
    }

    /**
     * カテゴリ定義を検証する。
     *
     * @param shop        SHOP定義
     * @param categoryKey Map上のカテゴリキー
     * @param category    カテゴリ定義
     * @param usedSlots   使用済みカテゴリスロット
     */
    private void validateCategory(
            final ShopDto shop,
            final String categoryKey,
            final ShopCategoryDto category,
            final Set<Integer> usedSlots
    ) {
        validateCategoryId(categoryKey, category);
        validateCategoryName(category);
        validateCategoryIcon(category);
        validateCategorySlot(
                shop,
                category,
                usedSlots
        );
        validateItems(category);
    }

    /**
     * カテゴリIDを検証する。
     *
     * @param categoryKey Map上のカテゴリキー
     * @param category    カテゴリ定義
     */
    private void validateCategoryId(
            final String categoryKey,
            final ShopCategoryDto category
    ) {
        if (categoryKey == null || categoryKey.isBlank()) {
            throw invalidShop(
                    "categories",
                    "Shop category ID must not be blank"
            );
        }

        if (category.getId() == null || category.getId().isBlank()) {
            throw invalidCategory(
                    categoryKey,
                    "id",
                    "Shop category ID must not be blank"
            );
        }

        if (!categoryKey.equals(category.getId())) {
            throw invalidCategory(
                    categoryKey,
                    "id",
                    "Category map key and DTO ID must match"
                            + " / dtoId="
                            + category.getId()
            );
        }
    }

    /**
     * カテゴリ名を検証する。
     *
     * @param category カテゴリ定義
     */
    private void validateCategoryName(
            final ShopCategoryDto category
    ) {
        if (category.getName() == null || category.getName().isBlank()) {
            throw invalidCategory(
                    category.getId(),
                    "name",
                    "Shop category name must not be blank"
            );
        }
    }

    /**
     * カテゴリアイコンを検証する。
     *
     * @param category カテゴリ定義
     */
    private void validateCategoryIcon(
            final ShopCategoryDto category
    ) {
        final Material icon = category.getIcon();

        if (icon == null) {
            throw invalidCategory(
                    category.getId(),
                    "icon",
                    "Shop category icon must not be null"
            );
        }

        if (icon == Material.AIR) {
            throw invalidCategory(
                    category.getId(),
                    "icon",
                    "AIR cannot be used as a category icon"
            );
        }

        if (!icon.isItem()) {
            throw invalidCategory(
                    category.getId(),
                    "icon",
                    "Category icon material cannot be used as an item"
                            + " / material="
                            + icon.name()
            );
        }
    }

    /**
     * カテゴリ表示スロットを検証する。
     *
     * @param shop      SHOP定義
     * @param category  カテゴリ定義
     * @param usedSlots 使用済みスロット
     */
    private void validateCategorySlot(
            final ShopDto shop,
            final ShopCategoryDto category,
            final Set<Integer> usedSlots
    ) {
        final int slot = category.getSlot();

        if (slot < 0 || slot >= shop.getSize()) {
            throw invalidCategory(
                    category.getId(),
                    "slot",
                    "Category slot is outside the inventory range"
                            + " / slot="
                            + slot
                            + " / inventorySize="
                            + shop.getSize()
            );
        }

        if (!usedSlots.add(slot)) {
            throw invalidCategory(
                    category.getId(),
                    "slot",
                    "Category slot is already used"
                            + " / slot="
                            + slot
            );
        }
    }

    /**
     * カテゴリ内の商品を検証する。
     *
     * @param category カテゴリ定義
     */
    private void validateItems(
            final ShopCategoryDto category
    ) {
        final Map<String, ShopItemDto> items =
                Objects.requireNonNull(
                        category.getItems(),
                        "shop category items must not be null"
                );

        for (Map.Entry<String, ShopItemDto> entry
                : items.entrySet()) {

            final String itemKey = entry.getKey();
            final ShopItemDto item =
                    Objects.requireNonNull(
                            entry.getValue(),
                            "shop item must not be null"
                    );

            validateItem(
                    category.getId(),
                    itemKey,
                    item
            );
        }
    }

    /**
     * SHOP商品を検証する。
     *
     * @param categoryId 所属カテゴリID
     * @param itemKey    Map上の商品キー
     * @param item       SHOP商品
     */
    private void validateItem(
            final String categoryId,
            final String itemKey,
            final ShopItemDto item
    ) {
        validateItemId(
                categoryId,
                itemKey,
                item
        );
        validateReferencedItem(
                categoryId,
                item
        );
        validatePrices(
                categoryId,
                item
        );
        validateAmount(
                categoryId,
                item
        );
        validateLimit(
                categoryId,
                item
        );
        validateCommands(
                categoryId,
                item
        );
    }

    /**
     * SHOP商品IDを検証する。
     *
     * @param categoryId カテゴリID
     * @param itemKey    Map上の商品キー
     * @param item       SHOP商品
     */
    private void validateItemId(
            final String categoryId,
            final String itemKey,
            final ShopItemDto item
    ) {
        if (itemKey == null || itemKey.isBlank()) {
            throw invalidItem(
                    categoryId,
                    "<blank>",
                    "id",
                    "Shop item ID must not be blank"
            );
        }

        if (item.getId() == null
                || item.getId().isBlank()) {

            throw invalidItem(
                    categoryId,
                    itemKey,
                    "id",
                    "Shop item ID must not be blank"
            );
        }

        if (!itemKey.equals(item.getId())) {
            throw invalidItem(
                    categoryId,
                    itemKey,
                    "id",
                    "Item map key and DTO ID must match"
                            + " / dtoId="
                            + item.getId()
            );
        }
    }

    /**
     * RPGアイテム参照を検証する。
     *
     * @param categoryId カテゴリID
     * @param item       SHOP商品
     */
    private void validateReferencedItem(
            final String categoryId,
            final ShopItemDto item
    ) {
        final String itemId = item.getItemId();

        if (itemId == null || itemId.isBlank()) {
            throw invalidItem(
                    categoryId,
                    item.getId(),
                    "itemId",
                    "Referenced RPG item ID must not be blank"
            );
        }

        if (itemRepository.findById(itemId) == null) {
            throw invalidItem(
                    categoryId,
                    item.getId(),
                    "itemId",
                    "Referenced RPG item definition was not found"
                            + " / referencedId="
                            + itemId
            );
        }
    }

    /**
     * 購入価格と売却価格を検証する。
     *
     * @param categoryId カテゴリID
     * @param item       SHOP商品
     */
    private void validatePrices(
            final String categoryId,
            final ShopItemDto item
    ) {
        if (item.getPrice() < 0) {
            throw invalidItem(
                    categoryId,
                    item.getId(),
                    "price",
                    "Purchase price must be 0 or greater"
                            + " / price="
                            + item.getPrice()
            );
        }

        if (item.getSellPrice() < 0) {
            throw invalidItem(
                    categoryId,
                    item.getId(),
                    "sellPrice",
                    "Sell price must be 0 or greater"
                            + " / sellPrice="
                            + item.getSellPrice()
            );
        }
    }

    /**
     * 購入個数を検証する。
     *
     * @param categoryId カテゴリID
     * @param item       SHOP商品
     */
    private void validateAmount(
            final String categoryId,
            final ShopItemDto item
    ) {
        if (item.getAmount() < 1) {
            throw invalidItem(
                    categoryId,
                    item.getId(),
                    "amount",
                    "Purchase amount must be 1 or greater"
                            + " / amount="
                            + item.getAmount()
            );
        }
    }

    /**
     * 購入上限を検証する。
     *
     * <p>
     * 0は購入上限なしとして許可する。
     * </p>
     *
     * @param categoryId カテゴリID
     * @param item       SHOP商品
     */
    private void validateLimit(
            final String categoryId,
            final ShopItemDto item
    ) {
        if (item.getLimit() < 0) {
            throw invalidItem(
                    categoryId,
                    item.getId(),
                    "limit",
                    "Purchase limit must be 0 or greater"
                            + " / limit="
                            + item.getLimit()
            );
        }
    }

    /**
     * COMMAND商品のコマンド定義を検証する。
     *
     * @param categoryId カテゴリID
     * @param item       SHOP商品
     */
    private void validateCommands(
            final String categoryId,
            final ShopItemDto item
    ) {
        final var commands = item.getCommands();

        if (commands == null) {
            throw invalidItem(
                    categoryId,
                    item.getId(),
                    "commands",
                    "Commands must not be null"
            );
        }

        if (item.isCommandItem()
                && commands.isEmpty()) {

            throw invalidItem(
                    categoryId,
                    item.getId(),
                    "commands",
                    "COMMAND shop item requires at least one command"
            );
        }

        for (String command : commands) {
            if (command == null || command.isBlank()) {
                throw invalidItem(
                        categoryId,
                        item.getId(),
                        "commands",
                        "Command must not be blank"
                );
            }
        }
    }

    /**
     * SHOP全体の定義不正例外を生成する。
     *
     * @param propertyName プロパティ名
     * @param reason       不正理由
     * @return 定義不正例外
     */
    private InvalidDefinitionException invalidShop(
            final String propertyName,
            final String reason
    ) {
        return new InvalidDefinitionException(
                "shop",
                "shop",
                propertyName,
                reason
        );
    }

    /**
     * カテゴリ定義不正例外を生成する。
     *
     * @param categoryId   カテゴリID
     * @param propertyName プロパティ名
     * @param reason       不正理由
     * @return 定義不正例外
     */
    private InvalidDefinitionException invalidCategory(
            final String categoryId,
            final String propertyName,
            final String reason
    ) {
        return new InvalidDefinitionException(
                "shop-category",
                categoryId,
                propertyName,
                reason
        );
    }

    /**
     * SHOP商品定義不正例外を生成する。
     *
     * @param categoryId   カテゴリID
     * @param itemId       SHOP商品ID
     * @param propertyName プロパティ名
     * @param reason       不正理由
     * @return 定義不正例外
     */
    private InvalidDefinitionException invalidItem(
            final String categoryId,
            final String itemId,
            final String propertyName,
            final String reason
    ) {
        return new InvalidDefinitionException(
                "shop-item",
                categoryId + "/" + itemId,
                propertyName,
                reason
        );
    }
}