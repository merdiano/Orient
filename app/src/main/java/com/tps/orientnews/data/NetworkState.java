package com.tps.orientnews.data;

/**
 * Created by merdan on 8/9/18.
 */

public class NetworkState {
    public Status status;
    public String msg;
    NetworkState(Status status){
        this.status = status;
    }
    NetworkState(Status status,String msg){
        this.status = status;
        this.msg = msg;
    }
    public static NetworkState LOADED = new NetworkState(Status.SUCCESS);
    public static NetworkState LOADING = new NetworkState(Status.RUNNING);

    public static NetworkState error(String msg){
        return new NetworkState(Status.FAILED,msg);
    }
}

