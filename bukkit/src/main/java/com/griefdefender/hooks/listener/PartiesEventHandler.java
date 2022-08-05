/*
 * This file is part of GDHooks, licensed under the MIT License (MIT).
 *
 * Copyright (c) bloodmc
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.griefdefender.hooks.listener;

import com.alessiodp.parties.api.events.bukkit.party.BukkitPartiesPartyPostCreateEvent;
import com.alessiodp.parties.api.events.bukkit.party.BukkitPartiesPartyPostDeleteEvent;
import com.alessiodp.parties.api.events.bukkit.party.BukkitPartiesPartyPreCreateEvent;
import com.alessiodp.parties.api.events.bukkit.player.BukkitPartiesPlayerPostJoinEvent;
import com.alessiodp.parties.api.events.bukkit.player.BukkitPartiesPlayerPostLeaveEvent;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.data.PlayerData;
import com.griefdefender.hooks.GDHooks;
import com.griefdefender.hooks.provider.clan.parties.GDClanPlayer;
import com.griefdefender.hooks.provider.clan.parties.PartiesClanProvider;
import com.griefdefender.lib.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PartiesEventHandler implements Listener {
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onClanCreatePre(BukkitPartiesPartyPreCreateEvent event) {
        if (GDHooks.getInstance().getConfig().getData().clan.clanRequireTown && event.getPartyPlayer() != null) {
            final Player player = Bukkit.getPlayer(event.getPartyPlayer().getPlayerUUID());
            final World world = player.getWorld();
            if (!GriefDefender.getCore().isEnabled(world.getUID())) {
                return;
            }
    
            final PlayerData playerData = GriefDefender.getCore().getPlayerData(world.getUID(), player.getUniqueId());
            for (Claim claim : playerData.getClaims()) {
                if (claim.isTown()) {
                    // Okay, adding the clan in post event
                    return;
                }
            }
            event.setCancelled(true);
            GriefDefender.getAudienceProvider().getSender(player).sendMessage(Component.text("You must own a town in order to create a clan."));
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onClanCreatePost(BukkitPartiesPartyPostCreateEvent event) {
        ((PartiesClanProvider) GDHooks.getInstance().getClanProvider()).addClan(event.getParty());
        ((PartiesClanProvider) GDHooks.getInstance().getClanProvider()).getClanPlayerMap().put(event.getCreator().getPlayerUUID(), new GDClanPlayer(event.getParty(), event.getCreator().getPlayerUUID()));
        GDHooks.getInstance().updateClanCompletions();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onClanDisband(BukkitPartiesPartyPostDeleteEvent event) {
        ((PartiesClanProvider) GDHooks.getInstance().getClanProvider()).removeClan(event.getParty());
        GDHooks.getInstance().updateClanCompletions();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onClanPlayerJoin(BukkitPartiesPlayerPostJoinEvent event) {
        ((PartiesClanProvider) GDHooks.getInstance().getClanProvider()).getClanPlayerMap().put(event.getPartyPlayer().getPlayerUUID(), new GDClanPlayer(event.getParty(), event.getPartyPlayer().getPlayerUUID()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onClanPlayerLeave(BukkitPartiesPlayerPostLeaveEvent event) {
        ((PartiesClanProvider) GDHooks.getInstance().getClanProvider()).getClanPlayerMap().remove(event.getPartyPlayer().getPlayerUUID());
    }
}
