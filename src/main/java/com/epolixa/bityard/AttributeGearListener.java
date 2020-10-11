package com.epolixa.bityard;

import com.google.common.collect.Multimap;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.w3c.dom.Attr;

import java.util.*;

public class AttributeGearListener implements Listener {

    private final Bityard bityard;

    private final static Random random = new Random(System.currentTimeMillis());

    private HashMap<Material, Double> baseAttackDamageMap;

    public AttributeGearListener(Bityard bityard) {
        this.bityard = bityard;

        buildBaseAttackDamageMap();
    }


    // Intercept inventory event
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        try {
            bityard.log("[onInventoryOpen] " + event.getInventory().getType().name() + " inventory open");
            Inventory inv = event.getInventory();
            for (ItemStack is : inv.getContents()) {
                if (is != null) {
                    bityard.log("[onInventoryOpen] found " + is.getType().name());
                    if (is.getType() != Material.AIR) {
                        is = generateItemAttributes(is);
                    }
                }
            }
        } catch (Exception e) {
            bityard.log("[onInventoryOpen] caught error: " + e.toString());
            e.printStackTrace();
        }
    }


    // Intercept anvil & smithing table use
    /*public void onSmithingTableUse(PrepareAnvilEvent event) {
        try {
            Inventory inv = event.getInventory();
            for (ItemStack is : inv.getContents()) {
                if (is.getType() == Material.DIAMOND_SWORD) {
                    is = generateItemAttributes(is);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/


    // generate appropriate attributes for an attribute-latent item
    private ItemStack generateItemAttributes(ItemStack itemStack) {
        try {
            String itemStackName = itemStack.getType().name();
            bityard.log("[generateItemAttributes] generating attributes for " + itemStackName);

            // check if item has nbt tag for latent attributes
            if (isItemLatent(itemStack)) {
                bityard.log("[generateItemAttributes] " + itemStackName + " item is latent");

                // randomly pick an appropriate attribute buff (healthy, lucky, strong, or swift)
                Attribute attribute = pickAttribute();

                // randomly pick a value for the attribute buff relative to base gear values
                double attributeBuff = pickAttributeBuff(attribute);

                // calculate the actual attribute value to apply to the item
                double attributeValue = calculateAttributeValue(itemStack, attribute, attributeBuff);

                // apply the values to the item
                itemStack = applyAttributeToItem(itemStack, attribute, attributeValue);

                // modify the item description/lore with updated attribute values
                itemStack = updateItemLore(itemStack);

            } else {
                bityard.log("[generateItemAttributes] did not generate attributes for " + itemStackName + ", item does not have latent attributes");
            }

        } catch (Exception e) {
            bityard.log("[generateItemAttributes] caught error: " + e.toString());
            e.printStackTrace();
        }

        // return the item
        return itemStack;
    }


    // check if item has latent attributes
    private boolean isItemLatent(ItemStack itemStack) {
        try {
            bityard.log("[isItemLatent] checking if " + itemStack.getType().name() + " has latent attributes");
            boolean hasAttr = false;
            boolean hasLatent = false;
            ItemMeta itemMeta = itemStack.getItemMeta();
            hasAttr = itemMeta.hasAttributeModifiers();
            List<String> lore = itemMeta.getLore();
            if (lore != null) {
                bityard.log("[isItemLatent] " + itemStack.getType().name() + " has lore...");
                for (String line : lore) {
                    bityard.log("[isItemLatent] - " + line);
                }
                hasLatent = lore.get(0).contains("Latent Attributes");
            }
            bityard.log("[isItemLatent] " + itemStack.getType().name() + ": hasAttr = " + hasAttr + ", hasLatent = " + hasLatent);
            return hasLatent;
        } catch (Exception e) {
            bityard.log("[isItemLatent] caught error: " + e.toString());
            e.printStackTrace();
        }
        return false;
    }


    // randomly pick an appropriate attribute buff (healthy, lucky, strong, or swift)
    private Attribute pickAttribute() {
        try {
            bityard.log("[pickAttribute] picking random attribute");
            Attribute attribute;
            switch (random.nextInt(4)) {
                case 1:
                    attribute = Attribute.GENERIC_MAX_HEALTH;
                case 2:
                    attribute = Attribute.GENERIC_MOVEMENT_SPEED;
                case 3:
                    attribute = Attribute.GENERIC_ATTACK_DAMAGE;
                default:
                    attribute = Attribute.GENERIC_LUCK;
            }
            bityard.log("[pickAttribute] picked " + attribute.name());
            return attribute;
        } catch (Exception e) {
            bityard.log("[pickAttribute] caught error: " + e.toString());
            e.printStackTrace();
        }
        return null;
    }


    // randomly pick a value for the attribute buff
    private double pickAttributeBuff(Attribute attribute) {
        try {
            bityard.log("[pickAttributeBuff] picking buff value for " + attribute.name());
            Double attributeBuff = 0.0;
            switch (attribute) {
                case GENERIC_MOVEMENT_SPEED:
                    attributeBuff = random.nextDouble();
                    break;
                case GENERIC_MAX_HEALTH:
                case GENERIC_ATTACK_DAMAGE:
                case GENERIC_LUCK:
                    attributeBuff = (double)random.nextInt(3);
                    break;
                default:
                    attributeBuff = 0.0;
                    break;
            }
            attributeBuff = (double)Math.round(attributeBuff * 10) / 10;
            bityard.log("[pickAttributeBuff] picked buff value of +" + attributeBuff + " " + attribute.name());
            return attributeBuff;
        } catch (Exception e) {
            bityard.log("[pickAttributeBuff] caught error: " + e.toString());
            e.printStackTrace();
        }
        return 0.0;
    }


    // calculate the actual attribute values for the item
    private double calculateAttributeValue(ItemStack itemStack, Attribute attribute, double attributeBuff) {
        try {
            bityard.log("[calculateAttributeValue] calculating value for +" + attributeBuff + " " + attribute.name() + " for " + itemStack.getType().name());
            double attributeValue = attributeBuff;
            if (attribute == Attribute.GENERIC_ATTACK_DAMAGE) {
                attributeValue = attributeBuff + baseAttackDamageMap.get(itemStack.getType());
            }
            bityard.log("[calculateAttributeValue] calculated value of " + attributeValue + " " + attribute.name() + " for " + itemStack.getType().name());
            return attributeValue;
        } catch (Exception e) {
            bityard.log("[calculateAttributeValue] caught error: " + e.toString());
            e.printStackTrace();
        }
        return 0;
    }


    // apply the values to the item
    private ItemStack applyAttributeToItem(ItemStack itemStack, Attribute attribute, double attributeValue) {
        try {
            bityard.log("[applyAttributesToItem] applying " + attributeValue + " " + attribute.name() + " to " + itemStack.getType().name());
            ItemMeta itemMeta = itemStack.getItemMeta();
            AttributeModifier attributeModifier = new AttributeModifier(UUID.randomUUID(), attribute.getKey().toString(), attributeValue, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
            itemMeta.addAttributeModifier(attribute, attributeModifier);
            itemStack.setItemMeta(itemMeta);
            bityard.log("[applyAttributesToItem] done");
        } catch (Exception e) {
            bityard.log("[applyAttributesToItem] caught error: " + e.toString());
            e.printStackTrace();
        }
        return itemStack;
    }


    // modify the item description/lore with updated attribute values
    private ItemStack updateItemLore(ItemStack itemStack) {
        try {
            bityard.log("[updateItemLore] updating item lore");

            // first, decide if it is a weapon or armor, as they display tags differently

        } catch (Exception e) {
            bityard.log("[updateItemLore] caught error: " + e.toString());
            e.printStackTrace();
        }
        return itemStack;
    }


    // only need this for attack damage, as gear do not have base max health, luck, or speed
    public void buildBaseAttackDamageMap() {
        try {
            bityard.log("[buildBaseAttackDamageMap] building base attack damage map for each gear");
            this.baseAttackDamageMap = new HashMap<Material, Double>();
            baseAttackDamageMap.put(Material.WOODEN_SWORD, 4.0);
            baseAttackDamageMap.put(Material.GOLDEN_SWORD, 4.0);
            baseAttackDamageMap.put(Material.STONE_SWORD, 5.0);
            baseAttackDamageMap.put(Material.IRON_SWORD, 6.0);
            baseAttackDamageMap.put(Material.DIAMOND_SWORD, 7.0);
            baseAttackDamageMap.put(Material.NETHERITE_SWORD, 8.0);
            bityard.log("[buildBaseAttackDamageMap] done");
        } catch (Exception e) {
            bityard.log("[buildBaseAttackDamageMap] caught error: " + e.toString());
            e.printStackTrace();
        }
    }

}
