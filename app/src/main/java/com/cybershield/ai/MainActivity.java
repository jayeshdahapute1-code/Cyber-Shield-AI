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
        final int BG=Color.rgb(2,8,7), PANEL=Color.rgb(6,20,18), PANEL2=Color.rgb(8,28,25);
        final int GREEN=Color.rgb(0,255,145), CYAN=Color.rgb(0,220,255), RED=Color.rgb(255,55,70);
        final int WHITE=Color.rgb(238,250,248), MUTED=Color.rgb(145,170,165), YELLOW=Color.rgb(255,210,40), PURPLE=Color.rgb(220,80,255);
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
            rect(c,10,12,350,58,Color.rgb(4,22,19),14);
            stroke(c,10,12,350,58,Color.rgb(18,76,63),14);
            txt(c,"‹",31,43,32,WHITE,Paint.Align.CENTER);
            bold(c,title,55,38,16,WHITE,Paint.Align.LEFT);
            rect(c,286,24,337,48,Color.rgb(5,50,39),12);
            txt(c,"● LIVE",311,40,8,GREEN,Paint.Align.CENTER);
            add("back",10,12,52,60);
        }
        void brand(Canvas c){
            bold(c,"CyberShield",22,36,24,WHITE,Paint.Align.LEFT);
            bold(c,"AI",182,36,24,GREEN,Paint.Align.LEFT);
            txt(c,"YOUR DIGITAL GUARDIAN",22,55,8,MUTED,Paint.Align.LEFT);
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
            rect(c,15,72,345,190,Color.rgb(4,30,25),22);
            stroke(c,15,72,345,190,Color.rgb(0,190,140),22);
            shield(c,82,130,42);
            bold(c,"PROTECTION CENTER",140,101,15,WHITE,Paint.Align.LEFT);
            bold(c,"READY",140,127,24,GREEN,Paint.Align.LEFT);
            txt(c,"Local security checks are available",140,148,9,MUTED,Paint.Align.LEFT);
            txt(c,networkStatus(),140,167,8,CYAN,Paint.Align.LEFT);
            button(c,"scan",22,205,175,253,GREEN);
            button(c,"securitysettings",185,205,338,253,CYAN);
            bold(c,"SECURITY MODULES",18,285,10,MUTED,Paint.Align.LEFT);
            String[][] a={{"apk","⚠","APK Guard","APK metadata + hash"},{"link","⌁","Link Guard","Phishing heuristics"},{"apps","▦","App Audit","Installed app audit"},{"wifi","⌁","Network","Connectivity check"},{"messages","▣","Messages","User-enabled scan"},{"email","✉","Email","Phishing analysis"},{"calls","☎","Call Guard","Screening setup"},{"reports","▤","Reports","Scan history"},{"settings","⚙","Settings","Controls & privacy"}};
            for(int i=0;i<a.length;i++){
                int col=i%3,row=i/3; float l=15+col*113,t=305+row*70;
                int ac=i==0?RED:(i==8?PURPLE:CYAN);
                rect(c,l,t,l+105,t+60,PANEL2,14);stroke(c,l,t,l+105,t+60,Color.rgb(13,60,51),14);
                txt(c,a[i][1],l+17,t+23,18,ac,Paint.Align.CENTER);
                bold(c,a[i][2],l+56,t+22,9,WHITE,Paint.Align.CENTER);
                txt(c,a[i][3],l+56,t+41,6,MUTED,Paint.Align.CENTER);
                add(a[i][0],l,t,l+105,t+60);
            }
        }

        void scan(Canvas c){
            top(c,"Security Scan");
            rect(c,20,72,340,142,PANEL2,18);stroke(c,20,72,340,142,GREEN,18);
            txt(c,"◉",48,116,30,GREEN,Paint.Align.CENTER);
            bold(c,"LOCAL DEVICE AUDIT",80,99,14,WHITE,Paint.Align.LEFT);
            txt(c,"Apps • network • security configuration",80,119,9,MUTED,Paint.Align.LEFT);
            button(c,"startscan","RUN FULL AUDIT",60,165,300,212,GREEN);
            card(c,230,70,CYAN,"Installed apps","Tap Run Full Audit to inspect non-system apps.");
            card(c,315,70,CYAN,"Network","Current: "+networkStatus());
            card(c,400,70,YELLOW,"APK Guard","Select an APK to calculate hash and metadata.");
            txt(c,"No cloud upload is performed by these local checks.",180,500,8,MUTED,Paint.Align.CENTER);
        }

        void threat(Canvas c){
            top(c,"Threat Detected");
            rect(c,15,65,345,135,Color.rgb(40,5,9),14);stroke(c,15,65,345,135,RED,14);txt(c,"⚠",38,103,28,RED,Paint.Align.CENTER);bold(c,"HIGH RISK",72,94,18,RED,Paint.Align.LEFT);txt(c,"Suspicious Message Found",72,116,10,WHITE,Paint.Align.LEFT);
            card(c,150,120,CYAN,"From: +91 98765 43210","Congratulations! You won ₹50,000. Click here to claim now.");
            card(c,280,145,RED,"AI Analysis","• Phishing pattern detected\n• Suspicious domain\n• Prize / fake offer\n• Unknown sender");
            button(c,"block","BLOCK & DELETE",15,445,345,492,RED);
            button(c,"home","REPORT",20,505,165,548,Color.LTGRAY);button(c,"home","IGNORE",195,505,340,548,Color.LTGRAY);
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
            top(c,"Email Scanner");tabs(c,new String[]{"Inbox (12)","Spam (3)","All Mail"});
            String[] n={"Amazon","PayPal","Microsoft","Bank","Job Offer","Google"};
            String[] st={"Safe","Phishing","Suspicious","Safe","Scam","Safe"};
            for(int i=0;i<n.length;i++){float y=120+i*63;rect(c,12,y,348,y+55,PANEL,12);txt(c,"✉",32,y+32,18,i==1||i==4?RED:CYAN,Paint.Align.CENTER);bold(c,n[i],58,y+20,11,WHITE,Paint.Align.LEFT);txt(c,"Account and security message preview",58,y+38,8,MUTED,Paint.Align.LEFT);badge(c,304,y+15,st[i],i==1||i==4?RED:(st[i].equals("Suspicious")?YELLOW:GREEN));}
            button(c,"scanemail","✉  SCAN EMAILS",70,540,290,584,CYAN);
        }
        void tabs(Canvas c,String[] t){
            float w=320f/t.length;for(int i=0;i<t.length;i++){rect(c,20+i*w,70,20+(i+1)*w,105,i==0?Color.rgb(5,60,48):PANEL,12);txt(c,t[i],20+(i+.5f)*w,92,9,i==0?GREEN:WHITE,Paint.Align.CENTER);}
        }
        void badge(Canvas c,float x,float y,String s,int col){rect(c,x,y,x+43,y+20,Color.argb(50,Color.red(col),Color.green(col),Color.blue(col)),8);txt(c,s,x+21,y+14,7,col,Paint.Align.CENTER);}
        void link(Canvas c){
            top(c,"Link Scanner");rect(c,18,72,342,112,PANEL,10);txt(c,"https://example.com",30,98,11,WHITE,Paint.Align.LEFT);button(c,"scanlink","⌕  SCAN LINK",65,122,295,165,GREEN);
            rect(c,15,185,345,405,Color.rgb(35,6,9),16);stroke(c,15,185,345,405,RED,16);txt(c,"⚠",42,228,30,RED,Paint.Align.CENTER);bold(c,"DANGEROUS LINK",80,222,15,RED,Paint.Align.LEFT);bold(c,"https://free-reward123.com",80,245,10,WHITE,Paint.Align.LEFT);txt(c,"Risk Level",80,275,9,MUTED,Paint.Align.LEFT);badge(c,105,263,"HIGH",RED);txt(c,"Analysis Result:",30,305,10,WHITE,Paint.Align.LEFT);txt(c,"• Phishing website",30,325,9,WHITE,Paint.Align.LEFT);txt(c,"• Steals personal data",30,344,9,WHITE,Paint.Align.LEFT);txt(c,"• Not a trusted domain",30,363,9,WHITE,Paint.Align.LEFT);
            button(c,"blocklink","BLOCK",25,425,170,468,RED);button(c,"home","OPEN ANYWAY",190,425,335,468,Color.LTGRAY);
            rect(c,15,485,345,555,Color.rgb(4,45,30),14);bold(c,"✓  Safe Link Example",32,515,11,GREEN,Paint.Align.LEFT);txt(c,"https://www.google.com",32,535,8,MUTED,Paint.Align.LEFT);
        }
        void apps(Canvas c){
            top(c,"App Security");tabs(c,new String[]{"Installed Apps","Risk Apps (3)"});
            String[] n={"WhatsApp","Instagram","Facebook","Game Mod APK","Unknown App","Chrome"};int[] co={GREEN,GREEN,GREEN,RED,YELLOW,GREEN};
            for(int i=0;i<n.length;i++){float y=120+i*58;rect(c,12,y,348,y+50,PANEL,12);txt(c,"▣",31,y+30,17,co[i],Paint.Align.CENTER);bold(c,n[i],55,y+20,11,WHITE,Paint.Align.LEFT);txt(c,i==3?"Malware indicators":"No issues found",55,y+37,8,MUTED,Paint.Align.LEFT);badge(c,296,y+15,i==3?"High Risk":(i==4?"Suspicious":"Safe"),co[i]);}
            button(c,"scanapps","⌕  SCAN APPS",75,500,285,544,CYAN);
        }
        void apk(Canvas c){
            top(c,"APK Guard");rect(c,12,70,348,130,Color.rgb(45,5,10),14);stroke(c,12,70,348,130,RED,14);txt(c,"⚠",35,106,25,RED,Paint.Align.CENTER);bold(c,"Third-Party App Installation",65,98,12,RED,Paint.Align.LEFT);txt(c,"Risk Detected",65,116,10,WHITE,Paint.Align.LEFT);
            card(c,145,90,RED,"CoolGame.apk","Version 1.2.3  •  Size 42 MB\nFrom: Unknown Source");
            rect(c,15,250,345,430,PANEL,16);stroke(c,15,250,345,430,RED,16);bold(c,"AI Analysis",30,280,14,RED,Paint.Align.LEFT);txt(c,"⚠ Not from Google Play Store",30,310,10,WHITE,Paint.Align.LEFT);txt(c,"⚠ Requests dangerous permissions",30,335,10,WHITE,Paint.Align.LEFT);txt(c,"⚠ Known bad behavior indicators",30,360,10,WHITE,Paint.Align.LEFT);txt(c,"⚠ Signature not verified",30,385,10,WHITE,Paint.Align.LEFT);bold(c,"Risk Level",30,415,10,WHITE,Paint.Align.LEFT);badge(c,105,400,"HIGH",RED);
            button(c,"scanapk","⌕  SCAN APK",65,450,295,494,GREEN);button(c,"cancelapk","CANCEL INSTALLATION",15,510,170,554,RED);button(c,"continueapk","CONTINUE ANYWAY",185,510,345,554,Color.LTGRAY);
        }
        void wifi(Canvas c){
            top(c,"Wi-Fi Security");txt(c,"◉",180,175,76,RED,Paint.Align.CENTER);bold(c,"Unsafe Network",180,220,20,RED,Paint.Align.CENTER);txt(c,"Public_WiFi_Free",180,242,11,WHITE,Paint.Align.CENTER);txt(c,"This network is not encrypted.",180,263,10,RED,Paint.Align.CENTER);
            rect(c,15,285,345,420,PANEL,16);bold(c,"Risks:",30,315,12,WHITE,Paint.Align.LEFT);txt(c,"• Data interception",30,340,10,WHITE,Paint.Align.LEFT);txt(c,"• Password theft",30,363,10,WHITE,Paint.Align.LEFT);txt(c,"• Man-in-the-middle attack",30,386,10,WHITE,Paint.Align.LEFT);
            button(c,"disconnect","⌁  DISCONNECT",60,445,300,488,GREEN);button(c,"home","USE VPN",60,500,300,543,Color.LTGRAY);
        }
        void calls(Canvas c){
            top(c,"Call & Contact Protection");rect(c,12,72,348,170,PANEL,16);txt(c,"☎",35,120,28,GREEN,Paint.Align.CENTER);bold(c,"Incoming Call",72,104,12,WHITE,Paint.Align.LEFT);txt(c,"+91 94236 56789",72,128,13,WHITE,Paint.Align.LEFT);badge(c,260,102,"Possible Spam",RED);txt(c,"Fraud Risk",30,154,10,RED,Paint.Align.LEFT);
            button(c,"blockcall","BLOCK",20,190,125,232,RED);button(c,"reportcall","REPORT",135,190,235,232,CYAN);button(c,"allowcall","ALLOW",245,190,340,232,GREEN);
            card(c,260,180,Color.rgb(20,90,75),"Recent Calls","+91 94236 56789   Spam\nBank   Safe\nUnknown   Suspicious");
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
            top(c,"Security Reports");
            rect(c,15,72,345,205,PANEL2,18);stroke(c,15,72,345,205,CYAN,18);
            bold(c,"LATEST LOCAL AUDIT",30,101,10,MUTED,Paint.Align.LEFT);
            bold(c,lastScanTime,30,126,18,WHITE,Paint.Align.LEFT);
            bold(c,String.valueOf(lastCheckedApps),72,165,28,CYAN,Paint.Align.CENTER);
            txt(c,"apps checked",72,185,8,MUTED,Paint.Align.CENTER);
            bold(c,String.valueOf(lastRiskApps),180,165,28,lastRiskApps>0?YELLOW:GREEN,Paint.Align.CENTER);
            txt(c,"risk indicators",180,185,8,MUTED,Paint.Align.CENTER);
            bold(c,networkStatus().startsWith("No active")?"OFF":"ON",288,165,24,GREEN,Paint.Align.CENTER);
            txt(c,"network",288,185,8,MUTED,Paint.Align.CENTER);
            card(c,225,82,GREEN,"What this report means","These are local checks, not a certified malware verdict.");
            card(c,320,82,CYAN,"Next actions","Inspect APKs, links and apps individually for more detail.");
            button(c,"startscan","RUN AUDIT AGAIN",60,425,300,470,GREEN);
        }

        void ai(Canvas c){
            top(c,"AI Security Assistant");rect(c,15,72,345,170,PANEL,18);txt(c,"◉",180,115,40,PURPLE,Paint.Align.CENTER);bold(c,"CyberShield AI",180,142,16,WHITE,Paint.Align.CENTER);txt(c,"Ask about a security alert, link or app.",180,160,9,MUTED,Paint.Align.CENTER);
            String[] q={"Is this message safe?","Check this link for phishing","Is this APK risky?","Explain this security alert"};
            for(int i=0;i<q.length;i++)button(c,"q"+i,q[i],25,195+i*58,335,238+i*58,PURPLE);
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
            else if(id.equals("apk"))screen="apk";
            else if(id.equals("messages"))screen="messages";
            else if(id.equals("email"))screen="email";
            else if(id.equals("link")){screen="link";linkCheck();}
            else if(id.equals("apps")){screen="apps";realAppCheck();}
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
            else if(id.equals("blockcall"))info("Call Guard","Caller blocked in this demo. Real call blocking requires supported Android APIs.");
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