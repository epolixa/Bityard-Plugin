package com.epolixa.bityard;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
            Inventory inv = event.getInventory();
            for (ItemStack is : inv.getContents()) {
                if (is.getType() == Material.DIAMOND_SWORD) {
                    is = generateItemAttributes(is);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Intercept anvil & smithing table use
    public void onSmithingTableUse(PrepareAnvilEvent event) {
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
    }

    // generate appropriate attributes for an attribute-latent item
    private ItemStack generateItemAttributes(ItemStack is) {
        try {
            // check if item has latent attributes

            // decide if this is a weapon, tool, armor, etc

            // randomly pick an appropriate attribute

            // randomly pick a value for the attribute relative to base gear values

            // calculate the actual attribute values for the item

            // apply the values to the item

            // modify the item description/lore with updated attribute values

        } catch (Exception e) {
            e.printStackTrace();
        }

        // return the item
        return is;
    }
}
