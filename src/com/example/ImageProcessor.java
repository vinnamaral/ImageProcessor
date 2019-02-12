package com.example;

import com.a2ia.Engine;
import com.a2ia.data.*;
import com.a2ia.data.Boolean;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by fauvelle on 27/05/2014.
 */
public class ImageProcessor
{
   private Engine mEngine;
   private Input mInput;

   public ImageProcessor(String params) throws Exception
   {
      CheckEnginesDatPresence(params);
      mEngine = new Engine(params);
      mEngine.activateTraces(0);
   }

   private byte[] LoadFileToBuffer(String imagePath) throws IOException
   {
      // To simulate MemoryImage loading, we load the file inside a byte buffer
      File file = new File(imagePath);
      byte [] fileData = new byte[(int)file.length()];
      DataInputStream dis = new DataInputStream( new FileInputStream(file) );
      dis.readFully(fileData);
      dis.close();
      return fileData;
   }

   public void SetImages(String imagePath, String rearImagePath) throws IOException
   {
      /* Create an Image */
      boolean useFileImage = true; // Set false to use MemoryImage

      if (useFileImage)
      {
         FileImage image = mInput.createFileImage();
         image.setFileName(imagePath);
         image.setImageFormat(ImageFormat.JPEG);
         image.setTransportModel(TransportModel.MobilePhone);

         if (!rearImagePath.isEmpty())
         {
            /* Create the rearImage and add it in additional pages */
            FileImage rearImage = new FileImage();
            rearImage.setFileName(rearImagePath);
            rearImage.setImageFormat(ImageFormat.JPEG);
            rearImage.setTransportModel(TransportModel.MobilePhone);
            mInput.addAdditionalPageItem(rearImage);
         }
      }
      else
      {
         {
            // To simulate MemoryImage loading, we load the file inside a byte buffer
            byte[] fileData = LoadFileToBuffer(imagePath);

            /* Now create a memory image */
            MemoryImage image = mInput.createMemoryImage();
            /* and set the buffer */
            image.setBuffer(fileData);
            image.setImageFormat(ImageFormat.JPEG);
            image.setTransportModel(TransportModel.MobilePhone);
         }

         if (!rearImagePath.isEmpty())
         {
            // To simulate MemoryImage loading, we load the file inside a byte buffer
            byte[] fileData = LoadFileToBuffer(imagePath);

            /* Create the rearImage and add it in additional pages */
            MemoryImage rearImage = new MemoryImage();
            rearImage.setBuffer(fileData);
            rearImage.setImageFormat(ImageFormat.JPEG);
            rearImage.setTransportModel(TransportModel.MobilePhone);
            mInput.addAdditionalPageItem(rearImage);
         }
      }
   }

   public Output ProcessImage()
   {
      return mEngine.process(mInput);
   }

   private void CheckEnginesDatPresence(String enginesDat) throws Exception
   {
      File file = new File(enginesDat);
      if (!file.exists())
      {
         throw new Exception("Check the presence of the file " + enginesDat);
      }
   }

   public void SetLicense(String s) throws Exception
   {
      mEngine.setLicense(s);
   }

   public void PrepareCheckInput() throws IOException
   {
      /** Create an input object that will include imagePreprocessing,
       document recognition settings and image properties  **/
      mInput = new Input();

      /* define Parameters for image quality analysis */
      mInput.getImageQuality().setEnable(Boolean.Yes );
      mInput.getImageQuality().getImageTooDark().setEnable(Boolean.Yes);

      /* Create a check input dedicated for Check processing */
      CheckInput check = mInput.createCheckDocument().getCheck();

      /* Set recognition settings for check processing */
      check.setAmountRecognition(Boolean.Yes); // Activate amount recognition
      check.setCountry(Country.US);            // Define the country
      check.setCurrency(Currency.USD);         // Define the currency

      /* Activate Codeline recognition into Check fields  */
      check.getFieldsInput().getCodeline().setEnable(Boolean.Yes);

      /* define Parameter for invalidity Detection */
      check.setInvalidityDetection(Boolean.Yes);
      check.setRearInvalidityDetection(Boolean.Yes);

      CheckInvalidityDetails invalidityDetails = check.getInvalidityDetails();

      invalidityDetails.setLAR(Boolean.No);
      invalidityDetails.setPayeeName(Boolean.No);
      invalidityDetails.setDate(Boolean.No);
      invalidityDetails.setCodeline(Boolean.No);
      invalidityDetails.setPayorsNameAndAddress(Boolean.No);
      invalidityDetails.setCAR(Boolean.Yes);
      invalidityDetails.setSignature(Boolean.Yes);
      invalidityDetails.setPayeeEndorsement(Boolean.Yes);

      /* Set verbose information to have located document and preprocessed image */
      mInput.setVerbose(Boolean.Yes);
      mInput.getVerboseDetails().setPreprocessedImage(Boolean.Yes);
      mInput.getVerboseDetails().getPreprocessedImageFormat().setOutputFormat(OutputFormat.JPEG);
      mInput.getVerboseDetails().setLocatedDocumentImage(Boolean.Yes);
      mInput.getVerboseDetails().getLocatedDocumentImageFormat().setOutputFormat(OutputFormat.JPEG);
      mInput.getVerboseDetails().getLocatedDocumentImageFormat().setQualityLevel(85);
   }

   public void Clean()
   {
      mEngine.clean();
   }
}
