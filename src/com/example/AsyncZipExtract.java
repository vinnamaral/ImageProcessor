package com.example;

import android.os.AsyncTask;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: fauvelle
 * Date: 22/11/12
 * Time: 15:34
 * To change this template use File | Settings | File Templates.
 */
public class AsyncZipExtract extends AsyncTask<String, String, String>
{
   private final String to;
   private final InputStream from;
   private final boolean clean;
   private final MainActivity activity;
   private String country;

   public AsyncZipExtract(InputStream from, String to, String country, boolean clean, MainActivity activity)
   {
      this.from = from;
      this.to = to;
      this.clean = clean;
      this.activity = activity;
      this.country = country;
   }

   static public String extractZip(InputStream from, String to, String country, boolean clean)
   {
      // Create a directory in the SDCard to store the files
      File file = new File(to);
      String folder = "";

      // Loop through all the files and folders
      try
      {
         folder = to + File.separator + "parms"+File.separator;

         // Check that init for this country as already be done
         File init = new File(folder+country+".ok");
         if (init.exists())
            return folder;

         // Open the ZipInputStream
         ZipInputStream in = new ZipInputStream(from);

         for (ZipEntry entry = in.getNextEntry(); entry != null; entry = in.getNextEntry())
         {
            try
            {
               String leaf = entry.getName();

               // Remove Country part to allow extraction of different countries in same folder "parms"
               int pos = leaf.indexOf("/");
               leaf = "parms"+leaf.substring(pos);

               String innerFileName = to + File.separator + leaf;

               File innerFile = new File(innerFileName);

               // Check if it is a folder
               if (entry.isDirectory())
               {
                  if (innerFile.exists())
                  {
                     if (clean)
                     {
                        innerFile.delete();
                     }
                     else
                     {
                        continue;
                     }
                  }

                  if (!innerFile.exists())
                  {
                     // Its a folder, create that folder
                     innerFile.mkdirs();
                  }
               }
               else
               {
                  // sanity check, create parent folder if not exist, but we should be here
                  if (!innerFile.getParentFile().exists())
                  {
                     innerFile.getParentFile().mkdirs();
                  }

                  if (innerFile.exists())
                  {
                     if (clean)
                     {
                        innerFile.delete();
                     }
                     else
                     {
                        continue;
                     }
                  }

                  // Create a file output stream
                  FileOutputStream outputStream = new FileOutputStream(innerFileName);
                  final int BUFFER = 2048;

                  // Buffer the ouput to the file
                  BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream, BUFFER);

                  // Write the contents
                  int count = 0;
                  byte[] data = new byte[BUFFER];
                  while ((count = in.read(data, 0, BUFFER)) != -1)
                  {
                     bufferedOutputStream.write(data, 0, count);
                  }

                  // Flush and close the buffers
                  bufferedOutputStream.flush();
                  bufferedOutputStream.close();
               }

               // Close the current entry
               in.closeEntry();
            }
            catch (IOException e)
            {
               e.printStackTrace();
            }
         }

         in.close();
      }
      catch (IOException e)
      {
         e.printStackTrace();
         return "";
      }

      // If initialization is ok, put a file saying it's ok so we don't do it again next time
      try
      {
         File init = new File(folder+country+".ok");
         init.createNewFile();
         FileWriter stdoutWriter = new FileWriter(init, true);
         stdoutWriter.write("initialized");
         stdoutWriter.flush();
      }
      catch (IOException e)
      {
      }

      return folder;
   }

   @Override
   protected String doInBackground(String... strings)
   {
      return extractZip(from, to, country, clean);
   }

   @Override
   protected void onPostExecute(String result)
   {
      if (!result.isEmpty())
      {
         try
         {
            activity.initEngine(result);
         }
         catch (Exception e)
         {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
      }
   }
}
