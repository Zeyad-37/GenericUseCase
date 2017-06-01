package com.zeyad.usecases.db;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.zeyad.usecases.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * {@link DataBaseManager} implementation.
 */
public class RealmManager implements DataBaseManager {

    private static final String REALM_OBJECT_INVALID = "RealmObject is invalid",
            JSON_INVALID = "JSONObject is invalid", NO_ID = "Could not find id!";
    private final Handler backgroundHandler;

    public RealmManager(Looper backgroundLooper) {
        backgroundHandler = new Handler(backgroundLooper);
    }

    /**
     * Gets an {@link Flowable} which will emit an Object.
     *
     * @param dataClass    Class type of the items to get.
     * @param idColumnName Name of the id field.
     * @param itemIdL      The item id to retrieve data.
     * @param itemIdS      The item id to retrieve data.
     */
    @NonNull
    @Override
    public <M> Flowable<M> getById(@NonNull final String idColumnName, final Long itemIdL, final String itemIdS,
                                   Class dataClass) {
        return Flowable.defer(() -> {
            if (itemIdL <= 0 && itemIdS == null) {
                return Flowable.error(new IllegalArgumentException("Id can not be less than Zero."));
            }
            Realm realm = Realm.getDefaultInstance();
            return itemIdS == null ?
                    Utils.getInstance().toFlowable(realm.where(dataClass)
                            .equalTo(idColumnName, itemIdL).findAll().asObservable()
                            .filter(results -> ((RealmResults) results).isLoaded())
                            .map(o -> realm.copyFromRealm((RealmResults) o))
                            .doOnUnsubscribe(() -> closeRealm(realm))) :
                    Utils.getInstance().toFlowable(realm.where(dataClass)
                            .equalTo(idColumnName, itemIdS).findAll().asObservable()
                            .filter(results -> ((RealmResults) results).isLoaded())
                            .map(o -> realm.copyFromRealm((RealmResults) o))
                            .doOnUnsubscribe(() -> closeRealm(realm)));
        });
    }

    /**
     * Gets an {@link Flowable} which will emit a List of Objects.
     *
     * @param clazz Class type of the items to get.
     */
    @NonNull
    @Override
    public <M> Flowable<List<M>> getAll(Class clazz) {
        return Flowable.defer(() -> {
            Realm realm = Realm.getDefaultInstance();
            return Utils.getInstance().toFlowable(realm.where(clazz).findAll().asObservable()
                    .filter(results -> ((RealmResults) results).isLoaded())
                    .map(o -> realm.copyFromRealm((RealmResults) o))
                    .doOnUnsubscribe(() -> closeRealm(realm)));
        });
    }

    /**
     * Takes a query to be executed and return a list of containing the result.
     *
     * @param queryFactory The query used to look for inside the DB.
     * @param <M>          the return type from the query
     * @return {@link List<M>} a result list that matches the given query.
     */
    @NonNull
    @Override
    public <M extends RealmModel> Flowable<List<M>> getQuery(@NonNull RealmQueryProvider<M> queryFactory) {
        return Flowable.defer(() -> {
            Realm realm = Realm.getDefaultInstance();
            return Utils.getInstance().toFlowable(queryFactory.create(realm).findAll().asObservable()
                    .filter(RealmResults::isLoaded)
                    .map(realm::copyFromRealm)
                    .doOnUnsubscribe(() -> closeRealm(realm)));
        });
    }

    /**
     * Puts and element into the DB.
     *
     * @param realmModel Element to insert in the DB.
     * @param dataClass  Class type of the items to be put.
     */
    @NonNull
    @Override
    public <M extends RealmModel> Single<Boolean> put(@Nullable M realmModel, @NonNull Class dataClass) {
        return Single.fromCallable(() -> {
            if (realmModel == null) {
                throw new IllegalArgumentException(REALM_OBJECT_INVALID);
            } else {
                Realm realm = Realm.getDefaultInstance();
                try {
                    RealmModel result =
                            executeWriteOperationInRealm(realm, () -> realm.copyToRealmOrUpdate(realmModel));
                    return RealmObject.isValid(result);
                } finally {
                    closeRealm(realm);
                }
            }
        });
    }


    /**
     * Puts and element into the DB.
     *
     * @param jsonObject Element to insert in the DB.
     * @param dataClass  Class type of the items to be put.
     */
    @NonNull
    @Override
    public Single<Boolean> put(@Nullable JSONObject jsonObject, @Nullable String idColumnName,
                               @NonNull Class dataClass) {
        return Single.fromCallable(() -> {
            if (jsonObject == null) {
                throw new IllegalArgumentException(JSON_INVALID);
            } else {
                updateJsonObjectWithIdValue(jsonObject, idColumnName, dataClass);
                Realm realm = Realm.getDefaultInstance();
                try {
                    RealmModel result = executeWriteOperationInRealm(realm,
                            () -> realm.createOrUpdateObjectFromJson(dataClass, jsonObject));
                    return RealmObject.isValid(result);
                } finally {
                    closeRealm(realm);
                }
            }
        });
    }

    /**
     * Puts and element into the DB.
     *
     * @param jsonArray    Element to insert in the DB.
     * @param idColumnName Name of the id field.
     * @param dataClass    Class type of the items to be put.
     */
    @NonNull
    @Override
    public Single<Boolean> putAll(@NonNull JSONArray jsonArray, String idColumnName, @NonNull Class dataClass) {
        return Single.fromCallable(() -> {
            updateJsonArrayWithIdValue(jsonArray, idColumnName, dataClass);
            Realm realm = Realm.getDefaultInstance();
            try {
                executeWriteOperationInRealm(realm,
                        () -> realm.createOrUpdateAllFromJson(dataClass, jsonArray));
                return true;
            } finally {
                closeRealm(realm);
            }
        });
    }

