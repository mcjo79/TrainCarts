package com.bergerkiller.bukkit.tc.signactions;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.event.block.SignChangeEvent;

import com.bergerkiller.bukkit.common.utils.StringUtil;
import com.bergerkiller.bukkit.tc.MinecartGroup;
import com.bergerkiller.bukkit.tc.MinecartMember;
import com.bergerkiller.bukkit.tc.Permission;
import com.bergerkiller.bukkit.tc.events.GroupCreateEvent;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.utils.TrackWalkIterator;

public class SignActionSpawn extends SignAction {

	@Override
	public void execute(SignActionEvent info) {
		if (info.isAction(SignActionType.REDSTONE_ON)) {
			if (info.isTrainSign() || info.isCartSign()) {
				if (info.isType("spawn")) {
					if (!info.hasRails()) return;
					int idx = info.getLine(1).lastIndexOf(" ");
					double force = 0.0;
					if (idx != -1) {
						force = StringUtil.tryParse(info.getLine(1).substring(idx + 1), 0.0);
					}

					//Get the cart types to spawn
					ArrayList<Integer> types = new ArrayList<Integer>();
					for (char cart : (info.getLine(2) + info.getLine(3)).toCharArray()) {
						if (cart == 'm' || cart == 'M') {
							types.add(0);
						} else if (cart == 's' || cart == 'S') {
							types.add(1);
						} else if (cart == 'p' || cart == 'P') {
							types.add(2);
						}
					}

					if (types.size() == 0) return;

					Location[] locs = new Location[types.size()];
					TrackWalkIterator iter = new TrackWalkIterator(info.getRailLocation(), info.getFacing());
					for (int i = 0; i < types.size(); i++) {
						if (!iter.hasNext()) return;
						locs[i] = iter.next();
						//not taken?
						if (MinecartMember.getAt(locs[i]) != null) return;
					}

					//Spawn
					MinecartGroup group = MinecartGroup.create();
					for (int i = 0; i < locs.length; i++) {
						MinecartMember mm = MinecartMember.spawn(locs[i], types.get(i));
						group.add(mm);
						if (force != 0 && i == 0) {
							mm.addActionLaunch(info.getFacing(), 2, force);
						}
					}
					GroupCreateEvent.call(group);
				}
			}
		}
	}

	@Override
	public boolean build(SignChangeEvent event, String type, SignActionMode mode) {
		if (mode != SignActionMode.NONE) {
			if (type.startsWith("spawn")) {
				return handleBuild(event, Permission.BUILD_SPAWNER, "train spawner", "spawn trains on the tracks above when powered by redstone");
			}
		}
		return false;
	}

}
