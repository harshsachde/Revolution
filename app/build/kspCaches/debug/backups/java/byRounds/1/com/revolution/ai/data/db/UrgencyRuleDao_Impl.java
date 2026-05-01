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
import com.revolution.ai.data.model.UrgencyRule;
import java.lang.Class;
import java.lang.Exception;
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
public final class UrgencyRuleDao_Impl implements UrgencyRuleDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<UrgencyRule> __insertionAdapterOfUrgencyRule;

  private final EntityDeletionOrUpdateAdapter<UrgencyRule> __deletionAdapterOfUrgencyRule;

  private final EntityDeletionOrUpdateAdapter<UrgencyRule> __updateAdapterOfUrgencyRule;

  public UrgencyRuleDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfUrgencyRule = new EntityInsertionAdapter<UrgencyRule>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `urgency_rules` (`id`,`contactName`,`appPackage`,`keyword`,`threshold`,`timeWindowMinutes`,`isEnabled`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UrgencyRule entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getContactName());
        statement.bindString(3, entity.getAppPackage());
        statement.bindString(4, entity.getKeyword());
        statement.bindLong(5, entity.getThreshold());
        statement.bindLong(6, entity.getTimeWindowMinutes());
        final int _tmp = entity.isEnabled() ? 1 : 0;
        statement.bindLong(7, _tmp);
      }
    };
    this.__deletionAdapterOfUrgencyRule = new EntityDeletionOrUpdateAdapter<UrgencyRule>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `urgency_rules` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UrgencyRule entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfUrgencyRule = new EntityDeletionOrUpdateAdapter<UrgencyRule>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `urgency_rules` SET `id` = ?,`contactName` = ?,`appPackage` = ?,`keyword` = ?,`threshold` = ?,`timeWindowMinutes` = ?,`isEnabled` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UrgencyRule entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getContactName());
        statement.bindString(3, entity.getAppPackage());
        statement.bindString(4, entity.getKeyword());
        statement.bindLong(5, entity.getThreshold());
        statement.bindLong(6, entity.getTimeWindowMinutes());
        final int _tmp = entity.isEnabled() ? 1 : 0;
        statement.bindLong(7, _tmp);
        statement.bindLong(8, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final UrgencyRule rule, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfUrgencyRule.insert(rule);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final UrgencyRule rule, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfUrgencyRule.handle(rule);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final UrgencyRule rule, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfUrgencyRule.handle(rule);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<UrgencyRule>> getAll() {
    final String _sql = "SELECT * FROM urgency_rules";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"urgency_rules"}, new Callable<List<UrgencyRule>>() {
      @Override
      @NonNull
      public List<UrgencyRule> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfContactName = CursorUtil.getColumnIndexOrThrow(_cursor, "contactName");
          final int _cursorIndexOfAppPackage = CursorUtil.getColumnIndexOrThrow(_cursor, "appPackage");
          final int _cursorIndexOfKeyword = CursorUtil.getColumnIndexOrThrow(_cursor, "keyword");
          final int _cursorIndexOfThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "threshold");
          final int _cursorIndexOfTimeWindowMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeWindowMinutes");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final List<UrgencyRule> _result = new ArrayList<UrgencyRule>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UrgencyRule _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpContactName;
            _tmpContactName = _cursor.getString(_cursorIndexOfContactName);
            final String _tmpAppPackage;
            _tmpAppPackage = _cursor.getString(_cursorIndexOfAppPackage);
            final String _tmpKeyword;
            _tmpKeyword = _cursor.getString(_cursorIndexOfKeyword);
            final int _tmpThreshold;
            _tmpThreshold = _cursor.getInt(_cursorIndexOfThreshold);
            final int _tmpTimeWindowMinutes;
            _tmpTimeWindowMinutes = _cursor.getInt(_cursorIndexOfTimeWindowMinutes);
            final boolean _tmpIsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp != 0;
            _item = new UrgencyRule(_tmpId,_tmpContactName,_tmpAppPackage,_tmpKeyword,_tmpThreshold,_tmpTimeWindowMinutes,_tmpIsEnabled);
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
  public Object getActiveRules(final Continuation<? super List<UrgencyRule>> $completion) {
    final String _sql = "SELECT * FROM urgency_rules WHERE isEnabled = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<UrgencyRule>>() {
      @Override
      @NonNull
      public List<UrgencyRule> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfContactName = CursorUtil.getColumnIndexOrThrow(_cursor, "contactName");
          final int _cursorIndexOfAppPackage = CursorUtil.getColumnIndexOrThrow(_cursor, "appPackage");
          final int _cursorIndexOfKeyword = CursorUtil.getColumnIndexOrThrow(_cursor, "keyword");
          final int _cursorIndexOfThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "threshold");
          final int _cursorIndexOfTimeWindowMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeWindowMinutes");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final List<UrgencyRule> _result = new ArrayList<UrgencyRule>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UrgencyRule _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpContactName;
            _tmpContactName = _cursor.getString(_cursorIndexOfContactName);
            final String _tmpAppPackage;
            _tmpAppPackage = _cursor.getString(_cursorIndexOfAppPackage);
            final String _tmpKeyword;
            _tmpKeyword = _cursor.getString(_cursorIndexOfKeyword);
            final int _tmpThreshold;
            _tmpThreshold = _cursor.getInt(_cursorIndexOfThreshold);
            final int _tmpTimeWindowMinutes;
            _tmpTimeWindowMinutes = _cursor.getInt(_cursorIndexOfTimeWindowMinutes);
            final boolean _tmpIsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp != 0;
            _item = new UrgencyRule(_tmpId,_tmpContactName,_tmpAppPackage,_tmpKeyword,_tmpThreshold,_tmpTimeWindowMinutes,_tmpIsEnabled);
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