    @NonNull
    @Override
    public <T extends RealmModel> Single putAll(List<T> realmObjects, Class dataClass) {
        return Single.fromCallable(() -> {
            Realm realm = Realm.getDefaultInstance();
            try {
                executeWriteOperationInRealm(realm, () -> realm.copyToRealmOrUpdate(realmObjects));
                return Single.just(true);
            } finally {
                closeRealm(realm);
            }
        });
    }

    /**
     * Evict all elements of the DB.
     *
     * @param clazz Class type of the items to be deleted.
     */
    @NonNull
    @Override
    public Single<Boolean> evictAll(@NonNull Class clazz) {
        return Single.fromCallable(() -> {
            Realm realm = Realm.getDefaultInstance();
            try {
                executeWriteOperationInRealm(realm, () -> realm.delete(clazz));
                return true;
            } finally {
                closeRealm(realm);
            }
        });
    }

    /**
     * Evict element by id of the DB.
     *
     * @param clazz        Class type of the items to be deleted.
     * @param idFieldName  The id used to look for inside the DB.
     * @param idFieldValue Name of the id field.
     */
    @Override
    public boolean evictById(@NonNull Class clazz, @NonNull String idFieldName, final long idFieldValue) {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmModel toDelete = realm.where(clazz).equalTo(idFieldName, idFieldValue).findFirst();
            if (toDelete == null) {
                return false;
            } else {
                //                executeWriteOperationInRealm(realm, () -> RealmObject.deleteFromRealm(toDelete));
                executeWriteOperationInRealm(realm, new Execute() {
                    @Override
                    public void run() {
                        RealmObject.deleteFromRealm(toDelete);
                    }
                });
                return !RealmObject.isValid(toDelete);
            }
        } finally {
            closeRealm(realm);
        }
    }

    /**
     * Evict a collection elements of the DB.
     *
     * @param idFieldName The id used to look for inside the DB.
     * @param list        List of ids to be deleted.
     * @param dataClass   Class type of the items to be deleted.
     */
    @NonNull
    @Override
    public Flowable<Boolean> evictCollection(@NonNull String idFieldName, @NonNull List<Long> list,
                                             @NonNull Class dataClass) {
//        return Flowable.fromIterable(list)
//                .flatMap(aLong -> Flowable.fromCallable(() -> evictById(dataClass, idFieldName, aLong)))
//                .reduce((aBoolean, aBoolean2) -> aBoolean && aBoolean2)
//                .toFlowable();
        return Flowable.fromCallable(() -> {
            boolean isDeleted = true;
            int size = list.size();
            for (int i = 0; i < size; i++) {
                isDeleted = isDeleted && evictById(dataClass, idFieldName, list.get(i));
            }
            return isDeleted;
        });
    }

    private void closeRealm(@NonNull Realm realm) {
        backgroundHandler.post(() -> {
            if (!realm.isClosed()) {
                realm.close();
                Log.d(RealmManager.class.getSimpleName(), "realm instance closed!");
            }
        });
    }

    private void executeWriteOperationInRealm(@NonNull Realm realm, @NonNull Execute execute) {
        if (realm.isInTransaction()) {
            realm.cancelTransaction();
        }
        realm.beginTransaction();
        execute.run();
        realm.commitTransaction();
    }

    private <T> T executeWriteOperationInRealm(@NonNull Realm realm, @NonNull ExecuteAndReturn<T> executor) {
        T toReturnValue;
        if (realm.isInTransaction()) {
            realm.cancelTransaction();
        }
        realm.beginTransaction();
        toReturnValue = executor.run();
        realm.commitTransaction();
        return toReturnValue;
    }

    @NonNull
    private JSONArray updateJsonArrayWithIdValue(@NonNull JSONArray jsonArray, @Nullable String idColumnName,
                                                 Class dataClass) throws JSONException {
        if (idColumnName == null || idColumnName.isEmpty()) {
            throw new IllegalArgumentException(NO_ID);
        }
        int length = jsonArray.length();
        for (int i = 0; i < length; i++) {
            if (jsonArray.opt(i) instanceof JSONObject) {
                updateJsonObjectWithIdValue(jsonArray.optJSONObject(i), idColumnName, dataClass);
            }
        }
        return jsonArray;
    }

    @NonNull
    private JSONObject updateJsonObjectWithIdValue(@NonNull JSONObject jsonObject, @Nullable String idColumnName,
                                                   Class dataClass) throws JSONException {
        if (idColumnName == null || idColumnName.isEmpty()) {
            throw new IllegalArgumentException(NO_ID);
        }
        if (jsonObject.optInt(idColumnName) == 0) {
            jsonObject.put(idColumnName, getNextId(dataClass, idColumnName));
        }
        return jsonObject;
    }

    private int getNextId(Class clazz, String column) {
        Realm realm = Realm.getDefaultInstance();
        try {
            Number currentMax = realm.where(clazz).max(column);
            return currentMax != null ? currentMax.intValue() + 1 : 1;
        } finally {
            realm.close();
        }
    }

    private interface Execute {
        void run();
    }

    private interface ExecuteAndReturn<T> {
        @NonNull
        T run();
    }
}
