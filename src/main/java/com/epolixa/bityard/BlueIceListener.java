package com.epolixa.bityard;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.Locale;

public class BlueIceListener implements Listener
{
    private final Bityard bityard;

    public BlueIceListener(Bityard bityard)
    {
        this.bityard = bityard;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event)
    {
        try
        {
            Block b = event.getBlockPlaced();
            if (b.getType() == Material.BLUE_ICE)
            {
                freezeWater(b.getLocation().add(1,0,0));
                freezeWater(b.getLocation().add(-1,0,0));
                freezeWater(b.getLocation().add(0,1,0));
                freezeWater(b.getLocation().add(0,-1,0));
                freezeWater(b.getLocation().add(0,0,1));
                freezeWater(b.getLocation().add(0,0,-1));
            } else if (b.getType() == Material.WATER) {
                freezeWater(b.getLocation());
            }
        }
        catch (Exception e)
        {
            bityard.log(e.toString());
        }
    }



    private void freezeWater(Location l) {
        try
        {
            Block b = l.getBlock();
            if (b.getType() == Material.WATER) {
                BlockData bd = b.getBlockData();
                if (bd instanceof Levelled) {
                    Levelled lv = (Levelled)bd;
                    if (lv.getLevel() == 0) {
                        b.setType(Material.ICE);
                        l.getWorld().playSound(l, Sound.ITEM_SWEET_BERRIES_PICK_FROM_BUSH, SoundCategory.BLOCKS, 1f, 0.7f);
                    }
                }
            }
        }
        catch (Exception e)
        {
            bityard.log(e.toString());
        }
    }
}
