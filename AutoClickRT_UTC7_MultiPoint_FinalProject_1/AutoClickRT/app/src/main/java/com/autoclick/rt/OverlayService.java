package com.autoclick.rt;
import android.app.*;import android.content.*;import android.graphics.*;import android.graphics.drawable.ColorDrawable;import android.os.*;import android.provider.Settings;import android.view.*;import android.widget.*;import java.text.*;import java.util.*;

public class OverlayService extends Service {
 WindowManager wm; View panel; WindowManager.LayoutParams lp; static float[] lastPoint; Handler h=new Handler(Looper.getMainLooper());
 public void onCreate(){super.onCreate();notif();if(Settings.canDrawOverlays(this))show();}
 void notif(){String id="acrt";if(Build.VERSION.SDK_INT>=26)getSystemService(NotificationManager.class).createNotificationChannel(new NotificationChannel(id,"Auto Click RT",NotificationManager.IMPORTANCE_LOW));startForeground(7,new Notification.Builder(this,id).setContentTitle("Auto Click RT").setContentText("Overlay đang hoạt động").setSmallIcon(android.R.drawable.ic_media_play).build());}
 void show(){
  wm=(WindowManager)getSystemService(WINDOW_SERVICE);
  LinearLayout b=new LinearLayout(this);b.setPadding(6,4,6,4);b.setBackgroundColor(Color.rgb(35,35,45));
  Button pick=new Button(this);pick.setText("CHỌN");b.addView(pick);
  Button close=new Button(this);close.setText("X");b.addView(close);
  TextView xy=new TextView(this);xy.setTextColor(Color.WHITE);xy.setTextSize(13);xy.setPadding(6,6,6,6);b.addView(xy);
  lp=new WindowManager.LayoutParams(-2,-2,Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT);
  lp.gravity=Gravity.TOP|Gravity.LEFT;lp.x=20;lp.y=200;wm.addView(b,lp);panel=b;
  pick.setOnClickListener(v->{
    lastPoint=new float[]{lp.x+100,lp.y+40};
    xy.setText("X="+(int)lastPoint[0]+" Y="+(int)lastPoint[1]);
    Toast.makeText(this,"Đã chọn tọa độ. Quay lại app và bấm 'DÙNG TỌA ĐỘ ĐÃ VẼ'.",Toast.LENGTH_LONG).show();
  });
  close.setOnClickListener(v->stopSelf());
  b.setOnTouchListener(new Drag(wm,lp));
 }
 public void onDestroy(){if(panel!=null)wm.removeView(panel);h.removeCallbacksAndMessages(null);super.onDestroy();}public IBinder onBind(Intent i){return null;}
 static class Drag implements View.OnTouchListener{WindowManager w;WindowManager.LayoutParams p;float sx,sy;int ix,iy;Drag(WindowManager w,WindowManager.LayoutParams p){this.w=w;this.p=p;}public boolean onTouch(View v,MotionEvent e){if(e.getAction()==0){sx=e.rawX;sy=e.rawY;ix=p.x;iy=p.y;return true;}if(e.getAction()==2){p.x=ix+(int)(e.rawX-sx);p.y=iy+(int)(e.rawY-sy);w.updateViewLayout(v,p);return true;}return false;}}
}
