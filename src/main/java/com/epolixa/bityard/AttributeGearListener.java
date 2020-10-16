package com.epolixa.bityard;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
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

    private HashMap<Material, EquipmentSlot> equipmentSlotMap;
    private HashMap<Material, Multimap<Attribute, AttributeModifier>> baseAttributeModifiersMap;

    public AttributeGearListener(Bityard bityard) {
        this.bityard = bityard;

        buildEquipmentSlotMap();
        buildBaseAttributeModifiersMap();
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

                // apply the values to the item
                itemStack = applyAttributeToItem(itemStack, attribute, attributeBuff);

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
                    break;
                case 2:
                    attribute = Attribute.GENERIC_MOVEMENT_SPEED;
                    break;
                case 3:
                    attribute = Attribute.GENERIC_ATTACK_DAMAGE;
                    break;
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
            double attributeBuff = 0.0;
            switch (attribute) {
                case GENERIC_MOVEMENT_SPEED:
                    attributeBuff = random.nextDouble();
                    break;
                case GENERIC_MAX_HEALTH:
                case GENERIC_ATTACK_DAMAGE:
                case GENERIC_LUCK:
                    attributeBuff = random.nextDouble() + 1.0;
                    break;
                default:
                    attributeBuff = 0.0;
                    break;
            }
            attributeBuff = (double)Math.floor(attributeBuff * 10) / 10;
            bityard.log("[pickAttributeBuff] picked buff value of +" + attributeBuff + " " + attribute.name());
            return attributeBuff;
        } catch (Exception e) {
            bityard.log("[pickAttributeBuff] caught error: " + e.toString());
            e.printStackTrace();
        }
        return 0.0;
    }


    // apply the values to the item
    private ItemStack applyAttributeToItem(ItemStack itemStack, Attribute attribute, double attributeValue) {
        try {
            bityard.log("[applyAttributesToItem] applying " + attributeValue + " " + attribute.name() + " to " + itemStack.getType().name());
            ItemMeta itemMeta = itemStack.getItemMeta();

            // Get the base modifiers
            Multimap<Attribute, AttributeModifier> baseModifiers = this.baseAttributeModifiersMap.get(itemStack.getType());

            // Compare buffed modifier to existing modifiers and overwrite if applicable
            /*Collection<AttributeModifier> baseModifier = baseModifiers.get(attribute);
            if (baseModifier != null) {
                attributeValue = baseModifier. + attributeValue;
                baseModifiers.removeAll(attribute);
            }*/
            EquipmentSlot es = this.equipmentSlotMap.get(itemStack.getType());
            AttributeModifier attributeModifier = new AttributeModifier(UUID.randomUUID(), attribute.getKey().toString(), attributeValue, AttributeModifier.Operation.ADD_NUMBER, es == null ? EquipmentSlot.HAND : es);
            baseModifiers.put(attribute, attributeModifier);

            // Set the attributes to the item
            itemMeta.setAttributeModifiers(baseModifiers);
            itemStack.setItemMeta(itemMeta);

            bityard.log("[applyAttributesToItem] done");
        } catch (Exception e) {
            bityard.log("[applyAttributesToItem] caught error: " + e.toString());
            e.printStackTrace();
        }
        return itemStack;
    }


    // modify the item description/lore to reflect buffed attribute values
    private ItemStack updateItemLore(ItemStack itemStack) {
        try {
            bityard.log("[updateItemLore] updating item lore");

            // first, decide if it is a weapon or armor, as they display tags differently
            if (equipmentSlotMap.get(itemStack.getType()) == EquipmentSlot.HAND) {

            } else {

            }

        } catch (Exception e) {
            bityard.log("[updateItemLore] caught error: " + e.toString());
            e.printStackTrace();
        }
        return itemStack;
    }


    public void buildEquipmentSlotMap() {
        try {
            bityard.log("[buildEquipmentSlotMap] building equipment slot for each gear");
            this.equipmentSlotMap = new HashMap<Material, EquipmentSlot>();
            equipmentSlotMap.put(Material.STONE_SWORD,   EquipmentSlot.HAND);
            equipmentSlotMap.put(Material.DIAMOND_SWORD, EquipmentSlot.HAND);
            bityard.log("[buildEquipmentSlotMap] done");
        } catch (Exception e) {
            bityard.log("[buildEquipmentSlotMap] caught error: " + e.toString());
            e.printStackTrace();
        }
    }


    public void buildBaseAttributeModifiersMap() {
        try {
            bityard.log("[buildBaseAttributeModifiersMap] building base attack damage map for each gear");
            this.baseAttributeModifiersMap = new HashMap<Material, Multimap<Attribute, AttributeModifier>>();
            baseAttributeModifiersMap.put(Material.STONE_SWORD,   buildAttributeMap(equipmentSlotMap.get(Material.STONE_SWORD),   0.0, 0.0, 0.0, 5.0, 1.6, 0.0, 0.0, 0.0));
            baseAttributeModifiersMap.put(Material.DIAMOND_SWORD, buildAttributeMap(equipmentSlotMap.get(Material.DIAMOND_SWORD), 0.0, 0.0, 0.0, 7.0, 1.6, 0.0, 0.0, 0.0));
            bityard.log("[buildBaseAttributeModifiersMap] done");
        } catch (Exception e) {
            bityard.log("[buildBaseAttributeModifiersMap] caught error: " + e.toString());
            e.printStackTrace();
        }
    }


    public ListMultimap<Attribute, AttributeModifier> buildAttributeMap(EquipmentSlot es, double maxHealth, double knockbackResistance, double movementSpeed, double attackDamage, double attackSpeed, double armor, double armorToughness, double luck) {
        try {
            bityard.log("[buildAttributeMap] enter");
            ListMultimap<Attribute, AttributeModifier> attributeMap = ArrayListMultimap.create();
            if (attackDamage != 0.0) attributeMap.put(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(), Attribute.GENERIC_ATTACK_DAMAGE.name(), attackDamage, AttributeModifier.Operation.ADD_NUMBER, es));
            if (attackSpeed != 0.0)  attributeMap.put(Attribute.GENERIC_ATTACK_SPEED,  new AttributeModifier(UUID.randomUUID(), Attribute.GENERIC_ATTACK_SPEED.name(),  attackSpeed,  AttributeModifier.Operation.ADD_NUMBER, es));
            bityard.log("[buildAttributeMap] done");
            return attributeMap;
        } catch (Exception e) {
            bityard.log("[buildAttributeMap] caught error: " + e.toString());
            e.printStackTrace();
        }
        return null;
    }

}
