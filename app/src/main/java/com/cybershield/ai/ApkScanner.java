package com.cybershield.ai;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import java.io.*;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ApkScanner{
    private ApkScanner(){}
    public static String scan(Context c,Uri uri){
        File f=null;try{
            f=File.createTempFile("cybershield-",".apk",c.getCacheDir());MessageDigest md=MessageDigest.getInstance("SHA-256");long size=0;
            try(InputStream in=c.getContentResolver().openInputStream(uri);OutputStream out=new FileOutputStream(f)){if(in==null)return"Unable to open selected file.";byte[] z=new byte[8192];int n;while((n=in.read(z))!=-1){out.write(z,0,n);md.update(z,0,n);size+=n;}}
            int dex=0,libs=0,susp=0;try(ZipInputStream zin=new ZipInputStream(new FileInputStream(f))){ZipEntry e;while((e=zin.getNextEntry())!=null){String n=e.getName().toLowerCase(Locale.ROOT);if(n.endsWith(".dex"))dex++;if(n.endsWith(".so"))libs++;if(n.contains("crack")||n.contains("hack")||n.contains("keygen")||n.contains("payload"))susp++;}}
            StringBuilder h=new StringBuilder();for(byte b:md.digest())h.append(String.format(Locale.US,"%02x",b));
            PackageManager pm=c.getPackageManager();int flags=PackageManager.GET_PERMISSIONS;if(Build.VERSION.SDK_INT>=28)flags|=PackageManager.GET_SIGNING_CERTIFICATES;PackageInfo pi=pm.getPackageArchiveInfo(f.getAbsolutePath(),flags);
            StringBuilder r=new StringBuilder("APK SECURITY REPORT\n\n");r.append("File size: ").append(size).append(" bytes\nSHA-256: ").append(h).append("\nDEX files: ").append(dex).append("\nNative libraries: ").append(libs).append("\nSuspicious filename indicators: ").append(susp).append("\n\n");
            if(pi!=null){r.append("Package: ").append(pi.packageName).append("\nVersion: ").append(pi.versionName==null?"unknown":pi.versionName).append("\n\nRequested permissions:\n");if(pi.requestedPermissions==null||pi.requestedPermissions.length==0)r.append("• None reported\n");else for(String q:pi.requestedPermissions)r.append("• ").append(q.substring(q.lastIndexOf('.')+1)).append("\n");
                if(Build.VERSION.SDK_INT>=28&&pi.signingInfo!=null){Signature[] s=pi.signingInfo.hasMultipleSigners()?pi.signingInfo.getApkContentsSigners():pi.signingInfo.getSigningCertificateHistory();if(s!=null&&s.length>0){MessageDigest cd=MessageDigest.getInstance("SHA-256");r.append("\nCertificate SHA-256: ");for(byte b:cd.digest(s[0].toByteArray()))r.append(String.format(Locale.US,"%02x",b));r.append("\n");}}
            }else r.append("Could not parse APK metadata.\n");
            r.append("\nImportant: metadata, hashes and heuristics cannot by themselves prove that an APK is malware.");return r.toString();
        }catch(Exception e){return"APK scan failed safely: "+e.getMessage();}finally{if(f!=null)f.delete();}
    }
}