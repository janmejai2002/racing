package com.github.hornta.racing.events;

import com.github.hornta.racing.objects.Race;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CreateRaceEvent extends RaceEvent {
  private static final HandlerList handlers = new HandlerList();

  public CreateRaceEvent(Race race) {
    super(race);
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlers;
  }
}
