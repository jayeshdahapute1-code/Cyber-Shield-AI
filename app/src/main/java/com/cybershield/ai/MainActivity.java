package com.cybershield.ai;

import android.app.*;
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
    static final int APK=42, CALL_ROLE=43, FILE=44;
    static final int BG=Color.rgb(248,251,255), CARD=Color.WHITE, NAVY=Color.rgb(15,32,66);
    static final int BLUE=Color.rgb(28,105,245), CYAN=Color.rgb(22,174,232), GREEN=Color.rgb(28,195,102);
    static final int PURPLE=Color.rgb(117,73,224), PINK=Color.rgb(235,45,103), ORANGE=Color.rgb(245,166,22);
    static final int RED=Color.rgb(232,50,74), TEXT=Color.rgb(15,32,66), MUTED=Color.rgb(91,111,139);
    SharedPreferences prefs; TextView apkOut, fileOut;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(BG); getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        prefs=getSharedPreferences("security",MODE_PRIVATE); home();
    }
    int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    GradientDrawable bg(int color,float radius,int stroke){
        GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp((int)radius));
        if(stroke!=0)d.setStroke(dp(1),stroke); return d;
    }
    GradientDrawable gradient(int c1,int c2,float radius){
        GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{c1,c2});
        d.setCornerRadius(dp((int)radius)); return d;
    }
    TextView txt(String s,float size,int color,boolean bold){
        TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL); t.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));
        return t;
    }
    Button btn(String s,int color){
        Button b=new Button(this); b.setText(s); b.setTextColor(color==Color.WHITE?TEXT:color); b.setTextSize(12);
        b.setAllCaps(false); b.setTypeface(Typeface.DEFAULT,Typeface.BOLD); b.setPadding(dp(8),0,dp(8),0);
        b.setBackground(bg(Color.WHITE,28,0)); return b;
    }
    Button primary(String s){
        Button b=new Button(this); b.setText(s); b.setTextColor(Color.WHITE); b.setTextSize(13); b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT,Typeface.BOLD); b.setBackground(gradient(BLUE,CYAN,28)); return b;
    }
    LinearLayout page(){
        LinearLayout p=new LinearLayout(this); p.setOrientation(LinearLayout.VERTICAL);
        p.setPadding(dp(10),dp(30),dp(10),dp(8)); p.setBackgroundColor(BG); return p;
    }
    void mount(LinearLayout p){ScrollView s=new ScrollView(this);s.setFillViewport(true);s.setBackgroundColor(BG);s.addView(p);setContentView(s);}
    void space(LinearLayout p,int h){p.addView(new Space(this),new LinearLayout.LayoutParams(1,dp(h)));}
    LinearLayout panel(LinearLayout parent,int color){
        LinearLayout c=new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(12),dp(10),dp(12),dp(10)); c.setBackground(bg(color,22,0));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(4),0,dp(4));parent.addView(c,lp);return c;
    }
    void title(LinearLayout c,String a,String b){
        c.addView(txt(a,15,TEXT,true),new LinearLayout.LayoutParams(-1,dp(27)));
        c.addView(txt(b,10,MUTED,false),new LinearLayout.LayoutParams(-1,dp(29)));
    }
    void header(LinearLayout p){
        LinearLayout h=new LinearLayout(this);h.setGravity(Gravity.CENTER_VERTICAL);
        ImageView shield=new ImageView(this);shield.setImageResource(R.drawable.cybershield_app_icon);shield.setScaleType(ImageView.ScaleType.CENTER_INSIDE);h.addView(shield,new LinearLayout.LayoutParams(dp(34),dp(34)));
        LinearLayout names=new LinearLayout(this);names.setOrientation(LinearLayout.VERTICAL);
        LinearLayout brand=new LinearLayout(this);
        brand.addView(txt("CyberShield",20,NAVY,true),new LinearLayout.LayoutParams(-2,dp(29)));
        brand.addView(txt(" AI",20,BLUE,true),new LinearLayout.LayoutParams(-2,dp(29)));
        names.addView(brand,new LinearLayout.LayoutParams(-1,dp(29)));
        names.addView(txt("Smart Protection for a Safer Digital Life",8,MUTED,false),new LinearLayout.LayoutParams(-1,dp(19)));
        h.addView(names,new LinearLayout.LayoutParams(0,dp(42),1));
        TextView bell=txt("●",12,RED,true);bell.setGravity(Gravity.CENTER);h.addView(bell,new LinearLayout.LayoutParams(dp(30),dp(42)));
        TextView set=txt("⚙",23,NAVY,false);set.setGravity(Gravity.CENTER);set.setOnClickListener(v->settings());h.addView(set,new LinearLayout.LayoutParams(dp(38),dp(42)));
        p.addView(h,new LinearLayout.LayoutParams(-1,dp(42)));
    }
    void nav(LinearLayout p,String active){
        space(p,8); LinearLayout n=new LinearLayout(this); n.setPadding(dp(5),dp(5),dp(5),dp(5)); n.setGravity(Gravity.CENTER);
        n.setBackground(bg(Color.WHITE,24,Color.rgb(218,227,240)));
        String[][] a={{"⌂","Home","home"},{"◆","Threats","threats"},{"▥","Reports","reports"},{"●","AI Assistant","ai"}};
        for(String[] x:a){
            Button b=btn(x[0]+"\n"+x[1],active.equals(x[2])?BLUE:MUTED);
            b.setTextSize(13); b.setGravity(Gravity.CENTER); b.setBackground(active.equals(x[2])?bg(Color.rgb(239,246,255),20,0):bg(Color.TRANSPARENT,20,0));
            n.addView(b,new LinearLayout.LayoutParams(0,dp(50),1));
            b.setOnClickListener(v->{if(x[2].equals("home"))home();else if(x[2].equals("reports"))reports();else if(x[2].equals("ai"))ai();else threats();});
        } p.addView(n,new LinearLayout.LayoutParams(-1,dp(58)));
    }
    void home(){
        LinearLayout p=page(); header(p);
        LinearLayout hero=panel(p,Color.rgb(232,248,255)); hero.setPadding(dp(10),dp(8),dp(10),dp(8));
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);
        TextView ring=txt("✓\n98%\nSecure",20,NAVY,true);ring.setGravity(Gravity.CENTER);ring.setBackground(bg(Color.WHITE,90,BLUE));
        top.addView(ring,new LinearLayout.LayoutParams(dp(88),dp(88)));
        LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);info.setGravity(Gravity.CENTER_VERTICAL);info.setPadding(dp(12),0,dp(4),0);
        TextView protectedTitle=txt("✓  Your Device is Protected",16,NAVY,true);protectedTitle.setGravity(Gravity.CENTER_VERTICAL);info.addView(protectedTitle,new LinearLayout.LayoutParams(-1,dp(28)));
        int checked=prefs.getInt("checked",0),risk=prefs.getInt("risk",0);
        TextView scanLine=txt(checked+" apps scanned  •  "+risk+" risk indicators",10,MUTED,false);scanLine.setGravity(Gravity.CENTER_VERTICAL);info.addView(scanLine,new LinearLayout.LayoutParams(-1,dp(24)));
        TextView networkLine=txt(SecurityEngine.networkStatus(this),9,MUTED,false);networkLine.setGravity(Gravity.CENTER_VERTICAL);info.addView(networkLine,new LinearLayout.LayoutParams(-1,dp(25)));
        Button audit=primary("▶  Run Full Security Audit");info.addView(audit,new LinearLayout.LayoutParams(-1,dp(38)));audit.setOnClickListener(v->audit());
        top.addView(info,new LinearLayout.LayoutParams(0,dp(102),1));hero.addView(top,new LinearLayout.LayoutParams(-1,dp(102)));
        space(p,8);LinearLayout sh=new LinearLayout(this);sh.setGravity(Gravity.CENTER_VERTICAL);
        sh.addView(txt("Security Modules",21,NAVY,true),new LinearLayout.LayoutParams(0,dp(40),1));
        TextView all=txt("All Modules  ›",13,BLUE,true);sh.addView(all,new LinearLayout.LayoutParams(dp(100),dp(40)));p.addView(sh);
        String[][] m={{"▦","App Security","Scan installed apps","apps","cyan"},{"↗","Link Guard","Detect harmful & phishing links","link","green"},{"▣","APK Guard","Scan third-party APK files","apk","pink"},{"▤","Message Guard","Detect scam messages & links","messages","orange"},{"✉","Email Scanner","Check suspicious emails & attachments","email","purple"},{"☎","Call Guard","Block spam & fraud calls","calls","red"},{"⌁","Wi-Fi Security","Check network safety & connection","wifi","cyan"},{"▤","File Scanner","Hash & inspect selected files","files","orange"},{"⚙","Device Security","Check core Android security state","device","purple"},{"⚙","Settings","Privacy, permissions & app controls","settings","gray"}};
        for(int i=0;i<m.length;i+=2){
            LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);
            for(int j=0;j<2;j++){
                String[] z=m[i+j];int ac=color(z[4]);LinearLayout c=panel(row,Color.WHITE);c.setPadding(dp(10),dp(8),dp(10),dp(8));
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(128),1);lp.setMargins(j==0?0:dp(5),dp(3),j==1?0:dp(5),dp(3));row.removeView(c);row.addView(c,lp);
                TextView ic=txt(z[0],27,ac,true);ic.setGravity(Gravity.CENTER);ic.setBackground(bg(Color.rgb(241,247,255),18,0));c.addView(ic,new LinearLayout.LayoutParams(dp(48),dp(48)));
                c.addView(txt(z[1],14,NAVY,true),new LinearLayout.LayoutParams(-1,dp(23)));
                c.addView(txt(z[2],9,MUTED,false),new LinearLayout.LayoutParams(-1,dp(24)));
                LinearLayout bottom=new LinearLayout(this);bottom.setGravity(Gravity.CENTER_VERTICAL);
                bottom.addView(txt("✓  "+(z[3].equals("apps")?"Last scan: Just now":z[3].equals("wifi")?"Network safe":"Protection ready"),9,GREEN,true),new LinearLayout.LayoutParams(0,dp(26),1));
                Button open=btn("Open",ac);open.setTextSize(10);open.setBackground(bg(Color.rgb(239,246,255),22,0));bottom.addView(open,new LinearLayout.LayoutParams(dp(58),dp(27)));c.addView(bottom);
                c.setOnClickListener(v->module(z[3]));open.setOnClickListener(v->module(z[3]));
            } p.addView(row,new LinearLayout.LayoutParams(-1,dp(136)));
        }
        LinearLayout ai=panel(p,Color.rgb(239,233,255));LinearLayout ar=new LinearLayout(this);ar.setGravity(Gravity.CENTER_VERTICAL);
        TextView bot=txt("✦",36,PURPLE,true);bot.setGravity(Gravity.CENTER);ar.addView(bot,new LinearLayout.LayoutParams(dp(52),dp(105)));
        LinearLayout at=new LinearLayout(this);at.setOrientation(LinearLayout.VERTICAL);at.addView(txt("AI Security Assistant",17,NAVY,true),new LinearLayout.LayoutParams(-1,dp(24)));at.addView(txt("Ask about apps, links, APKs, calls or messages",10,MUTED,false),new LinearLayout.LayoutParams(-1,dp(24)));
        Button ask=btn("Ask anything about your security...   ›",PURPLE);ask.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);ask.setBackground(bg(Color.WHITE,25,Color.rgb(196,177,255)));at.addView(ask,new LinearLayout.LayoutParams(-1,dp(38)));ar.addView(at,new LinearLayout.LayoutParams(0,dp(105),1));ai.addView(ar);ask.setOnClickListener(v->ai());
        LinearLayout status=panel(p,Color.WHITE);status.setOrientation(LinearLayout.HORIZONTAL);
        String[] ss={"✓\nReal-time\nProtection","☎\nCall Guard","▤\nMessage Guard","⌁\nNetwork Safe"};
        for(String s:ss){TextView t=txt(s,12,s.startsWith("✓")?GREEN:NAVY,true);t.setGravity(Gravity.CENTER);t.setLineSpacing(1.05f,1.0f);status.addView(t,new LinearLayout.LayoutParams(0,dp(50),1));}
        nav(p,"home");mount(p);
    }
    int color(String s){if(s.equals("green"))return GREEN;if(s.equals("purple"))return PURPLE;if(s.equals("pink"))return PINK;if(s.equals("red"))return RED;if(s.equals("orange"))return ORANGE;if(s.equals("gray"))return Color.rgb(92,110,133);return CYAN;}
    void module(String s){switch(s){case"apps":apps();break;case"link":link();break;case"apk":apk();break;case"messages":messages();break;case"email":email();break;case"calls":calls();break;case"wifi":wifi();break;case"files":files();break;case"device":deviceSecurity();break;default:settings();}}
    void audit(){Toast.makeText(this,"Running local security audit…",Toast.LENGTH_SHORT).show();new Thread(()->{SecurityEngine.ScanResult r=SecurityEngine.scanApps(this);prefs.edit().putInt("checked",r.checked).putInt("risk",r.risks).putString("time",new SimpleDateFormat("dd MMM yyyy, HH:mm",Locale.US).format(new Date())).apply();runOnUiThread(this::reports);}).start();}
    void simplePage(String title,String sub,String heading,String body,int ac,String button,View.OnClickListener action){
        LinearLayout p=page();headerBack(p,title,sub);LinearLayout c=panel(p,Color.WHITE);title(c,heading,body);Button b=primary(button);c.addView(b,new LinearLayout.LayoutParams(-1,dp(50)));b.setOnClickListener(action);nav(p,"home");mount(p);
    }
    void headerBack(LinearLayout p,String title,String sub){
        LinearLayout h=new LinearLayout(this);h.setGravity(Gravity.CENTER_VERTICAL);TextView back=txt("‹",34,NAVY,false);back.setGravity(Gravity.CENTER);h.addView(back,new LinearLayout.LayoutParams(dp(42),dp(55)));
        LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.addView(txt(title,19,NAVY,true),new LinearLayout.LayoutParams(-1,dp(23)));l.addView(txt(sub,10,MUTED,false),new LinearLayout.LayoutParams(-1,dp(22)));h.addView(l,new LinearLayout.LayoutParams(0,dp(55),1));h.setPadding(0,dp(2),0,0);back.setOnClickListener(v->home());p.addView(h,new LinearLayout.LayoutParams(-1,dp(52)));
    }
    void apps(){LinearLayout p=page();headerBack(p,"App Security","Installed-app audit");LinearLayout c=panel(p,Color.WHITE);title(c,"Installed App Scanner","Real package metadata, permissions and installed-app inventory.");Button b=primary("Scan All Installed Apps");c.addView(b,new LinearLayout.LayoutParams(-1,dp(50)));TextView out=txt("No audit run yet.",10,TEXT,false);c.addView(out,new LinearLayout.LayoutParams(-1,dp(300)));b.setOnClickListener(v->new Thread(()->{SecurityEngine.ScanResult r=SecurityEngine.scanApps(this);prefs.edit().putInt("checked",r.checked).putInt("risk",r.risks).putString("time",new SimpleDateFormat("dd MMM yyyy, HH:mm",Locale.US).format(new Date())).apply();runOnUiThread(()->out.setText("Apps checked: "+r.checked+"\nRisk indicators: "+r.risks+"\n\n"+r.details+"\n\nLocal indicators are not a malware verdict.\n\n"+SecurityEngine.appReport(this)));}).start());nav(p,"home");mount(p);}
    void link(){LinearLayout p=page();headerBack(p,"Link Guard","Local phishing-risk analysis");LinearLayout c=panel(p,Color.WHITE);title(c,"Analyze a URL","Checks HTTPS, host format and common suspicious patterns.");EditText in=new EditText(this);in.setHint("https://example.com/login");in.setTextColor(TEXT);in.setHintTextColor(MUTED);in.setSingleLine(true);in.setBackground(bg(Color.rgb(246,249,253),18,Color.rgb(215,225,238)));c.addView(in,new LinearLayout.LayoutParams(-1,dp(52)));Button b=primary("Analyze Link");c.addView(b,new LinearLayout.LayoutParams(-1,dp(50)));TextView out=txt("Results appear here.",10,TEXT,false);c.addView(out,new LinearLayout.LayoutParams(-1,dp(170)));b.setOnClickListener(v->out.setText(SecurityEngine.analyzeUrl(in.getText().toString())));nav(p,"home");mount(p);}
    void apk(){LinearLayout p=page();headerBack(p,"APK Guard","Third-party APK inspection");LinearLayout c=panel(p,Color.WHITE);title(c,"Select an APK","SHA-256 • package metadata • permissions • certificate • archive indicators");Button b=primary("Select & Scan APK");c.addView(b,new LinearLayout.LayoutParams(-1,dp(50)));apkOut=txt("No APK selected.",9,TEXT,false);c.addView(apkOut,new LinearLayout.LayoutParams(-1,dp(380)));b.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("application/vnd.android.package-archive");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,APK);});nav(p,"home");mount(p);}
    void messages(){LinearLayout p=page();headerBack(p,"Message Guard","Notification-based protection");LinearLayout c=panel(p,Color.WHITE);title(c,"Message & Notification Checks","With explicit notification access, the service can inspect notification text locally for common scam indicators.");Button b=primary("Enable Notification Access");c.addView(b,new LinearLayout.LayoutParams(-1,dp(50)));Button s=btn("Refresh Status",CYAN);c.addView(s,new LinearLayout.LayoutParams(-1,dp(50)));TextView out=txt(notifyStatus(),10,TEXT,false);c.addView(out,new LinearLayout.LayoutParams(-1,dp(140)));b.setOnClickListener(v->CyberFeatures.openNotificationSettings(this));s.setOnClickListener(v->out.setText(notifyStatus()));nav(p,"home");mount(p);}
    String notifyStatus(){return CyberFeatures.notificationStatus(this);}
    void files(){LinearLayout p=page();headerBack(p,"File Scanner","Local file inspection");LinearLayout c=panel(p,Color.WHITE);title(c,"Scan a File","Calculates SHA-256 and checks filename indicators without uploading the file.");Button b=primary("Select & Scan File");c.addView(b,new LinearLayout.LayoutParams(-1,dp(50)));fileOut=txt("No file selected.",10,TEXT,false);c.addView(fileOut,new LinearLayout.LayoutParams(-1,dp(260)));b.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,FILE);});nav(p,"home");mount(p);}
    void deviceSecurity(){LinearLayout p=page();headerBack(p,"Device Security","Core Android security state");LinearLayout c=panel(p,Color.WHITE);title(c,"Security Check","Checks device lock state, network validation and app-install controls.");boolean secure=false;try{android.app.KeyguardManager k=(android.app.KeyguardManager)getSystemService(KEYGUARD_SERVICE);secure=k!=null&&k.isDeviceSecure();}catch(Exception ignored){}boolean vpn=SecurityEngine.networkStatus(this).contains("VPN");boolean unknown=false;try{unknown=getPackageManager().canRequestPackageInstalls();}catch(Exception ignored){}String msg="Screen lock: "+(secure?"SECURE":"NOT SECURED")+"\nNetwork: "+SecurityEngine.networkStatus(this)+"\nUnknown-source install permission: "+(unknown?"ALLOWED":"NOT ALLOWED")+"\nVPN transport: "+(vpn?"DETECTED":"NOT DETECTED")+"\n\nThese checks describe device state; they are not a guarantee that the device is malware-free.";c.addView(txt(msg,11,TEXT,false),new LinearLayout.LayoutParams(-1,dp(230)));Button b=btn("Open Android Security Settings",BLUE);c.addView(b,new LinearLayout.LayoutParams(-1,dp(38)));b.setOnClickListener(v->settings());nav(p,"home");mount(p);}
    void email(){simplePage("Email Scanner","Secure provider connection","Email Scanning","Real mailbox scanning requires explicit provider authorization. This app will not show fake inbox results.",ORANGE,"Connect Email Provider",v->Toast.makeText(this,"Email OAuth module is not connected yet.",Toast.LENGTH_LONG).show());}
    void calls(){LinearLayout p=page();headerBack(p,"Call Guard","Real Android call-screening protection");LinearLayout c=panel(p,Color.WHITE);title(c,"Call Protection","Select CyberShield AI as the call-screening app. Numbers added below are rejected by Android before they ring.");Button b=primary("Set Up Call Screening");c.addView(b,new LinearLayout.LayoutParams(-1,dp(50)));TextView out=txt("Checking role…",10,TEXT,false);c.addView(out,new LinearLayout.LayoutParams(-1,dp(55)));b.setOnClickListener(v->callRole());EditText num=new EditText(this);num.setHint("+919876543210");num.setTextColor(TEXT);num.setHintTextColor(MUTED);num.setSingleLine(true);c.addView(num,new LinearLayout.LayoutParams(-1,dp(52)));Button add=btn("Add Number to Block List",RED);c.addView(add,new LinearLayout.LayoutParams(-1,dp(38)));TextView list=txt("Blocked numbers:\n"+prefs.getString("blocked_numbers","None"),10,TEXT,false);c.addView(list,new LinearLayout.LayoutParams(-1,dp(150)));add.setOnClickListener(v->{String n=SecurityEngine.normalizeNumber(num.getText().toString());if(n.length()<5){Toast.makeText(this,"Enter a valid phone number.",Toast.LENGTH_SHORT).show();return;}String old=prefs.getString("blocked_numbers","");String value=old.equals("None")||old.isEmpty()?n:old+","+n;prefs.edit().putString("blocked_numbers",value).apply();list.setText("Blocked numbers:\n"+value.replace(",","\n"));num.setText("");});c.addView(txt("Calls blocked: "+prefs.getInt("blocked_calls",0),10,TEXT,false),new LinearLayout.LayoutParams(-1,dp(45)));callStatus(out);nav(p,"home");mount(p);}
    void callStatus(TextView out){CyberFeatures.callStatus(this,out);} void callRole(){CyberFeatures.callRole(this,CALL_ROLE);}
    void wifi(){simplePage("Wi-Fi Security","Live Android connectivity state","Current Network",SecurityEngine.networkStatus(this),CYAN,"Refresh Network Status",v->Toast.makeText(this,SecurityEngine.networkStatus(this),Toast.LENGTH_SHORT).show());}
    void settings(){LinearLayout p=page();headerBack(p,"Settings","Privacy, permissions and Android controls");setting(p,"Android Security","Open system security controls",v->startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS)),CYAN);setting(p,"Notification Access","Control Message Guard",v->CyberFeatures.openNotificationSettings(this),GREEN);setting(p,"Unknown App Sources","Control APK installation permission",v->{try{Intent i=new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);i.setData(Uri.parse("package:"+getPackageName()));startActivity(i);}catch(Exception e){startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));}},RED);setting(p,"About CyberShield AI","Original working security modules • v1.5 • "+CyberFeatures.variantName(),v->new AlertDialog.Builder(this).setTitle("CyberShield AI").setMessage("Working Android security modules: app audit, APK inspection, link analysis, notification scanning, call screening and network status.").setPositiveButton("OK",null).show(),PURPLE);nav(p,"home");mount(p);}
    void setting(LinearLayout p,String a,String b,View.OnClickListener l,int ac){LinearLayout c=panel(p,Color.WHITE);title(c,a,b);Button x=btn("Open",ac);c.addView(x,new LinearLayout.LayoutParams(-1,dp(44)));x.setOnClickListener(l);}
    void reports(){LinearLayout p=page();headerBack(p,"Security Reports","Latest local audit");int a=prefs.getInt("checked",0),r=prefs.getInt("risk",0);LinearLayout c=panel(p,r>0?Color.rgb(255,242,244):Color.WHITE);title(c,"Latest Security Audit","Apps checked: "+a+"  •  Indicators: "+r+"\nLast audit: "+prefs.getString("time","Not scanned yet"));Button b=primary("Run Audit Again");c.addView(b,new LinearLayout.LayoutParams(-1,dp(38)));b.setOnClickListener(v->audit());LinearLayout n=panel(p,Color.WHITE);title(n,"Network",SecurityEngine.networkStatus(this));nav(p,"reports");mount(p);}
    void threats(){LinearLayout p=page();headerBack(p,"Threat Center","Observed indicators, not fake detections");int r=prefs.getInt("risk",0);LinearLayout c=panel(p,r>0?Color.rgb(255,242,244):Color.WHITE);title(c,r>0?"Indicators Need Review":"No Local App Indicators",r+" package-name indicators from the latest audit.");c.addView(txt("CyberShield AI separates observed indicators from confirmed malware verdicts.\n\nUse APK Guard for file analysis and Link Guard for URLs.",10,TEXT,false),new LinearLayout.LayoutParams(-1,dp(170)));nav(p,"threats");mount(p);}
    void ai(){LinearLayout p=page();headerBack(p,"AI Assistant","Security guidance");LinearLayout c=panel(p,Color.rgb(239,233,255));title(c,"CyberShield AI","Transparent local security guidance. Cloud AI/reputation can be connected later.");EditText in=new EditText(this);in.setHint("Ask about an APK, link, message or call");in.setTextColor(TEXT);in.setHintTextColor(MUTED);c.addView(in,new LinearLayout.LayoutParams(-1,dp(58)));Button b=primary("Analyze Question");c.addView(b,new LinearLayout.LayoutParams(-1,dp(38)));TextView o=txt("Answer appears here.",10,TEXT,false);c.addView(o,new LinearLayout.LayoutParams(-1,dp(180)));b.setOnClickListener(v->o.setText(answer(in.getText().toString())));nav(p,"ai");mount(p);}
    String answer(String q){String x=q==null?"":q.toLowerCase(Locale.ROOT);if(x.contains("apk"))return"Use APK Guard to inspect SHA-256, package metadata, permissions, certificate and archive indicators. These checks do not prove malware.";if(x.contains("link")||x.contains("url"))return"Use Link Guard to inspect the domain, HTTPS and common phishing patterns. Avoid entering credentials on suspicious pages.";if(x.contains("message")||x.contains("sms")||x.contains("whatsapp"))return"Message Guard can inspect notification text only after explicit notification access. It cannot silently read private WhatsApp databases.";if(x.contains("call"))return"Call Guard uses Android's call-screening role and is conservative about blocking.";return"Start with Full Security Audit, then review App Security, APK Guard, Link Guard and Network status.";}
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);if(requestCode==APK&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){apk();apkOut.setText("Scanning selected APK…");Uri u=data.getData();new Thread(()->{String r=ApkScanner.scan(this,u);runOnUiThread(()->apkOut.setText(r));}).start();}else if(requestCode==CALL_ROLE)calls();else if(requestCode==FILE&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){Uri u=data.getData();if(fileOut!=null)fileOut.setText("Scanning selected file…");new Thread(()->{String rr=FileScanner.scan(this,u);runOnUiThread(()->{if(fileOut!=null)fileOut.setText(rr);});}).start();}}
}
