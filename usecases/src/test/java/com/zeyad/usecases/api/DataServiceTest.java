package com.zeyad.usecases.api;

import com.zeyad.usecases.TestRealmModel;
import com.zeyad.usecases.db.RealmQueryProvider;
import com.zeyad.usecases.requests.FileIORequest;
import com.zeyad.usecases.requests.GetRequest;
import com.zeyad.usecases.requests.PostRequest;
import com.zeyad.usecases.stores.CloudDataStore;
import com.zeyad.usecases.stores.DataStoreFactory;
import com.zeyad.usecases.stores.DiskDataStore;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author by ZIaDo on 5/9/17.
 */
public class DataServiceTest {

    private DataService dataService;
    private DataStoreFactory dataStoreFactory;
    private GetRequest getRequest;
    private PostRequest postRequest;
    private Flowable observable;

    @Before
    public void setUp() throws Exception {
        observable = Flowable.just(true);
        postRequest = new PostRequest.Builder(Object.class, false).build();
        getRequest = new GetRequest.Builder(Object.class, false).build();
        dataStoreFactory = mock(DataStoreFactory.class);
        when(dataStoreFactory.dynamically(anyString())).thenReturn(mock(CloudDataStore.class));
        when(dataStoreFactory.disk()).thenReturn(mock(DiskDataStore.class));
        when(dataStoreFactory.cloud()).thenReturn(mock(CloudDataStore.class));
        dataService = new DataService(dataStoreFactory, AndroidSchedulers.mainThread(), mock(Scheduler.class));
    }

    @Test
    public void getList() throws Exception {
        when(dataStoreFactory.dynamically(anyString()).dynamicGetList(anyString(), any(Class.class),
                anyBoolean(), anyBoolean())).thenReturn(Flowable.just(Collections.EMPTY_LIST));

        dataService.getList(getRequest);

        verify(dataStoreFactory.dynamically(anyString()), times(1)).dynamicGetList(anyString(),
                any(Class.class), anyBoolean(), anyBoolean());
    }

    @Test
    public void getObject() throws Exception {
        when(dataStoreFactory.dynamically(anyString()).dynamicGetObject(anyString(), anyString(),
                anyInt(), any(Class.class), anyBoolean(), anyBoolean())).thenReturn(observable);

        dataService.getObject(getRequest);

        verify(dataStoreFactory.dynamically(anyString()), times(1)).dynamicGetObject(anyString(),
                anyString(), anyInt(), any(Class.class), anyBoolean(), anyBoolean());
    }

    @Test
    public void getListOffLineFirst() throws Exception {
        when(dataStoreFactory.cloud().dynamicGetList(anyString(), any(Class.class),
                anyBoolean(), anyBoolean())).thenReturn(Flowable.just(Collections.EMPTY_LIST));
        when(dataStoreFactory.disk().dynamicGetList(anyString(), any(Class.class),
                anyBoolean(), anyBoolean())).thenReturn(Flowable.just(Collections.EMPTY_LIST));

        dataService.getListOffLineFirst(getRequest);

        verify(dataStoreFactory.cloud(), times(1)).dynamicGetList(anyString(), any(Class.class),
                anyBoolean(), anyBoolean());
        verify(dataStoreFactory.disk(), times(1)).dynamicGetList(anyString(), any(Class.class),
                anyBoolean(), anyBoolean());
    }

    @Test
    public void getObjectOffLineFirst() throws Exception {
        when(dataStoreFactory.cloud().dynamicGetObject(anyString(), anyString(),
                anyInt(), any(Class.class), anyBoolean(), anyBoolean())).thenReturn(observable);
        when(dataStoreFactory.disk().dynamicGetObject(anyString(), anyString(),
                anyInt(), any(Class.class), anyBoolean(), anyBoolean())).thenReturn(observable);

        dataService.getObjectOffLineFirst(getRequest);

        verify(dataStoreFactory.cloud(), times(1)).dynamicGetObject(anyString(),
                anyString(), anyInt(), any(Class.class), anyBoolean(), anyBoolean());
        verify(dataStoreFactory.disk(), times(1)).dynamicGetObject(anyString(),
                anyString(), anyInt(), any(Class.class), anyBoolean(), anyBoolean());
    }

    @Test
    public void patchObject() throws Exception {
        when(dataStoreFactory.dynamically(anyString()).dynamicPatchObject(anyString(), anyString(),
                any(JSONObject.class), any(Class.class), anyBoolean(), anyBoolean())).thenReturn(observable);

        dataService.patchObject(postRequest);

        verify(dataStoreFactory.dynamically(anyString()), times(1)).dynamicPatchObject(anyString(),
                anyString(), any(JSONObject.class), any(Class.class), anyBoolean(), anyBoolean());
    }

