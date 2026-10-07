package com.autoclick.rt;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.text.*;
import java.util.*; import org.json.*;

public class MainActivity extends Activity {
    LinearLayout list; TextView clock, offset;
    ArrayList<ClickPoint> points = new ArrayList<>();
    final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);

    public void onCreate(Bundle b){super.onCreate(b); fmt.setTimeZone(TimeZone.getTimeZone("Asia/Bangkok")); build(); tick();}
    TextView tv(String s,int sz){TextView v=new TextView(this);v.setText(s);v.setTextSize(sz);v.setPadding(8,10,8,8);return v;}
    Button bt(String s){Button b=new Button(this);b.setText(s);return b;}

    void build(){
        ScrollView sc=new ScrollView(this);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(20,16,20,20);
        root.addView(tv("AUTO CLICK RT",28)); root.addView(tv("Đa điểm • mili-giây • múi giờ UTC+7",16));
        clock=tv("",18); offset=tv("Giờ chuẩn: đang đồng bộ…",15);root.addView(clock);root.addView(offset);

        Button sync=bt("ĐỒNG BỘ GIỜ QUỐC TẾ");sync.setOnClickListener(v->syncTime());root.addView(sync);
        Button overlay=bt("BẬT HIỂN THỊ TRÊN ỨNG DỤNG KHÁC");
        overlay.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()))));root.addView(overlay);
        Button acc=bt("BẬT TRỢ NĂNG / ACCESSIBILITY");
        acc.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));root.addView(acc);

        root.addView(tv("LỊCH CHẠY (UTC+7)",20));
        EditText schedule=new EditText(this);schedule.setHint("2026-10-07 19:00:00.500");schedule.setSingleLine();root.addView(schedule);

        root.addView(tv("CÁC ĐIỂM CLICK",20));
        list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);root.addView(list);
        addPoint();

        Button add=bt("+ THÊM ĐIỂM");add.setOnClickListener(v->addPoint());root.addView(add);
        Button draw=bt("VẼ / CHỌN TỌA ĐỘ TRÊN MÀN HÌNH");
        draw.setOnClickListener(v->startService(new Intent(this,OverlayService.class)));root.addView(draw);

        Button save=bt("LƯU CẤU HÌNH");
        save.setOnClickListener(v->{
            String when=schedule.getText().toString().trim();
            try{
                JSONArray a=new JSONArray();
                for(ClickPoint p:points){
                    JSONObject o=new JSONObject();
                    o.put("x",p.x); o.put("y",p.y); o.put("count",p.count); o.put("gap",p.gapMs);
                    a.put(o);
                }
                getSharedPreferences("cfg",0).edit().putString("schedule",when).putString("points_json",a.toString()).apply();
                Toast.makeText(this,"Đã lưu "+points.size()+" điểm",Toast.LENGTH_SHORT).show();
            }catch(Exception e){Toast.makeText(this,"Không lưu được cấu hình",Toast.LENGTH_SHORT).show();}
        });root.addView(save);

        Button start=bt("▶ CHẠY THEO LỊCH");
        start.setOnClickListener(v->{
            save.performClick();
            Scheduler.start(this);
            Toast.makeText(this,"Đã lên lịch UTC+7",Toast.LENGTH_SHORT).show();
        });root.addView(start);
        Button stop=bt("■ STOP");stop.setOnClickListener(v->Scheduler.stop());root.addView(stop);

        root.addView(tv("Mỗi điểm có X/Y, số lần bấm và khoảng cách giữa các lần bấm. Thứ tự điểm từ trên xuống dưới.",14));
        sc.addView(root);setContentView(sc);
    }

    void addPoint(){
        final int idx=points.size()+1; ClickPoint p=new ClickPoint(500,1000,1,100);points.add(p);
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);
        TextView title=tv("Điểm "+idx,18);
        LinearLayout row=new LinearLayout(this);
        EditText x=field(""+p.x,"X");EditText y=field(""+p.y,"Y");EditText n=field(""+p.count,"Số lần");EditText gap=field(""+p.gapMs,"Gap ms");
        row.addView(x,new LinearLayout.LayoutParams(0,-2,1));row.addView(y,new LinearLayout.LayoutParams(0,-2,1));
        row.addView(n,new LinearLayout.LayoutParams(0,-2,1));row.addView(gap,new LinearLayout.LayoutParams(0,-2,1));
        card.addView(title);card.addView(row);
        Button remove=bt("XÓA ĐIỂM "+idx);remove.setOnClickListener(v->{points.remove(p);list.removeView(card);});
        Button use=bt("DÙNG TỌA ĐỘ ĐÃ VẼ");use.setOnClickListener(v->{float[] q=OverlayService.lastPoint;if(q!=null){x.setText(""+(int)q[0]);y.setText(""+(int)q[1]);}});
        card.addView(use);card.addView(remove);list.addView(card);
    }
    EditText field(String s,String hint){EditText e=new EditText(this);e.setText(s);e.setHint(hint);e.setInputType(2);return e;}

    void tick(){clock.setText("Giờ UTC+7: "+fmt.format(new Date()));new Handler().postDelayed(this::tick,10);}
    void syncTime(){offset.setText("Đã yêu cầu đồng bộ. UTC+7 = Asia/Bangkok. Android sẽ dùng thời gian hệ thống.");}
}
class ClickPoint {int x,y,count;long gapMs;ClickPoint(int a,int b,int c,long d){x=a;y=b;count=c;gapMs=d;}}
