package com.example.rpg.admin.service;

import com.example.rpg.admin.config.ReloadTarget;
import com.example.rpg.item.repository.interfaces.IAttributeRepository;
import com.example.rpg.item.repository.interfaces.IEffectRepository;
import com.example.rpg.item.repository.interfaces.IEnchantmentRepository;
import com.example.rpg.item.repository.interfaces.IItemRepository;
import com.example.rpg.shop.repository.interfaces.IShopRepository;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * RPGプラグインが使用する設定ファイルの再読み込みを管理する。
 *
 * <p>
 * 設定ファイルの再読み込みだけでなく、
 * 読み込んだ定義間の整合性検証も実行する。
 * </p>
 */
public final class ConfigurationReloadService {

    /**
     * プラグイン本体。
     */
    private final JavaPlugin plugin;

    /**
     * アイテムRepository
     */
    private final IItemRepository itemRepository;

    /**
     * Attribute Repository。
     */
    private final IAttributeRepository attributeRepository;

    /**
     * Enchantment Repository。
     */
    private final IEnchantmentRepository enchantmentRepository;

    /**
     * Effect Repository。
     */
    private final IEffectRepository effectRepository;

    /**
     * SHOP Repository。
     */
    private final IShopRepository shopRepository;

    /**
     * 設定再読み込みServiceを生成する。
     *
     * @param plugin                プラグイン本体
     * @param itemRepository        アイテムRepository
     * @param attributeRepository   Attribute Repository
     * @param enchantmentRepository Enchantment Repository
     * @param effectRepository      Effect Repository
     * @param shopRepository        SHOP Repository
     */
    public ConfigurationReloadService(
            final JavaPlugin plugin,
            final IItemRepository itemRepository,
            final IAttributeRepository attributeRepository,
            final IEnchantmentRepository enchantmentRepository,
            final IEffectRepository effectRepository,
            final IShopRepository shopRepository
    ) {
        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin must not be null"
        );
        this.itemRepository = Objects.requireNonNull(
                itemRepository,
                "itemRepository must not be null"
        );
        this.attributeRepository = Objects.requireNonNull(
                attributeRepository,
                "attributeRepository must not be null"
        );
        this.enchantmentRepository = Objects.requireNonNull(
                enchantmentRepository,
                "enchantmentRepository must not be null"
        );
        this.effectRepository = Objects.requireNonNull(
                effectRepository,
                "effectRepository must not be null"
        );
        this.shopRepository = Objects.requireNonNull(
                shopRepository,
                "shopRepository must not be null"
        );
    }

    /**
     * 指定された設定を再読み込みする。
     *
     * @param target 再読み込み対象
     */
    public void reload(final ReloadTarget target) {
        Objects.requireNonNull(
                target,
                "target must not be null"
        );

        switch (target) {
            case ALL -> reloadAll();
            case CONFIG -> reloadConfig();
            case ITEMS -> {
                reloadItems();
                reloadShop();
            }
            case ATTRIBUTES -> reloadAttributes();
            case ENCHANTMENTS -> reloadEnchantments();
            case EFFECTS -> reloadEffects();
            case SHOP -> reloadShop();
        }

        plugin.getLogger().info(
                "Configuration reloaded: "
                        + target.getCommandName()
        );
    }

    /**
     * すべての設定を依存順に再読み込みする。
     */
    private void reloadAll() {
        reloadConfig();

        /*
         * Itemが参照する定義を先に読み込む。
         */
        reloadAttributes();
        reloadEnchantments();
        reloadEffects();

        // 参照先の準備後にItemを読み込み、
        // Item単体と参照関係を検証する。
        reloadItems();

        // SHOPはItemを参照するため最後に読み込む。
        reloadShop();
    }

    /**
     * config.ymlを再読み込みする。
     */
    private void reloadConfig() {
        plugin.reloadConfig();
    }

    /**
     * items.ymlを再読み込みして検証する。
     */
    private void reloadItems() {
        itemRepository.load();
    }

    /**
     * attributes.ymlを再読み込みして検証する。
     */
    private void reloadAttributes() {
        attributeRepository.load();
    }

    /**
     * enchantments.ymlを再読み込みして検証する。
     */
    private void reloadEnchantments() {
        enchantmentRepository.load();
    }

    /**
     * effects.ymlを再読み込みして検証する。
     */
    private void reloadEffects() {
        effectRepository.load();
    }

    /**
     * shop.ymlを再読み込みする。
     */
    private void reloadShop() {
        shopRepository.load();
    }
}