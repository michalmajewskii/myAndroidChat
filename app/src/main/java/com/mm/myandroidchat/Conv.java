package com.mm.myandroidchat;

import android.content.Context;

public class Conv {

    public static boolean seen;
    public long timestamp;

    public Conv(){ }

    public static boolean isSeen(){return seen; }
    public void setSeen(boolean seen){this.seen=seen;}
    public long getTimestamp(){return timestamp;}
    public void setTimestamp(long timestamp){this.timestamp =timestamp;}
    public Conv (boolean seen, long timestamp){
        this.seen=seen;
        this.timestamp=timestamp;

    }


}