    @Test
    public void postObject() throws Exception {
        when(dataStoreFactory.dynamically(anyString()).dynamicPostObject(anyString(), anyString(),
                any(JSONObject.class), any(Class.class), anyBoolean(), anyBoolean())).thenReturn(observable);

        dataService.postObject(postRequest);

        verify(dataStoreFactory.dynamically(anyString()), times(1)).dynamicPostObject(anyString(),
                anyString(), any(JSONObject.class), any(Class.class), anyBoolean(), anyBoolean());
    }

    @Test
    public void postList() throws Exception {
        when(dataStoreFactory.dynamically(anyString()).dynamicPostList(anyString(), anyString(),
                any(JSONArray.class), any(Class.class), anyBoolean(), anyBoolean())).thenReturn(observable);

        dataService.postList(postRequest);

        verify(dataStoreFactory.dynamically(anyString()), times(1)).dynamicPostList(anyString(),
                anyString(), any(JSONArray.class), any(Class.class), anyBoolean(), anyBoolean());
    }

    @Test
    public void putObject() throws Exception {
        when(dataStoreFactory.dynamically(anyString()).dynamicPutObject(anyString(), anyString(),
                any(JSONObject.class), any(Class.class), anyBoolean(), anyBoolean())).thenReturn(observable);

        dataService.putObject(postRequest);

        verify(dataStoreFactory.dynamically(anyString()), times(1)).dynamicPutObject(anyString(),
                anyString(), any(JSONObject.class), any(Class.class), anyBoolean(), anyBoolean());
    }

    @Test
    public void putList() throws Exception {
        when(dataStoreFactory.dynamically(anyString()).dynamicPutList(anyString(), anyString(),
                any(JSONArray.class), any(Class.class), anyBoolean(), anyBoolean())).thenReturn(observable);

        dataService.putList(postRequest);

        verify(dataStoreFactory.dynamically(anyString()), times(1)).dynamicPutList(anyString(),
                anyString(), any(JSONArray.class), any(Class.class), anyBoolean(), anyBoolean());
    }

    @Test
    public void deleteItemById() throws Exception {
        when(dataStoreFactory.dynamically(anyString()).dynamicDeleteCollection(anyString(), anyString(),
                any(JSONArray.class), any(Class.class), anyBoolean(), anyBoolean())).thenReturn(observable);

        dataService.deleteItemById(postRequest);

        verify(dataStoreFactory.dynamically(anyString()), times(1)).dynamicDeleteCollection(anyString(),
                anyString(), any(JSONArray.class), any(Class.class), anyBoolean(), anyBoolean());
    }

    @Test
    public void deleteCollection() throws Exception {
        when(dataStoreFactory.dynamically(anyString()).dynamicDeleteCollection(anyString(), anyString(),
                any(JSONArray.class), any(Class.class), anyBoolean(), anyBoolean())).thenReturn(observable);

        dataService.deleteCollectionByIds(postRequest);

        verify(dataStoreFactory.dynamically(anyString()), times(1)).dynamicDeleteCollection(anyString(),
                anyString(), any(JSONArray.class), any(Class.class), anyBoolean(), anyBoolean());
    }

    @Test
    public void deleteAll() throws Exception {
        when(dataStoreFactory.disk().dynamicDeleteAll(any(Class.class))).thenReturn(Completable.complete());

        dataService.deleteAll(postRequest);

        verify(dataStoreFactory.disk(), times(1)).dynamicDeleteAll(any(Class.class));
    }

    @Test
    public void queryDisk() throws Exception {
        when(dataStoreFactory.disk().queryDisk(any(RealmQueryProvider.class)))
                .thenReturn(observable);

        dataService.queryDisk(realm -> realm.where(TestRealmModel.class));

        verify(dataStoreFactory.disk(), times(1)).queryDisk(any(RealmQueryProvider.class));
    }

    @Test
    public void uploadFile() throws Exception {
        when(dataStoreFactory.cloud().dynamicUploadFile(anyString(), any(File.class),
                anyString(), (HashMap<String, Object>) anyMap(), anyBoolean(), anyBoolean(),
                anyBoolean(), any(Class.class))).thenReturn(observable);

        dataService.uploadFile(new FileIORequest());

        verify(dataStoreFactory.cloud(), times(1)).dynamicUploadFile(anyString(),
                any(File.class), anyString(), (HashMap<String, Object>) anyMap(), anyBoolean(),
                anyBoolean(), anyBoolean(), any(Class.class));
    }

    @Test
    public void downloadFile() throws Exception {
        when(dataStoreFactory.cloud().dynamicDownloadFile(anyString(), any(File.class),
                anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(observable);

        dataService.downloadFile(new FileIORequest());

        verify(dataStoreFactory.cloud(), times(1)).dynamicDownloadFile(anyString(),
                any(File.class), anyBoolean(), anyBoolean(), anyBoolean());
    }
}