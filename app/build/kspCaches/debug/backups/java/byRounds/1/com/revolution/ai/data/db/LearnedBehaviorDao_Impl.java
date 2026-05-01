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
import com.revolution.ai.data.model.LearnedBehavior;
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
public final class LearnedBehaviorDao_Impl implements LearnedBehaviorDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<LearnedBehavior> __insertionAdapterOfLearnedBehavior;

  private final EntityDeletionOrUpdateAdapter<LearnedBehavior> __deletionAdapterOfLearnedBehavior;

  private final EntityDeletionOrUpdateAdapter<LearnedBehavior> __updateAdapterOfLearnedBehavior;

  public LearnedBehaviorDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfLearnedBehavior = new EntityInsertionAdapter<LearnedBehavior>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `learned_behaviors` (`id`,`pattern`,`response`,`frequency`,`lastUsed`,`tag`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LearnedBehavior entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getPattern());
        statement.bindString(3, entity.getResponse());
        statement.bindLong(4, entity.getFrequency());
        statement.bindLong(5, entity.getLastUsed());
        statement.bindString(6, entity.getTag());
      }
    };
    this.__deletionAdapterOfLearnedBehavior = new EntityDeletionOrUpdateAdapter<LearnedBehavior>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `learned_behaviors` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LearnedBehavior entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfLearnedBehavior = new EntityDeletionOrUpdateAdapter<LearnedBehavior>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `learned_behaviors` SET `id` = ?,`pattern` = ?,`response` = ?,`frequency` = ?,`lastUsed` = ?,`tag` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LearnedBehavior entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getPattern());
        statement.bindString(3, entity.getResponse());
        statement.bindLong(4, entity.getFrequency());
        statement.bindLong(5, entity.getLastUsed());
        statement.bindString(6, entity.getTag());
        statement.bindLong(7, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final LearnedBehavior behavior,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfLearnedBehavior.insert(behavior);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final LearnedBehavior behavior,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfLearnedBehavior.handle(behavior);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final LearnedBehavior behavior,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfLearnedBehavior.handle(behavior);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<LearnedBehavior>> getAll() {
    final String _sql = "SELECT * FROM learned_behaviors ORDER BY frequency DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"learned_behaviors"}, new Callable<List<LearnedBehavior>>() {
      @Override
      @NonNull
      public List<LearnedBehavior> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPattern = CursorUtil.getColumnIndexOrThrow(_cursor, "pattern");
          final int _cursorIndexOfResponse = CursorUtil.getColumnIndexOrThrow(_cursor, "response");
          final int _cursorIndexOfFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "frequency");
          final int _cursorIndexOfLastUsed = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUsed");
          final int _cursorIndexOfTag = CursorUtil.getColumnIndexOrThrow(_cursor, "tag");
          final List<LearnedBehavior> _result = new ArrayList<LearnedBehavior>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LearnedBehavior _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPattern;
            _tmpPattern = _cursor.getString(_cursorIndexOfPattern);
            final String _tmpResponse;
            _tmpResponse = _cursor.getString(_cursorIndexOfResponse);
            final int _tmpFrequency;
            _tmpFrequency = _cursor.getInt(_cursorIndexOfFrequency);
            final long _tmpLastUsed;
            _tmpLastUsed = _cursor.getLong(_cursorIndexOfLastUsed);
            final String _tmpTag;
            _tmpTag = _cursor.getString(_cursorIndexOfTag);
            _item = new LearnedBehavior(_tmpId,_tmpPattern,_tmpResponse,_tmpFrequency,_tmpLastUsed,_tmpTag);
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
  public Object findMatching(final String query,
      final Continuation<? super List<LearnedBehavior>> $completion) {
    final String _sql = "SELECT * FROM learned_behaviors WHERE pattern LIKE '%' || ? || '%' ORDER BY frequency DESC LIMIT 5";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<LearnedBehavior>>() {
      @Override
      @NonNull
      public List<LearnedBehavior> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPattern = CursorUtil.getColumnIndexOrThrow(_cursor, "pattern");
          final int _cursorIndexOfResponse = CursorUtil.getColumnIndexOrThrow(_cursor, "response");
          final int _cursorIndexOfFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "frequency");
          final int _cursorIndexOfLastUsed = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUsed");
          final int _cursorIndexOfTag = CursorUtil.getColumnIndexOrThrow(_cursor, "tag");
          final List<LearnedBehavior> _result = new ArrayList<LearnedBehavior>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LearnedBehavior _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPattern;
            _tmpPattern = _cursor.getString(_cursorIndexOfPattern);
            final String _tmpResponse;
            _tmpResponse = _cursor.getString(_cursorIndexOfResponse);
            final int _tmpFrequency;
            _tmpFrequency = _cursor.getInt(_cursorIndexOfFrequency);
            final long _tmpLastUsed;
            _tmpLastUsed = _cursor.getLong(_cursorIndexOfLastUsed);
            final String _tmpTag;
            _tmpTag = _cursor.getString(_cursorIndexOfTag);
            _item = new LearnedBehavior(_tmpId,_tmpPattern,_tmpResponse,_tmpFrequency,_tmpLastUsed,_tmpTag);
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
