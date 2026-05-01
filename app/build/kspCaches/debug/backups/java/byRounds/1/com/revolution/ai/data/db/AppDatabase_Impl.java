package com.revolution.ai.data.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile ActionLogDao _actionLogDao;

  private volatile LearnedBehaviorDao _learnedBehaviorDao;

  private volatile UrgencyRuleDao _urgencyRuleDao;

  private volatile EmergencyContactDao _emergencyContactDao;

  private volatile AppPermissionDao _appPermissionDao;

  private volatile BlockedKeywordDao _blockedKeywordDao;

  private volatile ScheduledTaskDao _scheduledTaskDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `action_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `command` TEXT NOT NULL, `action` TEXT NOT NULL, `result` TEXT NOT NULL, `wasSuccessful` INTEGER NOT NULL, `category` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `learned_behaviors` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `pattern` TEXT NOT NULL, `response` TEXT NOT NULL, `frequency` INTEGER NOT NULL, `lastUsed` INTEGER NOT NULL, `tag` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `urgency_rules` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `contactName` TEXT NOT NULL, `appPackage` TEXT NOT NULL, `keyword` TEXT NOT NULL, `threshold` INTEGER NOT NULL, `timeWindowMinutes` INTEGER NOT NULL, `isEnabled` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `emergency_contacts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `phoneNumber` TEXT NOT NULL, `alwaysAlert` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `app_permissions` (`packageName` TEXT NOT NULL, `appName` TEXT NOT NULL, `isAllowed` INTEGER NOT NULL, `iconUri` TEXT, PRIMARY KEY(`packageName`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `blocked_keywords` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `keyword` TEXT NOT NULL, `isActive` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `scheduled_tasks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `command` TEXT NOT NULL, `scheduledTime` INTEGER NOT NULL, `timezone` TEXT NOT NULL, `isRecurring` INTEGER NOT NULL, `recurringInterval` INTEGER NOT NULL, `isCompleted` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'fe3140d535fe641d76b86eded60c997c')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `action_logs`");
        db.execSQL("DROP TABLE IF EXISTS `learned_behaviors`");
        db.execSQL("DROP TABLE IF EXISTS `urgency_rules`");
        db.execSQL("DROP TABLE IF EXISTS `emergency_contacts`");
        db.execSQL("DROP TABLE IF EXISTS `app_permissions`");
        db.execSQL("DROP TABLE IF EXISTS `blocked_keywords`");
        db.execSQL("DROP TABLE IF EXISTS `scheduled_tasks`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsActionLogs = new HashMap<String, TableInfo.Column>(7);
        _columnsActionLogs.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsActionLogs.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsActionLogs.put("command", new TableInfo.Column("command", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsActionLogs.put("action", new TableInfo.Column("action", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsActionLogs.put("result", new TableInfo.Column("result", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsActionLogs.put("wasSuccessful", new TableInfo.Column("wasSuccessful", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsActionLogs.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysActionLogs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesActionLogs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoActionLogs = new TableInfo("action_logs", _columnsActionLogs, _foreignKeysActionLogs, _indicesActionLogs);
        final TableInfo _existingActionLogs = TableInfo.read(db, "action_logs");
        if (!_infoActionLogs.equals(_existingActionLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "action_logs(com.revolution.ai.data.model.ActionLog).\n"
                  + " Expected:\n" + _infoActionLogs + "\n"
                  + " Found:\n" + _existingActionLogs);
        }
        final HashMap<String, TableInfo.Column> _columnsLearnedBehaviors = new HashMap<String, TableInfo.Column>(6);
        _columnsLearnedBehaviors.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLearnedBehaviors.put("pattern", new TableInfo.Column("pattern", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLearnedBehaviors.put("response", new TableInfo.Column("response", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLearnedBehaviors.put("frequency", new TableInfo.Column("frequency", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLearnedBehaviors.put("lastUsed", new TableInfo.Column("lastUsed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLearnedBehaviors.put("tag", new TableInfo.Column("tag", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLearnedBehaviors = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesLearnedBehaviors = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoLearnedBehaviors = new TableInfo("learned_behaviors", _columnsLearnedBehaviors, _foreignKeysLearnedBehaviors, _indicesLearnedBehaviors);
        final TableInfo _existingLearnedBehaviors = TableInfo.read(db, "learned_behaviors");
        if (!_infoLearnedBehaviors.equals(_existingLearnedBehaviors)) {
          return new RoomOpenHelper.ValidationResult(false, "learned_behaviors(com.revolution.ai.data.model.LearnedBehavior).\n"
                  + " Expected:\n" + _infoLearnedBehaviors + "\n"
                  + " Found:\n" + _existingLearnedBehaviors);
        }
        final HashMap<String, TableInfo.Column> _columnsUrgencyRules = new HashMap<String, TableInfo.Column>(7);
        _columnsUrgencyRules.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUrgencyRules.put("contactName", new TableInfo.Column("contactName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUrgencyRules.put("appPackage", new TableInfo.Column("appPackage", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUrgencyRules.put("keyword", new TableInfo.Column("keyword", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUrgencyRules.put("threshold", new TableInfo.Column("threshold", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUrgencyRules.put("timeWindowMinutes", new TableInfo.Column("timeWindowMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUrgencyRules.put("isEnabled", new TableInfo.Column("isEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUrgencyRules = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUrgencyRules = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUrgencyRules = new TableInfo("urgency_rules", _columnsUrgencyRules, _foreignKeysUrgencyRules, _indicesUrgencyRules);
        final TableInfo _existingUrgencyRules = TableInfo.read(db, "urgency_rules");
        if (!_infoUrgencyRules.equals(_existingUrgencyRules)) {
          return new RoomOpenHelper.ValidationResult(false, "urgency_rules(com.revolution.ai.data.model.UrgencyRule).\n"
                  + " Expected:\n" + _infoUrgencyRules + "\n"
                  + " Found:\n" + _existingUrgencyRules);
        }
        final HashMap<String, TableInfo.Column> _columnsEmergencyContacts = new HashMap<String, TableInfo.Column>(4);
        _columnsEmergencyContacts.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("phoneNumber", new TableInfo.Column("phoneNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("alwaysAlert", new TableInfo.Column("alwaysAlert", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysEmergencyContacts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesEmergencyContacts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoEmergencyContacts = new TableInfo("emergency_contacts", _columnsEmergencyContacts, _foreignKeysEmergencyContacts, _indicesEmergencyContacts);
        final TableInfo _existingEmergencyContacts = TableInfo.read(db, "emergency_contacts");
        if (!_infoEmergencyContacts.equals(_existingEmergencyContacts)) {
          return new RoomOpenHelper.ValidationResult(false, "emergency_contacts(com.revolution.ai.data.model.EmergencyContact).\n"
                  + " Expected:\n" + _infoEmergencyContacts + "\n"
                  + " Found:\n" + _existingEmergencyContacts);
        }
        final HashMap<String, TableInfo.Column> _columnsAppPermissions = new HashMap<String, TableInfo.Column>(4);
        _columnsAppPermissions.put("packageName", new TableInfo.Column("packageName", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppPermissions.put("appName", new TableInfo.Column("appName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppPermissions.put("isAllowed", new TableInfo.Column("isAllowed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppPermissions.put("iconUri", new TableInfo.Column("iconUri", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAppPermissions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAppPermissions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAppPermissions = new TableInfo("app_permissions", _columnsAppPermissions, _foreignKeysAppPermissions, _indicesAppPermissions);
        final TableInfo _existingAppPermissions = TableInfo.read(db, "app_permissions");
        if (!_infoAppPermissions.equals(_existingAppPermissions)) {
          return new RoomOpenHelper.ValidationResult(false, "app_permissions(com.revolution.ai.data.model.AppPermissionEntry).\n"
                  + " Expected:\n" + _infoAppPermissions + "\n"
                  + " Found:\n" + _existingAppPermissions);
        }
        final HashMap<String, TableInfo.Column> _columnsBlockedKeywords = new HashMap<String, TableInfo.Column>(3);
        _columnsBlockedKeywords.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBlockedKeywords.put("keyword", new TableInfo.Column("keyword", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBlockedKeywords.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBlockedKeywords = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesBlockedKeywords = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoBlockedKeywords = new TableInfo("blocked_keywords", _columnsBlockedKeywords, _foreignKeysBlockedKeywords, _indicesBlockedKeywords);
        final TableInfo _existingBlockedKeywords = TableInfo.read(db, "blocked_keywords");
        if (!_infoBlockedKeywords.equals(_existingBlockedKeywords)) {
          return new RoomOpenHelper.ValidationResult(false, "blocked_keywords(com.revolution.ai.data.model.BlockedKeyword).\n"
                  + " Expected:\n" + _infoBlockedKeywords + "\n"
                  + " Found:\n" + _existingBlockedKeywords);
        }
        final HashMap<String, TableInfo.Column> _columnsScheduledTasks = new HashMap<String, TableInfo.Column>(8);
        _columnsScheduledTasks.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScheduledTasks.put("command", new TableInfo.Column("command", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScheduledTasks.put("scheduledTime", new TableInfo.Column("scheduledTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScheduledTasks.put("timezone", new TableInfo.Column("timezone", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScheduledTasks.put("isRecurring", new TableInfo.Column("isRecurring", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScheduledTasks.put("recurringInterval", new TableInfo.Column("recurringInterval", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScheduledTasks.put("isCompleted", new TableInfo.Column("isCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScheduledTasks.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysScheduledTasks = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesScheduledTasks = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoScheduledTasks = new TableInfo("scheduled_tasks", _columnsScheduledTasks, _foreignKeysScheduledTasks, _indicesScheduledTasks);
        final TableInfo _existingScheduledTasks = TableInfo.read(db, "scheduled_tasks");
        if (!_infoScheduledTasks.equals(_existingScheduledTasks)) {
          return new RoomOpenHelper.ValidationResult(false, "scheduled_tasks(com.revolution.ai.data.model.ScheduledTask).\n"
                  + " Expected:\n" + _infoScheduledTasks + "\n"
                  + " Found:\n" + _existingScheduledTasks);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "fe3140d535fe641d76b86eded60c997c", "2c1143d1b1ff96a972dbb632f92a3a8e");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "action_logs","learned_behaviors","urgency_rules","emergency_contacts","app_permissions","blocked_keywords","scheduled_tasks");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `action_logs`");
      _db.execSQL("DELETE FROM `learned_behaviors`");
      _db.execSQL("DELETE FROM `urgency_rules`");
      _db.execSQL("DELETE FROM `emergency_contacts`");
      _db.execSQL("DELETE FROM `app_permissions`");
      _db.execSQL("DELETE FROM `blocked_keywords`");
      _db.execSQL("DELETE FROM `scheduled_tasks`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ActionLogDao.class, ActionLogDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(LearnedBehaviorDao.class, LearnedBehaviorDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(UrgencyRuleDao.class, UrgencyRuleDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(EmergencyContactDao.class, EmergencyContactDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AppPermissionDao.class, AppPermissionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(BlockedKeywordDao.class, BlockedKeywordDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ScheduledTaskDao.class, ScheduledTaskDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ActionLogDao actionLogDao() {
    if (_actionLogDao != null) {
      return _actionLogDao;
    } else {
      synchronized(this) {
        if(_actionLogDao == null) {
          _actionLogDao = new ActionLogDao_Impl(this);
        }
        return _actionLogDao;
      }
    }
  }

  @Override
  public LearnedBehaviorDao learnedBehaviorDao() {
    if (_learnedBehaviorDao != null) {
      return _learnedBehaviorDao;
    } else {
      synchronized(this) {
        if(_learnedBehaviorDao == null) {
          _learnedBehaviorDao = new LearnedBehaviorDao_Impl(this);
        }
        return _learnedBehaviorDao;
      }
    }
  }

  @Override
  public UrgencyRuleDao urgencyRuleDao() {
    if (_urgencyRuleDao != null) {
      return _urgencyRuleDao;
    } else {
      synchronized(this) {
        if(_urgencyRuleDao == null) {
          _urgencyRuleDao = new UrgencyRuleDao_Impl(this);
        }
        return _urgencyRuleDao;
      }
    }
  }

  @Override
  public EmergencyContactDao emergencyContactDao() {
    if (_emergencyContactDao != null) {
      return _emergencyContactDao;
    } else {
      synchronized(this) {
        if(_emergencyContactDao == null) {
          _emergencyContactDao = new EmergencyContactDao_Impl(this);
        }
        return _emergencyContactDao;
      }
    }
  }

  @Override
  public AppPermissionDao appPermissionDao() {
    if (_appPermissionDao != null) {
      return _appPermissionDao;
    } else {
      synchronized(this) {
        if(_appPermissionDao == null) {
          _appPermissionDao = new AppPermissionDao_Impl(this);
        }
        return _appPermissionDao;
      }
    }
  }

  @Override
  public BlockedKeywordDao blockedKeywordDao() {
    if (_blockedKeywordDao != null) {
      return _blockedKeywordDao;
    } else {
      synchronized(this) {
        if(_blockedKeywordDao == null) {
          _blockedKeywordDao = new BlockedKeywordDao_Impl(this);
        }
        return _blockedKeywordDao;
      }
    }
  }

  @Override
  public ScheduledTaskDao scheduledTaskDao() {
    if (_scheduledTaskDao != null) {
      return _scheduledTaskDao;
    } else {
      synchronized(this) {
        if(_scheduledTaskDao == null) {
          _scheduledTaskDao = new ScheduledTaskDao_Impl(this);
        }
        return _scheduledTaskDao;
      }
    }
  }
}
