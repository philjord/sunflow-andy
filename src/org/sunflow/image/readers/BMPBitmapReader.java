package org.sunflow.image.readers;

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

public class BMPBitmapReader
  implements BitmapReader
{
  public Bitmap load(String paramString, boolean paramBoolean)
    throws IOException, BitmapReader.BitmapFormatException
  {
      InputStream localObject1;
    try
    {
      URLConnection localURLConnection = new URL(paramString).openConnection();
      if ((localURLConnection instanceof JarURLConnection))
      {
        JarURLConnection localJarURLConnection = (JarURLConnection)localURLConnection;
        URL localURL = localJarURLConnection.getJarFileURL();
        if (localURL.getProtocol().equalsIgnoreCase("file"))
          try
          {
            if (new File(localURL.toURI()).canWrite())
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
    BufferedImage localBufferedImage;
    try
    {
      localBufferedImage = ImageIO.read( localObject1);
    }
    finally
    {
      localObject1.close();
    }
    int i = localBufferedImage.getWidth();
    int j = localBufferedImage.getHeight();
    byte[] arrayOfByte = new byte[3 * i * j];
    int k = 0;
    int m = 0;
    while (k < j)
    {
      int n = 0;
      while (n < i)
      {
        int i1 = localBufferedImage.getRGB(n, j - 1 - k);
        arrayOfByte[(m + 0)] = (byte)(i1 >> 16);
        arrayOfByte[(m + 1)] = (byte)(i1 >> 8);
        arrayOfByte[(m + 2)] = (byte)i1;
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
    return  new BitmapRGB8(i, j, arrayOfByte);
  }
}