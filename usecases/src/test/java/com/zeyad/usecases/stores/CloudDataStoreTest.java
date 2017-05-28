package com.zeyad.usecases.stores;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.test.rule.BuildConfig;

import com.zeyad.usecases.TestRealmModel;
import com.zeyad.usecases.db.DataBaseManager;
import com.zeyad.usecases.db.RealmManager;
import com.zeyad.usecases.exceptions.NetworkConnectionException;
import com.zeyad.usecases.mapper.DAOMapper;
import com.zeyad.usecases.network.ApiConnection;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subscribers.TestSubscriber;
import io.realm.RealmModel;
import io.realm.RealmObject;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @author by ZIaDo on 2/14/17.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class CloudDataStoreTest {
    private CloudDataStore cloudDataStore;
    private Context mockContext;
    private ApiConnection mockApiConnection;
    private DataBaseManager mockDataBaseManager;
    private Flowable observable;

    @Before
    public void setUp() throws Exception {
        observable = Flowable.just(new Object());
        mockContext = mock(Context.class);
        mockApiConnection = mock(ApiConnection.class);
        mockDataBaseManager = mock(RealmManager.class);
        changeStateOfNetwork(mockContext, true);
        when(mockDataBaseManager.put(any(JSONObject.class), anyString(), any(Class.class)))
                .thenReturn(Completable.complete());
        when(mockDataBaseManager.putAll(any(JSONArray.class), anyString(), any(Class.class)))
                .thenReturn(Completable.complete());
        when(mockDataBaseManager.putAll(anyList(), any(Class.class))).thenReturn(Completable.complete());
        cloudDataStore = new CloudDataStore(mockApiConnection, mockDataBaseManager, DAOMapper.getInstance(),
                mockContext);
        HandlerThread backgroundThread = new HandlerThread("backgroundThread");
        backgroundThread.start();
        com.zeyad.usecases.Config.setBackgroundThread(AndroidSchedulers.from(backgroundThread.getLooper()));
    }

    @Test
    public void dynamicGetObject() throws Exception {
        when(mockApiConnection.dynamicGetObject(anyString(), anyBoolean())).thenReturn(observable);

        cloudDataStore.dynamicGetObject("", "", 0L, "", Object.class, false, false);

        verify(mockApiConnection, times(1)).dynamicGetObject(anyString(), anyBoolean());
        verifyDBInteractions(0, 0, 0, 0, 0, 0);
    }

    @Test
    public void dynamicGetObjectCanWillPersist() throws Exception {
        cloudDataStore.mCanPersist = true;
        when(mockApiConnection.dynamicGetObject(anyString(), anyBoolean())).thenReturn(observable);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicGetObject("", "", 0L, "", Object.class, true, false)
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();

        verify(mockApiConnection, times(1)).dynamicGetObject(anyString(), anyBoolean());
        verifyDBInteractions(0, 0, 1, 0, 0, 0);
    }

    @Test
    public void dynamicGetList() throws Exception {
        List<TestRealmModel> testRealmObjects = new ArrayList<>();
        testRealmObjects.add(new TestRealmModel());
        Flowable<List> observable = Flowable.just(testRealmObjects);
        when(mockApiConnection.dynamicGetList(anyString(), anyBoolean())).thenReturn(observable);

        cloudDataStore.dynamicGetList("", Object.class, false, false);

        verify(mockApiConnection, times(1)).dynamicGetList(anyString(), anyBoolean());
        verifyDBInteractions(0, 0, 0, 0, 0, 0);
    }

    @Test
    public void dynamicGetListCanWillPersist() throws Exception {
        cloudDataStore.mCanPersist = true;
        Flowable<List> observable = Flowable.just(Collections.singletonList(new TestRealmModel()));
        when(mockApiConnection.dynamicGetList(anyString(), anyBoolean())).thenReturn(observable);

        TestSubscriber<List> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicGetList("", Object.class, true, false)
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();

        verify(mockApiConnection, times(1)).dynamicGetList(anyString(), anyBoolean());
//        verifyDBInteractions(0, 1, 0, 0, 0, 0);
    }

    @Test
    public void dynamicPatchObject() throws Exception {
        when(mockApiConnection.dynamicPatch(anyString(), any(RequestBody.class))).thenReturn(observable);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicPatchObject("", "", new JSONObject(), Object.class, Object.class, false,
                false)
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();

        verify(mockApiConnection, times(1)).dynamicPatch(anyString(), any(RequestBody.class));
        verifyDBInteractions(0, 0, 0, 0, 0, 0);
    }

    @Test
    public void dynamicPatchObjectCanWillPersist() throws Exception {
        cloudDataStore.mCanPersist = true;
        when(mockApiConnection.dynamicPatch(anyString(), any(RequestBody.class))).thenReturn(observable);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicPatchObject("", "", new JSONObject(), Object.class, Object.class, true,
                false)
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();

        verify(mockApiConnection, times(1)).dynamicPatch(anyString(), any(RequestBody.class));
        verifyDBInteractions(0, 0, 1, 0, 0, 0);
    }

    @Test
    public void dynamicPatchObjectNoNetwork() throws Exception {
        changeStateOfNetwork(mockContext, false);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicPatchObject("", "", new JSONObject(), Object.class, Object.class, false,
                true)
                .subscribe(testSubscriber);

        testSubscriber.assertNoValues();
        verifyDBInteractions(0, 0, 0, 0, 0, 0);
    }

    @Test
    public void dynamicPatchObjectNoNetworkNoQueue() throws Exception {
        changeStateOfNetwork(mockContext, false);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicPatchObject("", "", new JSONObject(), Object.class, Object.class, false,
                false)
                .subscribe(testSubscriber);

        testSubscriber.assertError(NetworkConnectionException.class);
        verifyDBInteractions(0, 0, 0, 0, 0, 0);
    }

    @Test
    public void dynamicPostObject() throws Exception {
        when(mockApiConnection.dynamicPost(anyString(), any(RequestBody.class))).thenReturn(observable);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicPostObject("", "", new JSONObject(), Object.class, Object.class, false,
                false)
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();

        verify(mockApiConnection, times(1)).dynamicPost(anyString(), any(RequestBody.class));
        verifyDBInteractions(0, 0, 0, 0, 0, 0);
    }

    @Test
    public void dynamicPostObjectCanWillPersist() throws Exception {
        cloudDataStore.mCanPersist = true;
        when(mockApiConnection.dynamicPost(anyString(), any(RequestBody.class))).thenReturn(observable);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicPostObject("", "", new JSONObject(), Object.class, Object.class, true,
                false)
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();

        verify(mockApiConnection, times(1)).dynamicPost(anyString(), any(RequestBody.class));
        verifyDBInteractions(0, 0, 1, 0, 0, 0);
    }

    @Test
    public void dynamicPostObjectNoNetwork() throws Exception {
        changeStateOfNetwork(mockContext, false);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicPostObject("", "", new JSONObject(), Object.class, Object.class, false,
                true)
                .subscribe(testSubscriber);

        testSubscriber.assertNoValues();
        verifyDBInteractions(0, 0, 0, 0, 0, 0);
    }

    @Test
    public void dynamicPostObjectNoNetworkNoQueue() throws Exception {
        changeStateOfNetwork(mockContext, false);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicPostObject("", "", new JSONObject(), Object.class, Object.class, false,
                false)
                .subscribe(testSubscriber);

        testSubscriber.assertError(NetworkConnectionException.class);
        verifyDBInteractions(0, 0, 0, 0, 0, 0);
    }

    @Test
    public void dynamicPostList() throws Exception {
        when(mockApiConnection.dynamicPost(anyString(), any(RequestBody.class))).thenReturn(observable);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicPostList("", "", new JSONArray(), Object.class, Object.class, false,
                false)
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();

        verify(mockApiConnection, times(1)).dynamicPost(anyString(), any(RequestBody.class));
        verifyDBInteractions(0, 0, 0, 0, 0, 0);
    }

    @Test
    public void dynamicPostListCanWillPersist() throws Exception {
        cloudDataStore.mCanPersist = true;
        when(mockApiConnection.dynamicPost(anyString(), any(RequestBody.class))).thenReturn(observable);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicPostList("", "", new JSONArray(), Object.class, Object.class, true,
                false)
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();

        verify(mockApiConnection, times(1)).dynamicPost(anyString(), any(RequestBody.class));
        verifyDBInteractions(1, 0, 0, 0, 0, 0);
    }

    @Test
    public void dynamicPostListNoNetwork() throws Exception {
        changeStateOfNetwork(mockContext, false);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicPostList("", "", new JSONArray(), Object.class, Object.class, false,
                true)
                .subscribe(testSubscriber);

        testSubscriber.assertNoValues();
        verifyDBInteractions(0, 0, 0, 0, 0, 0);
    }

    @Test
    public void dynamicPostListNoNetworkNoQueue() throws Exception {
        changeStateOfNetwork(mockContext, false);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicPostList("", "", new JSONArray(), Object.class, Object.class, false,
                false)
                .subscribe(testSubscriber);

        testSubscriber.assertError(NetworkConnectionException.class);
        verifyDBInteractions(0, 0, 0, 0, 0, 0);
    }

    @Test
    public void dynamicPutObject() throws Exception {
        when(mockApiConnection.dynamicPut(anyString(), any(RequestBody.class))).thenReturn(observable);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicPutObject("", "", new JSONObject(), Object.class, Object.class, false,
                false)
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();

        verify(mockApiConnection, times(1)).dynamicPut(anyString(), any(RequestBody.class));
        verifyDBInteractions(0, 0, 0, 0, 0, 0);
    }

    @Test
    public void dynamicPutObjectCanWillPersist() throws Exception {
        cloudDataStore.mCanPersist = true;
        when(mockApiConnection.dynamicPut(anyString(), any(RequestBody.class))).thenReturn(observable);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicPutObject("", "", new JSONObject(), Object.class, Object.class, true,
                false)
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();

        verify(mockApiConnection, times(1)).dynamicPut(anyString(), any(RequestBody.class));
        verifyDBInteractions(0, 0, 1, 0, 0, 0);
    }

    @Test
    public void dynamicPutObjectNoNetwork() throws Exception {
        changeStateOfNetwork(mockContext, false);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicPutObject("", "", new JSONObject(), Object.class, Object.class, false,
                true)
                .subscribe(testSubscriber);

        testSubscriber.assertNoValues();
        verifyDBInteractions(0, 0, 0, 0, 0, 0);
    }

    @Test
    public void dynamicPutObjectNoNetworkNoQueue() throws Exception {
        changeStateOfNetwork(mockContext, false);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicPutObject("", "", new JSONObject(), Object.class, Object.class, false,
                false)
                .subscribe(testSubscriber);

        testSubscriber.assertError(NetworkConnectionException.class);
        verifyDBInteractions(0, 0, 0, 0, 0, 0);
    }

    @Test
    public void dynamicPutList() throws Exception {
        when(mockApiConnection.dynamicPut(anyString(), any(RequestBody.class))).thenReturn(observable);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicPutList("", "", new JSONArray(), Object.class, Object.class, false, false)
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();

        verify(mockApiConnection, times(1)).dynamicPut(anyString(), any(RequestBody.class));
        verifyDBInteractions(0, 0, 0, 0, 0, 0);
    }

    @Test
    public void dynamicPutListCanWillPersist() throws Exception {
        cloudDataStore.mCanPersist = true;
        when(mockApiConnection.dynamicPut(anyString(), any(RequestBody.class))).thenReturn(observable);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicPutList("", "", new JSONArray(), Object.class, Object.class, true, false)
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();

        verify(mockApiConnection, times(1)).dynamicPut(anyString(), any(RequestBody.class));
        verifyDBInteractions(1, 0, 0, 0, 0, 0);
    }

    @Test
    public void dynamicPutListNoNetwork() throws Exception {
        changeStateOfNetwork(mockContext, false);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicPostList("", "", new JSONArray(), Object.class, Object.class, false, true)
                .subscribe(testSubscriber);

        testSubscriber.assertNoValues();
        verifyDBInteractions(0, 0, 0, 0, 0, 0);
    }

    @Test
    public void dynamicPutListNoNetworkNoQueue() throws Exception {
        changeStateOfNetwork(mockContext, false);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicPutList("", "", new JSONArray(), Object.class, Object.class, false, false)
                .subscribe(testSubscriber);

        testSubscriber.assertError(NetworkConnectionException.class);
        verifyDBInteractions(0, 0, 0, 0, 0, 0);
    }

    @Test
    public void dynamicDeleteCollection() throws Exception {
        when(mockApiConnection.dynamicDelete(anyString(), any(RequestBody.class))).thenReturn(observable);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicDeleteCollection("", "", new JSONArray(), Object.class, Object.class,
                false, false)
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();

        verify(mockApiConnection, times(1)).dynamicDelete(anyString(), any(RequestBody.class));
        verifyDBInteractions(0, 0, 0, 0, 0, 0);
    }

    @Test
    public void dynamicDeleteCollectionCanWillPersist() throws Exception {
        cloudDataStore.mCanPersist = true;
        when(mockApiConnection.dynamicDelete(anyString(), any(RequestBody.class))).thenReturn(observable);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicDeleteCollection("", "", new JSONArray(), Object.class, Object.class,
                true, false)
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();

        verify(mockApiConnection, times(1)).dynamicDelete(anyString(), any(RequestBody.class));
        verifyDBInteractions(0, 0, 0, 0, 0, 0);
    }

    @Test
    public void dynamicDeleteCollectionNoNetwork() throws Exception {
        changeStateOfNetwork(mockContext, false);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicDeleteCollection("", "", new JSONArray(), Object.class, Object.class,
                false, true)
                .subscribe(testSubscriber);

        testSubscriber.assertNoValues();
        verifyDBInteractions(0, 0, 0, 0, 0, 0);
    }

    @Test
    public void dynamicDeleteCollectionNoNetworkNoQueue() throws Exception {
        changeStateOfNetwork(mockContext, false);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicDeleteCollection("", "", new JSONArray(), Object.class, Object.class,
                false, false)
                .subscribe(testSubscriber);

        testSubscriber.assertError(NetworkConnectionException.class);
        verifyDBInteractions(0, 0, 0, 0, 0, 0);
    }

    @Test//(expected = IllegalStateException.class)
    public void dynamicDeleteAll() throws Exception {
        Completable completable = cloudDataStore.dynamicDeleteAll(Object.class);

        // Verify repository interactions
        verifyZeroInteractions(mockApiConnection);
        verifyZeroInteractions(mockDataBaseManager);

        // Assert return type
        assertEquals(IllegalStateException.class, completable.blockingGet().getClass());
    }

    @Test
    public void dynamicUploadFile() throws Exception {
        when(mockApiConnection.dynamicUpload(anyString(), anyMap(), any(MultipartBody.Part.class)))
                .thenReturn(observable);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicUploadFile("", new File(""), "", new HashMap(), false, false, false,
                Object.class)
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();

        verify(mockApiConnection, times(1)).dynamicUpload(anyString(), anyMap(), any(MultipartBody.Part.class));
        verifyDBInteractions(0, 0, 0, 0, 0, 0);
    }

    @Test
    public void dynamicUploadFileNoNetwork() throws Exception {
        changeStateOfNetwork(mockContext, false);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicUploadFile("", new File(""), "", new HashMap(), false, false, true,
                Object.class)
                .subscribe(testSubscriber);

        testSubscriber.assertNoValues();
        verifyDBInteractions(0, 0, 0, 0, 0, 0);
    }

    @Test
    public void dynamicUploadFileNoNetworkNoQueue() throws Exception {
        changeStateOfNetwork(mockContext, false);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicUploadFile("", new File(""), "", new HashMap(), false, false, false,
                Object.class)
                .subscribe(testSubscriber);

        testSubscriber.assertError(NetworkConnectionException.class);
        verifyDBInteractions(0, 0, 0, 0, 0, 0);
    }

    @Test
    public void dynamicDownloadFile() throws Exception {
        when(mockApiConnection.dynamicDownload(anyString())).thenReturn(observable);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicDownloadFile("", new File(""), false, false, false)
                .subscribe(testSubscriber);

//        testSubscriber.assertNoErrors();

        verify(mockApiConnection, times(1)).dynamicDownload(anyString());
        verifyDBInteractions(0, 0, 0, 0, 0, 0);
    }

    @Test
    public void dynamicDownloadFileNoNetwork() throws Exception {
        changeStateOfNetwork(mockContext, false);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicDownloadFile("", new File(""), false, false, true)
                .subscribe(testSubscriber);

        testSubscriber.assertNoValues();
        verifyDBInteractions(0, 0, 0, 0, 0, 0);
    }

    @Test
    public void dynamicDownloadFileNoNetworkNoQueue() throws Exception {
        changeStateOfNetwork(mockContext, false);

        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        cloudDataStore.dynamicDownloadFile("", new File(""), false, false, false)
                .subscribe(testSubscriber);

        testSubscriber.assertError(NetworkConnectionException.class);
        verifyDBInteractions(0, 0, 0, 0, 0, 0);
    }

    @Test(expected = RuntimeException.class)
    public void queryDisk() throws Exception {
        Flowable observable = cloudDataStore.queryDisk(realm -> realm.where(TestRealmModel.class));

        // Verify repository interactions
        verifyZeroInteractions(mockApiConnection);
        verifyZeroInteractions(mockDataBaseManager);

        // Assert return type
        RuntimeException expected = new RuntimeException();
        assertEquals(expected.getClass(), observable.first(expected).blockingGet().getClass());
    }

    private void verifyDBInteractions(int putAllJ, int putAllL, int putJ, int putO, int putM, int evict) {
        verify(mockDataBaseManager, times(putAllJ)).putAll(any(JSONArray.class), anyString(), any(Class.class));
        verify(mockDataBaseManager, times(putAllL)).putAll(anyList(), any(Class.class));
        verify(mockDataBaseManager, times(putJ)).put(any(JSONObject.class), anyString(), any(Class.class));
        verify(mockDataBaseManager, times(putO)).put(any(RealmObject.class), any(Class.class));
        verify(mockDataBaseManager, times(putM)).put(any(RealmModel.class), any(Class.class));
        verify(mockDataBaseManager, atLeast(evict)).evictById(any(Class.class), anyString(), anyLong());
    }

    @NonNull
    private Context changeStateOfNetwork(@NonNull Context mockedContext, boolean toEnable) {
        ConnectivityManager connectivityManager = Mockito.mock(ConnectivityManager.class);
        Mockito.when(mockedContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network network = Mockito.mock(Network.class);
            Network[] networks = new Network[]{network};
            Mockito.when(connectivityManager.getAllNetworks()).thenReturn(networks);
            NetworkInfo networkInfo = Mockito.mock(NetworkInfo.class);
            Mockito.when(connectivityManager.getNetworkInfo(network)).thenReturn(networkInfo);
            Mockito.when(networkInfo.getState()).thenReturn(toEnable ? NetworkInfo.State.CONNECTED :
                    NetworkInfo.State.DISCONNECTED);
        } else {
            NetworkInfo networkInfo = Mockito.mock(NetworkInfo.class);
            Mockito.when(connectivityManager.getAllNetworkInfo()).thenReturn(new NetworkInfo[]{networkInfo});
            Mockito.when(networkInfo.getState()).thenReturn(toEnable ? NetworkInfo.State.CONNECTED :
                    NetworkInfo.State.DISCONNECTED);
        }
        return mockedContext;
    }
}
