package com.zeyad.usecases.data.services;

import android.annotation.TargetApi;
import android.app.job.JobScheduler;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.Job;

import org.mockito.Mockito;

import static com.zeyad.usecases.data.services.GenericNetworkQueueIntentService.DOWNLOAD_FILE;
import static com.zeyad.usecases.data.services.GenericNetworkQueueIntentService.JOB_TYPE;
import static com.zeyad.usecases.data.services.GenericNetworkQueueIntentService.PAYLOAD;
import static com.zeyad.usecases.data.services.GenericNetworkQueueIntentService.POST;
import static com.zeyad.usecases.data.services.GenericNetworkQueueIntentService.UPLOAD_FILE;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class GenericJobServiceJUnitTestRobot {


    private static final String TASK_PARAM_PAYLOAD = "some_payload";
    private static final Context MOCKED_CONTEXT = Mockito.mock(Context.class); // InstrumentationRegistry.getContext()
    private static final FirebaseJobDispatcher JOB_SCHEDULER = Mockito.mock(FirebaseJobDispatcher.class);

    private static Job createJobParam(String jobType) {
        Job jobParameters = Mockito.mock(Job.class);
        final Bundle extraBundle = getJobParamsExtraBundle(jobType);
        Mockito.when(jobParameters.getExtras()).thenReturn(extraBundle);
//        Mockito.when(jobParameters.getTag()).thenReturn(tag);
        return jobParameters;
    }

    @NonNull
    private static Bundle getJobParamsExtraBundle(String jobType) {
        final Bundle bundle = new Bundle();
        bundle.putString(PAYLOAD, TASK_PARAM_PAYLOAD);
        bundle.putString(JOB_TYPE, jobType);
        return bundle;
    }

    @NonNull
    static String getTaskParamPayload() {
        return TASK_PARAM_PAYLOAD;
    }

    static Context getMockedContext() {
//        Mockito.when(MOCKED_CONTEXT.getMainLooper()).thenReturn(Mockito.mock(Looper.class));
        Mockito.when(MOCKED_CONTEXT.getSystemService(Context.JOB_SCHEDULER_SERVICE)).thenReturn(JOB_SCHEDULER);
        return MOCKED_CONTEXT;
    }

    static boolean runForDownloadFile() {
        final GenericJobService service = new GenericJobService();
        service.setContext(getMockedContext());
        service.setApplicationContext(getMockedContext());
        return service.onStartJob(createJobParam(DOWNLOAD_FILE));
    }

    static boolean runForUploadFile() {
        final GenericJobService service = new GenericJobService();
        service.setContext(getMockedContext());
        service.setApplicationContext(getMockedContext());
        return service.onStartJob(createJobParam(UPLOAD_FILE));
    }

    static boolean runForPost() {
        final GenericJobService service = new GenericJobService();
        service.setContext(getMockedContext());
        service.setApplicationContext(getMockedContext());
        return service.onStartJob(createJobParam(POST));
    }

    static void clearAll() {
        Mockito.reset(MOCKED_CONTEXT);
    }

    static Job scheduleJob() {
        final Job mockedJobInfo = Mockito.mock(Job.class);
        final GenericJobService genericJobService = new GenericJobService();
        genericJobService.setContext(GenericJobServiceJUnitTestRobot.getMockedContext());
        genericJobService.setApplicationContext(GenericJobServiceJUnitTestRobot.getMockedContext());
        genericJobService.scheduleJob(mockedJobInfo);
        return mockedJobInfo;
    }

    public static FirebaseJobDispatcher getMockedJobScheduler() {
        return JOB_SCHEDULER;
    }
}