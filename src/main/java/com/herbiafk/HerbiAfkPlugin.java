package com.herbiafk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.herbiboars.HerbiboarPlugin;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.*;

@Slf4j
@PluginDescriptor(
	name = "Herbi AFK"
)
@PluginDependency(HerbiboarPlugin.class)
public class HerbiAfkPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private HerbiAfkConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private HerbiAfkOverlay overlay;

	@Inject
	private HerbiboarPlugin herbiboarPlugin;

	@Getter
	private List<WorldPoint> pathLinePoints;
	@Getter
	private WorldPoint nextSearchSpot;

	private boolean varbitChanged = false;

	private static final List<WorldPoint> END_LOCATIONS = ImmutableList.of(
			new WorldPoint(3693, 3798, 0),
			new WorldPoint(3702, 3808, 0),
			new WorldPoint(3703, 3826, 0),
			new WorldPoint(3710, 3881, 0),
			new WorldPoint(3700, 3877, 0),
			new WorldPoint(3715, 3840, 0),
			new WorldPoint(3751, 3849, 0),
			new WorldPoint(3685, 3869, 0),
			new WorldPoint(3681, 3863, 0)
	);

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		log.info("Example started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		varbitChanged = true;
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		if (varbitChanged) {
			getTrailWorldPoints();
			varbitChanged = false;
		}
	}

	public void getTrailWorldPoints() {
		List<? extends Enum<?>> currentPath = herbiboarPlugin.getCurrentPath();
		int currentPathSize = currentPath.size();

		if (currentPathSize >= 1) {
			WorldPoint startLocation, endLocation;
			if (herbiboarPlugin.getFinishId() > 0) {
				startLocation = HerbiboarSearchSpot.valueOf(currentPath.get(currentPathSize - 1).toString()).getLocation();
				endLocation = END_LOCATIONS.get(herbiboarPlugin.getFinishId() - 1);
			}
			else if (currentPathSize == 1) {
				startLocation = herbiboarPlugin.getStartPoint();
				endLocation = HerbiboarSearchSpot.valueOf(currentPath.get(0).toString()).getLocation();
			} else {
				startLocation = HerbiboarSearchSpot.valueOf(currentPath.get(currentPathSize - 2).toString()).getLocation();
				endLocation = HerbiboarSearchSpot.valueOf(currentPath.get(currentPathSize - 1).toString()).getLocation();
			}

			nextSearchSpot = endLocation;
			pathLinePoints = Arrays.asList(startLocation, endLocation);
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		//You stun the creature.
		//GAMEMESSAGE
		//Your herbiboar harvest count is:
	}

	public boolean isInHerbiboarArea() {
		return herbiboarPlugin.isInHerbiboarArea();
	}

	@Provides
	HerbiAfkConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HerbiAfkConfig.class);
	}
}
