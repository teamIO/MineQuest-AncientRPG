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
import com.theminequest.MineQuest.API.CompleteStatus;
import com.theminequest.MineQuest.API.Managers;
import com.theminequest.MineQuest.API.Events.QuestEvent;
import com.theminequest.MineQuest.API.Group.QuestGroup;

public class RewardExpEvent extends QuestEvent {

	private int exp;
	
	/*
	 * (non-Javadoc)
	 * @see com.theminequest.MineQuest.API.Events.QuestEvent#parseDetails(java.lang.String[])
	 * [0] amount of exp
	 */
	@Override
	public void parseDetails(String[] details) {
		exp = Integer.parseInt(details[0]);
	}

	@Override
	public boolean conditions() {
		return true;
	}

	@Override
	public CompleteStatus action() {
		QuestGroup g = Managers.getQuestGroupManager().get(getQuest());
		for (Player p : g.getMembers()){
			PlayerData data = ApiManager.getApiManager().getPlayerData(p);
			data.getXpSystem().addXP(exp);
		}
		return CompleteStatus.SUCCESS;
	}

	@Override
	public Integer switchTask() {
		return null;
	}

}
