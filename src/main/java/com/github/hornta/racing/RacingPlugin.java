package com.github.hornta.racing;

import com.github.hornta.commando.*;
import com.github.hornta.messenger.MessageManager;
import com.github.hornta.messenger.MessagesBuilder;
import com.github.hornta.messenger.MessengerException;
import com.github.hornta.messenger.Translation;
import com.github.hornta.messenger.Translations;
import com.github.hornta.racing.api.FileAPI;
import com.github.hornta.racing.api.StorageType;
import com.github.hornta.racing.commands.*;
import com.github.hornta.racing.commands.argumentHandlers.*;
import com.github.hornta.racing.enums.Permission;
import com.github.hornta.racing.enums.RespawnType;
import com.github.hornta.racing.enums.TeleportAfterRaceWhen;
import com.github.hornta.racing.mcmmo.McMMOListener;
import com.github.hornta.racing.objects.RaceCommandExecutor;
import com.github.hornta.versioned_config.Configuration;
import com.github.hornta.versioned_config.ConfigurationBuilder;
import com.github.hornta.versioned_config.ConfigurationException;
import com.github.hornta.versioned_config.Migration;
import com.github.hornta.versioned_config.Patch;
import com.github.hornta.versioned_config.Type;
import com.gmail.nossr50.mcMMO;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class RacingPlugin extends JavaPlugin {
  private static RacingPlugin instance;
  private boolean isNoteBlockAPILoaded;
  private boolean isHolographicDisplaysLoaded;
  private Economy economy;
  private Commando commando;
  private Translations translations;
  private RacingManager racingManager;
  private Configuration<ConfigKey> configuration;

  public static RacingPlugin getInstance() {
    return instance;
  }

  public static Logger logger() {
    return instance.getLogger();
  }

  public Economy getEconomy() {
    return economy;
  }

  public boolean isNoteBlockAPILoaded() {
    return isNoteBlockAPILoaded;
  }

  public boolean isHolographicDisplaysLoaded() {
    return isHolographicDisplaysLoaded;
  }

  public Commando getCommando() {
    return commando;
  }

  public Translations getTranslations() {
    return translations;
  }

  public RacingManager getRacingManager() {
    return racingManager;
  }

  public Configuration<ConfigKey> getConfiguration() {
    return configuration;
  }

  @Override
  public void onEnable() {
    instance = this;
    new Metrics(this, 5356);
    isNoteBlockAPILoaded = Bukkit.getPluginManager().isPluginEnabled("NoteBlockAPI");
    isHolographicDisplaysLoaded = Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays");

    if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
      {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
          economy = rsp.getProvider();
        }
      }
    }

    try {
      setupConfig();
    } catch (ConfigurationException e) {
      getLogger().severe("Failed to setup configuration: " + e.getMessage());
      setEnabled(false);
      return;
    }
    try {
      setupMessages();
    } catch (MessengerException e) {
      getLogger().severe("Failed to setup messages: " + e.getMessage());
      setEnabled(false);
      return;
    }
    setupNoteBlockAPI();
    setupObjects();
    setupCommands();
  }

  @Override
  public void onDisable() {
    if(racingManager != null) {
      racingManager.shutdown();
    }
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
    return commando.handleCommand(sender, command, args);
  }

  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
    return commando.handleAutoComplete(sender, command, args);
  }

  private void setupConfig() throws ConfigurationException {
    File cfgFile = new File(getDataFolder(), "config.yml");
    ConfigurationBuilder<ConfigKey> cb = new ConfigurationBuilder<>(cfgFile);
    cb.addMigration(new Migration<>(1, () -> {
      Patch<ConfigKey> patch = new Patch<>();
      patch.set(ConfigKey.LANGUAGE, "language", "english", Type.STRING);
      // https://www.loc.gov/standards/iso639-2/php/code_list.php
      patch.set(ConfigKey.LOCALE, "locale", "en", Type.STRING);
      patch.set(ConfigKey.SONGS_DIRECTORY, "songs_directory", "songs", Type.STRING);
      patch.set(ConfigKey.STORAGE, "storage.current", StorageType.FILE.name(), Type.STRING);
      patch.set(ConfigKey.FILE_RACE_DIRECTORY, "storage.file.directory", "races", Type.STRING);
      patch.set(ConfigKey.RACE_PREPARE_TIME, "prepare_time", 60, Type.INTEGER);
      patch.set(ConfigKey.RACE_ANNOUNCE_INTERVALS, "race_announce_intervals", Arrays.asList(30, 10), Type.LIST);
      patch.set(ConfigKey.COUNTDOWN, "countdown", 10, Type.INTEGER);
      patch.set(ConfigKey.RESPAWN_PLAYER_DEATH, "respawn.player.death", RespawnType.FROM_LAST_CHECKPOINT, Type.STRING);
      patch.set(ConfigKey.RESPAWN_PLAYER_INTERACT, "respawn.player.interact", RespawnType.NONE, Type.STRING);
      patch.set(ConfigKey.RESPAWN_ELYTRA_DEATH, "respawn.elytra.death", RespawnType.FROM_START, Type.STRING);
      patch.set(ConfigKey.RESPAWN_ELYTRA_INTERACT, "respawn.elytra.interact", RespawnType.FROM_START, Type.STRING);
      patch.set(ConfigKey.RESPAWN_PIG_DEATH, "respawn.pig.death", RespawnType.FROM_LAST_CHECKPOINT, Type.STRING);
      patch.set(ConfigKey.RESPAWN_PIG_INTERACT, "respawn.pig.interact", RespawnType.NONE, Type.STRING);
      patch.set(ConfigKey.RESPAWN_HORSE_DEATH, "respawn.horse.death", RespawnType.FROM_LAST_CHECKPOINT, Type.STRING);
      patch.set(ConfigKey.RESPAWN_HORSE_INTERACT, "respawn.horse.interact", RespawnType.NONE, Type.STRING);
      patch.set(ConfigKey.RESPAWN_BOAT_DEATH, "respawn.boat.death", RespawnType.FROM_LAST_CHECKPOINT, Type.STRING);
      patch.set(ConfigKey.RESPAWN_BOAT_INTERACT, "respawn.boat.interact", RespawnType.NONE, Type.STRING);
      patch.set(ConfigKey.RESPAWN_MINECART_DEATH, "respawn.minecart.death", RespawnType.FROM_LAST_CHECKPOINT, Type.STRING);
      patch.set(ConfigKey.RESPAWN_MINECART_INTERACT, "respawn.minecart.interact", RespawnType.FROM_LAST_CHECKPOINT, Type.STRING);
      patch.set(ConfigKey.DISCORD_ENABLED, "discord.enabled", false, Type.BOOLEAN);
      patch.set(ConfigKey.DISCORD_TOKEN, "discord.bot_token", "", Type.STRING);
      patch.set(ConfigKey.DISCORD_ANNOUNCE_CHANNEL, "discord.announce_channel", "", Type.STRING);
      patch.set(ConfigKey.ADVENTURE_ON_START, "adventure_mode_on_start", true, Type.BOOLEAN);
      patch.set(ConfigKey.ELYTRA_RESPAWN_ON_GROUND, "elytra_respawn_on_ground", true, Type.BOOLEAN);
      patch.set(ConfigKey.BLOCKED_COMMANDS, "blocked_commands", Arrays.asList("spawn", "wild", "wilderness", "rtp", "tpa", "tpo", "tp", "tpahere", "tpaccept", "tpdeny", "tpyes", "tpno", "tppos", "warp", "home", "rc spawn", "racing spawn"), Type.LIST);
      patch.set(ConfigKey.PREVENT_JOIN_FROM_GAME_MODE, "prevent_join_from_game_mode", Collections.emptyList(), Type.LIST);
      patch.set(ConfigKey.START_ON_JOIN_SIGN, "start_on_join.sign", false, Type.BOOLEAN);
      patch.set(ConfigKey.START_ON_JOIN_COMMAND, "start_on_join.command", false, Type.BOOLEAN);
      patch.set(ConfigKey.TELEPORT_AFTER_RACE_ENABLED, "teleport_after_race.enabled", false, Type.BOOLEAN);
      patch.set(ConfigKey.TELEPORT_AFTER_RACE_WHEN, "teleport_after_race.when", TeleportAfterRaceWhen.PARTICIPANT_FINISHES, Type.STRING);
      patch.set(ConfigKey.VERBOSE, "verbose", false, Type.BOOLEAN);
      patch.set(ConfigKey.CHECKPOINT_PARTICLES_DURING_RACE, "checkpoint_particles_during_race", true, Type.BOOLEAN);
      patch.set(ConfigKey.SCOREBOARD_ENABLED, "scoreboard.enabled", true, Type.BOOLEAN);
      patch.set(ConfigKey.SCOREBOARD_DISPLAY_MILLISECONDS, "scoreboard.display_milliseconds", false, Type.BOOLEAN);
      patch.set(ConfigKey.SCOREBOARD_WORLD_RECORD, "scoreboard.display_world_record", true, Type.BOOLEAN);
      patch.set(ConfigKey.SCOREBOARD_WORLD_RECORD_HOLDER, "scoreboard.display_world_record_holder", true, Type.BOOLEAN);
      patch.set(ConfigKey.SCOREBOARD_WORLD_RECORD_FASTEST_LAP, "scoreboard.display_world_record_fastest_lap", false, Type.BOOLEAN);
      patch.set(ConfigKey.SCOREBOARD_WORLD_RECORD_FASTEST_LAP_HOLDER, "scoreboard.display_world_record_fastest_lap_holder", false, Type.BOOLEAN);
      patch.set(ConfigKey.SCOREBOARD_PERSONAL_RECORD, "scoreboard.display_personal_record", true, Type.BOOLEAN);
      patch.set(ConfigKey.SCOREBOARD_PERSONAL_RECORD_FASTEST_LAP, "scoreboard.display_record_fastest_lap", false, Type.BOOLEAN);
      patch.set(ConfigKey.SCOREBOARD_TIME, "scoreboard.display_time", true, Type.BOOLEAN);
      patch.set(ConfigKey.SCOREBOARD_LAP_TIME, "scoreboard.display_lap_time", false, Type.BOOLEAN);
      patch.set(ConfigKey.SCOREBOARD_FASTEST_LAP, "scoreboard.display_fastest_lap", false, Type.BOOLEAN);
      patch.set(ConfigKey.SCOREBOARD_TICKS_PER_UPDATE, "scoreboard.ticks_per_update", 1, Type.INTEGER);
      patch.set(ConfigKey.ALLOW_CHORUS_FRUIT_TP, "allow_chorus_fruit_tp", false, Type.BOOLEAN);
      patch.set(ConfigKey.ALLOW_ENDER_PEARL_TP, "allow_ender_pearl_tp", false, Type.BOOLEAN);
      patch.set(ConfigKey.ALLOW_FOOD_LEVEL_CHANGE, "allow_food_level_change", false, Type.BOOLEAN);
      return patch;
    }));
    configuration = cb.create();
  }

  private void setupMessages() throws MessengerException {
    MessageManager messageManager = new MessagesBuilder()
      .add(MessageKey.CREATE_RACE_SUCCESS, "commands.create_race.success")
      .add(MessageKey.CREATE_RACE_NAME_OCCUPIED, "commands.create_race.error_name_occupied")
      .add(MessageKey.DELETE_RACE_SUCCESS, "commands.delete_race.success")
      .add(MessageKey.CHANGE_RACE_NAME_SUCCESS, "commands.change_race_name.success")
      .add(MessageKey.RACE_ADD_CHECKPOINT_SUCCESS, "commands.race_add_checkpoint.success")
      .add(MessageKey.RACE_ADD_CHECKPOINT_IS_OCCUPIED, "commands.race_add_checkpoint.error_is_occupied")
      .add(MessageKey.RACE_DELETE_CHECKPOINT_SUCCESS, "commands.race_delete_checkpoint.success")
      .add(MessageKey.RACE_ADD_STARTPOINT_SUCCESS, "commands.race_add_startpoint.success")
      .add(MessageKey.RACE_ADD_STARTPOINT_IS_OCCUPIED, "commands.race_add_startpoint.error_is_occupied")
      .add(MessageKey.RACE_DELETE_STARTPOINT_SUCCESS, "commands.race_delete_startpoint.success")
      .add(MessageKey.RACE_SPAWN_NOT_ENABLED, "commands.race_spawn.error_not_enabled")
      .add(MessageKey.RACE_SET_SPAWN_SUCCESS, "commands.race_set_spawn.success")
      .add(MessageKey.LIST_RACES_LIST, "commands.list_races.race_list")
      .add(MessageKey.LIST_RACES_ITEM, "commands.list_races.race_list_item")
      .add(MessageKey.RACE_SET_TYPE_SUCCESS, "commands.race_set_type.success")
      .add(MessageKey.RACE_SET_TYPE_NOCHANGE, "commands.race_set_type.error_nochange")
      .add(MessageKey.RACE_SET_START_ORDER_SUCCESS, "commands.race_set_start_order.success")
      .add(MessageKey.RACE_SET_START_ORDER_NOCHANGE, "commands.race_set_start_order.error_nochange")
      .add(MessageKey.RACE_SET_SONG_SUCCESS, "commands.race_set_song.success")
      .add(MessageKey.RACE_SET_SONG_NOCHANGE, "commands.race_set_song.error_nochange")
      .add(MessageKey.RACE_UNSET_SONG_SUCCESS, "commands.race_unset_song.success")
      .add(MessageKey.RACE_UNSET_SONG_ALREADY_UNSET, "commands.race_unset_song.error_already_unset")
      .add(MessageKey.START_RACE_ALREADY_STARTED, "commands.start_race.error_already_started")
      .add(MessageKey.START_RACE_MISSING_STARTPOINT, "commands.start_race.error_missing_startpoint")
      .add(MessageKey.START_RACE_MISSING_CHECKPOINT, "commands.start_race.error_missing_checkpoint")
      .add(MessageKey.START_RACE_MISSING_CHECKPOINTS, "commands.start_race.error_missing_checkpoints")
      .add(MessageKey.START_RACE_NOT_ENABLED, "commands.start_race.error_not_enabled")
      .add(MessageKey.START_RACE_NO_ENABLED, "commands.start_race.error_no_enabled")
      .add(MessageKey.STOP_RACE_SUCCESS, "commands.stop_race.success")
      .add(MessageKey.STOP_RACE_NOT_STARTED, "commands.stop_race.error_not_started")
      .add(MessageKey.JOIN_RACE_SUCCESS, "commands.join_race.success")
      .add(MessageKey.JOIN_RACE_CHARGED, "commands.join_race.charged")
      .add(MessageKey.JOIN_RACE_NOT_OPEN, "commands.join_race.error_not_open")
      .add(MessageKey.JOIN_RACE_IS_FULL, "commands.join_race.error_is_full")
      .add(MessageKey.JOIN_RACE_IS_PARTICIPATING, "commands.join_race.error_is_participating")
      .add(MessageKey.JOIN_RACE_IS_PARTICIPATING_OTHER, "commands.join_race.error_is_participating_other")
      .add(MessageKey.JOIN_RACE_NOT_AFFORD, "commands.join_race.error_not_afford")
      .add(MessageKey.JOIN_RACE_GAME_MODE, "commands.join_race.error_game_mode")
      .add(MessageKey.RACE_SKIP_WAIT_NOT_STARTED, "commands.race_skip_wait.error_not_started")
      .add(MessageKey.RELOAD_SUCCESS, "commands.reload.success")
      .add(MessageKey.RELOAD_FAILED, "commands.reload.failed")
      .add(MessageKey.RELOAD_MESSAGES_FAILED, "commands.reload.failed_messages")
      .add(MessageKey.RELOAD_NOT_RACES, "commands.reload.not_races")
      .add(MessageKey.RELOAD_RACES_FAILED, "commands.reload.races_failed")
      .add(MessageKey.RELOAD_NOT_LANGUAGE, "commands.reload.not_language")
      .add(MessageKey.RACE_SET_STATE_SUCCESS, "commands.race_set_state.success")
      .add(MessageKey.RACE_SET_STATE_NOCHANGE, "commands.race_set_state.error_nochange")
      .add(MessageKey.RACE_SET_STATE_ONGOING, "commands.race_set_state.error_ongoing")
      .add(MessageKey.RACE_HELP_TITLE, "commands.race_help.title")
      .add(MessageKey.RACE_HELP_ITEM, "commands.race_help.item")
      .add(MessageKey.RACE_SET_ENTRYFEE, "commands.race_set_entryfee.success")
      .add(MessageKey.RACE_SET_WALKSPEED, "commands.race_set_walkspeed.success")
      .add(MessageKey.RACE_SET_PIG_SPEED, "commands.race_set_pig_speed.success")
      .add(MessageKey.RACE_SET_HORSE_SPEED, "commands.race_set_horse_speed.success")
      .add(MessageKey.RACE_SET_HORSE_JUMP_STRENGTH, "commands.race_set_horse_jump_strength.success")
      .add(MessageKey.RACE_ADD_POTION_EFFECT, "commands.race_add_potion_effect.success")
      .add(MessageKey.RACE_REMOVE_POTION_EFFECT, "commands.race_remove_potion_effect.success")
      .add(MessageKey.RACE_CLEAR_POTION_EFFECTS, "commands.race_clear_potion_effects.success")
      .add(MessageKey.RACE_LEAVE_NOT_PARTICIPATING, "commands.race_leave.error_not_participating")
      .add(MessageKey.RACE_LEAVE_SUCCESS, "commands.race_leave.success")
      .add(MessageKey.RACE_LEAVE_BROADCAST, "commands.race_leave.leave_broadcast")
      .add(MessageKey.RACE_LEAVE_PAYBACK, "commands.race_leave.leave_payback")
      .add(MessageKey.RACE_INFO_SUCCESS, "commands.race_info.success")
      .add(MessageKey.RACE_INFO_NO_POTION_EFFECTS, "commands.race_info.no_potion_effects")
      .add(MessageKey.RACE_INFO_POTION_EFFECT, "commands.race_info.potion_effect_item")
      .add(MessageKey.RACE_INFO_ENTRY_FEE_LINE, "commands.race_info.entry_fee_line")
      .add(MessageKey.RACE_TOP_TYPE_FASTEST, "commands.race_top.types.fastest")
      .add(MessageKey.RACE_TOP_TYPE_FASTEST_LAP, "commands.race_top.types.fastest_lap")
      .add(MessageKey.RACE_TOP_TYPE_MOST_RUNS, "commands.race_top.types.most_runs")
      .add(MessageKey.RACE_TOP_TYPE_MOST_WINS, "commands.race_top.types.most_wins")
      .add(MessageKey.RACE_TOP_TYPE_WIN_RATIO, "commands.race_top.types.win_ratio")
      .add(MessageKey.RACE_TOP_HEADER, "commands.race_top.header")
      .add(MessageKey.RACE_TOP_ITEM, "commands.race_top.item")
      .add(MessageKey.RACE_TOP_ITEM_NONE, "commands.race_top.item_none")
      .add(MessageKey.RACE_RESET_TOP, "commands.race_reset_top.success")
      .add(MessageKey.RACE_NOT_FOUND, "validators.race_not_found")
      .add(MessageKey.RACE_ALREADY_EXIST, "validators.race_already_exist")
      .add(MessageKey.CHECKPOINT_NOT_FOUND, "validators.checkpoint_not_found")
      .add(MessageKey.CHECKPOINT_ALREADY_EXIST, "validators.checkpoint_already_exist")
      .add(MessageKey.STARTPOINT_NOT_FOUND, "validators.startpoint_not_found")
      .add(MessageKey.STARTPOINT_ALREADY_EXIST, "validators.startpoint_already_exist")
      .add(MessageKey.TYPE_NOT_FOUND, "validators.type_not_found")
      .add(MessageKey.START_ORDER_NOT_FOUND, "validators.start_order_not_found")
      .add(MessageKey.STATE_NOT_FOUND, "validators.state_not_found")
      .add(MessageKey.SONG_NOT_FOUND, "validators.song_not_found")
      .add(MessageKey.VALIDATE_NON_INTEGER, "validators.validate_non_integer")
      .add(MessageKey.VALIDATE_NON_NUMBER, "validators.validate_non_number")
      .add(MessageKey.VALIDATE_MIN_EXCEED, "validators.min_exceed")
      .add(MessageKey.VALIDATE_MAX_EXCEED, "validators.max_exceed")
      .add(MessageKey.RACE_POTION_EFFECT_NOT_FOUND, "validators.race_potion_effect_not_found")
      .add(MessageKey.POTION_EFFECT_NOT_FOUND, "validators.potion_effect_not_found")
      .add(MessageKey.STAT_TYPE_NOT_FOUND, "validators.stat_type_not_found")
      .add(MessageKey.RACE_CANCELED, "race_canceled")
      .add(MessageKey.NOSHOW_DISQUALIFIED, "race_start_noshow_disqualified")
      .add(MessageKey.GAME_MODE_DISQUALIFIED, "race_start_gamemode_disqualified")
      .add(MessageKey.GAME_MODE_DISQUALIFIED_TARGET, "race_start_gamemode_disqualified_target")
      .add(MessageKey.QUIT_DISQUALIFIED, "race_start_quit_disqualified")
      .add(MessageKey.DEATH_DISQUALIFIED, "race_death_disqualified")
      .add(MessageKey.DEATH_DISQUALIFIED_TARGET, "race_death_disqualified_target")
      .add(MessageKey.EDIT_NO_EDIT_MODE, "edit_no_edit_mode")
      .add(MessageKey.RACE_PARTICIPANT_RESULT, "race_participant_result")
      .add(MessageKey.PARTICIPATE_CLICK_TEXT, "race_participate_click_text")
      .add(MessageKey.PARTICIPATE_HOVER_TEXT, "race_participate_hover_text")
      .add(MessageKey.PARTICIPATE_TEXT, "race_participate_text")
      .add(MessageKey.PARTICIPATE_TEXT_FEE, "race_participate_text_fee")
      .add(MessageKey.PARTICIPATE_DISCORD, "race_participate_discord")
      .add(MessageKey.PARTICIPATE_DISCORD_FEE, "race_participate_discord_fee")
      .add(MessageKey.PARTICIPATE_TEXT_TIMELEFT, "race_participate_text_timeleft")
      .add(MessageKey.RACE_COUNTDOWN, "race_countdown_subtitle")
      .add(MessageKey.RACE_NEXT_LAP, "race_next_lap_actionbar")
      .add(MessageKey.RACE_FINAL_LAP, "race_final_lap_actionbar")
      .add(MessageKey.RESPAWN_INTERACT_START, "race_type_respawn_start_info")
      .add(MessageKey.RESPAWN_INTERACT_LAST, "race_type_respawn_last_info")
      .add(MessageKey.SKIP_WAIT_HOVER_TEXT, "race_skipwait_hover_text")
      .add(MessageKey.SKIP_WAIT_CLICK_TEXT, "race_skipwait_click_text")
      .add(MessageKey.SKIP_WAIT, "race_skipwait")
      .add(MessageKey.STOP_RACE_HOVER_TEXT, "race_stop_hover_text")
      .add(MessageKey.STOP_RACE_CLICK_TEXT, "race_stop_click_text")
      .add(MessageKey.STOP_RACE, "race_stop")
      .add(MessageKey.SIGN_REGISTERED, "race_sign_registered")
      .add(MessageKey.SIGN_UNREGISTERED, "race_sign_unregistered")
      .add(MessageKey.RACE_SIGN_LINES, "race_sign_lines")
      .add(MessageKey.RACE_SIGN_FASTEST_LINES, "race_sign_fastest_lines")
      .add(MessageKey.RACE_SIGN_STATS_LINES, "race_sign_stats_lines")
      .add(MessageKey.SIGN_NOT_STARTED, "race_sign_status_not_started")
      .add(MessageKey.SIGN_LOBBY, "race_sign_status_lobby")
      .add(MessageKey.SIGN_STARTED, "race_sign_status_in_game")
      .add(MessageKey.BLOCKED_CMDS, "race_blocked_cmd")
      .add(MessageKey.NO_PERMISSION_COMMAND, "no_permission_command")
      .add(MessageKey.MISSING_ARGUMENTS_COMMAND, "missing_arguments_command")
      .add(MessageKey.COMMAND_NOT_FOUND, "command_not_found")
      .add(MessageKey.TIME_UNIT_SECOND, "timeunit.second")
      .add(MessageKey.TIME_UNIT_SECONDS, "timeunit.seconds")
      .add(MessageKey.TIME_UNIT_MINUTE, "timeunit.minute")
      .add(MessageKey.TIME_UNIT_MINUTES, "timeunit.minutes")
      .add(MessageKey.TIME_UNIT_HOUR, "timeunit.hour")
      .add(MessageKey.TIME_UNIT_HOURS, "timeunit.hours")
      .add(MessageKey.TIME_UNIT_DAY, "timeunit.day")
      .add(MessageKey.TIME_UNIT_DAYS, "timeunit.days")
      .add(MessageKey.TIME_UNIT_NOW, "timeunit.now")
      .add(MessageKey.SCOREBOARD_HEADING_FORMAT, "scoreboard.heading_format")
      .add(MessageKey.SCOREBOARD_TITLE_FORMAT, "scoreboard.title_format")
      .add(MessageKey.SCOREBOARD_TEXT_FORMAT, "scoreboard.text_format")
      .add(MessageKey.SCOREBOARD_WORLD_RECORD, "scoreboard.world_record")
      .add(MessageKey.SCOREBOARD_WORLD_RECORD_FASTEST_LAP, "scoreboard.world_record_fastest_lap")
      .add(MessageKey.SCOREBOARD_PERSONAL_RECORD, "scoreboard.personal_record")
      .add(MessageKey.SCOREBOARD_TIME, "scoreboard.time")
      .add(MessageKey.SCOREBOARD_FASTEST_LAP, "scoreboard.fastest_lap")
      .add(MessageKey.SCOREBOARD_LAP_TAG, "scoreboard.lap_tag")
      .add(MessageKey.SCOREBOARD_NO_TIME_STATS, "scoreboard.no_time_stats")
      .add(MessageKey.SCOREBOARD_NO_NAME_STATS, "scoreboard.no_name_stats")
      .add(MessageKey.LAP_SINGULAR, "lap.singular")
      .add(MessageKey.LAP_PLURAL, "lap.plural")
      .build();

    translations = new Translations(this, messageManager);
    String language = configuration.get(ConfigKey.LANGUAGE);
    Translation translation = translations.createTranslation(language);
    messageManager.setTranslation(translation);
  }

  private void setupNoteBlockAPI() {
    if(isNoteBlockAPILoaded) {
      SongManager.init(this);
      getServer().getPluginManager().registerEvents(SongManager.getInstance(), this);
    }
  }

  private void setupCommands() {
    commando = new Commando();

    commando.setNoPermissionHandler((CommandSender commandSender, CarbonCommand command) -> MessageManager.sendMessage(commandSender, MessageKey.NO_PERMISSION_COMMAND));

    commando.setMissingArgumentHandler((CommandSender commandSender, CarbonCommand command) -> {
      MessageManager.setValue("usage", command.getHelpText());
      MessageManager.sendMessage(commandSender, MessageKey.MISSING_ARGUMENTS_COMMAND);
    });

    commando.setMissingCommandHandler((CommandSender sender, List<CarbonCommand> suggestions) -> {
      MessageManager.setValue("suggestions", suggestions.stream()
        .map(CarbonCommand::getHelpText)
        .collect(Collectors.joining("\n")));
      MessageManager.sendMessage(sender, MessageKey.COMMAND_NOT_FOUND);
    });

    commando.handleValidation((ValidationResult result) -> {
      switch (result.getStatus()) {
        case ERR_INCORRECT_TYPE:
          MessageManager.setValue("help_text", result.getCommand().getHelpText());
          MessageManager.setValue("argument", result.getArgument().getName());
          MessageManager.setValue("received", result.getValue());
          if (result.getArgument().getType() == CarbonArgumentType.INTEGER) {
            MessageManager.sendMessage(result.getCommandSender(), MessageKey.VALIDATE_NON_INTEGER);
          } else if (result.getArgument().getType() == CarbonArgumentType.NUMBER) {
            MessageManager.sendMessage(result.getCommandSender(), MessageKey.VALIDATE_NON_INTEGER);
          }
          break;

        case ERR_MIN_LIMIT:
        case ERR_MAX_LIMIT:
          MessageManager.setValue("help_text", result.getCommand().getHelpText());
          MessageManager.setValue("argument", result.getArgument().getName());
          MessageManager.setValue("received", result.getValue());
          if(result.getStatus() == ValidationStatus.ERR_MIN_LIMIT) {
            MessageManager.setValue("expected", result.getArgument().getMin());
            MessageManager.sendMessage(result.getCommandSender(), MessageKey.VALIDATE_MIN_EXCEED);
          } else {
            MessageManager.setValue("expected", result.getArgument().getMax());
            MessageManager.sendMessage(result.getCommandSender(), MessageKey.VALIDATE_MAX_EXCEED);
          }
          break;

        case ERR_OTHER:
          if(result.getArgument().getType() == CarbonArgumentType.POTION_EFFECT) {
            MessageManager.setValue("potion_effect", result.getValue());
            MessageManager.sendMessage(result.getCommandSender(), MessageKey.POTION_EFFECT_NOT_FOUND);
          }
          break;
        case ERR_MAX_LENGTH:
        case ERR_MIN_LENGTH:
        case ERR_PATTERN:
        default:
          break;
      }
    });

    ICarbonArgument raceArgument =
      new CarbonArgument.Builder("race")
        .setHandler(new RaceArgumentHandler(racingManager, true))
        .create();

    commando
      .addCommand("racing create")
      .withHandler(new CommandCreateRace(racingManager))
      .withArgument(
        new CarbonArgument.Builder("race")
          .setHandler(new RaceArgumentHandler(racingManager, false))
          .showTabCompletion(false)
          .create()
      )
      .requiresPermission(Permission.COMMAND_CREATE.toString())
      .requiresPermission(Permission.RACING_MODIFY.toString())
      .preventConsoleCommandSender();

    commando
      .addCommand("racing delete")
      .withHandler(new CommandDeleteRace(racingManager))
      .withArgument(raceArgument)
      .requiresPermission(Permission.COMMAND_DELETE.toString())
      .requiresPermission(Permission.RACING_MODIFY.toString());

    commando
      .addCommand("racing list")
      .withHandler(new CommandRaces(racingManager))
      .requiresPermission(Permission.COMMAND_LIST.toString())
      .requiresPermission(Permission.RACING_PLAYER.toString());

    commando
      .addCommand("racing addcheckpoint")
      .withHandler(new CommandAddCheckpoint(racingManager))
      .withArgument(raceArgument)
      .requiresPermission(Permission.COMMAND_ADD_CHECKPOINT.toString())
      .requiresPermission(Permission.RACING_MODIFY.toString())
      .preventConsoleCommandSender();

    ICarbonArgument checkpointArgument = new CarbonArgument.Builder("point")
      .setHandler(new CheckpointArgumentHandler(racingManager, true))
      .dependsOn(raceArgument)
      .create();

    commando
      .addCommand("racing deletecheckpoint")
      .withHandler(new CommandDeleteCheckpoint(racingManager))
      .withArgument(raceArgument)
      .withArgument(checkpointArgument)
      .requiresPermission(Permission.COMMAND_DELETE_CHECKPOINT.toString())
      .requiresPermission(Permission.RACING_MODIFY.toString());

    commando
      .addCommand("racing tpcheckpoint")
      .withHandler(new CommandRaceTeleportPoint(racingManager))
      .withArgument(raceArgument)
      .withArgument(checkpointArgument)
      .requiresPermission(Permission.COMMAND_TELEPORT_CHECKPOINT.toString())
      .requiresPermission(Permission.RACING_MODIFY.toString())
      .preventConsoleCommandSender();

    commando
      .addCommand("racing spawn")
      .withHandler(new CommandRaceSpawn(racingManager))
      .withArgument(raceArgument)
      .requiresPermission(Permission.RACING_PLAYER.toString())
      .requiresPermission(Permission.COMMAND_SPAWN.toString())
      .preventConsoleCommandSender();

    commando
      .addCommand("racing setspawn")
      .withHandler(new CommandRaceSetSpawn(racingManager))
      .withArgument(raceArgument)
      .requiresPermission(Permission.COMMAND_SET_SPAWN.toString())
      .requiresPermission(Permission.RACING_MODIFY.toString())
      .preventConsoleCommandSender();

    commando
      .addCommand("racing setstate")
      .withHandler(new CommandSetRaceState(racingManager))
      .withArgument(raceArgument)
      .withArgument(
        new CarbonArgument.Builder("state")
          .setHandler(new RaceStateArgumentHandler())
          .create()
      )
      .requiresPermission(Permission.COMMAND_SET_STATE.toString())
      .requiresPermission(Permission.RACING_MODIFY.toString());

    commando
      .addCommand("racing setname")
      .withHandler(new CommandSetRaceName(racingManager))
      .withArgument(raceArgument)
      .withArgument(
        new CarbonArgument.Builder("name")
          .setType(CarbonArgumentType.STRING)
          .create()
      )
      .requiresPermission(Permission.COMMAND_SET_NAME.toString())
      .requiresPermission(Permission.RACING_MODIFY.toString());

    commando
      .addCommand("racing settype")
      .withHandler(new CommandSetType(racingManager))
      .withArgument(raceArgument)
      .withArgument(
        new CarbonArgument.Builder("type")
          .setHandler(new RaceTypeArgumentHandler())
          .create()
      )
      .requiresPermission(Permission.COMMAND_SET_TYPE.toString())
      .requiresPermission(Permission.RACING_MODIFY.toString());

    commando
      .addCommand("racing setstartorder")
      .withHandler(new CommandSetStartOrder(racingManager))
      .withArgument(raceArgument)
      .withArgument(
        new CarbonArgument.Builder("order")
          .setHandler(new StartOrderArgumentHandler())
          .create()
      )
      .requiresPermission(Permission.COMMAND_SET_START_ORDER.toString())
      .requiresPermission(Permission.RACING_MODIFY.toString());

    if(economy != null) {
      commando
        .addCommand("racing setentryfee")
        .withHandler(new CommandSetEntryFee(racingManager))
        .withArgument(raceArgument)
        .withArgument(
          new CarbonArgument.Builder("fee")
            .setType(CarbonArgumentType.NUMBER)
            .setMin(0)
            .create()
        )
        .requiresPermission(Permission.COMMAND_SET_ENTRY_FEE.toString())
        .requiresPermission(Permission.RACING_MODIFY.toString());
    }


    ICarbonArgument speedArgument = new CarbonArgument.Builder("speed")
      .setType(CarbonArgumentType.NUMBER)
      .setMin(0)
      .create();

    commando
      .addCommand("racing setwalkspeed")
      .withHandler(new CommandSetWalkSpeed(racingManager))
      .withArgument(raceArgument)
      .withArgument(speedArgument)
      .requiresPermission(Permission.COMMAND_SET_WALK_SPEED.toString())
      .requiresPermission(Permission.RACING_MODIFY.toString());

    commando
      .addCommand("racing setpigspeed")
      .withHandler(new CommandSetPigSpeed(racingManager))
      .withArgument(raceArgument)
      .withArgument(speedArgument)
      .requiresPermission(Permission.COMMAND_SET_PIG_SPEED.toString())
      .requiresPermission(Permission.RACING_MODIFY.toString());

    commando
      .addCommand("racing sethorsespeed")
      .withHandler(new CommandSetHorseSpeed(racingManager))
      .withArgument(raceArgument)
      .withArgument(speedArgument)
      .requiresPermission(Permission.COMMAND_SET_HORSE_SPEED.toString())
      .requiresPermission(Permission.RACING_MODIFY.toString());

    commando
      .addCommand("racing sethorsejumpstrength")
      .withHandler(new CommandSetHorseJumpStrength(racingManager))
      .withArgument(raceArgument)
      .withArgument(
        new CarbonArgument.Builder("jump_strength")
          .setType(CarbonArgumentType.NUMBER)
          .setMin(0)
          .create()
      )
      .requiresPermission(Permission.COMMAND_SET_HORSE_JUMP_STRENGTH.toString())
      .requiresPermission(Permission.RACING_MODIFY.toString());

    commando
      .addCommand("racing addpotioneffect")
      .withHandler(new CommandAddPotionEffect(racingManager))
      .withArgument(raceArgument)
      .withArgument(
        new CarbonArgument.Builder("effect")
          .setType(CarbonArgumentType.POTION_EFFECT)
          .create()
      )
      .withArgument(
        new CarbonArgument.Builder("amplifier")
          .setType(CarbonArgumentType.INTEGER)
          .setMin(0)
          .setMax(255)
          .create()
      )
      .requiresPermission(Permission.COMMAND_ADD_POTION_EFFECT.toString())
      .requiresPermission(Permission.RACING_MODIFY.toString());

    commando
      .addCommand("racing removepotioneffect")
      .withHandler(new CommandRemovePotionEffect(racingManager))
      .withArgument(raceArgument)
      .withArgument(
        new CarbonArgument.Builder("effect")
          .setHandler(new RacePotionEffectArgumentHandler(racingManager))
          .dependsOn(raceArgument)
          .create()
      )
      .requiresPermission(Permission.COMMAND_REMOVE_POTION_EFFECT.toString())
      .requiresPermission(Permission.RACING_MODIFY.toString());

    commando
      .addCommand("racing clearpotioneffects")
      .withHandler(new CommandClearPotionEffects(racingManager))
      .withArgument(raceArgument)
      .requiresPermission(Permission.COMMAND_CLEAR_POTION_EFFECTS.toString())
      .requiresPermission(Permission.RACING_MODIFY.toString());

    commando
      .addCommand("racing addstartpoint")
      .withHandler(new CommandAddStartpoint(racingManager))
      .withArgument(raceArgument)
      .requiresPermission(Permission.COMMAND_ADD_STARTPOINT.toString())
      .requiresPermission(Permission.RACING_MODIFY.toString())
      .preventConsoleCommandSender();

    ICarbonArgument startPointArgument =
      new CarbonArgument.Builder("point")
        .setHandler(new StartPointArgumentHandler(racingManager, true))
        .dependsOn(raceArgument)
        .create();

    commando
      .addCommand("racing deletestartpoint")
      .withHandler(new CommandDeleteStartpoint(racingManager))
      .withArgument(raceArgument)
      .withArgument(startPointArgument)
      .requiresPermission(Permission.COMMAND_DELETE_STARTPOINT.toString())
      .requiresPermission(Permission.RACING_MODIFY.toString());

    commando
      .addCommand("racing tpstartpoint")
      .withHandler(new CommandRaceTeleportStart(racingManager))
      .withArgument(raceArgument)
      .withArgument(startPointArgument)
      .requiresPermission(Permission.COMMAND_TELEPORT_STARTPOINT.toString())
      .requiresPermission(Permission.RACING_MODIFY.toString())
      .preventConsoleCommandSender();

    if(isNoteBlockAPILoaded) {
      ICarbonArgument songArgument =
        new CarbonArgument.Builder("song")
          .setHandler(new SongArgumentHandler())
          .create();

      commando
        .addCommand("racing setsong")
        .withHandler(new CommandSetSong(racingManager))
        .withArgument(raceArgument)
        .withArgument(songArgument)
        .requiresPermission(Permission.COMMAND_SET_SONG.toString())
        .requiresPermission(Permission.RACING_MODIFY.toString());

      commando
        .addCommand("racing unsetsong")
        .withHandler(new CommandUnsetSong(racingManager))
        .withArgument(raceArgument)
        .requiresPermission(Permission.COMMAND_UNSET_SONG.toString())
        .requiresPermission(Permission.RACING_MODIFY.toString());

      commando
        .addCommand("racing playsong")
        .withHandler(new CommandPlaySong())
        .withArgument(songArgument)
        .requiresPermission(Permission.COMMAND_PLAY_SONG.toString())
        .requiresPermission(Permission.RACING_MODIFY.toString())
        .preventConsoleCommandSender();

      commando
        .addCommand("racing stopsong")
        .withHandler(new CommandStopSong())
        .requiresPermission(Permission.COMMAND_STOP_SONG.toString())
        .requiresPermission(Permission.RACING_MODIFY.toString())
        .preventConsoleCommandSender();
    }

    ICarbonArgument lapsArgument = new CarbonArgument.Builder("laps")
      .setType(CarbonArgumentType.INTEGER)
      .setDefaultValue(CommandSender.class, 1)
      .setMin(1)
      .create();

    commando
      .addCommand("racing start")
      .withHandler(new CommandStartRace(racingManager))
      .withArgument(raceArgument)
      .withArgument(lapsArgument)
      .requiresPermission(Permission.COMMAND_START.toString())
      .requiresPermission(Permission.RACING_MODERATOR.toString());

    commando
      .addCommand("racing startrandom")
      .withHandler(new CommandStartRace(racingManager))
      .withArgument(lapsArgument)
      .requiresPermission(Permission.COMMAND_START_RANDOM.toString())
      .requiresPermission(Permission.RACING_MODERATOR.toString());

    commando
      .addCommand("racing join")
      .withHandler(new CommandJoinRace(racingManager))
      .withArgument(raceArgument)
      .requiresPermission(Permission.COMMAND_JOIN.toString())
      .requiresPermission(Permission.RACING_PLAYER.toString())
      .preventConsoleCommandSender();

    commando
      .addCommand("racing stop")
      .withHandler(new CommandStopRace(racingManager))
      .withArgument(raceArgument)
      .requiresPermission(Permission.COMMAND_STOP.toString())
      .requiresPermission(Permission.RACING_MODERATOR.toString());

    commando
      .addCommand("racing skipwait")
      .withHandler(new CommandSkipWait(racingManager))
      .withArgument(raceArgument)
      .requiresPermission(Permission.COMMAND_SKIPWAIT.toString())
      .requiresPermission(Permission.RACING_MODERATOR.toString());

    commando
      .addCommand("racing leave")
      .withHandler(new CommandLeave(racingManager))
      .requiresPermission(Permission.COMMAND_LEAVE.toString())
      .requiresPermission(Permission.RACING_PLAYER.toString())
      .preventConsoleCommandSender();

    commando
      .addCommand("racing reload")
      .withHandler(new CommandReload())
      .requiresPermission(Permission.COMMAND_RELOAD.toString())
      .requiresPermission(Permission.RACING_ADMIN.toString());

    commando
      .addCommand("racing help")
      .withHandler(new CommandHelp())
      .requiresPermission(Permission.COMMAND_HELP.toString())
      .requiresPermission(Permission.RACING_PLAYER.toString());

    commando
      .addCommand("racing info")
      .withArgument(raceArgument)
      .withHandler(new CommandInfo(racingManager))
      .requiresPermission(Permission.COMMAND_INFO.toString())
      .requiresPermission(Permission.RACING_MODERATOR.toString());

    ICarbonArgument statArgument = new CarbonArgument.Builder("stat")
      .setHandler(new RaceStatArgumentHandler())
      .create();

    commando
      .addCommand("racing top")
      .withArgument(raceArgument)
      .withArgument(statArgument)
      .withArgument(lapsArgument)
      .withHandler(new CommandTop(racingManager))
      .requiresPermission(Permission.COMMAND_TOP.toString())
      .requiresPermission(Permission.RACING_PLAYER.toString());

    commando
      .addCommand("racing resettop")
      .withArgument(raceArgument)
      .withHandler(new CommandResetTop(racingManager))
      .requiresPermission(Permission.COMMAND_RESET_TOP.toString())
      .requiresPermission(Permission.RACING_ADMIN.toString());
  }

  private void setupObjects() {
    RaceCommandExecutor raceCommandExecutor = new RaceCommandExecutor();
    getServer().getPluginManager().registerEvents(raceCommandExecutor, this);

    racingManager = new RacingManager();
    getServer().getPluginManager().registerEvents(racingManager, this);

    SignManager signManager = new SignManager(racingManager);
    getServer().getPluginManager().registerEvents(signManager, this);

    DiscordManager discordManager = new DiscordManager();
    getServer().getPluginManager().registerEvents(discordManager, this);

    String storageTypeString = RacingPlugin.getInstance().getConfiguration().get(ConfigKey.STORAGE);
    StorageType storageType = StorageType.valueOf(storageTypeString.toUpperCase(Locale.ENGLISH));
    switch (storageType) {
      case FILE:
        racingManager.setAPI(new FileAPI(this));
        break;
      case CUSTOM:
        break;
      default:
    }
    racingManager.load();
    initMcMMO();
  }

  private void initMcMMO() {
    Plugin plugin = getServer().getPluginManager().getPlugin("mcMMO");

    if (!(plugin instanceof mcMMO)) {
      return;
    }

    Bukkit.getPluginManager().registerEvents(new McMMOListener(racingManager), RacingPlugin.getInstance());
  }

  public static void debug(String message, Object... args) {
    if(RacingPlugin.getInstance().getConfiguration().<Boolean>get(ConfigKey.VERBOSE)) {
      try {
        RacingPlugin.getInstance().getLogger().info(String.format(message, args));
      } catch (IllegalFormatConversionException e) {
        RacingPlugin.getInstance().getLogger().log(Level.SEVERE, e.getMessage(), e);
      }
    }
  }
}
