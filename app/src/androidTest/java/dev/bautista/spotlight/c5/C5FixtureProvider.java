package dev.bautista.spotlight.c5;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.*;
import android.provider.DocumentsContract;
import android.provider.DocumentsProvider;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Test APK only. SDK/Java-only so the provider can run outside the instrumented target process. */
public final class C5FixtureProvider extends DocumentsProvider {
    public static final String AUTHORITY="dev.bautista.spotlight.c5.physicalfixtures";
    private final List<String> names=Arrays.asList("doc-01.txt","real-three-pages.pdf","ocr-raster-fixture.pdf");
    private final AtomicInteger writers=new AtomicInteger();
    private volatile long delayMillis=0;
    private volatile boolean unavailable=false;
    private File root(){File root=new File(getContext().getFilesDir(),"c5-controlled-fixtures");root.mkdirs();return root;}
    private android.net.Uri tree(){return DocumentsContract.buildTreeDocumentUri(AUTHORITY,"root");}
    @Override public boolean onCreate(){try{reset();return true;}catch(IOException e){throw new IllegalStateException(e);}}
    private void reset() throws IOException {
        try(FileOutputStream out=new FileOutputStream(new File(root(),"doc-01.txt"))){out.write("El presupuesto aprobado del proyecto es de 2400 pesos.".getBytes(StandardCharsets.UTF_8));}
        for(String name:names)if(name.endsWith(".pdf")){
            try(InputStream in=getContext().getAssets().open("c5/"+name);OutputStream out=new FileOutputStream(new File(root(),name))){
                byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);
            }
        }
        delayMillis=0;unavailable=false;
    }
    private Cursor row(Map<String,Object> values,String[] projection,boolean include) {
        String[] cols=projection==null?values.keySet().toArray(new String[0]):projection;
        MatrixCursor cursor=new MatrixCursor(cols);
        if(include){Object[] row=new Object[cols.length];for(int i=0;i<cols.length;i++)row[i]=values.get(cols[i]);cursor.addRow(row);}
        return cursor;
    }
    @Override public Cursor queryRoots(String[] projection){
        Map<String,Object> v=new LinkedHashMap<>();
        v.put(DocumentsContract.Root.COLUMN_ROOT_ID,"c5-root");v.put(DocumentsContract.Root.COLUMN_DOCUMENT_ID,"root");
        v.put(DocumentsContract.Root.COLUMN_TITLE,"SPOTLIGHT C5 · SOLO FIXTURES");
        v.put(DocumentsContract.Root.COLUMN_SUMMARY,"Sólo archivos ficticios de prueba");
        v.put(DocumentsContract.Root.COLUMN_ICON,android.R.drawable.ic_menu_agenda);
        v.put(DocumentsContract.Root.COLUMN_MIME_TYPES,"*/*");
        v.put(DocumentsContract.Root.COLUMN_AVAILABLE_BYTES,root().getFreeSpace());
        v.put(DocumentsContract.Root.COLUMN_FLAGS,DocumentsContract.Root.FLAG_LOCAL_ONLY|DocumentsContract.Root.FLAG_SUPPORTS_IS_CHILD);
        return row(v,projection,true);
    }
    @Override public Cursor queryDocument(String id,String[] projection) throws FileNotFoundException {
        if(!id.equals("root")&&!names.contains(id))throw new FileNotFoundException("Unknown fixture");
        File f=id.equals("root")?root():new File(root(),id);
        Map<String,Object> v=new LinkedHashMap<>();
        v.put(DocumentsContract.Document.COLUMN_DOCUMENT_ID,id);
        v.put(DocumentsContract.Document.COLUMN_DISPLAY_NAME,id.equals("root")?"C5 documentos ficticios":id);
        v.put(DocumentsContract.Document.COLUMN_MIME_TYPE,id.equals("root")?DocumentsContract.Document.MIME_TYPE_DIR:id.endsWith(".pdf")?"application/pdf":"text/plain");
        v.put(DocumentsContract.Document.COLUMN_SIZE,f.length());v.put(DocumentsContract.Document.COLUMN_LAST_MODIFIED,f.lastModified());
        v.put(DocumentsContract.Document.COLUMN_FLAGS,0);
        return row(v,projection,f.exists());
    }
    @Override public Cursor queryChildDocuments(String parent,String[] projection,String order) throws FileNotFoundException {
        if(!parent.equals("root"))throw new FileNotFoundException("Unknown fixture root");
        MatrixCursor result;
        try(Cursor sample=queryDocument("doc-01.txt",projection)){result=new MatrixCursor(sample.getColumnNames());}
        for(String id:names)try(Cursor c=queryDocument(id,projection)){
            if(c.moveToFirst()){Object[] row=new Object[c.getColumnCount()];for(int i=0;i<row.length;i++)row[i]=c.getType(i)==Cursor.FIELD_TYPE_INTEGER?c.getLong(i):c.getString(i);result.addRow(row);}
        }
        return result;
    }
    @Override public boolean isChildDocument(String parent,String id){return parent.equals("root")&&names.contains(id);}
    @Override public ParcelFileDescriptor openDocument(String id,String mode,CancellationSignal signal) throws FileNotFoundException {
        if(!names.contains(id)||!mode.equals("r"))throw new FileNotFoundException("Read-only fixed fixture");
        if(signal!=null)signal.throwIfCanceled();
        if(unavailable)throw new FileNotFoundException("Controlled fixture read unavailable");
        File file=new File(root(),id);
        if(delayMillis==0)return ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY);
        try{
            final byte[] bytes=Files.readAllBytes(file.toPath());final long wait=Math.min(3000,Math.max(1,delayMillis));
            final ParcelFileDescriptor[] pipe=ParcelFileDescriptor.createReliablePipe();writers.incrementAndGet();
            Thread thread=new Thread(()->{
                try(OutputStream out=new ParcelFileDescriptor.AutoCloseOutputStream(pipe[1])){Thread.sleep(wait);out.write(bytes);}
                catch(Exception ignored){}finally{writers.decrementAndGet();}
            },"c5-fixture-bounded-writer");
            thread.setDaemon(true);thread.start();return pipe[0];
        }catch(IOException e){throw new FileNotFoundException(e.toString());}
    }
    @Override public Bundle call(String method,String arg,Bundle extras) {
        if(!method.startsWith("c5:"))return super.call(method,arg,extras);
        if(getContext().checkUriPermission(tree(),Binder.getCallingPid(),Binder.getCallingUid(),Intent.FLAG_GRANT_READ_URI_PERMISSION)!=PackageManager.PERMISSION_GRANTED)throw new SecurityException("Fixture tree read grant required");
        try{
            switch(method){
                case "c5:reset":reset();break;
                case "c5:block":delayMillis=Math.min(3000,Math.max(0,extras==null?0:extras.getLong("millis")));break;
                case "c5:replace":try(FileOutputStream out=new FileOutputStream(new File(root(),"doc-01.txt"))){out.write("El presupuesto aprobado del proyecto es de 9999 pesos.".getBytes(StandardCharsets.UTF_8));}break;
                case "c5:delete":new File(root(),"doc-01.txt").delete();break;
                case "c5:unavailable":unavailable=extras!=null&&extras.getBoolean("enabled");break;
                case "c5:status":break;
                default:throw new IllegalArgumentException("Unknown fixture command");
            }
        }catch(IOException e){throw new IllegalStateException(e);}
        if(Arrays.asList("c5:reset","c5:replace","c5:delete","c5:unavailable").contains(method))getContext().getContentResolver().notifyChange(tree(),null);
        Bundle result=new Bundle();result.putInt("writers",writers.get());result.putLong("delay_ms",delayMillis);return result;
    }
}
