package com.autoclick.rt;

import android.content.Context;
import android.os.*;
import java.text.*;
import java.util.*;

public final class Scheduler {
    private static volatile boolean running=false;
    private static Thread worker;

    public static synchronized void start(Context c){
        stop();
        final Context app=c.getApplicationContext();
        running=true;
        worker=new Thread(() -> {
            try {
                SharedPreferencesLike cfg = new SharedPreferencesLike(app);
                String schedule=cfg.getString("schedule","");
                Date target=parseUtc7(schedule);
                if(target==null) return;

                // Convert wall-clock target to a monotonic deadline once.
                long wallNow=System.currentTimeMillis();
                long monoNow=SystemClock.elapsedRealtime();
                long deadline=monoNow + (target.getTime()-wallNow);

                while(running){
                    long left=deadline-SystemClock.elapsedRealtime();
                    if(left<=0) break;
                    if(left>1000) Thread.sleep(left-500);
                    else if(left>50) Thread.sleep(left-20);
                    else Thread.yield();
                }
                if(!running) return;

                // Current build stores points in the activity UI. The service performs
                // the actual gestures; this trigger guarantees the schedule gate.
                ClickAccessibilityService.executeSavedSequence(app);
            }catch(Throwable ignored){}
        },"AutoClick-Scheduler");
        worker.start();
    }

    static Date parseUtc7(String s){
        try{
            SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS",Locale.US);
            f.setLenient(false); f.setTimeZone(TimeZone.getTimeZone("Asia/Bangkok"));
            return f.parse(s);
        }catch(Exception e){return null;}
    }
    public static synchronized void stop(){
        running=false;
        if(worker!=null){worker.interrupt();worker=null;}
    }
    static class SharedPreferencesLike {
        android.content.SharedPreferences p;
        SharedPreferencesLike(Context c){p=c.getSharedPreferences("cfg",0);}
        String getString(String k,String d){return p.getString(k,d);}
    }
}
