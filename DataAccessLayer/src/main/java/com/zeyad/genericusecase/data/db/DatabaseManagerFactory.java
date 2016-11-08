package com.zeyad.genericusecase.data.db;

import android.support.annotation.Nullable;

import com.zeyad.genericusecase.Config;

import static com.zeyad.genericusecase.Config.REALM;

/**
 * DatabaseManager interface implementer.
 */
public class DatabaseManagerFactory {
    /**
     * @return DataBaseManager the implemented instance of the DatabaseManager.
     */
    @Nullable
    public static DataBaseManager getInstance() {
        if (Config.getInstance().getDBType() == REALM)
            return RealmManager.getInstance();
        return null;
    }

    /**
     * Creates a RealmManager instance
     */
    public static void initRealm() {
        RealmManager.init();
        Config.getInstance().setDBType(REALM);
    }
}
