package com.github.hornta.racing.hd_top_list.commands;

import se.hornta.commando.ICommandHandler;
import se.hornta.messenger.MessageManager;
import com.github.hornta.racing.MessageKey;
import com.github.hornta.racing.enums.RaceStatType;
import com.github.hornta.racing.hd_top_list.HDTopList;
import com.github.hornta.racing.hd_top_list.HDTopListManager;
import org.bukkit.command.CommandSender;

import java.util.Locale;

public class CommandSetStatHDTopList implements ICommandHandler {
  @Override
  public void handle(CommandSender commandSender, String[] args, int i) {
    HDTopList topList = HDTopListManager.getTopList(args[0]);
    RaceStatType statType = RaceStatType.valueOf(args[1].toUpperCase(Locale.ENGLISH));
    RaceStatType oldStat = topList.getStatType();
    topList.setStatType(statType);
    HDTopListManager.updateTopList(topList, () -> {
      MessageManager.setValue("old_stat", oldStat.name());
      MessageManager.setValue("new_stat", statType.name());
      MessageManager.sendMessage(commandSender, MessageKey.HD_TOP_LIST_SET_STAT);
    });
  }
}
