package com.example;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.a2ia.data.MemoryImage;
import com.a2ia.data.Output;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author A2iA
 */

public class AsyncProcessImage extends AsyncTask<String, String, Output>
{
   MainActivity mActivity;
   ImageProcessor mIp;

   public AsyncProcessImage(MainActivity p, ImageProcessor ip)
   {
      super();
      mActivity = p;
      mIp = ip;
   }

   @Override
   protected void onPreExecute()
   {
      super.onPreExecute();
      publishProgress("AsyncProcessImage : PreExecute");
   }

   @Override
   protected void onPostExecute(Output result)
   {
      super.onPostExecute(result);

      publishProgress("AsyncProcessImage : PostExecute : showResult");
      // Give result to Activity to handle it, here we'll just display the result
      mActivity.showResult(result);

      publishProgress("AsyncProcessImage : Clean engine");
      mIp.Clean();
   }

   @Override
   protected void onProgressUpdate(String ... v)
   {
      if(v.length > 0)
      {
         mActivity.reportMessage(v[0]);
      }
   }


   @Override
   protected Output doInBackground(String... params)
   {
      publishProgress("AsyncProcessImage : starting ProcessImage");

      Output output;

      try
      {
         output = mIp.ProcessImage();
         com.a2ia.data.Status status = output.getStatus();
         String statusContext = output.getStatusContext();
      }
      catch (Exception e)
      {
         output = handleException(e);
      }

      return output;
   }

   private Output handleException(Exception e)
   {
      String msg = new String();
      if (e.getMessage()!=null)
         msg = e.getMessage();

      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      String exceptionAsString = sw.toString();

      msg += "\n"+exceptionAsString;
      Log.e("a2ia", msg);

      Output output = new Output();
      output.setStatus(com.a2ia.data.Status.Unknown);
      output.setStatusContext(msg);

      return output;
   }
}
