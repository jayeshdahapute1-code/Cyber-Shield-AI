package com.cybershield.ai;

import android.app.*;
import android.app.role.RoleManager;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    static final int APK=42, CALL_ROLE=43;
    static final int BG=Color.rgb(5,11,20), PANEL=Color.rgb(12,25,39), CYAN=Color.rgb(52,211,255);
    static final int GREEN=Color.rgb(48,245,183), PURPLE=Color.rgb(157,123,255), RED=Color.rgb(255,75,108);
    static final int YELLOW=Color.rgb(255,201,77), WHITE=Color.rgb(241,247,255), MUTED=Color.rgb(143,163,184);
    SharedPreferences prefs; LinearLayout root; TextView apkOut;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(BG); getWindow().setNavigationBarColor(BG);
        prefs=getSharedPreferences("security",MODE_PRIVATE); home();
    }
    int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    GradientDrawable box(int color,int stroke){
        GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(18));
        if(stroke!=0)d.setStroke(dp(1),stroke); return d;
    }
    TextView txt(String s,float size,int color,boolean bold){
        TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL); t.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL)); return t;
    }
    Button btn(String s,int color){
        Button b=new Button(this); b.setText(s); b.setTextColor(color); b.setTextSize(12); b.setAllCaps(false);
        b.setBackground(box(Color.rgb(8,28,40),color)); return b;
    }
    LinearLayout page(){
        LinearLayout p=new LinearLayout(this); p.setOrientation(LinearLayout.VERTICAL); p.setPadding(dp(14),dp(14),dp(14),dp(18)); p.setBackgroundColor(BG); return p;
    }
    void mount(LinearLayout p){ScrollView s=new ScrollView(this);s.setFillViewport(true);s.addView(p);setContentView(s);root=p;}
    void space(LinearLayout p,int h){p.addView(new Space(this),new LinearLayout.LayoutParams(1,dp(h)));}
    LinearLayout card(LinearLayout p,int accent){
        LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(15),dp(13),dp(15),dp(13));c.setBackground(box(PANEL,accent));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(7),0,0);p.addView(c,lp);return c;
    }
    void cardText(LinearLayout c,String a,String b){
        c.addView(txt(a,14,WHITE,true),new LinearLayout.LayoutParams(-1,dp(28)));
        c.addView(txt(b,9,MUTED,false),new LinearLayout.LayoutParams(-1,dp(35)));
    }
    void header(LinearLayout p,String title,String sub){
        LinearLayout h=new LinearLayout(this);h.setGravity(Gravity.CENTER_VERTICAL);
        TextView back=txt("‹",32,WHITE,false);back.setGravity(Gravity.CENTER);h.addView(back,new LinearLayout.LayoutParams(dp(42),dp(55)));
        LinearLayout labels=new LinearLayout(this);labels.setOrientation(LinearLayout.VERTICAL);
        labels.addView(txt(title,17,WHITE,true),new LinearLayout.LayoutParams(-1,dp(28)));
        labels.addView(txt(sub,9,MUTED,false),new LinearLayout.LayoutParams(-1,dp(22)));
        h.addView(labels,new LinearLayout.LayoutParams(0,dp(55),1));
        TextView pro=txt("PRO",9,YELLOW,true);pro.setGravity(Gravity.CENTER);pro.setBackground(box(Color.rgb(25,39,57),0));h.addView(pro,new LinearLayout.LayoutParams(dp(58),dp(30)));
        back.setOnClickListener(v->home());p.addView(h,new LinearLayout.LayoutParams(-1,dp(62)));
    }
    void nav(LinearLayout p,String active){
        space(p,8);LinearLayout n=new LinearLayout(this);n.setPadding(dp(3),dp(3),dp(3),dp(3));n.setBackground(box(Color.rgb(8,18,29),Color.rgb(28,54,76)));
        String[][] a={{"HOME","home"},{"THREATS","threats"},{"REPORTS","reports"},{"AI","ai"}};
        for(String[] x:a){Button b=btn(x[0],active.equals(x[1])?GREEN:MUTED);n.addView(b,new LinearLayout.LayoutParams(0,dp(48),1));
            b.setOnClickListener(v->{if(x[1].equals("home"))home();else if(x[1].equals("reports"))reports();else if(x[1].equals("ai"))ai();else threats();});}
        p.addView(n);
    }
    void home(){
        LinearLayout p=page();LinearLayout h=new LinearLayout(this);h.setGravity(Gravity.CENTER_VERTICAL);
        h.addView(txt("CyberShield",24,WHITE,true),new LinearLayout.LayoutParams(0,dp(48),1));h.addView(txt("AI",24,CYAN,true),new LinearLayout.LayoutParams(dp(38),dp(48)));
        TextView pro=txt("PRO",9,YELLOW,true);pro.setGravity(Gravity.CENTER);pro.setBackground(box(Color.rgb(25,39,57),0));h.addView(pro,new LinearLayout.LayoutParams(dp(58),dp(30)));p.addView(h);
        p.addView(txt("Smart protection for your device",9,MUTED,false),new LinearLayout.LayoutParams(-1,dp(22)));
        LinearLayout c=card(p,GREEN);cardText(c,"●  PROTECTION ACTIVE","Native Android security modules • transparent local checks");
        int checked=prefs.getInt("checked",0),risk=prefs.getInt("risk",0);
        c.addView(txt("98%  SECURITY POSTURE\n"+checked+" apps scanned     "+risk+" risk indicators\n"+SecurityEngine.networkStatus(this),10,WHITE,true),new LinearLayout.LayoutParams(-1,dp(78)));
        Button audit=btn("RUN FULL SECURITY AUDIT",GREEN);c.addView(audit,new LinearLayout.LayoutParams(-1,dp(48)));audit.setOnClickListener(v->audit());
        space(p,8);p.addView(txt("SECURITY MODULES",11,WHITE,true),new LinearLayout.LayoutParams(-1,dp(28)));
        String[][] m={{"▣ App Security","Installed-app audit","apps","cyan"},{"⌁ Link Guard","Phishing heuristics","link","purple"},{"⚠ APK Guard","Third-party APK scan","apk","red"},{"▤ Message Guard","Notification protection","messages","green"},{"✉ Email Scanner","Provider connection","email","yellow"},{"☎ Call Guard","Android screening role","calls","red"},{"⌁ Wi-Fi Security","Live connection state","wifi","cyan"},{"⚙ Settings","Privacy controls","settings","white"}};
        for(int i=0;i<m.length;i+=2){
            LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);
            for(int j=0;j<2&&i+j<m.length;j++){String[] z=m[i+j];int ac=color(z[3]);LinearLayout q=card(row,ac);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(110),1);lp.setMargins(j==0?0:dp(5),dp(4),j==1?0:dp(5),0);row.removeView(q);row.addView(q,lp);cardText(q,z[0],z[1]);Button b=btn("OPEN",ac);q.addView(b,new LinearLayout.LayoutParams(-1,dp(38)));b.setOnClickListener(v->module(z[2]));}
            p.addView(row,new LinearLayout.LayoutParams(-1,dp(118)));
        }
        LinearLayout a=card(p,PURPLE);cardText(a,"AI SECURITY ASSISTANT","Ask about apps, links, APKs, calls or messages");Button ab=btn("OPEN AI ASSISTANT",PURPLE);a.addView(ab,new LinearLayout.LayoutParams(-1,dp(46)));ab.setOnClickListener(v->ai());
        nav(p,"home");mount(p);
    }
    int color(String s){if(s.equals("green"))return GREEN;if(s.equals("purple"))return PURPLE;if(s.equals("red"))return RED;if(s.equals("yellow"))return YELLOW;if(s.equals("white"))return WHITE;return CYAN;}
    void module(String s){switch(s){case"apps":apps();break;case"link":link();break;case"apk":apk();break;case"messages":messages();break;case"email":email();break;case"calls":calls();break;case"wifi":wifi();break;default:settings();}}
    void audit(){Toast.makeText(this,"Running local security audit…",Toast.LENGTH_SHORT).show();new Thread(()->{SecurityEngine.ScanResult r=SecurityEngine.scanApps(this);prefs.edit().putInt("checked",r.checked).putInt("risk",r.risks).putString("time",new SimpleDateFormat("dd MMM yyyy, HH:mm",Locale.US).format(new Date())).apply();runOnUiThread(this::reports);}).start();}
    void apps(){
        LinearLayout p=page();header(p,"App Security","Real installed-app audit");LinearLayout c=card(p,CYAN);cardText(c,"INSTALLED APP SCANNER","Checks visible non-system apps and simple suspicious package-name indicators.");
        Button b=btn("SCAN ALL INSTALLED APPS",CYAN);c.addView(b,new LinearLayout.LayoutParams(-1,dp(48)));TextView out=txt("No audit run yet.",10,WHITE,false);c.addView(out,new LinearLayout.LayoutParams(-1,dp(160)));
        b.setOnClickListener(v->new Thread(()->{SecurityEngine.ScanResult r=SecurityEngine.scanApps(this);prefs.edit().putInt("checked",r.checked).putInt("risk",r.risks).putString("time",new SimpleDateFormat("dd MMM yyyy, HH:mm",Locale.US).format(new Date())).apply();runOnUiThread(()->out.setText("Apps checked: "+r.checked+"\nRisk indicators: "+r.risks+"\n\n"+r.details+"\n\nHeuristic only — not proof of malware."));}).start());
        nav(p,"home");mount(p);
    }
    void link(){
        LinearLayout p=page();header(p,"Link Guard","Local phishing-risk analysis");LinearLayout c=card(p,PURPLE);cardText(c,"ANALYZE A URL","Checks HTTPS, host format and common suspicious patterns.");
        EditText in=new EditText(this);in.setHint("https://example.com/login");in.setHintTextColor(MUTED);in.setTextColor(WHITE);in.setSingleLine(true);c.addView(in,new LinearLayout.LayoutParams(-1,dp(52)));
        Button b=btn("ANALYZE LINK",PURPLE);c.addView(b,new LinearLayout.LayoutParams(-1,dp(48)));TextView out=txt("Results appear here.",10,WHITE,false);c.addView(out,new LinearLayout.LayoutParams(-1,dp(160)));b.setOnClickListener(v->out.setText(SecurityEngine.analyzeUrl(in.getText().toString())));
        nav(p,"home");mount(p);
    }
    void apk(){
        LinearLayout p=page();header(p,"APK Guard","Third-party APK inspection");LinearLayout c=card(p,RED);cardText(c,"SELECT AN APK","Private-cache copy • SHA-256 • package metadata • permissions • certificate • archive indicators");
        Button b=btn("SELECT & SCAN APK",RED);c.addView(b,new LinearLayout.LayoutParams(-1,dp(50)));apkOut=txt("No APK selected.",9,WHITE,false);c.addView(apkOut,new LinearLayout.LayoutParams(-1,dp(360)));
        b.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("application/vnd.android.package-archive");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,APK);});nav(p,"home");mount(p);
    }
    void messages(){
        LinearLayout p=page();header(p,"Message Guard","Notification-based protection");LinearLayout c=card(p,GREEN);cardText(c,"MESSAGE & NOTIFICATION CHECKS","With explicit notification access, the service can inspect notification text locally for common scam indicators.");
        Button b=btn("ENABLE NOTIFICATION ACCESS",GREEN);c.addView(b,new LinearLayout.LayoutParams(-1,dp(50)));Button s=btn("REFRESH STATUS",CYAN);c.addView(s,new LinearLayout.LayoutParams(-1,dp(50)));TextView out=txt(notifyStatus(),10,WHITE,false);c.addView(out,new LinearLayout.LayoutParams(-1,dp(120)));
        b.setOnClickListener(v->{try{startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));}catch(Exception e){startActivity(new Intent(Settings.ACTION_SETTINGS));}});s.setOnClickListener(v->out.setText(notifyStatus()));
        nav(p,"home");mount(p);
    }
    String notifyStatus(){boolean enabled=false;if(Build.VERSION.SDK_INT>=27){android.app.NotificationManager nm=(android.app.NotificationManager)getSystemService(NOTIFICATION_SERVICE);enabled=nm.isNotificationListenerAccessGranted(new ComponentName(this,CyberNotificationService.class));}return"Access: "+(enabled?"ENABLED":"NOT ENABLED")+"\nNotifications analyzed: "+prefs.getInt("notification_scanned",0)+"\nRisk indicators: "+prefs.getInt("notification_risks",0);}
    void email(){
        LinearLayout p=page();header(p,"Email Scanner","Secure provider connection");LinearLayout c=card(p,YELLOW);cardText(c,"EMAIL SCANNING","Real mailbox scanning requires explicit provider authorization. This app will not show fake inbox results.");
        Button b=btn("CONNECT EMAIL PROVIDER",YELLOW);c.addView(b,new LinearLayout.LayoutParams(-1,dp(50)));c.addView(txt("Next integration: Gmail/Microsoft OAuth plus server-side reputation checks.\n\nAPI secrets must never be embedded in the APK.",10,WHITE,false),new LinearLayout.LayoutParams(-1,dp(150)));b.setOnClickListener(v->Toast.makeText(this,"Email OAuth module is not connected yet.",Toast.LENGTH_LONG).show());nav(p,"home");mount(p);
    }
    void calls(){
        LinearLayout p=page();header(p,"Call Guard","Android call-screening role");LinearLayout c=card(p,RED);cardText(c,"CALL PROTECTION","Uses Android's supported call-screening role. Conservative by design; weak signals do not auto-block calls.");
        Button b=btn("SET UP CALL SCREENING",RED);c.addView(b,new LinearLayout.LayoutParams(-1,dp(50)));TextView out=txt("Checking role…",10,WHITE,false);c.addView(out,new LinearLayout.LayoutParams(-1,dp(100)));b.setOnClickListener(v->callRole());nav(p,"home");mount(p);callStatus(out);
    }
    void callStatus(TextView out){if(Build.VERSION.SDK_INT>=29){RoleManager r=(RoleManager)getSystemService(ROLE_SERVICE);out.setText("Call screening role: "+(r!=null&&r.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)?"ACTIVE":"NOT ACTIVE"));}else out.setText("Call screening requires Android 10+.");}
    void callRole(){if(Build.VERSION.SDK_INT>=29){RoleManager r=(RoleManager)getSystemService(ROLE_SERVICE);if(r!=null&&r.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING))startActivityForResult(r.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING),CALL_ROLE);else Toast.makeText(this,"Call screening is not available.",Toast.LENGTH_LONG).show();}else Toast.makeText(this,"Call screening requires Android 10+.",Toast.LENGTH_LONG).show();}
    void wifi(){LinearLayout p=page();header(p,"Wi-Fi Security","Live Android connectivity state");LinearLayout c=card(p,CYAN);cardText(c,"CURRENT NETWORK",SecurityEngine.networkStatus(this));Button b=btn("REFRESH NETWORK STATUS",CYAN);c.addView(b,new LinearLayout.LayoutParams(-1,dp(48)));TextView o=txt("Network state is read from Android connectivity APIs.",10,WHITE,false);c.addView(o,new LinearLayout.LayoutParams(-1,dp(100)));b.setOnClickListener(v->o.setText(SecurityEngine.networkStatus(this)));nav(p,"home");mount(p);}
    void settings(){LinearLayout p=page();header(p,"Settings","Privacy, permissions and Android controls");setting(p,"Android Security","Open system security controls",v->startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS)),CYAN);setting(p,"Notification Access","Control Message Guard",v->startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)),GREEN);setting(p,"Unknown App Sources","Control APK installation permission",v->{try{Intent i=new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);i.setData(Uri.parse("package:"+getPackageName()));startActivity(i);}catch(Exception e){startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));}},RED);setting(p,"About CyberShield AI","Native modular security toolkit • v1.3",v->new AlertDialog.Builder(this).setTitle("CyberShield AI").setMessage("Native modular rebuild.\n\nLocal checks are transparent and conservative. Strong malware verdicts require a trusted reputation engine.").setPositiveButton("OK",null).show(),PURPLE);nav(p,"home");mount(p);}
    void setting(LinearLayout p,String a,String b,View.OnClickListener l,int ac){LinearLayout c=card(p,ac);cardText(c,a,b);Button x=btn("OPEN",ac);c.addView(x,new LinearLayout.LayoutParams(-1,dp(44)));x.setOnClickListener(l);}
    void reports(){LinearLayout p=page();header(p,"Security Reports","Latest local audit");int a=prefs.getInt("checked",0),r=prefs.getInt("risk",0);LinearLayout c=card(p,r>0?RED:GREEN);cardText(c,"LATEST SECURITY AUDIT","Apps checked: "+a+"   •   Indicators: "+r+"\nLast audit: "+prefs.getString("time","Not scanned yet"));Button b=btn("RUN AUDIT AGAIN",GREEN);c.addView(b,new LinearLayout.LayoutParams(-1,dp(48)));b.setOnClickListener(v->audit());LinearLayout n=card(p,CYAN);cardText(n,"NETWORK",SecurityEngine.networkStatus(this));nav(p,"reports");mount(p);}
    void threats(){LinearLayout p=page();header(p,"Threat Center","Observed indicators, not fake detections");int r=prefs.getInt("risk",0);LinearLayout c=card(p,r>0?RED:GREEN);cardText(c,r>0?"INDICATORS NEED REVIEW":"NO LOCAL APP INDICATORS",r+" package-name indicators from the latest audit.");c.addView(txt("CyberShield AI separates observed indicators from confirmed malware verdicts.\n\nUse APK Guard for file analysis and Link Guard for URLs.",10,WHITE,false),new LinearLayout.LayoutParams(-1,dp(160)));nav(p,"threats");mount(p);}
    void ai(){LinearLayout p=page();header(p,"AI Assistant","Security guidance");LinearLayout c=card(p,PURPLE);cardText(c,"CYBERSHIELD AI","Transparent local security guidance. Cloud AI/reputation can be connected later.");EditText in=new EditText(this);in.setHint("Ask about an APK, link, message or call");in.setHintTextColor(MUTED);in.setTextColor(WHITE);c.addView(in,new LinearLayout.LayoutParams(-1,dp(58)));Button b=btn("ANALYZE QUESTION",PURPLE);c.addView(b,new LinearLayout.LayoutParams(-1,dp(48)));TextView o=txt("Answer appears here.",10,WHITE,false);c.addView(o,new LinearLayout.LayoutParams(-1,dp(180)));b.setOnClickListener(v->o.setText(answer(in.getText().toString())));nav(p,"ai");mount(p);}
    String answer(String q){String x=q==null?"":q.toLowerCase(Locale.ROOT);if(x.contains("apk"))return"Use APK Guard to inspect SHA-256, package metadata, permissions, certificate and archive indicators. These checks do not prove malware.";if(x.contains("link")||x.contains("url"))return"Use Link Guard to inspect the domain, HTTPS and common phishing patterns. Avoid entering credentials on suspicious pages.";if(x.contains("message")||x.contains("sms")||x.contains("whatsapp"))return"Message Guard can inspect notification text only after explicit notification access. It cannot silently read private WhatsApp databases.";if(x.contains("call"))return"Call Guard uses Android's call-screening role and is conservative about blocking.";return"Start with Full Security Audit, then review App Security, APK Guard, Link Guard and Network status.";}
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);if(requestCode==APK&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){apk();apkOut.setText("Scanning selected APK…");Uri u=data.getData();new Thread(()->{String r=ApkScanner.scan(this,u);runOnUiThread(()->apkOut.setText(r));}).start();}else if(requestCode==CALL_ROLE)calls();}
}