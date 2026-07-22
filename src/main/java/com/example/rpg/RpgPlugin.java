package com.example.rpg;

import com.example.rpg.admin.command.AdminCommand;
import com.example.rpg.admin.service.ConfigurationReloadService;
import com.example.rpg.command.DevHelpCommand;
import com.example.rpg.command.ExpCommand;
import com.example.rpg.command.MoneyCommand;
import com.example.rpg.command.PayCommand;
import com.example.rpg.common.message.MessageUtil;
import com.example.rpg.item.assembler.ItemAssembler;
import com.example.rpg.item.assembler.interfaces.IItemAssembler;
import com.example.rpg.item.factory.ItemFactory;
import com.example.rpg.item.factory.interfaces.IItemFactory;
import com.example.rpg.item.pdc.ItemPdcKeys;
import com.example.rpg.item.repository.YamlAttributeRepository;
import com.example.rpg.item.repository.YamlEffectRepository;
import com.example.rpg.item.repository.YamlEnchantmentRepository;
import com.example.rpg.item.repository.YamlItemRepository;
import com.example.rpg.item.repository.interfaces.IAttributeRepository;
import com.example.rpg.item.repository.interfaces.IEffectRepository;
import com.example.rpg.item.repository.interfaces.IEnchantmentRepository;
import com.example.rpg.item.repository.interfaces.IItemRepository;
import com.example.rpg.item.service.ItemPdcService;
import com.example.rpg.item.validator.AttributeDefinitionValidator;
import com.example.rpg.item.validator.EffectDefinitionValidator;
import com.example.rpg.item.validator.EnchantmentDefinitionValidator;
import com.example.rpg.item.validator.ItemDefinitionValidator;
import com.example.rpg.listener.BlockBreakListener;
import com.example.rpg.listener.EntityKillListener;
import com.example.rpg.listener.ServerPingListener;
import com.example.rpg.repository.MoneyRepository;
import com.example.rpg.repository.interfaces.IMoneyRepository;
import com.example.rpg.service.ExpService;
import com.example.rpg.shop.command.ShopCommand;
import com.example.rpg.shop.facade.ShopFacade;
import com.example.rpg.shop.listener.ShopListener;
import com.example.rpg.shop.menu.ShopMenu;
import com.example.rpg.shop.menu.pdc.ShopPdcKeys;
import com.example.rpg.shop.repository.ShopPurchaseRepository;
import com.example.rpg.shop.repository.YamlShopRepository;
import com.example.rpg.shop.repository.interfaces.IShopPurchaseRepository;
import com.example.rpg.shop.repository.interfaces.IShopRepository;
import com.example.rpg.shop.service.ShopService;
import com.example.rpg.shop.validator.ShopDefinitionValidator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class RpgPlugin extends JavaPlugin implements Listener {
    /**
     * リスポーン時のペナルティー減額率
     */
    final double RESPAWN_PENALTY = 0.3;
    /**
     * 経験値取得サービス
     */
    ExpService expService;
    /**
     * YamlShopRepository
     */
    private IShopRepository shopRepository;
    /**
     * 所持金Repository
     */
    private IMoneyRepository moneyRepository;
    /**
     * 購入履歴Repository
     */
    private IShopPurchaseRepository shopPurchaseRepository;
    /**
     * RPGアイテム定義Repository
     */
    private IItemRepository itemRepository;
    /**
     * RPG属性定義Repository
     */
    private IAttributeRepository attributeRepository;
    /**
     * RPGエンチャント定義Repository
     */
    private IEnchantmentRepository enchantmentRepository;
    /**
     * RPG効果定義Repository
     */
    private IEffectRepository effectRepository;
    /**
     * RPGアイテム用PDCキー
     */
    private ItemPdcKeys itemPdcKeys;
    /**
     * RPGアイテム生成Factory
     */
    private IItemFactory itemFactory;
    /**
     * Item定義Validator。
     */
    private ItemDefinitionValidator itemDefinitionValidator;
    /**
     * Attribute定義Validator。
     */
    private AttributeDefinitionValidator attributeDefinitionValidator;
    /**
     * Enchantment定義Validator。
     */
    private EnchantmentDefinitionValidator enchantmentDefinitionValidator;
    /**
     * Effect定義Validator。
     */
    private EffectDefinitionValidator effectDefinitionValidator;
    /**
     * SHOP定義Validatror
     */
    private ShopDefinitionValidator shopDefinitionValidator;
    /**
     * ShopService
     */
    private ShopService shopService;
    /**
     * ItemPdcService
     */
    private ItemPdcService itemPdcService;
    /**
     * 設定再読み込みService。
     */
    private ConfigurationReloadService configurationReloadService;
    /**
     * ShopMenu
     */
    private ShopMenu shopMenu;
    /**
     * ShopFacade
     */
    private ShopFacade shopFacade;
    /**
     * SHOP GUI用PDCキー
     */
    private ShopPdcKeys shopPdcKeys;

    /**
     * プラグイン有効化時の初期化処理。
     */
    @Override
    public void onEnable() {
        prepareResourceFiles();

        // YAMLから各定義を読み込むRepositoryを生成する。
        initializeRepositories();

        // 定義内容を検証するValidatorを生成する。
        initializeValidators();

        // FactoryやServiceの生成前に全定義を検証する。
        validateDefinitions();

        initializeItemFactory();
        initializeServices();
        initializeMenus();
        initializeFacades();

        registerCommands();
        registerListeners();

        getLogger().info("RpgPlugin enabled.");
    }

    /**
     * プラグイン無効化時の終了処理。
     */
    @Override
    public void onDisable() {
        if (moneyRepository != null) {
            moneyRepository.save();
        }

        if (shopPurchaseRepository != null) {
            shopPurchaseRepository.save();
        }

        getLogger().info("RpgPlugin disabled.");
    }

    /**
     * Repositoryを生成します。
     */
    private void initializeRepositories() {

        this.shopRepository = new YamlShopRepository(new File(getDataFolder(), "shop.yml"));
        this.moneyRepository = new MoneyRepository(
                this, new File(getDataFolder(), "money.yml"));
        this.shopPurchaseRepository = new ShopPurchaseRepository(
                this, new File(getDataFolder(), "shop-purchases.yml"));
        this.itemRepository = new YamlItemRepository(new File(getDataFolder(), "items.yml"));
        this.attributeRepository = new YamlAttributeRepository(new File(getDataFolder(), "attributes.yml"));
        this.enchantmentRepository = new YamlEnchantmentRepository(new File(getDataFolder(), "enchantments.yml"));
        this.effectRepository = new YamlEffectRepository(new File(getDataFolder(), "effects.yml"));
    }

    /**
     * Serviceを生成します。
     */
    private void initializeServices() {

        this.expService = new ExpService();

        this.itemPdcService = new ItemPdcService(itemPdcKeys);
        this.shopService = new ShopService(
                shopRepository,
                moneyRepository,
                shopPurchaseRepository,
                itemPdcService,
                itemFactory,
                itemRepository
        );
        this.configurationReloadService =
                new ConfigurationReloadService(
                        this,
                        itemRepository,
                        attributeRepository,
                        enchantmentRepository,
                        effectRepository,
                        shopRepository,
                        attributeDefinitionValidator,
                        enchantmentDefinitionValidator,
                        effectDefinitionValidator,
                        itemDefinitionValidator,
                        shopDefinitionValidator
                );
    }

    /**
     * RPGアイテム生成Factoryを初期化する。
     *
     * <p>
     * ItemFactoryがRepositoryから定義を取得し、
     * ItemBuilderを使用してItemStackを生成する。
     * </p>
     */
    private void initializeItemFactory() {
        this.itemPdcKeys = new ItemPdcKeys(this);

        final IItemAssembler itemAssembler =
                new ItemAssembler(
                        enchantmentRepository,
                        attributeRepository,
                        effectRepository,
                        itemPdcKeys,
                        this
                );
        this.itemFactory = new ItemFactory(
                itemRepository,
                itemAssembler
        );
    }

    /**
     * GUIを生成します。
     */
    private void initializeMenus() {
        this.shopPdcKeys = new ShopPdcKeys(this);

        this.shopMenu = new ShopMenu(
                shopRepository,
                shopPdcKeys,
                itemFactory
        );
    }

    /**
     * Facadeを生成する。
     */
    private void initializeFacades() {
        this.shopFacade = new ShopFacade(
                shopRepository,
                shopMenu,
                shopService,
                shopPdcKeys
        );
    }

    /**
     * Repositoryへ読み込まれた全定義を依存順に検証する。
     */
    private void validateDefinitions() {
        // Itemが参照する定義を先に検証する。
        attributeDefinitionValidator.validateAll(
                attributeRepository.findAll().values()
        );

        enchantmentDefinitionValidator.validateAll(
                enchantmentRepository.findAll()
        );

        effectDefinitionValidator.validateAll(
                effectRepository.findAll()
        );

        // 参照先が正常であることを確認後、Itemを検証する。
        itemDefinitionValidator.validateAll(
                itemRepository.findAll()
        );

        shopDefinitionValidator.validate(
                shopRepository.getShop()
        );
    }

    /**
     * 各設定定義のValidatorを初期化する。
     */
    private void initializeValidators() {
        this.attributeDefinitionValidator =
                new AttributeDefinitionValidator();

        this.enchantmentDefinitionValidator =
                new EnchantmentDefinitionValidator();

        this.effectDefinitionValidator =
                new EffectDefinitionValidator();

        /*
         * ItemValidatorは他定義との参照関係を確認するため、
         * 参照先Repositoryを注入する。
         */
        this.itemDefinitionValidator =
                new ItemDefinitionValidator(
                        attributeRepository,
                        enchantmentRepository,
                        effectRepository
                );

        this.shopDefinitionValidator =
                new ShopDefinitionValidator(
                        itemRepository
                );
    }

    /**
     * コマンドを登録する。
     *
     */
    private void registerCommands() {
        ShopCommand shopCommand = new ShopCommand(shopFacade, shopService);
        Objects.requireNonNull(getCommand("shop")).setExecutor(shopCommand);
        Objects.requireNonNull(getCommand("shop")).setTabCompleter(shopCommand);

        MoneyCommand moneyCommand = new MoneyCommand(moneyRepository);
        Objects.requireNonNull(getCommand("money")).setExecutor(moneyCommand);
        Objects.requireNonNull(getCommand("money")).setTabCompleter(moneyCommand);

        Objects.requireNonNull(getCommand("pay")).setExecutor(new PayCommand(moneyRepository));
        Objects.requireNonNull(getCommand("exp")).setExecutor(new ExpCommand(expService));

        final AdminCommand adminCommand = new AdminCommand(configurationReloadService, shopPurchaseRepository);
        Objects.requireNonNull(getCommand("admin")).setExecutor(adminCommand);
        Objects.requireNonNull(getCommand("admin")).setTabCompleter(adminCommand);


        // 開発時に使用するヘルプコマンド
        DevHelpCommand devCommand = new DevHelpCommand();
        Objects.requireNonNull(getCommand("devhelp")).setExecutor(devCommand);
        Objects.requireNonNull(getCommand("devhelp")).setTabCompleter(devCommand);
    }

    /**
     * Listenerを登録する。
     *
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new ServerPingListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new ShopListener(shopFacade), this);
        getServer().getPluginManager().registerEvents(new EntityKillListener(moneyRepository, expService), this);
    }

    /**
     * resources配下のファイルをpluginData配下へコピーする。
     *
     * <p>
     * BukkitのsaveResourceはJAR内resourcesからpluginDataへファイルをコピーする。
     * 既存ファイルを上書きすると運用中の設定や保存データを破壊するため、
     * 存在しない場合のみコピーする。
     * </p>
     *
     * @param fileName コピー対象ファイル名
     */
    private void copyResourceIfAbsent(String fileName, boolean replace) {
        File file = new File(getDataFolder(), fileName);

        // config.ymlの上書きフラグを確認する
        if (replace) {
            saveResource(fileName, true);
        } else {
            if (file.exists()) {
                return;
            }

            saveResource(fileName, false);
        }
    }

    /**
     * サーバー接続時にプレイヤーへRPGステータス風メッセージを表示する。
     *
     * @param event 発生イベント
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.ROOT);
        String lastLogingDate = sdf.format(new Date(player.getLastLogin() * 1000));

        player.sendMessage(MessageUtil.mm(
                "<gradient:#00ffff:#ff00ff>==============================</gradient>"
        ));

        player.sendMessage(MessageUtil.mm(
                "<gold>ようこそ、</gold><aqua>" + player.getName() + "</aqua><rainbow>（" + player.getUniqueId() + "）</rainbow><gold> さん！</gold>"
        ));

        player.sendMessage(MessageUtil.mm(
                "<yellow>Lv." + player.getLevel()
                        + "(Exp:" + String.format("%.2f", player.getExp() * 100) + ")</yellow> "
                        + "<green>Money </green><gold>" + moneyRepository.findMoney(player.getUniqueId()) + "G</gold>"
        ));

        player.sendMessage(MessageUtil.mm(
                "<gray>last login: " + lastLogingDate + "</gray>"
        ));

        player.sendMessage(MessageUtil.mm(
                "<gradient:#00ffff:#ff00ff>==============================</gradient>"
        ));
    }

    /**
     * プレイヤーのリスポーンイベント
     *
     * @param event 発生イベント
     */
    @EventHandler
    public void onRespown(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // プレイヤーがリスポーンした際に、所持金の30%を失う。
        // 銀行に預けているお金は減らない
        int currentMoney = moneyRepository.findMoney(uuid);
        int result = (int) Math.ceil(currentMoney * RESPAWN_PENALTY);

        moneyRepository.subtractMoney(player.getUniqueId(), result);

        player.sendMessage(MessageUtil.red(
                "所持金が" + result + "G 減少しました。"
        ));
    }

    /**
     * プラグインが利用するデフォルトリソースを準備する。
     *
     * <p>
     * saveDefaultConfig() は config.yml が存在しない場合のみコピーする。
     * 既存のconfig.ymlを上書きするとサーバー管理者の編集内容を破壊するため、
     * saveResource("config.yml", true) は使用しない。
     * </p>
     */
    private void prepareResourceFiles() {
        saveDefaultConfig();

        FileConfiguration config = getConfig();
        boolean isFileOverWrite = config.getBoolean("server-config.yml-config-replace", false);

        copyResourceIfAbsent("money.yml", isFileOverWrite);
        copyResourceIfAbsent("shop-purchases.yml", isFileOverWrite);
        copyResourceIfAbsent("shop.yml", isFileOverWrite);
        copyResourceIfAbsent("items.yml", isFileOverWrite);
        copyResourceIfAbsent("attributes.yml", isFileOverWrite);
        copyResourceIfAbsent("effects.yml", isFileOverWrite);
        copyResourceIfAbsent("enchantments.yml", isFileOverWrite);
    }

    /**
     * 指定された設定を再読み込みする。
     *
     * @param target 再読み込み対象
     */
    private void reloadConfiguration(
            final String target
    ) {
        switch (target) {
            case "all" -> {
                reloadConfig();
                reloadItemDefinitions();
            }
            case "config", "shop" -> reloadConfig();
            case "items", "attributes", "enchantments", "effects" -> {
                reloadConfig();
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported reload target: " + target
            );
        }
    }

    /**
     * アイテム関連の定義ファイルを再読み込みする。
     *
     * <p>
     * 参照関係を考慮し、詳細定義を先に読み込み、
     * 最後にアイテム本体を読み込む。
     * </p>
     */
    private void reloadItemDefinitions() {
        attributeRepository.load();
        enchantmentRepository.load();
        effectRepository.load();
        itemRepository.load();
    }
}