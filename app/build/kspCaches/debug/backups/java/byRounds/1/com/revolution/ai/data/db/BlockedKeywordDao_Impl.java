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
import com.revolution.ai.data.model.BlockedKeyword;
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
public final class BlockedKeywordDao_Impl implements BlockedKeywordDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<BlockedKeyword> __insertionAdapterOfBlockedKeyword;

  private final EntityDeletionOrUpdateAdapter<BlockedKeyword> __deletionAdapterOfBlockedKeyword;

  private final EntityDeletionOrUpdateAdapter<BlockedKeyword> __updateAdapterOfBlockedKeyword;

  public BlockedKeywordDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfBlockedKeyword = new EntityInsertionAdapter<BlockedKeyword>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `blocked_keywords` (`id`,`keyword`,`isActive`) VALUES (nullif(?, 0),?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BlockedKeyword entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getKeyword());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(3, _tmp);
      }
    };
    this.__deletionAdapterOfBlockedKeyword = new EntityDeletionOrUpdateAdapter<BlockedKeyword>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `blocked_keywords` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BlockedKeyword entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfBlockedKeyword = new EntityDeletionOrUpdateAdapter<BlockedKeyword>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `blocked_keywords` SET `id` = ?,`keyword` = ?,`isActive` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BlockedKeyword entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getKeyword());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(3, _tmp);
        statement.bindLong(4, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final BlockedKeyword keyword, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfBlockedKeyword.insert(keyword);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final BlockedKeyword keyword, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfBlockedKeyword.handle(keyword);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final BlockedKeyword keyword, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfBlockedKeyword.handle(keyword);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<BlockedKeyword>> getAll() {
    final String _sql = "SELECT * FROM blocked_keywords";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"blocked_keywords"}, new Callable<List<BlockedKeyword>>() {
      @Override
      @NonNull
      public List<BlockedKeyword> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfKeyword = CursorUtil.getColumnIndexOrThrow(_cursor, "keyword");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final List<BlockedKeyword> _result = new ArrayList<BlockedKeyword>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BlockedKeyword _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpKeyword;
            _tmpKeyword = _cursor.getString(_cursorIndexOfKeyword);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _item = new BlockedKeyword(_tmpId,_tmpKeyword,_tmpIsActive);
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
  public Object getActiveKeywords(final Continuation<? super List<BlockedKeyword>> $completion) {
    final String _sql = "SELECT * FROM blocked_keywords WHERE isActive = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<BlockedKeyword>>() {
      @Override
      @NonNull
      public List<BlockedKeyword> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfKeyword = CursorUtil.getColumnIndexOrThrow(_cursor, "keyword");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final List<BlockedKeyword> _result = new ArrayList<BlockedKeyword>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BlockedKeyword _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpKeyword;
            _tmpKeyword = _cursor.getString(_cursorIndexOfKeyword);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _item = new BlockedKeyword(_tmpId,_tmpKeyword,_tmpIsActive);
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
