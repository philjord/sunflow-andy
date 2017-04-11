package org.sunflow.image.readers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.sunflow.image.Bitmap;
import org.sunflow.image.BitmapReader;
import org.sunflow.image.Color;
import org.sunflow.image.formats.BitmapRGBA8;

import javaawt.Graphics2D;
import javaawt.image.BufferedImage;
import javaawt.imageio.ImageIO;

public class PNGBitmapReader
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
        URL  localObject3 = localJarURLConnection.getJarFileURL();
        if (localObject3.getProtocol().equalsIgnoreCase("file"))
          try
          {
            if (new File(localObject3.toURI()).canWrite())
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
    int i = localObject2.getTransparency() == 1 ? 1 : 0;
    if ((localObject2.getType() != 1) && (localObject2.getType() != 2))
    {
        localObject2 = new BufferedImage(localObject2.getWidth(), localObject2.getHeight(), i != 0 ? 1 : 2);
      Graphics2D localGraphics2D = (Graphics2D)localObject2.getGraphics();
      localGraphics2D.drawImage(localObject2, null, 0, 0);
      localGraphics2D.dispose();
     
    }
    int[] localObject3 = localObject2.getRaster().getDataElements(0, 0, localObject2.getWidth(), localObject2.getHeight(), null);
    int j = localObject2.getWidth();
    int k = localObject2.getHeight();
    byte[] arrayOfByte = new byte[4 * j * k];
    int m = 0;
    int n = 0;
    while (m < k)
    {
      int i1 = 0;
      while (i1 < j)
      {
        int i2 = localObject3[(i1 + (k - 1 - m) * j)];
        arrayOfByte[(n + 0)] = (byte)(i2 >> 16);
        arrayOfByte[(n + 1)] = (byte)(i2 >> 8);
        arrayOfByte[(n + 2)] = (byte)i2;
        arrayOfByte[(n + 3)] = (i != 0 ? -1 : (byte)(i2 >> 24));
        i1++;
        n += 4;
      }
      m++;
    }
    if (!paramBoolean)
      for (m = 0; m < arrayOfByte.length; m += 4)
      {
        arrayOfByte[(m + 0)] = Color.NATIVE_SPACE.rgbToLinear(arrayOfByte[(m + 0)]);
        arrayOfByte[(m + 1)] = Color.NATIVE_SPACE.rgbToLinear(arrayOfByte[(m + 1)]);
        arrayOfByte[(m + 2)] = Color.NATIVE_SPACE.rgbToLinear(arrayOfByte[(m + 2)]);
      }
    return new BitmapRGBA8(j, k, arrayOfByte);
  }
}