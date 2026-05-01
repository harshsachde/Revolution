package com.revolution.ai.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.revolution.ai.data.model.ScheduledTask;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ScheduledTaskDao_Impl implements ScheduledTaskDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ScheduledTask> __insertionAdapterOfScheduledTask;

  private final EntityDeletionOrUpdateAdapter<ScheduledTask> __deletionAdapterOfScheduledTask;

  private final EntityDeletionOrUpdateAdapter<ScheduledTask> __updateAdapterOfScheduledTask;

  public ScheduledTaskDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfScheduledTask = new EntityInsertionAdapter<ScheduledTask>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `scheduled_tasks` (`id`,`command`,`scheduledTime`,`timezone`,`isRecurring`,`recurringInterval`,`isCompleted`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ScheduledTask entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getCommand());
        statement.bindLong(3, entity.getScheduledTime());
        statement.bindString(4, entity.getTimezone());
        final int _tmp = entity.isRecurring() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindLong(6, entity.getRecurringInterval());
        final int _tmp_1 = entity.isCompleted() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
        statement.bindLong(8, entity.getCreatedAt());
      }
    };
    this.__deletionAdapterOfScheduledTask = new EntityDeletionOrUpdateAdapter<ScheduledTask>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `scheduled_tasks` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ScheduledTask entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfScheduledTask = new EntityDeletionOrUpdateAdapter<ScheduledTask>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `scheduled_tasks` SET `id` = ?,`command` = ?,`scheduledTime` = ?,`timezone` = ?,`isRecurring` = ?,`recurringInterval` = ?,`isCompleted` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ScheduledTask entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getCommand());
        statement.bindLong(3, entity.getScheduledTime());
        statement.bindString(4, entity.getTimezone());
        final int _tmp = entity.isRecurring() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindLong(6, entity.getRecurringInterval());
        final int _tmp_1 = entity.isCompleted() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
        statement.bindLong(8, entity.getCreatedAt());
        statement.bindLong(9, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final ScheduledTask task, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfScheduledTask.insertAndReturnId(task);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final ScheduledTask task, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfScheduledTask.handle(task);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final ScheduledTask task, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfScheduledTask.handle(task);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ScheduledTask>> getPendingTasks() {
    final String _sql = "SELECT * FROM scheduled_tasks WHERE isCompleted = 0 ORDER BY scheduledTime ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"scheduled_tasks"}, new Callable<List<ScheduledTask>>() {
      @Override
      @NonNull
      public List<ScheduledTask> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCommand = CursorUtil.getColumnIndexOrThrow(_cursor, "command");
          final int _cursorIndexOfScheduledTime = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledTime");
          final int _cursorIndexOfTimezone = CursorUtil.getColumnIndexOrThrow(_cursor, "timezone");
          final int _cursorIndexOfIsRecurring = CursorUtil.getColumnIndexOrThrow(_cursor, "isRecurring");
          final int _cursorIndexOfRecurringInterval = CursorUtil.getColumnIndexOrThrow(_cursor, "recurringInterval");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<ScheduledTask> _result = new ArrayList<ScheduledTask>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScheduledTask _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCommand;
            _tmpCommand = _cursor.getString(_cursorIndexOfCommand);
            final long _tmpScheduledTime;
            _tmpScheduledTime = _cursor.getLong(_cursorIndexOfScheduledTime);
            final String _tmpTimezone;
            _tmpTimezone = _cursor.getString(_cursorIndexOfTimezone);
            final boolean _tmpIsRecurring;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsRecurring);
            _tmpIsRecurring = _tmp != 0;
            final long _tmpRecurringInterval;
            _tmpRecurringInterval = _cursor.getLong(_cursorIndexOfRecurringInterval);
            final boolean _tmpIsCompleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new ScheduledTask(_tmpId,_tmpCommand,_tmpScheduledTime,_tmpTimezone,_tmpIsRecurring,_tmpRecurringInterval,_tmpIsCompleted,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<ScheduledTask>> getAll() {
    final String _sql = "SELECT * FROM scheduled_tasks ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"scheduled_tasks"}, new Callable<List<ScheduledTask>>() {
      @Override
      @NonNull
      public List<ScheduledTask> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCommand = CursorUtil.getColumnIndexOrThrow(_cursor, "command");
          final int _cursorIndexOfScheduledTime = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledTime");
          final int _cursorIndexOfTimezone = CursorUtil.getColumnIndexOrThrow(_cursor, "timezone");
          final int _cursorIndexOfIsRecurring = CursorUtil.getColumnIndexOrThrow(_cursor, "isRecurring");
          final int _cursorIndexOfRecurringInterval = CursorUtil.getColumnIndexOrThrow(_cursor, "recurringInterval");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<ScheduledTask> _result = new ArrayList<ScheduledTask>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScheduledTask _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCommand;
            _tmpCommand = _cursor.getString(_cursorIndexOfCommand);
            final long _tmpScheduledTime;
            _tmpScheduledTime = _cursor.getLong(_cursorIndexOfScheduledTime);
            final String _tmpTimezone;
            _tmpTimezone = _cursor.getString(_cursorIndexOfTimezone);
            final boolean _tmpIsRecurring;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsRecurring);
            _tmpIsRecurring = _tmp != 0;
            final long _tmpRecurringInterval;
            _tmpRecurringInterval = _cursor.getLong(_cursorIndexOfRecurringInterval);
            final boolean _tmpIsCompleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new ScheduledTask(_tmpId,_tmpCommand,_tmpScheduledTime,_tmpTimezone,_tmpIsRecurring,_tmpRecurringInterval,_tmpIsCompleted,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getDueTasks(final long time,
      final Continuation<? super List<ScheduledTask>> $completion) {
    final String _sql = "SELECT * FROM scheduled_tasks WHERE isCompleted = 0 AND scheduledTime <= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, time);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ScheduledTask>>() {
      @Override
      @NonNull
      public List<ScheduledTask> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCommand = CursorUtil.getColumnIndexOrThrow(_cursor, "command");
          final int _cursorIndexOfScheduledTime = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledTime");
          final int _cursorIndexOfTimezone = CursorUtil.getColumnIndexOrThrow(_cursor, "timezone");
          final int _cursorIndexOfIsRecurring = CursorUtil.getColumnIndexOrThrow(_cursor, "isRecurring");
          final int _cursorIndexOfRecurringInterval = CursorUtil.getColumnIndexOrThrow(_cursor, "recurringInterval");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<ScheduledTask> _result = new ArrayList<ScheduledTask>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScheduledTask _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCommand;
            _tmpCommand = _cursor.getString(_cursorIndexOfCommand);
            final long _tmpScheduledTime;
            _tmpScheduledTime = _cursor.getLong(_cursorIndexOfScheduledTime);
            final String _tmpTimezone;
            _tmpTimezone = _cursor.getString(_cursorIndexOfTimezone);
            final boolean _tmpIsRecurring;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsRecurring);
            _tmpIsRecurring = _tmp != 0;
            final long _tmpRecurringInterval;
            _tmpRecurringInterval = _cursor.getLong(_cursorIndexOfRecurringInterval);
            final boolean _tmpIsCompleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new ScheduledTask(_tmpId,_tmpCommand,_tmpScheduledTime,_tmpTimezone,_tmpIsRecurring,_tmpRecurringInterval,_tmpIsCompleted,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
