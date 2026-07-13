package com.example.rpg;

import com.example.rpg.command.*;
import com.example.rpg.facade.ShopFacade;
import com.example.rpg.item.ItemBuilder;
import com.example.rpg.item.pdc.ItemPdcKeys;
import com.example.rpg.item.repository.YamlItemRepository;
import com.example.rpg.item.repository.interfaces.IItemRepository;
import com.example.rpg.listener.BlockBreakListener;
import com.example.rpg.listener.EntityKillListener;
import com.example.rpg.listener.ServerPingListener;
import com.example.rpg.listener.ShopListener;
import com.example.rpg.menu.ShopMenu;
import com.example.rpg.menu.pdc.ShopPdcKeys;
import com.example.rpg.repository.MoneyRepository;
import com.example.rpg.repository.ShopPurchaseRepository;
import com.example.rpg.repository.ShopRepository;
import com.example.rpg.repository.interfaces.IMoneyRepository;
import com.example.rpg.repository.interfaces.IShopPurchaseRepository;
import com.example.rpg.repository.interfaces.IShopRepository;
import com.example.rpg.service.ExpService;
import com.example.rpg.service.ShopService;
import com.example.rpg.util.MessageUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public class RpgPlugin extends JavaPlugin implements Listener {

    /**
     * 経験値取得サービス
     */
    ExpService expService;
    /**
     * ShopRepository
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
     * RPGアイテム定義Repository。
     */
    private IItemRepository itemRepository;
    /**
     * RPGアイテム用PDCキー。
     */
    private ItemPdcKeys itemPdcKeys;

    /**
     * RPGアイテム生成Builder。
     */
    private ItemBuilder itemBuilder;
    /**
     * ShopService
     */
    private ShopService shopService;
    /**
     * ShopMenu
     */
    private ShopMenu shopMenu;

    /**
     * ShopFacade
     */
    private ShopFacade shopFacade;

    /**
     * SHOP GUI用PDCキー。
     */
    private ShopPdcKeys shopPdcKeys;

    /**
     * プラグイン有効化時の初期化処理。
     */
    @Override
    public void onEnable() {
        prepareResourceFiles();

        // 初期化フェーズ
        initializeRepositories();
        initializeServices();
        initializeBuilders();
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

        this.shopRepository = new ShopRepository(
                YamlConfiguration.loadConfiguration(new File(getDataFolder(), "shop.yml")));
        this.moneyRepository = new MoneyRepository(
                this, new File(getDataFolder(), "money.yml"));
        this.shopPurchaseRepository = new ShopPurchaseRepository(
                this, new File(getDataFolder(), "shop-purchases.yml"));
        this.itemRepository = new YamlItemRepository(
                YamlConfiguration.loadConfiguration(new File(getDataFolder(), "items.yml"))
        );
    }

    /**
     * Serviceを生成します。
     */
    private void initializeServices() {

        this.expService = new ExpService();
        this.shopService = new ShopService(
                shopRepository,
                moneyRepository,
                shopPurchaseRepository
        );
    }

    /**
     * Builderを生成する。
     */
    private void initializeBuilders() {
        this.itemPdcKeys = new ItemPdcKeys(this);

        this.itemBuilder = new ItemBuilder(
                itemRepository,
                itemPdcKeys
        );
    }

    /**
     * GUIを生成します。
     */
    private void initializeMenus() {
        this.shopPdcKeys = new ShopPdcKeys(this);

        this.shopMenu = new ShopMenu(
                shopRepository,
                shopPdcKeys
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
        Objects.requireNonNull(getCommand("rpg")).setExecutor(new RpgCommand(this));

        AdminCommand adminCommand = new AdminCommand(shopPurchaseRepository);
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
    private void copyResourceIfAbsent(String fileName) {
        File file = new File(getDataFolder(), fileName);

        if (file.exists()) {
            return;
        }

        saveResource(fileName, false);
    }

    /**
     * サーバー接続時にプレイヤーへRPGステータス風メッセージを表示する。
     *
     * @param event 参加イベント
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();

        player.sendMessage(MessageUtil.mm(
                "<gradient:#00ffff:#ff00ff>==============================</gradient>"
        ));

        player.sendMessage(MessageUtil.mm(
                "<gold>ようこそ、</gold><aqua>" + player.getName() + "</aqua><rainbow>（" + player.getUniqueId() + "）</rainbow><gold> さん！</gold>"
        ));

        player.sendMessage(MessageUtil.mm(
                "<yellow>Lv." + player.getLevel() + "</yellow> <red>HP 100</red> <blue>MP 50</blue> <green>Money 0G</green>"
        ));

        player.sendMessage(MessageUtil.mm(
                "<gradient:#00ffff:#ff00ff>==============================</gradient>"
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

        copyResourceIfAbsent("money.yml");
        copyResourceIfAbsent("shop-purchases.yml");
        copyResourceIfAbsent("shop.yml");
        copyResourceIfAbsent("items.yml");
    }

}