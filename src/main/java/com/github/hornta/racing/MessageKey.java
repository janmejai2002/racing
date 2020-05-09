package com.github.hornta.racing;

public enum MessageKey {
  CREATE_RACE_SUCCESS,
  CREATE_RACE_NAME_OCCUPIED,
  DELETE_RACE_SUCCESS,
  CHANGE_RACE_NAME_SUCCESS,
  RACE_ADD_CHECKPOINT_SUCCESS,
  RACE_ADD_CHECKPOINT_IS_OCCUPIED,
  RACE_DELETE_CHECKPOINT_SUCCESS,
  RACE_ADD_STARTPOINT_SUCCESS,
  RACE_ADD_STARTPOINT_IS_OCCUPIED,
  RACE_DELETE_STARTPOINT_SUCCESS,
  RACE_SPAWN_NOT_ENABLED,
  RACE_SET_SPAWN_SUCCESS,
  LIST_RACES_LIST,
  LIST_RACES_ITEM,
  RACE_SET_TYPE_SUCCESS,
  RACE_SET_TYPE_NOCHANGE,
  RACE_SET_START_ORDER_SUCCESS,
  RACE_SET_START_ORDER_NOCHANGE,
  RACE_SET_SONG_SUCCESS,
  RACE_SET_SONG_NOCHANGE,
  RACE_UNSET_SONG_SUCCESS,
  RACE_UNSET_SONG_ALREADY_UNSET,
  START_RACE_ALREADY_STARTED,
  START_RACE_MISSING_STARTPOINT,
  START_RACE_MISSING_CHECKPOINT,
  START_RACE_MISSING_CHECKPOINTS,
  START_RACE_NOT_ENABLED,
  START_RACE_NO_ENABLED,
  STOP_RACE_SUCCESS,
  STOP_RACE_NOT_STARTED,
  JOIN_RACE_SUCCESS,
  JOIN_RACE_CHARGED,
  JOIN_RACE_NOT_OPEN,
  JOIN_RACE_IS_FULL,
  JOIN_RACE_IS_PARTICIPATING,
  JOIN_RACE_IS_PARTICIPATING_OTHER,
  JOIN_RACE_NOT_AFFORD,
  JOIN_RACE_GAME_MODE,
  RACE_SKIP_WAIT_NOT_STARTED,
  RELOAD_SUCCESS,
  RELOAD_FAILED,
  RELOAD_MESSAGES_FAILED,
  RELOAD_NOT_RACES,
  RELOAD_RACES_FAILED,
  RELOAD_NOT_LANGUAGE,
  RACE_SET_STATE_SUCCESS,
  RACE_SET_STATE_NOCHANGE,
  RACE_SET_STATE_ONGOING,
  RACE_HELP_TITLE,
  RACE_HELP_ITEM,
  RACE_SET_ENTRYFEE,
  RACE_SET_WALKSPEED,
  RACE_SET_PIG_SPEED,
  RACE_SET_HORSE_SPEED,
  RACE_SET_HORSE_JUMP_STRENGTH,
  RACE_ADD_POTION_EFFECT,
  RACE_REMOVE_POTION_EFFECT,
  RACE_CLEAR_POTION_EFFECTS,
  RACE_LEAVE_NOT_PARTICIPATING,
  RACE_LEAVE_SUCCESS,
  RACE_LEAVE_BROADCAST,
  RACE_LEAVE_PAYBACK,
  RACE_INFO_SUCCESS,
  RACE_INFO_NO_POTION_EFFECTS,
  RACE_INFO_POTION_EFFECT,
  RACE_INFO_ENTRY_FEE_LINE,
  RACE_TOP_TYPE_FASTEST,
  RACE_TOP_TYPE_FASTEST_LAP,
  RACE_TOP_TYPE_MOST_RUNS,
  RACE_TOP_TYPE_MOST_WINS,
  RACE_TOP_TYPE_WIN_RATIO,
  RACE_TOP_HEADER,
  RACE_TOP_ITEM,
  RACE_TOP_ITEM_NONE,
  RACE_RESET_TOP,
  RACE_NOT_FOUND,
  RACE_ALREADY_EXIST,
  CHECKPOINT_NOT_FOUND,
  CHECKPOINT_ALREADY_EXIST,
  STARTPOINT_NOT_FOUND,
  STARTPOINT_ALREADY_EXIST,
  TYPE_NOT_FOUND,
  START_ORDER_NOT_FOUND,
  STATE_NOT_FOUND,
  SONG_NOT_FOUND,
  VALIDATE_NON_INTEGER,
  VALIDATE_NON_NUMBER,
  VALIDATE_MIN_EXCEED,
  VALIDATE_MAX_EXCEED,
  RACE_POTION_EFFECT_NOT_FOUND,
  POTION_EFFECT_NOT_FOUND,
  STAT_TYPE_NOT_FOUND,
  RACE_CANCELED,
  NOSHOW_DISQUALIFIED,
  GAME_MODE_DISQUALIFIED,
  GAME_MODE_DISQUALIFIED_TARGET,
  QUIT_DISQUALIFIED,
  DEATH_DISQUALIFIED,
  DEATH_DISQUALIFIED_TARGET,
  EDIT_NO_EDIT_MODE,
  RACE_PARTICIPANT_RESULT,
  PARTICIPATE_CLICK_TEXT,
  PARTICIPATE_HOVER_TEXT,
  PARTICIPATE_TEXT,
  PARTICIPATE_TEXT_FEE,
  PARTICIPATE_DISCORD,
  PARTICIPATE_DISCORD_FEE,
  PARTICIPATE_TEXT_TIMELEFT,
  RACE_COUNTDOWN,
  RACE_NEXT_LAP,
  RACE_FINAL_LAP,
  RESPAWN_INTERACT_START,
  RESPAWN_INTERACT_LAST,
  SKIP_WAIT_HOVER_TEXT,
  SKIP_WAIT_CLICK_TEXT,
  SKIP_WAIT,
  STOP_RACE_HOVER_TEXT,
  STOP_RACE_CLICK_TEXT,
  STOP_RACE,
  SIGN_REGISTERED,
  SIGN_UNREGISTERED,
  RACE_SIGN_LINES,
  RACE_SIGN_FASTEST_LINES,
  RACE_SIGN_STATS_LINES,
  SIGN_NOT_STARTED,
  SIGN_LOBBY,
  SIGN_STARTED,
  BLOCKED_CMDS,
  NO_PERMISSION_COMMAND,
  MISSING_ARGUMENTS_COMMAND,
  COMMAND_NOT_FOUND,
  TIME_UNIT_SECOND,
  TIME_UNIT_SECONDS,
  TIME_UNIT_MINUTE,
  TIME_UNIT_MINUTES,
  TIME_UNIT_HOUR,
  TIME_UNIT_HOURS,
  TIME_UNIT_DAY,
  TIME_UNIT_DAYS,
  TIME_UNIT_NOW,
  SCOREBOARD_HEADING_FORMAT,
  SCOREBOARD_TITLE_FORMAT,
  SCOREBOARD_TEXT_FORMAT,
  SCOREBOARD_WORLD_RECORD,
  SCOREBOARD_WORLD_RECORD_FASTEST_LAP,
  SCOREBOARD_PERSONAL_RECORD,
  SCOREBOARD_TIME,
  SCOREBOARD_FASTEST_LAP,
  SCOREBOARD_LAP_TAG,
  SCOREBOARD_NO_TIME_STATS,
  SCOREBOARD_NO_NAME_STATS,
  LAP_SINGULAR,
  LAP_PLURAL
}
