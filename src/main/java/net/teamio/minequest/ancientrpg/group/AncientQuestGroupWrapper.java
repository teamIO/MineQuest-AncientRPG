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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.ancientshores.AncientRPG.Party.AncientRPGParty;
import com.theminequest.MineQuest.API.CompleteStatus;
import com.theminequest.MineQuest.API.Managers;
import com.theminequest.MineQuest.API.BukkitEvents.GroupPlayerJoinedEvent;
import com.theminequest.MineQuest.API.BukkitEvents.GroupPlayerQuitEvent;
import com.theminequest.MineQuest.API.Group.Group;
import com.theminequest.MineQuest.API.Group.GroupException;
import com.theminequest.MineQuest.API.Group.QuestGroup;
import com.theminequest.MineQuest.API.Group.GroupException.GroupReason;
import com.theminequest.MineQuest.API.Group.QuestGroup.QuestStatus;
import com.theminequest.MineQuest.API.Quest.Quest;
import com.theminequest.MineQuest.API.Quest.QuestDetails;
import com.theminequest.MineQuest.API.Quest.QuestRequirement;
import com.theminequest.MineQuest.API.Quest.QuestUtils;
import com.theminequest.MineQuest.Group.Party;

public class AncientQuestGroupWrapper implements QuestGroup {
	
	private AncientRPGParty party;
	private long id;
	private LinkedHashMap<Player,Location> locations;
	private Quest quest;
	private QuestStatus status;
	
	public AncientQuestGroupWrapper(long groupid, AncientRPGParty party){
		this.id = groupid;
		this.party = party;
		locations = null;
		quest = null;
		status = QuestStatus.NOQUEST;
	}
	
	protected synchronized void dismantle(){
		if (quest!=null) {
			try {
				abandonQuest();
			} catch (GroupException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public long getID() {
		return id;
	}

	@Override
	public Player getLeader() {
		return party.mLeader;
	}

	@Override
	public void setLeader(Player p) throws GroupException {
		if (!contains(p))
			throw new GroupException(GroupReason.NOTONTEAM);
		party.mLeader = p;
	}

	@Override
	public List<Player> getMembers() {
		List<Player> players = new ArrayList<Player>();
		for (Player p : party.Member)
			players.add(p);
		return Collections.unmodifiableList(players);
	}

	@Override
	public void add(Player p) throws GroupException {
		onAdd(p);
		if (!party.addPlayer(p))
			throw new GroupException(GroupReason.BADCAPACITY);
	}
	
	public void onAdd(Player p) throws GroupException {
		GroupPlayerJoinedEvent e = new GroupPlayerJoinedEvent(this,p);
		Bukkit.getPluginManager().callEvent(e);
		if (e.isCancelled())
			throw new GroupException(GroupReason.EXTERNALEXCEPTION);
	}

	@Override
	public void remove(Player p) throws GroupException {
		party.removePlayer(p);
	}
	
	public void onRemove(Player p) throws GroupException {
		GroupPlayerQuitEvent event = new GroupPlayerQuitEvent(this,p);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()){
			if (event.getPlayer().isOnline())
				throw new GroupException(GroupReason.EXTERNALEXCEPTION);
		}
		if (locations!=null){
			try {
				moveBackToLocations(p);
				locations.remove(p);
			} catch (GroupException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean contains(Player p) {
		return party.Member.contains(p);
	}

	@Override
	public int getCapacity() {
		return AncientRPGParty.maxPlayers;
	}

	@Override
	public void setCapacity(int capacity) throws GroupException {
		throw new GroupException(new UnsupportedOperationException("Cannot set capacity!"));
	}

	@Override
	public boolean isPVP() {
		return party.friendlyFire;
	}

	@Override
	public void setPVP(boolean on) {
		party.friendlyFire = on;
	}

	@Override
	public void teleportPlayers(Location l) {
		for (Player p : party.Member)
			p.teleport(l);
	}

	@Override
	public int compareTo(Group o) {
		return Long.compare(id, o.getID());
	}

	@Override
	public synchronized Quest getQuest() {
		return quest;
	}

	@Override
	public QuestStatus getQuestStatus() {
		return status;
	}

	@Override
	public synchronized void startQuest(QuestDetails d) throws GroupException {
		if (quest!=null)
			throw new GroupException(GroupReason.ALREADYONQUEST);
		// check requirements
		List<QuestRequirement> requirements = d.getProperty(QuestDetails.QUEST_REQUIREMENTS);
		for (QuestRequirement r : requirements){
			if (!r.isSatisfied(getLeader()))
				throw new GroupException(GroupReason.REQUIREMENTSNOTFULFILLED);
		}
		quest = Managers.getQuestManager().startQuest(d,getLeader().getName());

		status = QuestStatus.NOTINQUEST;
		boolean loadworld = quest.getDetails().getProperty(QuestDetails.QUEST_LOADWORLD);
		if (!loadworld){
			quest.startQuest();
			status = QuestStatus.MAINWORLDQUEST;
		}
	}

	@Override
	public synchronized void abandonQuest() throws GroupException {
		if (quest==null)
			throw new GroupException(GroupReason.NOQUEST);
		quest.finishQuest(CompleteStatus.CANCELED);
		if (status==QuestStatus.INQUEST)
			exitQuest();
		Quest q = quest;
		status = QuestStatus.NOQUEST;
		quest = null;
		if (q!=null){
			q.cleanupQuest();
		}

	}
	@Override
	public synchronized void enterQuest() throws GroupException {
		if (quest==null)
			throw new GroupException(GroupReason.NOQUEST);
		if (status==QuestStatus.INQUEST)
			throw new GroupException(GroupReason.INSIDEQUEST);
		if (!quest.isInstanced())
			throw new GroupException(GroupReason.MAINWORLDQUEST);
		recordCurrentLocations();
		status = QuestStatus.INQUEST;
		teleportPlayers(QuestUtils.getSpawnLocation(quest));
		quest.startQuest();
	}

	public synchronized void recordCurrentLocations() {
		locations = new LinkedHashMap<Player,Location>();
		for (Player p : party.Member){
			locations.put(p, p.getLocation());
		}
	}
	
	public synchronized void moveBackToLocations() throws GroupException{
		for (Player p : party.Member){
			moveBackToLocations(p);
		}
		locations = null;
	}
	
	public synchronized void moveBackToLocations(Player p) throws GroupException {
		if (locations==null)
			throw new GroupException(GroupReason.NOLOCATIONS);
		p.teleport(locations.get(p));
		locations.remove(p);
	}

	public synchronized void exitQuest() throws GroupException {
		if (quest==null)
			throw new GroupException(GroupReason.NOQUEST);
		if (status!=QuestStatus.INQUEST)
			throw new GroupException(GroupReason.NOTINSIDEQUEST);
		if (!quest.isInstanced())
			throw new GroupException(GroupReason.MAINWORLDQUEST);
		if (quest.isFinished()==null)
			throw new GroupException(GroupReason.UNFINISHEDQUEST);
		moveBackToLocations();
		Quest q = quest;
		status = QuestStatus.NOQUEST;
		quest = null;
		q.cleanupQuest();
	}
	
	@Override
	public synchronized void finishQuest() throws GroupException {
		if (quest==null)
			throw new GroupException(GroupReason.NOQUEST);
		if (quest.isInstanced())
			throw new GroupException(GroupReason.NOTMAINWORLDQUEST);
		if (quest.isFinished()==null)
			throw new GroupException(GroupReason.UNFINISHEDQUEST);
		Quest q = quest;
		quest = null;
		status = QuestStatus.NOQUEST;
		q.cleanupQuest();
	}
}
