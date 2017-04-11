package org.sunflow.image.readers;

import javaawt.Graphics2D;
import javaawt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import javaawt.imageio.ImageIO;

import org.sunflow.image.Bitmap;
import org.sunflow.image.BitmapReader;
import org.sunflow.image.Color;
import org.sunflow.image.formats.BitmapRGB8;

public class JPGBitmapReader
  implements BitmapReader
{
  public Bitmap load(String paramString, boolean paramBoolean)
    throws IOException, BitmapReader.BitmapFormatException
  {
    URL localObject4;
    InputStream localObject1;
    try
    {
      URLConnection localURLConnection = new URL(paramString).openConnection();
      if ((localURLConnection instanceof JarURLConnection))
      {
        localObject4 = ((JarURLConnection)localURLConnection).getJarFileURL();
        if (localObject4.getProtocol().equalsIgnoreCase("file"))
          try
          {
            if (new File(localObject4.toURI()).canWrite())
              localURLConnection.setUseCaches(false);
          }
          catch (URISyntaxException localURISyntaxException)
          {
            throw new IOException(localURISyntaxException);
          }
      }
      localObject1 = localURLConnection.getInputStream();
    }
    catch (MalformedURLException localMalformedURLException)
    {
      localObject1 = new FileInputStream(paramString);
    }
    BufferedImage localObject2;
    try
    {
      localObject2 = ImageIO.read(localObject1);
    }
    finally
    {
      localObject1.close();
    }
    if (localObject2.getType() != BufferedImage.TYPE_INT_ARGB)
    {        
        BufferedImage  localObject3 = new BufferedImage(localObject2.getWidth(), localObject2.getHeight(), BufferedImage.TYPE_INT_ARGB);//i != 0 ? 1 : 2);
        Graphics2D localObject5 = (Graphics2D) localObject3.getGraphics();
      localObject5.drawImage(localObject2, null, 0, 0);
      localObject5.dispose();
      localObject2 = localObject3;
       
    }
    int[] localObject3 = localObject2.getRaster().getDataElements(0, 0, localObject2.getWidth(), localObject2.getHeight(), null);
    int i = localObject2.getWidth();
    int j = localObject2.getHeight();
    byte[] arrayOfByte = new byte[3 * i * j];
    int k = 0;
    int m = 0;
    while (k < j)
    {
      int n = 0;
      while (n < i)
      {
        int i1 = localObject3[(n + (j - 1 - k) * i)];
        //PJ 0<->2 swapped for BitMap on android
        arrayOfByte[(m + 2)] = (byte)(i1 >> 16);
        arrayOfByte[(m + 1)] = (byte)(i1 >> 8);
        arrayOfByte[(m + 0)] = (byte)i1;
        //PJ interestingly alpha is ignored
        n++;
        m += 3;
      }
      k++;
    }
    if (!paramBoolean)
      for (k = 0; k < arrayOfByte.length; k += 3)
      {
        arrayOfByte[(k + 0)] = Color.NATIVE_SPACE.rgbToLinear(arrayOfByte[(k + 0)]);
        arrayOfByte[(k + 1)] = Color.NATIVE_SPACE.rgbToLinear(arrayOfByte[(k + 1)]);
        arrayOfByte[(k + 2)] = Color.NATIVE_SPACE.rgbToLinear(arrayOfByte[(k + 2)]);
      }
    return new BitmapRGB8(i, j, arrayOfByte);
  }
}