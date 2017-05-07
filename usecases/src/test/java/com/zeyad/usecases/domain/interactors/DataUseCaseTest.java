package com.zeyad.usecases.domain.interactors;

import android.os.HandlerThread;
import android.os.Looper;

import com.zeyad.usecases.Config;
import com.zeyad.usecases.TestRealmModel;
import com.zeyad.usecases.data.db.RealmManager;
import com.zeyad.usecases.data.repository.DataRepository;
import com.zeyad.usecases.data.requests.FileIORequest;
import com.zeyad.usecases.data.requests.GetRequest;
import com.zeyad.usecases.data.requests.PostRequest;
import com.zeyad.usecases.domain.executors.UIThread;
import com.zeyad.usecases.domain.interactors.data.DataUseCase;
import com.zeyad.usecases.domain.interactors.data.IDataUseCase;
import com.zeyad.usecases.domain.repository.Data;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import java.io.File;
import java.util.HashMap;

import rx.Observable;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class DataUseCaseTest {

    private HashMap<String, Object> HASH_MAP = new HashMap<>();
    private UIThread mUIThread = getMockedUiThread();
    private IDataUseCase mDataUseCase;
    private Observable observable = Observable.just(true);
    private Data mData = getMockedDataRepo();
    private GetRequest getRequest = new GetRequest.GetRequestBuilder(Object.class, false)
            .build();

    private Data getMockedDataRepo() {
        final DataRepository dataRepository = Mockito.mock(DataRepository.class);
        Mockito.doReturn(observable)
                .when(dataRepository)
                .getObjectDynamicallyById(Mockito.anyString(), Mockito.anyString()
                        , Mockito.anyInt(), any(), Mockito.anyBoolean(), Mockito.anyBoolean());
        Mockito.doReturn(observable)
                .when(dataRepository)
                .getObjectDynamicallyById(Mockito.anyString(), Mockito.anyString()
                        , Mockito.anyInt(), any(), Mockito.anyBoolean(), Mockito.anyBoolean());
        Mockito.doReturn(observable)
                .when(dataRepository)
                .deleteAllDynamically(Mockito.anyString(), any(), Mockito.anyBoolean());
        Mockito.doReturn(observable)
                .when(dataRepository)
                .putListDynamically(Mockito.anyString(), Mockito.anyString(), any(JSONArray.class),
                        any(), Mockito.anyBoolean(), Mockito.anyBoolean());
        Mockito.doReturn(observable)
                .when(dataRepository)
                .putListDynamically(Mockito.anyString(), Mockito.anyString(), any(JSONArray.class),
                        any(), Mockito.anyBoolean(), Mockito.anyBoolean());
        Mockito.doReturn(observable)
                .when(dataRepository)
                .uploadFileDynamically(Mockito.anyString(), Mockito.any(File.class), Mockito.anyString(),
                        Mockito.any(HashMap.class), Mockito.anyBoolean(), Mockito.anyBoolean(),
                        Mockito.anyBoolean(), Mockito.any());
        Mockito.doReturn(observable)
                .when(dataRepository)
                .putObjectDynamically(Mockito.anyString(), Mockito.anyString(), any(JSONObject.class),
                        any(), Mockito.anyBoolean(), Mockito.anyBoolean());
        Mockito.doReturn(observable)
                .when(dataRepository)
                .deleteListDynamically(Mockito.anyString(), any(JSONArray.class), any(),
                        Mockito.anyBoolean(), Mockito.anyBoolean());
        Mockito.doReturn(observable)
                .when(dataRepository)
                .queryDisk(any(RealmManager.RealmQueryProvider.class));
        Mockito.doReturn(observable)
                .when(dataRepository)
                .postListDynamically(Mockito.anyString(), Mockito.anyString(), any(JSONArray.class),
                        any(), Mockito.anyBoolean(), Mockito.anyBoolean());
        Mockito.doReturn(observable)
                .when(dataRepository)
                .postObjectDynamically(Mockito.anyString(), Mockito.anyString(), any(JSONObject.class),
                        any(), Mockito.anyBoolean(), Mockito.anyBoolean());
        Mockito.doReturn(observable)
                .when(dataRepository)
                .postObjectDynamically(Mockito.anyString(), Mockito.anyString(), any(JSONObject.class),
                        any(), Mockito.anyBoolean(), Mockito.anyBoolean());
        return dataRepository;
    }

    private UIThread getMockedUiThread() {
        return Mockito.mock(UIThread.class);
    }

    @Before
    public void setUp() throws Exception {
        HandlerThread handlerThread = mock(HandlerThread.class);
        when(handlerThread.getLooper()).thenReturn(mock(Looper.class));
        mDataUseCase = getGenericUseImplementation((DataRepository) mData, mUIThread, handlerThread);
        Config.setBaseURL("www.google.com");
    }

    @Test
    public void testGetObject() {
        when(mData.getObjectDynamicallyById(anyString(), anyString(), anyInt(),
                any(Class.class), anyBoolean(), anyBoolean())).thenReturn(observable);

        mDataUseCase.getObject(getRequest);

        verify(mData, times(1)).getObjectDynamicallyById(anyString(), anyString(),
                anyInt(), any(Class.class), anyBoolean(), anyBoolean());
    }

    @Test
    public void testGetList() {
        when(mData.getListDynamically(anyString(), any(Class.class), anyBoolean(),
                anyBoolean())).thenReturn(observable);

        mDataUseCase.getList(getRequest);

        verify(mData, times(1)).getListDynamically(anyString(), any(Class.class),
                anyBoolean(), anyBoolean());
    }

    @Test
    public void testExecuteDynamicPostObject() {
        when(mData.postObjectDynamically(anyString(), anyString(), any(JSONObject.class), any(Class.class),
                anyBoolean(), anyBoolean())).thenReturn(observable);

        mDataUseCase.postObject(new PostRequest("", "",
                new JSONArray(), Object.class, false));

        verify(mData, times(1)).postObjectDynamically(anyString(), anyString(), any(JSONObject.class),
                any(Class.class), anyBoolean(), anyBoolean());
    }

    @Test
    public void testPutObject() {
        when(mData.putObjectDynamically(anyString(), anyString(), any(JSONObject.class), any(Class.class),
                anyBoolean(), anyBoolean())).thenReturn(observable);

        mDataUseCase.putObject(new PostRequest("", "", new JSONArray(), Object.class, false));

        verify(mData, times(1)).putObjectDynamically(anyString(), anyString(), any(JSONObject.class),
                any(Class.class), anyBoolean(), anyBoolean());
    }

    @Test
    public void testPostList() {
        when(mData.postListDynamically(anyString(), anyString(), any(JSONArray.class), any(Class.class),
                anyBoolean(), anyBoolean())).thenReturn(observable);

        mDataUseCase.postList(new PostRequest("", "", HASH_MAP, Object.class, false));

        verify(mData, times(1)).postListDynamically(anyString(), anyString(), any(JSONArray.class),
                any(Class.class), anyBoolean(), anyBoolean());
    }

    @Test
    public void testPutList() {
        when(mData.putListDynamically(anyString(), anyString(), any(JSONArray.class), any(Class.class),
                anyBoolean(), anyBoolean())).thenReturn(observable);

        mDataUseCase.putList(new PostRequest("", "", HASH_MAP, Object.class, false));

        verify(mData, times(1)).putListDynamically(anyString(), anyString(), any(JSONArray.class),
                any(Class.class), anyBoolean(), anyBoolean());
    }

    @Test
    public void testExecuteSearch() {
        when(mData.queryDisk(any(RealmManager.RealmQueryProvider.class))).thenReturn(observable);

        mDataUseCase.queryDisk(realm -> realm.where(TestRealmModel.class));

        verify(mData, times(1)).queryDisk(any(RealmManager.RealmQueryProvider.class));
    }

    @Test
    public void testDeleteCollection() {
        when(mData.deleteListDynamically(anyString(), any(JSONArray.class), any(Class.class),
                anyBoolean(), anyBoolean())).thenReturn(observable);

        mDataUseCase.deleteCollection(new PostRequest("", "", HASH_MAP, Object.class, false));

        verify(mData, times(1)).deleteListDynamically(anyString(), any(JSONArray.class),
                any(Class.class), anyBoolean(), anyBoolean());
    }

    @Test
    public void testDeleteAll_ifDataRepositoryCorrectMethodIsCalled_whenPostRequestIsPassed() {
        when(mData.deleteAllDynamically(anyString(), any(Class.class), anyBoolean())).thenReturn(observable);

        mDataUseCase.deleteAll(new PostRequest("", "", HASH_MAP, Object.class, false));

        verify(mData, times(1)).deleteAllDynamically(anyString(), any(Class.class), anyBoolean());
    }

    @Test
    public void uploadFile() {
        when(mData.uploadFileDynamically(anyString(), any(File.class), anyString(), (HashMap<String, Object>) anyMap(),
                anyBoolean(), anyBoolean(), anyBoolean(), any(Class.class)))
                .thenReturn(observable);
        mDataUseCase.uploadFile(new FileIORequest());
        verify(mData, times(1)).uploadFileDynamically(anyString(), any(File.class), anyString(),
                (HashMap<String, Object>) anyMap(), anyBoolean(), anyBoolean(), anyBoolean(), any(Class.class));
    }

    @Test
    public void downloadFile() {
        when(mData.downloadFileDynamically(anyString(), any(File.class), anyBoolean(),
                anyBoolean(), anyBoolean(), any(Class.class))).thenReturn(observable);
        mDataUseCase.downloadFile(new FileIORequest());
        verify(mData, times(1)).downloadFileDynamically(anyString(), any(File.class), anyBoolean(),
                anyBoolean(), anyBoolean(), any(Class.class));
    }

    public IDataUseCase getGenericUseImplementation(DataRepository datarepo, UIThread uithread,
                                                    HandlerThread handlerThread) {
        DataUseCase.init(datarepo, uithread, handlerThread);
        return DataUseCase.getInstance();
    }
}
