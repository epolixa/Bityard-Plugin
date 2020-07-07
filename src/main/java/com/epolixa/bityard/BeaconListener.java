package com.epolixa.bityard;

import com.destroystokyo.paper.event.block.BeaconEffectEvent;
import org.bukkit.*;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BeaconListener implements Listener
{

    private final Bityard bityard;

    public BeaconListener(Bityard bityard)
    {
        this.bityard = bityard;
    }

    @EventHandler
    public void onBeaconApplyEffect(BeaconEffectEvent event)
    {
        try
        {
            if (event.getBlock().getType() == Material.BEACON)
            {
                Player player = event.getPlayer();
                Beacon beacon = (Beacon)event.getBlock().getState();
                if (!player.hasPotionEffect(beacon.getPrimaryEffect().getType()))
                {
                    Location beaconLocation = beacon.getLocation();
                    World beaconWorld = beacon.getWorld();
                    org.bukkit.block.Sign sign = null;
                    Block signBlock = beaconWorld.getBlockAt(new Location(beaconWorld,beaconLocation.getX(),beaconLocation.getY()+1,beaconLocation.getZ()));
                    if (signBlock.getBlockData() instanceof Sign || signBlock.getBlockData() instanceof WallSign)
                    {
                        sign = (org.bukkit.block.Sign) signBlock.getState();
                    }
                    if (sign != null)
                    {
                        StringBuilder sb = new StringBuilder();
                        DyeColor color = sign.getColor();
                        sb.append(color == null ? ChatColor.GRAY : bityard.getColorMap().get(color));
                        StringBuilder lsb = new StringBuilder();
                        for (int i = 0; i < sign.getLines().length; i++)
                        {
                            String line = sign.getLine(i);
                            if (line.length() > 0)
                            {
                                if (lsb.toString().length() > 0)
                                {
                                    lsb.append(" ");
                                }
                                lsb.append(line);
                            }
                        }
                        sb.append(lsb.toString());
                        sb.append(ChatColor.RESET);
                        String signText = sb.toString();
                        player.sendTitle("", signText, 10, 70, 20);
                    }
                }
            }
        }
        catch (Exception e)
        {
            bityard.log("caught error: ");
            e.printStackTrace();
        }
    }

}