package com.example;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.a2ia.data.Boolean;
import com.a2ia.data.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity
{
   public ImageProcessor mIp;
   private AsyncProcessImage mAsyncProcessor;

   /**
    * Called when the activity is first created.
    */
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);

      try
      {
         String country = "US";

         // Get a stream the the engine datas ZIP file in assets folder
         InputStream stream = getAssets().open(country+".zip");

         // Get path to the application data on android device
         String appData = getApplicationContext().getFilesDir().getAbsolutePath();

         // Extract zip files and get the parms folder
         String parmPath = AsyncZipExtract.extractZip(stream, appData, country, false);

         initEngine(parmPath);

         // Launch the reco
         recoCheck();
      }
      catch (Throwable e)
      {
         AlertDialog alertDialog = new AlertDialog.Builder(this).create();
         alertDialog.setTitle("Error");
         alertDialog.setMessage(e.getMessage());
         alertDialog.setButton("OK", new DialogInterface.OnClickListener()
         {
            public void onClick(DialogInterface dialog, int which)
            {
               return;
            }
         });

         alertDialog.show();
      }
   }

   public void initEngine(String parmPath) throws Exception
   {
      // Initialize a new ImageProcessor
      mIp = new ImageProcessor(parmPath);

      // Activate the license
      mIp.SetLicense(""); // To be provided by A2iA before starting the program.
   }

   public void recoCheck() throws Exception
   {
      mIp.PrepareCheckInput();

      String imagePath = "/mnt/sdcard/images/Original.jpg";
      String rearImagePath = "/mnt/sdcard/images/fake.rear.jpg";
      mIp.SetImages(imagePath, rearImagePath);

      /* Process the input with an ASyncTask */
      mAsyncProcessor = (AsyncProcessImage) new AsyncProcessImage(this, mIp).execute("");
   }

   /**
    * Will be called by AsyncProcessImage onPostExecute
    */
   public void showResult(Output output)
   {
      Status st = output.getStatus();
      String stCt = output.getStatusContext();

      String msg = "";

      /* If no error, get the results */
      if (st==Status.OK)
      {
         /* Retrieve check recognition results */
         CheckOutput checkOutput = ((CheckDocumentResults)output.getDocumentResults()).getCheck();
         long amount = checkOutput.getResult().getAmount();
         long score = checkOutput.getResult().getScore();
         String codeline = checkOutput.getCodeline().getResult().getReco();
         long scoreCodeline = checkOutput.getCodeline().getResult().getScore();

         /* retrieve Check invalidity results */
         long invalidityScore = checkOutput.getInvalidity().getInvalidityScore();
         Boolean carInv = checkOutput.getInvalidity().getNoCAR();
         long carInvScore = checkOutput.getInvalidity().getNoCARScore();
         Boolean signatureInv = checkOutput.getInvalidity().getNoSignature();
         long signatureInvScore = checkOutput.getInvalidity().getNoSignatureScore();
         Boolean endorsInv = checkOutput.getInvalidity().getNoPayeeEndorsement();
         long endorsInvScore = checkOutput.getInvalidity().getNoPayeeEndorsementScore();

         /* retrieve Image quality results */
         long imageTooDark = output.getImageQuality().getImageTooDarkFlag();

         msg =
           "Status: "+st+"\n"+
           "Amount: "+amount+", score="+score+"\n"+
           "Codeline: "+codeline+", score="+scoreCodeline+"\n";


         /* Get the preprocessed image */
         MemoryImage img = (MemoryImage) output.getPreprocessedImage();
         byte[] bytes = img.getBuffer();


         try
         {
            // opens an output stream to save into file
            String storagePath = Environment.getExternalStorageDirectory().getPath();
            File fileImage = new File(storagePath+"/preprocessed_front.tif");
            FileOutputStream outputImage = new FileOutputStream(fileImage);
            outputImage.write(bytes);
            outputImage.close();
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }

         /* get the located document image */
         MemoryImage img2 = (MemoryImage) output.getLocatedDocumentImage();
         byte[] bytes2 = img2.getBuffer();
         Bitmap locatedImg = BitmapFactory.decodeByteArray(bytes2, 0, bytes2.length);

         try
         {
            // opens an output stream to save into file
            String storagePath = Environment.getExternalStorageDirectory().getPath();
            File fileImage = new File(storagePath+"/located_front.jpg");
            FileOutputStream outputImage = new FileOutputStream(fileImage);
            outputImage.write(bytes2);
            outputImage.close();
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }

         /* get the located document image */
         if (output.getAdditionalLocatedDocumentPageItemsCount()>0)
         {
            MemoryImage rearImg = (MemoryImage)output.getAdditionalLocatedDocumentPageItem(0);
            byte[] bytesRear = rearImg.getBuffer();

            try
            {
               // opens an output stream to save into file
               String storagePath = Environment.getExternalStorageDirectory().getPath();
               File fileImage = new File(storagePath+"/located_rear.jpg");
               FileOutputStream outputImage = new FileOutputStream(fileImage);
               outputImage.write(bytesRear);
               outputImage.close();
            }
            catch (IOException e)
            {
               e.printStackTrace();
            }
         }


         /* get the located document image */
         if (output.getAdditionalPreprocessedPageItemsCount()>0)
         {
            MemoryImage rearImg = (MemoryImage)output.getAdditionalPreprocessedPageItem(0);
            byte[] bytesRear = rearImg.getBuffer();

            try
            {
               // opens an output stream to save into file
               String storagePath = Environment.getExternalStorageDirectory().getPath();
               File fileImage = new File(storagePath+"/preprocessed_rear.jpg");
               FileOutputStream outputImage = new FileOutputStream(fileImage);
               outputImage.write(bytesRear);
               outputImage.close();
            }
            catch (IOException e)
            {
               e.printStackTrace();
            }
         }
      }
      else
      {
         msg = stCt;
      }

      AlertDialog alertDialog = new AlertDialog.Builder(this).create();
      alertDialog.setTitle("Results");
      alertDialog.setMessage(msg);
      alertDialog.setButton("OK", new DialogInterface.OnClickListener()
      {
         public void onClick(DialogInterface dialog, int which)
         {
            return;
         }
      });

      alertDialog.show();
   }

   /**
    * Called when doing a publishProgress in AsyncTask
    */
   public void reportMessage(String msg)
   {
      Toast t = Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG);
      TextView v = (TextView) t.getView().findViewById(android.R.id.message);
      t.show();

      TextView resultsView = (TextView) findViewById(R.id.results);
      if (resultsView!=null)
      {
         resultsView.append(msg+"\n");
      }
   }
}
