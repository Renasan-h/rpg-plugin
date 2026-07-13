package com.example.rpg.listener;

import com.example.rpg.common.message.MessageUtil;
import com.example.rpg.repository.interfaces.IMoneyRepository;
import com.example.rpg.service.ExpService;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityKillListener implements Listener {

    private final IMoneyRepository moneyRepository;
    private final ExpService expService;

    public EntityKillListener(IMoneyRepository moneyRepository, ExpService expService) {
        this.moneyRepository = moneyRepository;
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
            case SPIDER -> new int[] {8, 15};
            case ZOMBIE -> new int[] {10, 20};
            case SKELETON -> new int[] {15, 30};
            case CREEPER -> new int[] {20, 40};
            case ENDERMAN -> new int[] {25, 50};
            default -> new int[] {5, 10};
        };

        int totalMoney = moneyRepository.addMoney(killer.getUniqueId(), reward[0]);
        int currentExp = expService.addExp(killer.getUniqueId(), reward[1]);

        killer.sendMessage(MessageUtil.mm("""
                <gold> %d G</gold> <gray>獲得しました。 </gray><yellow>所持金: %d G</yellow>""".formatted(reward[0], totalMoney))
        );
        killer.sendMessage(MessageUtil.mm("""
                <gold> %d Exp</gold> <gray>獲得しました。 </gray><yellow>経験値: %d Exp</yellow>""".formatted(reward[1], currentExp))
        );

    }

}
