package com.cybershield.ai;

import android.content.Context;
import android.net.Uri;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Locale;

public final class FileScanner{
    private FileScanner(){}
    public static String scan(Context c, Uri uri){
        try{
            String name=uri.getLastPathSegment();
            if(name==null) name="Selected file";
            long size=0;
            MessageDigest md=MessageDigest.getInstance("SHA-256");
            try(InputStream in=c.getContentResolver().openInputStream(uri)){
                if(in==null)return "Unable to open selected file.";
                byte[] buf=new byte[8192]; int n;
                while((n=in.read(buf))!=-1){md.update(buf,0,n);size+=n;}
            }
            StringBuilder hash=new StringBuilder();
            for(byte b:md.digest())hash.append(String.format(Locale.US,"%02x",b));
            String l=name.toLowerCase(Locale.ROOT);
            boolean suspicious=l.contains("crack")||l.contains("hack")||l.contains("keygen")||l.contains("payload")||l.contains("modded")||l.endsWith(".exe")||l.endsWith(".bat")||l.endsWith(".cmd");
            return "FILE SECURITY REPORT\n\nName: "+name+"\nSize: "+size+" bytes\nSHA-256: "+hash+
                "\nFilename indicator: "+(suspicious?"REVIEW":"NONE")+
                "\n\nNo file content malware verdict is claimed. Hashes and filename heuristics are indicators only.";
        }catch(Exception e){return "File scan failed safely: "+e.getMessage();}
    }
}