package com.autoclick.rt;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.*;
import android.view.accessibility.AccessibilityEvent;
import android.content.*;
import java.util.*;
import org.json.*;

public class ClickAccessibilityService extends AccessibilityService {
    public static volatile ClickAccessibilityService instance;
    private final Handler h=new Handler(Looper.getMainLooper());

    @Override public void onServiceConnected(){instance=this;}
    @Override public void onAccessibilityEvent(AccessibilityEvent e){}
    @Override public void onInterrupt(){}

    public boolean tap(float x,float y,long duration){
        Path p=new Path(); p.moveTo(x,y);
        GestureDescription.StrokeDescription s=
            new GestureDescription.StrokeDescription(p,0,Math.max(20,duration));
        return dispatchGesture(new GestureDescription.Builder().addStroke(s).build(),null,h);
    }

    public static void executeSavedSequence(Context c){
        ClickAccessibilityService s=instance;
        if(s==null)return;
        String json=c.getSharedPreferences("cfg",0).getString("points_json","[]");
        try{
            JSONArray a=new JSONArray(json);
            s.executePoint(a,0);
        }catch(Exception ignored){}
    }

    private void executePoint(JSONArray a,int i){
        if(i>=a.length())return;
        try{
            JSONObject p=a.getJSONObject(i);
            final float x=(float)p.getDouble("x");
            final float y=(float)p.getDouble("y");
            final int count=Math.max(1,p.optInt("count",1));
            final long gap=Math.max(1,p.optLong("gap",100));
            doClicks(x,y,count,gap,()->executePoint(a,i+1));
        }catch(Exception ignored){}
    }

    private void doClicks(float x,float y,int left,long gap,Runnable next){
        if(left<=0){next.run();return;}
        tap(x,y,35);
        h.postDelayed(()->doClicks(x,y,left-1,gap,next),gap);
    }
}
