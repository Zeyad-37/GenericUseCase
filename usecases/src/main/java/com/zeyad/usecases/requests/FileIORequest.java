package com.zeyad.usecases.requests;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.HashMap;

/**
 * @author zeyad on 7/29/16.
 */
public class FileIORequest implements Parcelable {

    public static final Parcelable.Creator<FileIORequest> CREATOR =
            new Parcelable.Creator<FileIORequest>() {
                @NonNull
                @Override
                public FileIORequest createFromParcel(@NonNull Parcel source) {
                    return new FileIORequest(source);
                }

                @NonNull
                @Override
                public FileIORequest[] newArray(int size) {
                    return new FileIORequest[size];
                }
            };
    private File file;
    private String url;
    private boolean onWifi, whileCharging, queuable;
    private Class dataClass;
    private HashMap<String, Object> parameters;
    private HashMap<String, File> keyFileMap;

    public FileIORequest() {}

    private FileIORequest(@NonNull Builder uploadRequestBuilder) {
        url = uploadRequestBuilder.url;
        onWifi = uploadRequestBuilder.onWifi;
        whileCharging = uploadRequestBuilder.whileCharging;
        queuable = uploadRequestBuilder.queuable;
        file = uploadRequestBuilder.file;
        dataClass = uploadRequestBuilder.dataClass;
        keyFileMap = uploadRequestBuilder.keyFileMap;
        parameters = uploadRequestBuilder.parameters;
    }

    private FileIORequest(@NonNull Parcel in) {
        this.file = (File) in.readSerializable();
        this.url = in.readString();
        this.onWifi = in.readByte() != 0;
        this.whileCharging = in.readByte() != 0;
        this.queuable = in.readByte() != 0;
        this.dataClass = (Class) in.readSerializable();
        this.parameters = (HashMap<String, Object>) in.readSerializable();
        this.keyFileMap = (HashMap<String, File>) in.readSerializable();
    }

    public String getUrl() {
        return url;
    }

    public boolean isOnWifi() {
        return onWifi;
    }

    public boolean isWhileCharging() {
        return whileCharging;
    }

    public boolean isQueuable() {
        return queuable;
    }

    public Class getDataClass() {
        return dataClass;
    }

    public File getFile() {
        return file;
    }

    @NonNull
    public HashMap<String, Object> getParameters() {
        return parameters != null ? parameters : new HashMap<>();
    }

    public HashMap<String, File> getKeyFileMap() {
        return keyFileMap;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeSerializable(this.file);
        dest.writeString(this.url);
        dest.writeByte(this.onWifi ? (byte) 1 : (byte) 0);
        dest.writeByte(this.whileCharging ? (byte) 1 : (byte) 0);
        dest.writeByte(this.queuable ? (byte) 1 : (byte) 0);
        dest.writeSerializable(this.dataClass);
        dest.writeSerializable(this.parameters);
        dest.writeSerializable(this.keyFileMap);
    }

    public static class Builder {

        private final String url;
        private boolean onWifi, whileCharging, queuable;
        private Class dataClass;
        private HashMap<String, Object> parameters;
        private HashMap<String, File> keyFileMap;
        private File file;

        public Builder(String url) {
            this.url = url;
        }

        @NonNull
        public Builder responseType(Class dataClass) {
            this.dataClass = dataClass;
            return this;
        }

        @NonNull
        public Builder keyFileMapToUpload(HashMap<String, File> keyFileMap) {
            this.keyFileMap = keyFileMap;
            return this;
        }

        @NonNull
        public Builder file(File file) {
            this.file = file;
            return this;
        }

        @NonNull
        public Builder payLoad(HashMap<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }

        //        @NonNull
        //        public Builder queuable(boolean onWifi, boolean whileCharging) {
        //            queuable = true;
        //            this.onWifi = onWifi;
        //            this.whileCharging = whileCharging;
        //            return this;
        //        }

        @NonNull
        public FileIORequest build() {
            return new FileIORequest(this);
        }
    }
}

