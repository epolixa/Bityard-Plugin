package com.epolixa.bityard;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;

public class AttributeGearListener implements Listener {

    private final Bityard bityard;

    public AttributeGearListener(Bityard bityard) {
        this.bityard = bityard;
    }

    // Intercept inventory event
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        try {
            bityard.log("inventory open");
            Inventory inv = event.getInventory();
            for (ItemStack is : inv.getContents()) {
                if (isItemLatent(is)) {
                    is = generateItemAttributes(is);
                }
            }

        } catch (Exception e) {
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
            bityard.log("generating attributes for " + itemStackName);

            // check if item has nbt tag for latent attributes
            if (isItemLatent(itemStack)) {
                bityard.log(itemStackName + " item is latent");

                // randomly pick an appropriate attribute buff
                String attribute = pickAttribute(itemStack);

                // randomly pick a value for the attribute buff relative to base gear values
                double attributeBuff = pickAttributeBuff(attribute);

                // calculate the actual attribute values for the item
                double attributeValue = calculateAttributeValue(itemStack, attribute, attributeBuff);

                // apply the values to the item
                // modify the item description/lore with updated attribute values
                itemStack = applyAttributesToItem(itemStack, attribute, attributeValue);

            } else {
                bityard.log("did not generate attributes for " + itemStackName + ", item is not latent");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // return the item
        return itemStack;
    }


    // check if item has nbt tag for latent attributes
    private boolean isItemLatent(ItemStack itemStack) {
        try {
            bityard.log("checking if " + itemStack.getType().name() + " is latent");
            ItemMeta itemMeta = itemStack.getItemMeta();
            boolean hasAttr = itemMeta.hasAttributeModifiers();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // decide if this is a weapon, tool, armor, etc
    // randomly pick an appropriate type of attribute buff
    private String pickAttribute(ItemStack itemStack) {
        try {
            bityard.log("checking category of " + itemStack.getType().name());

            ItemMeta itemMeta = itemStack.getItemMeta();
            bityard.log(itemMeta.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "empty";
    }

    // randomly pick a value for the attribute buff
    private double pickAttributeBuff(String attribute) {
        return 0;
    }


    // calculate the actual attribute values for the item
    private double calculateAttributeValue(ItemStack itemStack, String attribute, double attributeBuff) {
        return 0;
    }


    // apply the values to the item
    // modify the item description/lore with updated attribute values
    private ItemStack applyAttributesToItem(ItemStack itemStack, String attribute, double attributeValue) {
        return itemStack;
    }

}
