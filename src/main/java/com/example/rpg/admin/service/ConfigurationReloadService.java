package com.example.rpg.admin.service;

import com.example.rpg.admin.config.ReloadTarget;
import com.example.rpg.item.repository.interfaces.IAttributeRepository;
import com.example.rpg.item.repository.interfaces.IEffectRepository;
import com.example.rpg.item.repository.interfaces.IEnchantmentRepository;
import com.example.rpg.item.repository.interfaces.IItemRepository;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * RPGプラグインが使用する設定ファイルの再読み込みを管理する。
 */
public final class ConfigurationReloadService {
    /**
     * プラグイン本体。
     */
    private final JavaPlugin plugin;

    /**
     * アイテムRepository。
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
     * 設定再読み込みServiceを生成する。
     *
     * @param plugin                プラグイン本体
     * @param itemRepository        アイテムRepository
     * @param attributeRepository   Attribute Repository
     * @param enchantmentRepository Enchantment Repository
     * @param effectRepository      Effect Repository
     */
    public ConfigurationReloadService(
            final JavaPlugin plugin,
            final IItemRepository itemRepository,
            final IAttributeRepository attributeRepository,
            final IEnchantmentRepository enchantmentRepository,
            final IEffectRepository effectRepository
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
            case ITEMS -> reloadItems();
            case ATTRIBUTES -> reloadAttributes();
            case ENCHANTMENTS -> reloadEnchantments();
            case EFFECTS -> reloadEffects();
        }

        plugin.getLogger().info(
                "Configuration reloaded: "
                        + target.getCommandName()
        );
    }

    /**
     * すべての設定を再読み込みする。
     */
    private void reloadAll() {
        reloadConfig();
        reloadItems();
        reloadAttributes();
        reloadEnchantments();
        reloadEffects();
    }

    /**
     * config.ymlを再読み込みする。
     */
    private void reloadConfig() {
        plugin.reloadConfig();
    }

    /**
     * items.ymlを再読み込みする。
     */
    private void reloadItems() {
        itemRepository.load();
    }

    /**
     * attributes.ymlを再読み込みする。
     */
    private void reloadAttributes() {
        attributeRepository.load();
    }

    /**
     * enchantments.ymlを再読み込みする。
     */
    private void reloadEnchantments() {
        enchantmentRepository.load();
    }

    /**
     * effects.ymlを再読み込みする。
     */
    private void reloadEffects() {
        effectRepository.load();
    }
}
