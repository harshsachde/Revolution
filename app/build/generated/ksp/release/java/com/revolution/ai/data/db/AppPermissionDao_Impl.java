package com.revolution.ai.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.revolution.ai.data.model.AppPermissionEntry;
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
public final class AppPermissionDao_Impl implements AppPermissionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AppPermissionEntry> __insertionAdapterOfAppPermissionEntry;

  private final EntityDeletionOrUpdateAdapter<AppPermissionEntry> __updateAdapterOfAppPermissionEntry;

  public AppPermissionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAppPermissionEntry = new EntityInsertionAdapter<AppPermissionEntry>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `app_permissions` (`packageName`,`appName`,`isAllowed`,`iconUri`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AppPermissionEntry entity) {
        statement.bindString(1, entity.getPackageName());
        statement.bindString(2, entity.getAppName());
        final int _tmp = entity.isAllowed() ? 1 : 0;
        statement.bindLong(3, _tmp);
        if (entity.getIconUri() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getIconUri());
        }
      }
    };
    this.__updateAdapterOfAppPermissionEntry = new EntityDeletionOrUpdateAdapter<AppPermissionEntry>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `app_permissions` SET `packageName` = ?,`appName` = ?,`isAllowed` = ?,`iconUri` = ? WHERE `packageName` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AppPermissionEntry entity) {
        statement.bindString(1, entity.getPackageName());
        statement.bindString(2, entity.getAppName());
        final int _tmp = entity.isAllowed() ? 1 : 0;
        statement.bindLong(3, _tmp);
        if (entity.getIconUri() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getIconUri());
        }
        statement.bindString(5, entity.getPackageName());
      }
    };
  }

  @Override
  public Object insert(final AppPermissionEntry entry,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAppPermissionEntry.insert(entry);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final AppPermissionEntry entry,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfAppPermissionEntry.handle(entry);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<AppPermissionEntry>> getAll() {
    final String _sql = "SELECT * FROM app_permissions ORDER BY appName ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"app_permissions"}, new Callable<List<AppPermissionEntry>>() {
      @Override
      @NonNull
      public List<AppPermissionEntry> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
          final int _cursorIndexOfIsAllowed = CursorUtil.getColumnIndexOrThrow(_cursor, "isAllowed");
          final int _cursorIndexOfIconUri = CursorUtil.getColumnIndexOrThrow(_cursor, "iconUri");
          final List<AppPermissionEntry> _result = new ArrayList<AppPermissionEntry>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AppPermissionEntry _item;
            final String _tmpPackageName;
            _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            final String _tmpAppName;
            _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
            final boolean _tmpIsAllowed;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAllowed);
            _tmpIsAllowed = _tmp != 0;
            final String _tmpIconUri;
            if (_cursor.isNull(_cursorIndexOfIconUri)) {
              _tmpIconUri = null;
            } else {
              _tmpIconUri = _cursor.getString(_cursorIndexOfIconUri);
            }
            _item = new AppPermissionEntry(_tmpPackageName,_tmpAppName,_tmpIsAllowed,_tmpIconUri);
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
  public Object getAllowedApps(final Continuation<? super List<AppPermissionEntry>> $completion) {
    final String _sql = "SELECT * FROM app_permissions WHERE isAllowed = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AppPermissionEntry>>() {
      @Override
      @NonNull
      public List<AppPermissionEntry> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
          final int _cursorIndexOfIsAllowed = CursorUtil.getColumnIndexOrThrow(_cursor, "isAllowed");
          final int _cursorIndexOfIconUri = CursorUtil.getColumnIndexOrThrow(_cursor, "iconUri");
          final List<AppPermissionEntry> _result = new ArrayList<AppPermissionEntry>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AppPermissionEntry _item;
            final String _tmpPackageName;
            _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            final String _tmpAppName;
            _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
            final boolean _tmpIsAllowed;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAllowed);
            _tmpIsAllowed = _tmp != 0;
            final String _tmpIconUri;
            if (_cursor.isNull(_cursorIndexOfIconUri)) {
              _tmpIconUri = null;
            } else {
              _tmpIconUri = _cursor.getString(_cursorIndexOfIconUri);
            }
            _item = new AppPermissionEntry(_tmpPackageName,_tmpAppName,_tmpIsAllowed,_tmpIconUri);
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

  @Override
  public Object getByPackage(final String pkg,
      final Continuation<? super AppPermissionEntry> $completion) {
    final String _sql = "SELECT * FROM app_permissions WHERE packageName = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, pkg);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AppPermissionEntry>() {
      @Override
      @Nullable
      public AppPermissionEntry call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
          final int _cursorIndexOfIsAllowed = CursorUtil.getColumnIndexOrThrow(_cursor, "isAllowed");
          final int _cursorIndexOfIconUri = CursorUtil.getColumnIndexOrThrow(_cursor, "iconUri");
          final AppPermissionEntry _result;
          if (_cursor.moveToFirst()) {
            final String _tmpPackageName;
            _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            final String _tmpAppName;
            _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
            final boolean _tmpIsAllowed;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAllowed);
            _tmpIsAllowed = _tmp != 0;
            final String _tmpIconUri;
            if (_cursor.isNull(_cursorIndexOfIconUri)) {
              _tmpIconUri = null;
            } else {
              _tmpIconUri = _cursor.getString(_cursorIndexOfIconUri);
            }
            _result = new AppPermissionEntry(_tmpPackageName,_tmpAppName,_tmpIsAllowed,_tmpIconUri);
          } else {
            _result = null;
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
