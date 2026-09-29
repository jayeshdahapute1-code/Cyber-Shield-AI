package com.cybershield.ai;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.net.Uri;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import java.security.MessageDigest;
import android.provider.Settings;
import android.view.*;
import android.widget.EditText;
import android.content.pm.*;
import android.text.InputType;
import java.util.*;
import java.net.*;
import java.io.*;

public class MainActivity extends Activity {
    ShieldView view;
    int lastCheckedApps=0;
    int lastRiskApps=0;
    String lastScanTime="Not scanned yet";
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(2,8,7));
        getWindow().setNavigationBarColor(Color.rgb(2,8,7));
        view=new ShieldView(this);
        setContentView(view);
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==42 && resultCode==RESULT_OK && data!=null && data.getData()!=null) scanSelectedApk(data.getData());
    }

    @Override public void onBackPressed(){
        if(view!=null && !view.screen.equals("home")){
            view.screen="home";
            view.invalidate();
        } else super.onBackPressed();
    }


    void info(String title,String msg){
        new AlertDialog.Builder(this).setTitle(title).setMessage(msg)
        .setPositiveButton("OK",null).show();
    }

    void realAppCheck(){
        PackageManager pm=getPackageManager();
        List<ApplicationInfo> apps=pm.getInstalledApplications(PackageManager.GET_META_DATA);
        int suspicious=0, checked=0;
        StringBuilder b=new StringBuilder();
        for(ApplicationInfo a:apps){
            if((a.flags & ApplicationInfo.FLAG_SYSTEM)!=0) continue;
            checked++;
            String n=pm.getApplicationLabel(a).toString();
            String p=a.packageName.toLowerCase(Locale.ROOT);
            boolean risk=p.contains("mod")||p.contains("hack")||p.contains("crack")||p.contains("cheat");
            if(risk){suspicious++; b.append("⚠ ").append(n).append(" — suspicious package name\\n");}
        }
        String result="Apps checked: "+checked+"\\nSuspicious indicators: "+suspicious+"\\n\\n";
        result += suspicious==0 ? "No simple risk indicators found. This is not a malware guarantee." : b.toString();
        info("Real App Security Check",result);
    }

    void linkCheck(){
        final EditText input=new EditText(this);
        input.setHint("https://example.com");
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI);
        new AlertDialog.Builder(this).setTitle("Real Link Check").setView(input)
        .setNegativeButton("Cancel",null)
        .setPositiveButton("Analyze",(d,w)->{
            String u=input.getText().toString().trim().toLowerCase(Locale.ROOT);
            if(u.isEmpty()){info("Link Check","Enter a URL first.");return;}
            boolean https=u.startsWith("https://");
            boolean suspicious=u.contains("@")||u.contains("bit.ly")||u.contains("tinyurl")||u.contains("free-")||u.contains("login-")||u.contains("verify-")||u.contains("reward");
            String result="HTTPS: "+(https?"YES":"NO")+"\\nSuspicious patterns: "+(suspicious?"DETECTED":"NONE")+"\\n\\n";
            result += suspicious ? "Risk indicator found. Do not enter passwords or payment details." : "No basic phishing indicators found. This is a heuristic check, not a guarantee.";
            info("Link Security Result",result);
        }).show();
    }

    void scanSelectedApk(Uri uri){
        File temp=null;
        try{
            temp=File.createTempFile("cybershield_scan_",".apk",getCacheDir());
            InputStream in=getContentResolver().openInputStream(uri);
            if(in==null) throw new IOException("Cannot open selected APK");
            OutputStream out=new FileOutputStream(temp);
            MessageDigest md=MessageDigest.getInstance("SHA-256");
            byte[] buf=new byte[8192]; int len; long size=0;
            while((len=in.read(buf))!=-1){out.write(buf,0,len);md.update(buf,0,len);size+=len;}
            in.close(); out.close();

            StringBuilder hash=new StringBuilder();
            for(byte x:md.digest()) hash.append(String.format(Locale.US,"%02x",x));

            PackageManager pm=getPackageManager();
            int flags=PackageManager.GET_PERMISSIONS;
            if(Build.VERSION.SDK_INT>=28) flags|=PackageManager.GET_SIGNING_CERTIFICATES;
            PackageInfo pi=pm.getPackageArchiveInfo(temp.getAbsolutePath(),flags);

            StringBuilder r=new StringBuilder();
            r.append("FILE ANALYSIS\\n");
            r.append("Size: ").append(size).append(" bytes\\n");
            r.append("SHA-256: ").append(hash).append("\\n\\n");
            if(pi!=null){
                r.append("Package: ").append(pi.packageName).append("\\n");
                r.append("Version: ").append(pi.versionName==null?"unknown":pi.versionName).append("\\n\\n");
                r.append("REQUESTED PERMISSIONS\\n");
                if(pi.requestedPermissions!=null){
                    for(String p:pi.requestedPermissions){
                        String shortName=p.substring(p.lastIndexOf('.')+1);
                        r.append("• ").append(shortName).append("\\n");
                    }
                } else r.append("• None reported\\n");

                if(Build.VERSION.SDK_INT>=28 && pi.signingInfo!=null){
                    Signature[] sigs=pi.signingInfo.hasMultipleSigners()
                            ?pi.signingInfo.getApkContentsSigners()
                            :pi.signingInfo.getSigningCertificateHistory();
                    if(sigs!=null && sigs.length>0){
                        MessageDigest sd=MessageDigest.getInstance("SHA-256");
                        byte[] cert=sd.digest(sigs[0].toByteArray());
                        StringBuilder certHash=new StringBuilder();
                        for(byte x:cert) certHash.append(String.format(Locale.US,"%02x",x));
                        r.append("\\nCERTIFICATE SHA-256\\n").append(certHash).append("\\n");
                    }
                }
                r.append("\\nANALYSIS LIMIT\\nMetadata, permissions and hashes do not prove malware status.");
            }else{
                r.append("Could not parse APK metadata. The selected file may not be a valid APK.");
            }
            info("APK Security Report",r.toString());
        }catch(Exception e){
            info("APK Scanner","Scan failed safely: "+e.getMessage());
        }finally{
            if(temp!=null) temp.delete();
        }
    }


    void runSecurityScan(){
        try{
            PackageManager pm=getPackageManager();
            List<ApplicationInfo> apps=pm.getInstalledApplications(PackageManager.GET_META_DATA);
            int checked=0,risk=0;
            for(ApplicationInfo a:apps){
                if((a.flags & ApplicationInfo.FLAG_SYSTEM)!=0) continue;
                checked++;
                String p=a.packageName.toLowerCase(Locale.ROOT);
                if(p.contains("mod")||p.contains("crack")||p.contains("cheat")||p.contains("hack")) risk++;
            }
            lastCheckedApps=checked; lastRiskApps=risk;
            lastScanTime=new java.text.SimpleDateFormat("dd MMM, HH:mm",Locale.US).format(new Date());
            view.screen="reports";
            view.invalidate();
        }catch(Exception e){info("Security Scan","Scan could not complete: "+e.getMessage());}
    }

    String networkStatus(){
        ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        Network n=cm.getActiveNetwork();
        if(n==null) return "No active network";
        NetworkCapabilities caps=cm.getNetworkCapabilities(n);
        if(caps==null) return "Network information unavailable";
        String type=caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)?"Wi-Fi":
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)?"Mobile data":
                caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)?"VPN":"Other";
        boolean validated=caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        boolean metered=!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
        return type+" • "+(validated?"Internet validated":"Internet not validated")+" • "+(metered?"Metered":"Unmetered");
    }

    void securitySettings(){
        try{startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));}
        catch(Exception e){info("Security Settings","Android security settings could not be opened.");}
    }

    void apkGuard(){
        new AlertDialog.Builder(this).setTitle("APK Guard")
        .setMessage("Third-party APK protection\n\nRisk checks:\n• Unknown source\n• Package/signature information\n• Requested permissions\n• Suspicious behavior indicators\n\nNormal Android apps cannot silently control every installer action. CyberShield AI can warn and guide the user.")
        .setNegativeButton("Cancel",null)
        .setNeutralButton("Unknown Sources",(d,w)->{
            try{
                Intent i=new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                i.setData(Uri.parse("package:"+getPackageName()));
                startActivity(i);
            }catch(Exception e){startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));}
        })
        .setPositiveButton("Select APK",(d,w)->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("application/vnd.android.package-archive");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,42);}).show();
    }

    class ShieldView extends View {
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        final int BG=Color.rgb(5,11,20), PANEL=Color.rgb(12,25,39), PANEL2=Color.rgb(15,31,48);
        final int GREEN=Color.rgb(48,245,183), CYAN=Color.rgb(52,211,255), RED=Color.rgb(255,75,108);
        final int WHITE=Color.rgb(241,247,255), MUTED=Color.rgb(143,163,184), YELLOW=Color.rgb(255,201,77), PURPLE=Color.rgb(157,123,255);
        final HashMap<String,RectF> hit=new HashMap<>();
        String screen="home";
        float S=1f,OX=0,OY=0;

        ShieldView(Context c){super(c);p.setTypeface(Typeface.create("sans",Typeface.NORMAL));setFocusable(true);}
        float X(float v){return OX+v*S;} float Y(float v){return OY+v*S;}
        void fill(Canvas c,int col){c.drawColor(col);}
        void rect(Canvas c,float l,float t,float r,float b,int col,float rad){p.setStyle(Paint.Style.FILL);p.setColor(col);c.drawRoundRect(X(l),Y(t),X(r),Y(b),rad*S,rad*S,p);}
        void stroke(Canvas c,float l,float t,float r,float b,int col,float rad){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.4f*S);p.setColor(col);c.drawRoundRect(X(l),Y(t),X(r),Y(b),rad*S,rad*S,p);p.setStyle(Paint.Style.FILL);}
        void txt(Canvas c,String s,float x,float y,float size,int col,Paint.Align a){p.setStyle(Paint.Style.FILL);p.setColor(col);p.setTextSize(size*S);p.setTextAlign(a);p.setTypeface(Typeface.create("sans",Typeface.NORMAL));c.drawText(s,X(x),Y(y),p);}
        void bold(Canvas c,String s,float x,float y,float size,int col,Paint.Align a){p.setStyle(Paint.Style.FILL);p.setColor(col);p.setTextSize(size*S);p.setTextAlign(a);p.setTypeface(Typeface.create("sans",Typeface.BOLD));c.drawText(s,X(x),Y(y),p);p.setTypeface(Typeface.DEFAULT);}
        void line(Canvas c,float x1,float y1,float x2,float y2,int col,float w){p.setColor(col);p.setStrokeWidth(w*S);c.drawLine(X(x1),Y(y1),X(x2),Y(y2),p);}
        void add(String id,float l,float t,float r,float b){hit.put(id,new RectF(X(l),Y(t),X(r),Y(b)));}

        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            float W=getWidth(),H=getHeight();
            S=Math.min(W/360f,H/760f); OX=(W-360*S)/2f; OY=0;
            fill(c,BG); hit.clear(); grid(c);
            if(screen.equals("home"))home(c);
            else if(screen.equals("scan"))scan(c);
            else if(screen.equals("threat"))threat(c);
            else if(screen.equals("messages"))messages(c);
            else if(screen.equals("email"))email(c);
            else if(screen.equals("link"))link(c);
            else if(screen.equals("apps"))apps(c);
            else if(screen.equals("apk"))apk(c);
            else if(screen.equals("wifi"))wifi(c);
            else if(screen.equals("calls"))calls(c);
            else if(screen.equals("settings"))settings(c);
            else if(screen.equals("reports"))reports(c);
            else if(screen.equals("ai"))ai(c);
            nav(c);
        }

        void grid(Canvas c){
            p.setStrokeWidth(0.6f*S);p.setColor(Color.rgb(8,38,33));
            for(int x=0;x<=360;x+=24)c.drawLine(X(x),Y(0),X(x),Y(760),p);
            for(int y=0;y<=760;y+=24)c.drawLine(X(0),Y(y),X(360),Y(y),p);
        }
        void top(Canvas c,String title){
            rect(c,10,12,350,64,Color.rgb(8,20,33),18);
            stroke(c,10,12,350,64,Color.rgb(30,69,94),18);
            txt(c,"‹",30,47,34,WHITE,Paint.Align.CENTER);
            bold(c,title,55,43,16,WHITE,Paint.Align.LEFT);
            rect(c,278,25,338,51,Color.rgb(25,39,57),13);
            txt(c,"PRO",308,42,9,YELLOW,Paint.Align.CENTER);
            add("back",10,10,52,66);
        }
        void brand(Canvas c){
            bold(c,"CyberShield",20,39,23,WHITE,Paint.Align.LEFT);
            bold(c,"AI",182,39,23,CYAN,Paint.Align.LEFT);
            rect(c,284,21,340,49,Color.rgb(25,39,57),14);
            txt(c,"PRO",312,39,9,YELLOW,Paint.Align.CENTER);
        }
        void shield(Canvas c,float cx,float cy,float r){
            Path q=new Path();q.moveTo(X(cx),Y(cy-r));q.lineTo(X(cx+r*.78f),Y(cy-r*.62f));q.lineTo(X(cx+r*.62f),Y(cy+r*.42f));q.quadTo(X(cx),Y(cy+r),X(cx-r*.62f),Y(cy+r*.42f));q.lineTo(X(cx-r*.78f),Y(cy-r*.62f));q.close();
            p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(4,47,36));c.drawPath(q,p);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3*S);p.setColor(GREEN);c.drawPath(q,p);p.setStyle(Paint.Style.FILL);
            txt(c,"✓",cx,cy+18,48,GREEN,Paint.Align.CENTER);
        }
        void card(Canvas c,float y,float h,int accent,String title,String sub){
            rect(c,12,y,348,y+h,PANEL,16);stroke(c,12,y,348,y+h,accent,16);
            bold(c,title,28,y+28,15,WHITE,Paint.Align.LEFT);
            txt(c,sub,28,y+48,11,MUTED,Paint.Align.LEFT);
        }
        void button(Canvas c,String id,String label,float l,float t,float r,float b,int accent){
            rect(c,l,t,r,b,Color.rgb(4,25,22),16);stroke(c,l,t,r,b,accent,16);
            txt(c,label,(l+r)/2,t+(b-t)/2+5,13,accent,Paint.Align.CENTER);add(id,l,t,r,b);
        }
        void home(Canvas c){
            brand(c);
            txt(c,"Smart protection for your device",20,61,9,MUTED,Paint.Align.LEFT);
            rect(c,15,78,345,212,Color.rgb(9,39,43),22);
            stroke(c,15,78,345,212,Color.rgb(39,214,180),22);
            shield(c,67,144,43);
            bold(c,"Protection Active",124,111,17,WHITE,Paint.Align.LEFT);
            txt(c,"Your device is protected",124,132,9,MUTED,Paint.Align.LEFT);
            bold(c,"98%",300,128,22,GREEN,Paint.Align.CENTER);
            txt(c,"SECURITY",300,146,7,MUTED,Paint.Align.CENTER);
            rect(c,28,169,332,199,Color.rgb(7,29,40),12);
            bold(c,String.valueOf(lastCheckedApps),72,189,14,CYAN,Paint.Align.CENTER);
            txt(c,"Apps Scanned",72,201,6,MUTED,Paint.Align.CENTER);
            bold(c,String.valueOf(lastRiskApps),180,189,14,RED,Paint.Align.CENTER);
            txt(c,"Threats",180,201,6,MUTED,Paint.Align.CENTER);
            bold(c,"0",288,189,14,GREEN,Paint.Align.CENTER);
            txt(c,"Unsafe Links",288,201,6,MUTED,Paint.Align.CENTER);

            bold(c,"Quick Actions",18,237,12,WHITE,Paint.Align.LEFT);
            String[][] a={{"apps","▣","App Security",CYAN},{"link","⌁","Link Scanner",PURPLE},{"messages","▤","Message Guard",GREEN},{"email","✉","Email Scanner",YELLOW},{"calls","☎","Call Guard",RED},{"wifi","⌁","Wi-Fi Security",CYAN}};
            for(int i=0;i<a.length;i++){
                int col=i%2,row=i/2; float l=15+col*170,t=250+row*59;
                int ac=(Integer)a[i][3];
                rect(c,l,t,l+160,t+50,PANEL,14);stroke(c,l,t,l+160,t+50,Color.rgb(30,61,82),14);
                rect(c,l+8,t+9,l+39,t+40,Color.argb(38,Color.red(ac),Color.green(ac),Color.blue(ac)),10);
                txt(c,a[i][1],l+24,t+29,16,ac,Paint.Align.CENTER);
                bold(c,a[i][2],l+51,t+30,10,WHITE,Paint.Align.LEFT);
                add((String)a[i][0],l,t,l+160,t+50);
            }

            rect(c,15,438,345,520,Color.rgb(12,29,47),16);
            stroke(c,15,438,345,520,Color.rgb(42,77,104),16);
            txt(c,"◉",38,477,25,PURPLE,Paint.Align.CENTER);
            bold(c,"AI Security Assistant",58,464,12,WHITE,Paint.Align.LEFT);
            txt(c,"Ask about apps, links, messages & threats",58,484,8,MUTED,Paint.Align.LEFT);
            txt(c,"›",326,479,22,MUTED,Paint.Align.CENTER);
            add("ai",15,438,345,520);

            rect(c,15,532,345,594,Color.rgb(8,25,39),16);
            bold(c,"SECURITY STATUS",30,555,9,MUTED,Paint.Align.LEFT);
            txt(c,networkStatus(),30,577,9,WHITE,Paint.Align.LEFT);
            txt(c,"●",320,577,14,GREEN,Paint.Align.CENTER);
        }
        void scan(Canvas c){
            top(c,"Security Scan");
            rect(c,15,78,345,171,Color.rgb(9,34,45),18);
            stroke(c,15,78,345,171,CYAN,18);
            txt(c,"◉",48,123,31,CYAN,Paint.Align.CENTER);
            bold(c,"Security Center",82,110,16,WHITE,Paint.Align.LEFT);
            txt(c,"Run a local device audit",82,131,9,MUTED,Paint.Align.LEFT);
            bold(c,lastScanTime,82,151,9,CYAN,Paint.Align.LEFT);
            button(c,"startscan","RUN FULL AUDIT",58,187,302,233,GREEN);
            card(c,252,72,CYAN,"Device Apps",lastCheckedApps+" apps checked in latest audit");
            card(c,337,72,PURPLE,"Network",networkStatus());
            card(c,422,72,YELLOW,"APK Guard","Hash • permissions • signature");
        }
        void threat(Canvas c){
            top(c,"Threats");
            tabs(c,new String[]{"All (7)","Apps (3)","Links (2)"});
            String[] n={"Game Mod APK","Suspicious Link","Unknown Sender","Risky Permission"};
            String[] s={"High Risk","Medium","Medium","Review"};
            int[] co={RED,YELLOW,YELLOW,PURPLE};
            for(int i=0;i<n.length;i++){
                float y=120+i*65;
                rect(c,12,y,348,y+56,PANEL,13);
                txt(c,i==0?"▣":(i==1?"⌁":"!"),35,y+33,18,co[i],Paint.Align.CENTER);
                bold(c,n[i],62,y+21,10,WHITE,Paint.Align.LEFT);
                txt(c,"Security indicator detected",62,y+39,8,MUTED,Paint.Align.LEFT);
                badge(c,278,y+17,s[i],co[i]);
            }
            rect(c,15,400,345,482,Color.rgb(11,28,44),16);
            bold(c,"Protection summary",30,425,11,WHITE,Paint.Align.LEFT);
            txt(c,"Review flagged items before taking action.",30,447,8,MUTED,Paint.Align.LEFT);
            txt(c,"Local checks • No automatic deletion",30,466,8,CYAN,Paint.Align.LEFT);
        }
        void messages(Canvas c){
            top(c,"Message Scanner");
            tabs(c,new String[]{"SMS","WhatsApp","Other Apps"});
            String[] names={"+91 98765 43210","Bank","Unknown","Friend","Delivery","+91 87654 3210","Google"};
            String[] states={"Dangerous","Safe","Suspicious","Safe","Safe","Safe","Safe"};
            for(int i=0;i<7;i++){float y=120+i*58;rect(c,12,y,348,y+50,PANEL,12);txt(c,"●",31,y+29,17,i==0?RED:GREEN,Paint.Align.CENTER);bold(c,names[i],52,y+20,11,WHITE,Paint.Align.LEFT);txt(c,"Message preview and security analysis",52,y+37,8,MUTED,Paint.Align.LEFT);badge(c,302,y+12,states[i],i==0?RED:(states[i].equals("Suspicious")?YELLOW:GREEN));}
            button(c,"scanmsg","⌕  SCAN MESSAGES",70,550,290,594,CYAN);
        }
        void email(Canvas c){
            top(c,"Email Scanner");
            tabs(c,new String[]{"Inbox (12)","Spam (3)","All Mail"});
            String[] n={"Amazon","PayPal","Microsoft","Bank of India","Unknown Sender"};
            String[] s={"Safe","Phishing","Safe","Suspicious","Phishing"};
            int[] co={GREEN,RED,GREEN,YELLOW,RED};
            for(int i=0;i<n.length;i++){
                float y=118+i*62;
                rect(c,12,y,348,y+54,PANEL,12);
                rect(c,22,y+10,52,y+44,Color.rgb(25,42,59),10);
                txt(c,"✉",37,y+32,16,co[i],Paint.Align.CENTER);
                bold(c,n[i],64,y+20,10,WHITE,Paint.Align.LEFT);
                txt(c,"Security message preview",64,y+38,8,MUTED,Paint.Align.LEFT);
                badge(c,281,y+16,s[i],co[i]);
            }
            button(c,"scanemail","SCAN EMAILS",70,525,290,571,CYAN);
        }
        void tabs(Canvas c,String[] t){
            float w=320f/t.length;for(int i=0;i<t.length;i++){rect(c,20+i*w,70,20+(i+1)*w,105,i==0?Color.rgb(5,60,48):PANEL,12);txt(c,t[i],20+(i+.5f)*w,92,9,i==0?GREEN:WHITE,Paint.Align.CENTER);}
        }
        void badge(Canvas c,float x,float y,String s,int col){
            float w=Math.max(58,s.length()*5.5f+18);
            rect(c,x,y,x+w,y+22,Color.argb(45,Color.red(col),Color.green(col),Color.blue(col)),9);
            txt(c,s,x+w/2,y+15,7,col,Paint.Align.CENTER);
        }
        void link(Canvas c){
            top(c,"Link Scanner");
            rect(c,18,78,342,120,PANEL,12);
            txt(c,"⌕",37,104,18,MUTED,Paint.Align.CENTER);
            txt(c,"Paste URL here...",57,104,10,MUTED,Paint.Align.LEFT);
            button(c,"scanlink","SCAN LINK",62,132,298,178,CYAN);
            rect(c,15,198,345,385,Color.rgb(13,29,45),17);
            stroke(c,15,198,345,385,Color.rgb(55,75,105),17);
            txt(c,"◎",42,238,25,CYAN,Paint.Align.CENTER);
            bold(c,"Ready to analyze",76,228,14,WHITE,Paint.Align.LEFT);
            txt(c,"HTTPS • domain • phishing patterns",76,249,8,MUTED,Paint.Align.LEFT);
            bold(c,"Example",30,284,9,MUTED,Paint.Align.LEFT);
            txt(c,"https://example.com/login",30,304,10,WHITE,Paint.Align.LEFT);
            badge(c,30,321,"ANALYZE",CYAN);
            txt(c,"Local heuristic only — no guaranteed verdict.",30,354,8,MUTED,Paint.Align.LEFT);
            rect(c,15,405,345,470,Color.rgb(9,32,40),14);
            txt(c,"✓",38,445,20,GREEN,Paint.Align.CENTER);
            bold(c,"Secure browsing tip",62,432,11,WHITE,Paint.Align.LEFT);
            txt(c,"Never enter passwords after suspicious links.",62,451,8,MUTED,Paint.Align.LEFT);
        }
        void apps(Canvas c){
            top(c,"App Security");
            tabs(c,new String[]{"Installed Apps","Risk Apps"});
            rect(c,18,113,342,151,PANEL,12);
            txt(c,"⌕",36,138,18,MUTED,Paint.Align.CENTER);
            txt(c,"Search installed apps...",55,138,9,MUTED,Paint.Align.LEFT);
            String[] n={"WhatsApp","Instagram","Facebook","Game Mod APK","Chrome","Telegram"};
            int[] co={GREEN,GREEN,GREEN,RED,GREEN,GREEN};
            String[] st={"Safe","Safe","Safe","Review","Safe","Safe"};
            for(int i=0;i<n.length;i++){
                float y=161+i*58;
                rect(c,12,y,348,y+50,PANEL,12);
                rect(c,23,y+10,53,y+36,Color.rgb(25,42,59),9);
                txt(c,"▣",38,y+29,16,co[i],Paint.Align.CENTER);
                bold(c,n[i],65,y+20,11,WHITE,Paint.Align.LEFT);
                txt(c,i==3?"Suspicious indicators":"Installed • local audit",65,y+37,8,MUTED,Paint.Align.LEFT);
                badge(c,286,y+14,st[i],co[i]);
                txt(c,"⋮",333,y+29,17,MUTED,Paint.Align.CENTER);
            }
            button(c,"scanapps","SCAN ALL APPS",70,525,290,571,CYAN);
        }
        void apk(Canvas c){
            top(c,"APK Guard");rect(c,12,70,348,130,Color.rgb(45,5,10),14);stroke(c,12,70,348,130,RED,14);txt(c,"⚠",35,106,25,RED,Paint.Align.CENTER);bold(c,"Third-Party App Installation",65,98,12,RED,Paint.Align.LEFT);txt(c,"Risk Detected",65,116,10,WHITE,Paint.Align.LEFT);
            card(c,145,90,RED,"CoolGame.apk","Version 1.2.3  •  Size 42 MB\nFrom: Unknown Source");
            rect(c,15,250,345,430,PANEL,16);stroke(c,15,250,345,430,RED,16);bold(c,"AI Analysis",30,280,14,RED,Paint.Align.LEFT);txt(c,"⚠ Not from Google Play Store",30,310,10,WHITE,Paint.Align.LEFT);txt(c,"⚠ Requests dangerous permissions",30,335,10,WHITE,Paint.Align.LEFT);txt(c,"⚠ Known bad behavior indicators",30,360,10,WHITE,Paint.Align.LEFT);txt(c,"⚠ Signature not verified",30,385,10,WHITE,Paint.Align.LEFT);bold(c,"Risk Level",30,415,10,WHITE,Paint.Align.LEFT);badge(c,105,400,"HIGH",RED);
            button(c,"scanapk","⌕  SCAN APK",65,450,295,494,GREEN);button(c,"cancelapk","CANCEL INSTALLATION",15,510,170,554,RED);button(c,"continueapk","CONTINUE ANYWAY",185,510,345,554,Color.LTGRAY);
        }
        void wifi(Canvas c){
            top(c,"Wi-Fi Security");
            rect(c,15,78,345,174,Color.rgb(8,37,40),18);
            stroke(c,15,78,345,174,CYAN,18);
            txt(c,"⌁",55,130,42,GREEN,Paint.Align.CENTER);
            bold(c,"Current Network",92,108,13,WHITE,Paint.Align.LEFT);
            txt(c,networkStatus(),92,129,9,MUTED,Paint.Align.LEFT);
            badge(c,272,104,"CONNECTED",GREEN);
            txt(c,"Privacy-first network check",92,151,8,MUTED,Paint.Align.LEFT);
            rect(c,15,190,345,345,PANEL,16);
            bold(c,"Network Security",30,216,12,WHITE,Paint.Align.LEFT);
            rect(c,27,230,173,292,Color.rgb(15,36,53),12);
            bold(c,"CONNECTION",40,252,8,MUTED,Paint.Align.LEFT);
            txt(c,"ACTIVE",40,273,12,GREEN,Paint.Align.LEFT);
            rect(c,187,230,333,292,Color.rgb(15,36,53),12);
            bold(c,"VALIDATION",200,252,8,MUTED,Paint.Align.LEFT);
            txt(c,networkStatus().contains("validated")?"YES":"UNKNOWN",200,273,12,CYAN,Paint.Align.LEFT);
            rect(c,27,302,333,328,Color.rgb(15,36,53),10);
            txt(c,"Network details are read from Android connectivity APIs.",38,320,7,MUTED,Paint.Align.LEFT);
            button(c,"disconnect","NETWORK SETTINGS",48,370,312,416,CYAN);
        }
        void calls(Canvas c){
            top(c,"Call Guard");
            rect(c,15,78,345,245,Color.rgb(25,14,34),20);
            stroke(c,15,78,345,245,Color.rgb(225,63,129),20);
            txt(c,"☎",180,132,48,RED,Paint.Align.CENTER);
            bold(c,"Call Protection",180,162,18,WHITE,Paint.Align.CENTER);
            txt(c,"Screen spam & fraud calls",180,181,9,MUTED,Paint.Align.CENTER);
            rect(c,31,197,329,230,Color.rgb(12,26,41),11);
            txt(c,"SCREENING STATUS",52,218,7,MUTED,Paint.Align.LEFT);
            txt(c,"SETUP REQUIRED",310,218,8,YELLOW,Paint.Align.RIGHT);
            rect(c,15,258,345,318,PANEL,14);
            bold(c,"Recent activity",28,281,11,WHITE,Paint.Align.LEFT);
            txt(c,"12 spam • 5 fraud indicators",28,301,9,MUTED,Paint.Align.LEFT);
            button(c,"blockcall","SET UP CALL SCREENING",42,335,318,381,CYAN);
            bold(c,"Recent Calls",20,414,13,WHITE,Paint.Align.LEFT);
            String[] n={"+91 94236 56789","+91 87654 32109","+91 98765 43210"};
            String[] s={"Possible Spam","Telemarketer","Safe Contact"};
            for(int i=0;i<3;i++){
                float y=430+i*52; rect(c,15,y,345,y+44,PANEL,12);
                txt(c,"☎",34,y+27,15,i==0?RED:(i==1?YELLOW:GREEN),Paint.Align.CENTER);
                bold(c,n[i],58,y+18,10,WHITE,Paint.Align.LEFT);
                txt(c,s[i],58,y+34,8,MUTED,Paint.Align.LEFT);
                badge(c,268,y+12,s[i],i==0?RED:(i==1?YELLOW:GREEN));
            }
        }
        void settings(Canvas c){
            top(c,"Settings");
            rect(c,15,70,345,124,Color.rgb(4,32,27),16);stroke(c,15,70,345,124,Color.rgb(0,190,140),16);
            txt(c,"●",35,101,18,GREEN,Paint.Align.CENTER);
            bold(c,"PROTECTION ACTIVE",60,95,13,WHITE,Paint.Align.LEFT);
            txt(c,"Local analysis • Privacy-first",60,113,8,MUTED,Paint.Align.LEFT);
            String[][] s={{"securitysettings","◉","Android Security","Open system security controls"},
                          {"scan","⌁","Scan Center","Run local device audit"},
                          {"apk","⚠","APK Guard","Select and inspect APK files"},
                          {"link","⌁","Link Guard","Analyze suspicious URLs"},
                          {"apps","▦","App Audit","Review installed apps"},
                          {"reports","▤","Reports","View latest scan summary"},
                          {"ai","◉","AI Assistant","Security guidance & analysis"},
                          {"about","ⓘ","About CyberShield AI","Version 1.2.0"}};
            for(int i=0;i<s.length;i++){
                float y=138+i*61;
                rect(c,15,y,345,y+50,PANEL2,14);stroke(c,15,y,345,y+50,Color.rgb(11,55,47),14);
                txt(c,s[i][1],34,y+29,17,i==2?RED:CYAN,Paint.Align.CENTER);
                bold(c,s[i][2],58,y+20,10,WHITE,Paint.Align.LEFT);
                txt(c,s[i][3],58,y+37,7,MUTED,Paint.Align.LEFT);
                txt(c,"›",326,y+30,20,MUTED,Paint.Align.CENTER);
                add(s[i][0],15,y,345,y+50);
            }
        }

        void reports(Canvas c){
            top(c,"Reports");
            tabs(c,new String[]{"7 Days","30 Days","All Time"});
            rect(c,15,120,345,295,PANEL,18);
            bold(c,"Security Overview",30,149,13,WHITE,Paint.Align.LEFT);
            bold(c,String.valueOf(lastCheckedApps),55,191,24,CYAN,Paint.Align.CENTER);
            txt(c,"Scanned",55,211,8,MUTED,Paint.Align.CENTER);
            bold(c,String.valueOf(lastRiskApps),145,191,24,RED,Paint.Align.CENTER);
            txt(c,"Indicators",145,211,8,MUTED,Paint.Align.CENTER);
            bold(c,"15",235,191,24,PURPLE,Paint.Align.CENTER);
            txt(c,"Blocked",235,211,8,MUTED,Paint.Align.CENTER);
            bold(c,"111",305,191,24,GREEN,Paint.Align.CENTER);
            txt(c,"Safe",305,211,8,MUTED,Paint.Align.CENTER);
            for(int i=0;i<6;i++){
                float x=35+i*48; float h=25+(i*13%55);
                rect(c,x,274-h,x+25,274,Color.rgb(28,130,170),6);
            }
            bold(c,"Latest audit",20,326,10,MUTED,Paint.Align.LEFT);
            txt(c,lastScanTime,20,346,10,WHITE,Paint.Align.LEFT);
            txt(c,networkStatus(),20,366,8,MUTED,Paint.Align.LEFT);
            button(c,"startscan","RUN AUDIT AGAIN",60,400,300,446,GREEN);
        }
        void ai(Canvas c){
            top(c,"AI Assistant");
            rect(c,15,78,345,172,Color.rgb(19,25,47),18);
            stroke(c,15,78,345,172,PURPLE,18);
            txt(c,"◉",48,123,33,PURPLE,Paint.Align.CENTER);
            bold(c,"Hi, I'm CyberShield AI",82,112,14,WHITE,Paint.Align.LEFT);
            txt(c,"Ask me anything about security.",82,132,9,MUTED,Paint.Align.LEFT);
            txt(c,"Local results are explained, not guaranteed.",82,150,7,MUTED,Paint.Align.LEFT);
            String[] q={"Is this link safe?","Check if this app is safe","Explain this threat","How to stay safer online?"};
            for(int i=0;i<q.length;i++){
                rect(c,20,190+i*58,340,239+i*58,PANEL,13);
                txt(c,"◉",40,220+i*58,14,CYAN,Paint.Align.CENTER);
                bold(c,q[i],60,222+i*58,9,WHITE,Paint.Align.LEFT);
                txt(c,"›",322,222+i*58,18,MUTED,Paint.Align.CENTER);
                add("q"+i,20,190+i*58,340,239+i*58);
            }
        }
        void nav(Canvas c){
            rect(c,0,665,360,760,Color.rgb(2,12,10),0);
            line(c,0,665,360,665,Color.rgb(15,60,50),1);
            String[][] n={{"home","⌂","HOME"},{"threat","!","THREATS"},{"reports","▤","REPORTS"},{"ai","◉","AI"}};
            for(int i=0;i<n.length;i++){
                float x=45+i*90; boolean active=screen.equals(n[i][0]); int ac=active?GREEN:MUTED;
                if(active) rect(c,x-28,676,x+28,704,Color.rgb(4,50,39),14);
                txt(c,n[i][1],x,697,19,ac,Paint.Align.CENTER);
                txt(c,n[i][2],x,722,7,ac,Paint.Align.CENTER);
                add(n[i][0],x-42,670,x+42,744);
            }
        }

        void action(String id){
            if(id.equals("home"))screen="home";
            else if(id.equals("scan"))screen="scan";
            else if(id.equals("startscan"))runSecurityScan();
            else if(id.equals("securitysettings"))securitySettings();
            else if(id.equals("about"))info("About CyberShield AI","Version 1.2.0\nPrivacy-first mobile security toolkit.\n\nLocal checks do not guarantee malware detection.");
            else if(id.equals("apk"))screen="apk";
            else if(id.equals("messages"))screen="messages";
            else if(id.equals("email"))screen="email";
            else if(id.equals("link"))screen="link";
            else if(id.equals("apps"))screen="apps";
            else if(id.equals("wifi"))screen="wifi";
            else if(id.equals("calls"))screen="calls";
            else if(id.equals("settings"))screen="settings";
            else if(id.equals("reports"))screen="reports";
            else if(id.equals("ai"))screen="ai";
            else if(id.equals("threat"))screen="threat";
            else if(id.equals("files"))info("File Scanner","Select a file/APK for metadata and security analysis.");
            else if(id.equals("scanapk"))apkGuard();
            else if(id.equals("continueapk"))info("Warning","Installing an unknown APK can expose your device to malware. Continue only if you trust the source.");
            else if(id.equals("cancelapk"))info("APK Guard","Installation cancelled.");
            else if(id.equals("block"))info("Message Protection","Blocking message content requires the appropriate Android role/permission. No message was silently deleted.");
            else if(id.equals("scanlink"))linkCheck();
            else if(id.equals("scanmsg"))info("Message Scanner","This module requires user-granted SMS/notification access before it can inspect message content.");
            else if(id.equals("scanemail"))info("Email Scanner","Connect an email provider through OAuth before scanning message content.");
            else if(id.equals("scanapps"))realAppCheck();
            else if(id.equals("disconnect"))info("Network Security",networkStatus()+"\n\nCyberShield AI cannot silently disconnect or reconfigure another network.");
            else if(id.equals("blockcall"))info("Call Guard","Call screening setup will be connected to Android's supported CallScreeningService flow in the next security-module build.");
            else if(id.equals("reportcall"))info("Call Guard","Spam report prepared.");
            else if(id.equals("allowcall"))info("Call Guard","Caller allowed.");
            else if(id.equals("back"))screen="home";
            else if(id.startsWith("q"))info("AI Assistant", "AI response module ready.\n\nFor this prototype, connect a trusted security-analysis backend before treating results as real malware verdicts.");
            invalidate();
        }

        @Override public boolean onTouchEvent(MotionEvent e){
            if(e.getAction()!=MotionEvent.ACTION_UP)return true;
            float x=e.getX(),y=e.getY();
            String found=null;
            for(Map.Entry<String,RectF> z:hit.entrySet())if(z.getValue().contains(x,y)){found=z.getKey();break;}
            if(found!=null)action(found);
            return true;
        }
    }
}