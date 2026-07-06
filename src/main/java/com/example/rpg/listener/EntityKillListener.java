package com.example.rpg.listener;

import com.example.rpg.service.ExpService;
import com.example.rpg.service.MoneyService;
import com.example.rpg.util.MessageUtil;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityKillListener implements Listener {

    private final MoneyService moneyService;
    private final ExpService expService;

    public EntityKillListener(MoneyService moneyService, ExpService expService) {
        this.moneyService = moneyService;
        this.expService = expService;
    }

    /**
     * 倒した敵に対応するお金と経験値を取得するためのリスナー
     * TODO: 将来的にはDBで取得ゴールド周りの管理を行いたい
     * param event 死亡イベント
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        var killer = event.getEntity().getKiller();

        if (killer == null) {
            return;
        }

        EntityType type = event.getEntityType();

        int[] reward = switch (type) {
            case SPIDER -> new int[]{8, 15};
            case ZOMBIE -> new int[]{10, 20};
            case SKELETON -> new int[]{15, 30};
            case CREEPER -> new int[]{20, 40};
            case ENDERMAN -> new int[]{25, 50};
            default -> new int[]{5, 10};
        };

        int totalMoney = moneyService.addMoney(killer.getUniqueId(), reward[0]);
        int currentExp = expService.addExp(killer.getUniqueId(), reward[1]);

        killer.sendMessage(MessageUtil.mm("""
                <gold> %d G</gold> <gray>獲得しました。 </gray><yellow>所持金: %d G</yellow>""".formatted(reward[0], totalMoney))
        );
        killer.sendMessage(MessageUtil.mm("""
                <gold> %d Exp</gold> <gray>獲得しました。 </gray><yellow>経験値: %d Exp</yellow>""".formatted(reward[1], currentExp))
        );

    }

}
