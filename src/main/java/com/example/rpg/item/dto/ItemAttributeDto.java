package com.example.rpg.item.dto;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;

import java.util.Objects;

/**
 * RPGアイテムへ付与するAttributeModifier情報を保持するDTO
 */
public class ItemAttributeDto {

    /**
     * AttributeModifier定義を識別するID。
     */
    private final String id;

    /**
     * 対象となるAttribute
     */
    private final Attribute attribute;

    /**
     * 能力値の変更量
     */
    private final double amount;

    /**
     * 変更方法
     */
    private final AttributeModifier.Operation operation;

    /**
     * 効果を発揮する装備部位
     */
    private final EquipmentSlotGroup slotGroup;

    /**
     * ItemAttributeを生成する。
     *
     * @param id        AttributeModifier定義ID
     * @param attribute 対象Attribute
     * @param amount    能力値変更量
     * @param operation 変更方法
     * @param slotGroup 効果を発揮する装備部位
     */
    public ItemAttributeDto(
            final String id,
            final Attribute attribute,
            final double amount,
            final AttributeModifier.Operation operation,
            final EquipmentSlotGroup slotGroup
    ) {
        this.id = Objects.requireNonNull(
                id,
                "id must not be null"
        );

        this.attribute = Objects.requireNonNull(
                attribute,
                "attribute must not be null"
        );
        this.operation = Objects.requireNonNull(
                operation,
                "operation must not be null"
        );
        this.slotGroup = Objects.requireNonNull(
                slotGroup,
                "slotGroup must not be null"
        );
        this.amount = amount;
    }

    /**
     * AttributeModifier定義IDを取得する。
     *
     * @return AttributeModifier定義ID
     */
    public String getId() {
        return id;
    }

    /**
     * Attributeを取得する。
     *
     * @return Attribute
     */
    public Attribute getAttribute() {
        return attribute;
    }

    /**
     * 能力値変更量を取得する。
     *
     * @return 変更量
     */
    public double getAmount() {
        return amount;
    }

    /**
     * 変更方法を取得する。
     *
     * @return Operation
     */
    public AttributeModifier.Operation getOperation() {
        return operation;
    }

    /**
     * 効果を発揮する装備部位を取得する。
     *
     * @return EquipmentSlotGroup
     */
    public EquipmentSlotGroup getSlotGroup() {
        return slotGroup;
    }
}
