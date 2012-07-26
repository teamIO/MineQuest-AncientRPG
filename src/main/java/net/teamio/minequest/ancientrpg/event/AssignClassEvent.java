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
package net.teamio.minequest.ancientrpg.event;

import org.bukkit.entity.Player;

import com.ancientshores.AncientRPG.PlayerData;
import com.ancientshores.AncientRPG.API.ApiManager;
import com.ancientshores.AncientRPG.Classes.AncientRPGClass;
import com.ancientshores.AncientRPG.Classes.Commands.ClassSetCommand;
import com.theminequest.MineQuest.API.CompleteStatus;
import com.theminequest.MineQuest.API.Managers;
import com.theminequest.MineQuest.API.Events.QuestEvent;
import com.theminequest.MineQuest.API.Group.QuestGroup;

public class AssignClassEvent extends QuestEvent {
	
	private String clazz;
	private boolean all;

	/*
	 * (non-Javadoc)
	 * @see com.theminequest.MineQuest.API.Events.QuestEvent#parseDetails(java.lang.String[])
	 * [0] class to assign to
	 * [1] // optional - assign to leader or all members
	 */
	@Override
	public void parseDetails(String[] details) {
		clazz = details[0];
		all = false;
		if (details.length!=1)
			all = true;
		if (!AncientRPGClass.classList.keySet().contains(clazz))
			throw new RuntimeException("Class does not exist!");
	}

	@Override
	public boolean conditions() {
		return true;
	}

	@Override
	public CompleteStatus action() {
		QuestGroup g = Managers.getQuestGroupManager().get(getQuest());
		AncientRPGClass newClass = AncientRPGClass.classList.get(clazz);
		if (all) {
			for (Player p : g.getMembers()){
				PlayerData data = ApiManager.getApiManager().getPlayerData(p);
				AncientRPGClass oldClass = ApiManager.getApiManager().getPlayerClass(data);
				ClassSetCommand.setClass(newClass, oldClass, p, p);
			}
		} else {
			Player p = g.getLeader();
			PlayerData data = ApiManager.getApiManager().getPlayerData(p);
			AncientRPGClass oldClass = ApiManager.getApiManager().getPlayerClass(data);
			ClassSetCommand.setClass(newClass, oldClass, p, p);
		}
		return CompleteStatus.SUCCESS;
	}

	@Override
	public Integer switchTask() {
		return null;
	}

}
