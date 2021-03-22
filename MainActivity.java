package com.example.scrollviewtopdf;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ConstraintLayout rootLayout;
    Button button;
    Bitmap bitmap;
    private String address = Environment.getExternalStorageDirectory() + File.separator + "PDFFromView.pdf";
    List<Bitmap> bmArray;

    String[] permissions = {"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootLayout = findViewById(R.id.root);
        button = findViewById(R.id.btn);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(MainActivity.this, permissions, 101);
                Log.d("size"," "+rootLayout.getWidth() +"  "+rootLayout.getWidth());
                bitmap = loadBitmapFromView(rootLayout, rootLayout.getWidth(), rootLayout.getHeight());
                bmArray = processBitmap(bitmap);
                createPdf(bmArray);
            }
        });


    }


    private List<Bitmap> processBitmap(Bitmap bm){
        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        float height = displaymetrics.heightPixels ;
        float width = displaymetrics.widthPixels ;

        float bmWidth = bm.getWidth();
        float bmHeight = bm.getHeight();

        int k = (int) (bmWidth/width);

        float newWidth = bmWidth;
        float newHeight = k*height;

        ArrayList<Bitmap> array = new ArrayList<>();

        if (bmHeight<height){
            array.add(bitmap);
            return array;
        }else {
            int count = (int) (bm.getHeight()/newHeight+1);
            int x=0, y=0;
            Bitmap b;
            for (int i = 0; i < count; i++) {
                if (i == count-1) {
                    b = Bitmap.createBitmap(bm, x, y, (int) newWidth, (int) (bitmap.getHeight() - (count - 1) * newHeight));
                    Bitmap dupBitmap = Bitmap.createBitmap((int)newWidth, (int)newHeight, Bitmap.Config.ARGB_8888);
                    Canvas c = new Canvas(dupBitmap);
                    c.drawBitmap(b, 0f, 0f, new Paint());
                    Paint paint = new Paint();
                    paint.setColor(Color.WHITE);
                    c.drawRect(0,(bitmap.getHeight() - (count - 1) * newHeight), newWidth, newHeight, paint);
                    b = dupBitmap;
                }
                else
                    b = Bitmap.createBitmap(bm, x, y, (int)newWidth, (int)newHeight);
                y+=newHeight;
                array.add(b);
            }
            return array;
        }
    }


    public static Bitmap loadBitmapFromView(View v, int width, int height) {
        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.draw(c);
        return b;
    }

    private void createPdf(List<Bitmap> bmArray){
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        float height = displaymetrics.heightPixels ;
        float width = displaymetrics.widthPixels ;

        int convertHeight = (int) height, convertWidth = (int) width;

        PdfDocument document = new PdfDocument();

        for (int i = 0; i < bmArray.size(); i++) {
            PdfDocument.PageInfo pageInfo2 = new PdfDocument.PageInfo.Builder(convertWidth, convertHeight, 1).create();
            PdfDocument.Page page2 = document.startPage(pageInfo2);

            Canvas canvas2 = page2.getCanvas();

            Paint paint2 = new Paint();
            canvas2.drawPaint(paint2);
            paint2.setColor(Color.BLUE);

            bitmap = Bitmap.createScaledBitmap(this.bmArray.get(i), convertWidth, convertHeight, true);
            if (i == bmArray.size()-1)
                bitmap = Bitmap.createScaledBitmap(this.bmArray.get(i), convertWidth, bmArray.get(i).getHeight(), true);

            canvas2.drawBitmap(bitmap, 0, 0 , null);
            document.finishPage(page2);
        }


        // write the document content
        File filePath;
        filePath = new File(address);
        try {
            document.writeTo(new FileOutputStream(filePath));

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        // close the document
        document.close();
        Toast.makeText(this, "PDF is created!!!", Toast.LENGTH_SHORT).show();
        openGeneratedPDF();

    }

    private void openGeneratedPDF(){
        File file = new File(address);
        if (file.exists())
        {
            Uri uri = Uri.parse(file.getPath());
            Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    file);
            Intent intent=new Intent(Intent.ACTION_VIEW);

            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(photoURI, "application/pdf");
            try
            {
                startActivity(intent);
            }
            catch(ActivityNotFoundException e)
            {
                Toast.makeText(MainActivity.this, "No Application available to view pdf", Toast.LENGTH_LONG).show();
            }
        }
    }
}