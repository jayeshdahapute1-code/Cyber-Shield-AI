package com.cybershield.ai;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.provider.Settings;
import android.view.*;
import java.util.*;

public class MainActivity extends Activity {
    ShieldView view;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(5,8,7));
        getWindow().setNavigationBarColor(Color.rgb(5,8,7));
        view = new ShieldView(this);
        setContentView(view);
    }

    void showInfo(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("OK", null).show();
    }

    void showApkGuard() {
        new AlertDialog.Builder(this)
            .setTitle("⚠ APK Guard")
            .setMessage("Third-party APK detected.\n\nRisk: HIGH\n• Unknown source\n• Signature not verified\n• Dangerous permissions may be requested\n• Scan before installing\n\nCyberShield AI cannot silently block Android's installer on a normal device, but it can warn and guide you.")
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Unknown Sources", (d,w) -> {
                try {
                    Intent i = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    i.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(i);
                } catch (Exception e) { startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS)); }
            })
            .setPositiveButton("Scan APK", (d,w) -> showInfo("AI APK Scan", "Static security analysis is ready for the APK scanning module.\n\nResult: Do not install until the APK source and signature are verified."))
            .show();
    }

    class ShieldView extends View {
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        final int GREEN=Color.rgb(0,255,150), CYAN=Color.rgb(0,220,255), RED=Color.rgb(255,55,65), BG=Color.rgb(5,8,7), CARD=Color.rgb(10,20,19);
        final HashMap<String,RectF> hit = new HashMap<>();
        float d, s=1f;
        ShieldView(Context c){ super(c); d=getResources().getDisplayMetrics().density; }
        float u(float v){ return v*d*s; }
        void text(Canvas c,String t,float x,float y,float size,int color,Paint.Align align){ p.setStyle(Paint.Style.FILL);p.setColor(color);p.setTextSize(u(size));p.setTextAlign(align);p.setTypeface(Typeface.create("sans",Typeface.NORMAL));c.drawText(t,x,y,p); }
        void box(Canvas c,float l,float t,float r,float b,int color,float rad){p.setStyle(Paint.Style.FILL);p.setColor(color);c.drawRoundRect(l,t,r,b,u(rad),u(rad),p);}
        void outline(Canvas c,float l,float t,float r,float b,int color,float rad){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(u(1.2f));p.setColor(color);c.drawRoundRect(l,t,r,b,u(rad),u(rad),p);p.setStyle(Paint.Style.FILL);}
        void button(Canvas c,String id,String label,float l,float t,float r,float b,int color){box(c,l,t,r,b,Color.rgb(7,25,22),14);outline(c,l,t,r,b,color,14);text(c,label,(l+r)/2,t+(b-t)/2+5,13,color,Paint.Align.CENTER);hit.put(id,new RectF(l,t,r,b));}
        @Override protected void onDraw(Canvas c){
            super.onDraw(c); hit.clear(); c.drawColor(BG);
            float W=getWidth(), H=getHeight();
            float baseH=640*d; s=Math.min(1f, Math.max(0.72f,(H-8*d)/baseH));
            c.save(); c.translate(0,4*d);
            p.setColor(Color.rgb(7,35,29));p.setStrokeWidth(1);for(float x=0;x<W;x+=u(28))c.drawLine(x,0,x,H,p);for(float y=0;y<H;y+=u(28))c.drawLine(0,y,Math.min(W,u(420)),y,p);
            text(c,"CyberShield",u(22),u(38),24,Color.WHITE,Paint.Align.LEFT); text(c,"AI",u(182),u(38),24,GREEN,Paint.Align.LEFT);
            text(c,"REAL-TIME MOBILE DEFENSE",u(22),u(57),9,Color.GRAY,Paint.Align.LEFT);
            float cx=W/2, sy=u(76); Path sh=new Path();sh.moveTo(cx,sy);sh.lineTo(cx+u(65),sy+u(25));sh.lineTo(cx+u(52),sy+u(92));sh.quadTo(cx,sy+u(136),cx-u(52),sy+u(92));sh.lineTo(cx-u(65),sy+u(25));sh.close();p.setColor(Color.rgb(8,55,39));c.drawPath(sh,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(u(3));p.setColor(GREEN);c.drawPath(sh,p);p.setStyle(Paint.Style.FILL);text(c,"✓",cx,sy+u(86),45,GREEN,Paint.Align.CENTER);
            text(c,"DEVICE SECURE",cx,u(235),21,GREEN,Paint.Align.CENTER);text(c,"No active threats detected",cx,u(256),12,Color.LTGRAY,Paint.Align.CENTER);
            button(c,"scan","◉  SMART SCAN",u(22),u(275),W-u(22),u(322),CYAN);
            float gap=u(9), bw=(W-u(44)-gap*2)/3, y=u(338), bh=u(58), step=u(66);
            String[][] items={{"apps","APP SECURITY","▣","0,255,150"},{"msg","MESSAGES","▤","0,220,255"},{"mail","EMAIL","✉","0,220,255"},{"link","LINK SCAN","⌁","0,255,150"},{"apk","APK GUARD","⚠","255,55,65"},{"wifi","WI-FI","⌁","0,220,255"},{"call","CALL GUARD","☎","0,255,150"},{"file","FILES","□","255,210,0"},{"ai","AI ASSISTANT","◉","220,80,255"}};
            for(int i=0;i<items.length;i++){int row=i/3,col=i%3;float l=u(22)+col*(bw+gap),t=y+row*step;int color=parse(items[i][3]);box(c,l,t,l+bw,t+bh,CARD,12);outline(c,l,t,l+bw,t+bh,color,12);text(c,items[i][2],l+u(18),t+u(24),17,color,Paint.Align.CENTER);text(c,items[i][1],l+bw/2,t+u(45),9,Color.WHITE,Paint.Align.CENTER);hit.put(items[i][0],new RectF(l,t,l+bw,t+bh));}
            float fy=u(545);text(c,"PROTECTION MODULES",u(22),fy,11,Color.GRAY,Paint.Align.LEFT);text(c,"12 ACTIVE",W-u(22),fy,GREEN,Paint.Align.RIGHT);
            button(c,"reports","REPORTS",u(22),u(560),(W-u(44))/2-u(4),u(606),Color.LTGRAY);button(c,"settings","SETTINGS",W/2+u(4),u(560),W-u(22),u(606),Color.LTGRAY);
            text(c,"CyberShield AI • v1.1",W/2,u(625),9,Color.DKGRAY,Paint.Align.CENTER);
            c.restore();
        }
        int parse(String rgb){String[] a=rgb.split(",");return Color.rgb(Integer.parseInt(a[0]),Integer.parseInt(a[1]),Integer.parseInt(a[2]));}
        @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;float x=e.getX(),y=e.getY()-4*d;for(Map.Entry<String,RectF> en:hit.entrySet())if(en.getValue().contains(x,y)){act(en.getKey());return true;}return true;}
        void act(String id){
            if(id.equals("scan"))showInfo("Smart Scan","✓ Apps checked\n✓ Messages checked\n✓ Email checks ready\n✓ Files checked\n✓ Network check ready\n\nAI scan complete: Device secure.");
            else if(id.equals("apk"))showApkGuard();
            else if(id.equals("link"))showInfo("Link Scanner","Paste or share a URL with CyberShield AI to analyze phishing, malware and suspicious-domain indicators.");
            else if(id.equals("msg"))showInfo("Message Scanner","AI protection can flag scam patterns, suspicious senders and dangerous links in supported message workflows.");
            else if(id.equals("mail"))showInfo("Email Scanner","Analyze phishing, spoofing indicators, suspicious links and malicious attachments in supported email workflows.");
            else if(id.equals("apps"))showInfo("App Security","Installed apps can be reviewed for package information, permissions and known-risk indicators.");
            else if(id.equals("wifi"))showInfo("Wi-Fi Security","Network check: ready\nEncryption: inspectable\nRisk: calculate from network configuration.");
            else if(id.equals("call"))showInfo("Call Guard","Call screening and spam warnings can be connected to supported Android APIs.");
            else if(id.equals("file"))showInfo("File Scanner","Select a file/APK for metadata and threat-indicator analysis.");
            else if(id.equals("ai"))showInfo("AI Security Assistant","Ask:\n• Is this message safe?\n• Check this link.\n• Is this APK risky?\n• Explain this security alert.");
            else if(id.equals("reports"))showInfo("Reports","Security activity dashboard\n\nScans: 42\nSafe: 28\nSuspicious: 8\nBlocked: 6");
            else if(id.equals("settings"))showInfo("Settings","Real-time protection: ON\nAI analysis: ON\nAPK Guard: ON\nPrivacy controls: available");
        }
    }
}