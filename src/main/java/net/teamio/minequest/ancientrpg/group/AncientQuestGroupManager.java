/*
 * This file is part of MineQuest-AncientRPG, AncientRPG addon to MineQuest.
 * MineQuest-AncientRPG is licensed under GNU General Public License v3.
 * Copyright (C) 2012 The MineQuest Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.teamio.minequest.ancientrpg.group;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.ancientshores.AncientRPG.API.AncientRPGPartyDisbandedEvent;
import com.ancientshores.AncientRPG.API.AncientRPGPartyJoinEvent;
import com.ancientshores.AncientRPG.API.AncientRPGPartyLeaveEvent;
import com.ancientshores.AncientRPG.API.ApiManager;
import com.ancientshores.AncientRPG.Party.AncientRPGParty;
import com.theminequest.MineQuest.API.CompleteStatus;
import com.theminequest.MineQuest.API.ManagerException;
import com.theminequest.MineQuest.API.BukkitEvents.GroupPlayerJoinedEvent;
import com.theminequest.MineQuest.API.BukkitEvents.GroupPlayerQuitEvent;
import com.theminequest.MineQuest.API.BukkitEvents.QuestCompleteEvent;
import com.theminequest.MineQuest.API.Group.Group;
import com.theminequest.MineQuest.API.Group.GroupException;
import com.theminequest.MineQuest.API.Group.QuestGroup;
import com.theminequest.MineQuest.API.Group.QuestGroupManager;
import com.theminequest.MineQuest.API.Quest.Quest;
import com.theminequest.MineQuest.Group.SingleParty;

public class AncientQuestGroupManager implements QuestGroupManager {
	
	private LinkedHashMap<AncientRPGParty,AncientQuestGroupWrapper> connector;
	private long groupid;
	
	
	public AncientQuestGroupManager(){
		groupid = 0;
		connector = new LinkedHashMap<AncientRPGParty,AncientQuestGroupWrapper>();
	}
	
	private void syncGroups(){
		HashSet<AncientRPGParty> extparties = AncientRPGParty.partys;
		for (AncientRPGParty party : extparties){
			if (!connector.containsKey(party)){
				connector.put(party,new AncientQuestGroupWrapper(groupid,party));
				groupid++;
			}
		}
	}
	
	private long getID(AncientRPGParty party) {
		if (party==null)
			return -1;
		AncientQuestGroupWrapper c = connector.get(party);
		if (c==null)
			return -1;
		return c.getID();
	}

	@Override
	public long indexOf(Player player) {
		syncGroups();
		AncientRPGParty party = AncientRPGParty.getPlayersParty(player);
		return getID(party);
	}

	@Override
	public void disposeGroup(Group group) {}

	@Override
	public void acceptInvite(Player player) throws ManagerException {}

	@Override
	public void denyInvite(Player player) throws ManagerException {}

	@Override
	public void invite(Player player, Group group) throws ManagerException {}

	@Override
	public boolean hasInvite(Player player) {
		return false;
	}

	@Override
	@EventHandler(priority = EventPriority.MONITOR)
	public void onGroupPlayerJoinedEvent(GroupPlayerJoinedEvent e) {}

	@Override
	@EventHandler(priority = EventPriority.MONITOR)
	public void onGroupPlayerQuitEvent(GroupPlayerQuitEvent e) {}

	@Override
	public QuestGroup createNewGroup(Player leader) {
		final AncientRPGParty mParty = new AncientRPGParty(leader);
		AncientRPGParty.partys.add(mParty);
		syncGroups();
		return connector.get(mParty);
	}

	@Override
	public QuestGroup createNewGroup(List<Player> members)
			throws ManagerException {
		final AncientRPGParty mParty = new AncientRPGParty(members.get(0));
		AncientRPGParty.partys.add(mParty);
		for (int i=1; i<members.size(); i++)
			mParty.addPlayer(members.get(i));
		syncGroups();
		return connector.get(mParty);
	}

	@Override
	public QuestGroup get(long id) {
		syncGroups();
		for (AncientQuestGroupWrapper w : connector.values()){
			if (w.getID()==id)
				return w;
		}
		return null;
	}

	@Override
	public QuestGroup get(Quest activeQuest) {
		if (!activeQuest.isInstanced()){
			// create faux questgroup with fake methods
			// and return that for events and such to use
			// get player from getQuestOwner()
			return new SingleParty(Bukkit.getPlayer(activeQuest.getQuestOwner()),activeQuest);
		}
		syncGroups();
		return get(indexOf(activeQuest));
	}

	@Override
	public QuestGroup get(Player player) {
		syncGroups();
		return get(indexOf(player));
	}

	@Override
	public long indexOf(Quest activeQuest) {
		for (AncientQuestGroupWrapper w : connector.values()){
			if (w.getQuest()!=null && w.getQuest().equals(activeQuest))
				return w.getID();
		}
		return -1;
	}

	@Override
	public void onPlayerQuit(PlayerQuitEvent e) {}

	@Override
	public void onPlayerKick(PlayerKickEvent e) {}

	@Override
	public void onQuestCompleteEvent(QuestCompleteEvent e) {
		Quest q = e.getQuest();
		if (!q.isInstanced() && e.getQuest().isFinished()!=CompleteStatus.CANCELED){
			try {
				e.getGroup().finishQuest();
			} catch (GroupException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	@EventHandler
	public void onPartyJoin(AncientRPGPartyJoinEvent e){
		syncGroups();
		try {
			connector.get(e.getParty()).onAdd(e.getPlayer());
		} catch (GroupException e1) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPartyLeave(AncientRPGPartyLeaveEvent e){
		syncGroups();
		try {
			connector.get(e.getParty()).onRemove(e.getPlayer());
		} catch (GroupException e1) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPartyDisband(AncientRPGPartyDisbandedEvent e) {
		syncGroups();
		connector.get(e.getParty()).dismantle();
		connector.remove(e.getParty());
	}

}
