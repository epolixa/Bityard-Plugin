package com.epolixa.bityard;

import org.bukkit.*;
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
                        World w = l.getWorld();
                        w.playSound(l, Sound.ENTITY_TURTLE_EGG_BREAK, SoundCategory.BLOCKS, 0.1f, 0.7f);
                        w.spawnParticle(Particle.WATER_BUBBLE, l.getX() + 0.5, l.getY() + 0.5, l.getZ() + 0.5, 4, 0.5, 0.5, 0.5);
                        if (w.getBlockAt(l.getBlockX(), l.getBlockY() + 1, l.getBlockZ()).getType() == Material.AIR) {
                            w.spawnParticle(Particle.WATER_SPLASH, l.getX() + 0.5, l.getY() + 1.1, l.getZ() + 0.5, 2, 0.25, 0.0, 0.25);
                        }
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
