/*
 * Copyright (C) 2017 Grigory Tureev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package doc.org.arpoffeine.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;

import java.sql.SQLException;

import doc.org.arpoffeine.domain.db.CookieBlackList;
import doc.org.arpoffeine.domain.db.DomainBlackList;
import doc.org.arpoffeine.util.Constants;

/**
 * Class that deals with the creation and updating of DB
 */

@SuppressWarnings("WeakerAccess")
@EBean(scope = EBean.Scope.Singleton)
public class DatabaseHelper extends OrmLiteSqliteOpenHelper implements Constants {

    private        final String TAG              = "DatabaseHelper";
    private static final String DATABASE_NAME    = "arPoffeine.db";
    private static final int    DATABASE_VERSION = 1;

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override @Background
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        if (DEBUG) Log.d(TAG, "onCreate");
        try {
            TableUtils.createTable(connectionSource, DomainBlackList.class);
            TableUtils.createTable(connectionSource, CookieBlackList.class);
        } catch (SQLException e) {
            Log.d(TAG, "Can not create database");
            throw new RuntimeException(e);
        }
    }

    @Override @Background
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        if (DEBUG) Log.d(TAG, "onUpgrade");
        try {
            TableUtils.dropTable(connectionSource, DomainBlackList.class, true);
            TableUtils.dropTable(connectionSource, CookieBlackList.class, true);
            onCreate(db, connectionSource);
        } catch (SQLException e) {
            Log.d(TAG, "Can not drop database");
            throw new RuntimeException(e);
        }
    }
}
