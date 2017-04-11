package org.sunflow.image.readers;

import java.io.BufferedInputStream;
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
import org.sunflow.image.formats.BitmapXYZ;

public class IGIBitmapReader
  implements BitmapReader
{
  public Bitmap load(String paramString, boolean paramBoolean)
    throws IOException, BitmapReader.BitmapFormatException
  {
      
      InputStream localObject;
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
      localObject = localURLConnection.getInputStream();
    }
    catch (MalformedURLException localMalformedURLException)
    {
      localObject = new FileInputStream(paramString);
    }
      localObject = new BufferedInputStream(localObject);
    int i = read32i(localObject);
    int j = read32i(localObject);
    localObject.skip(8L);
    int k = read32i(localObject);
    int m = read32i(localObject);
    int n = read32i(localObject);
    int i1 = read32i(localObject);
    int i2 = read32i(localObject);
    int i3 = read32i(localObject);
    localObject.skip(5000L);
    if (i != 66613373)
      throw new BitmapReader.BitmapFormatException("wrong magic: " + i);
    if (j != 1)
      throw new BitmapReader.BitmapFormatException("unsupported version: " + j);
    if (i1 != 0)
      throw new BitmapReader.BitmapFormatException("unsupported compression: " + i1);
    if (i3 != 0)
      throw new BitmapReader.BitmapFormatException("unsupported color space: " + i3);
    if (i2 != k * m * 12)
      throw new BitmapReader.BitmapFormatException("invalid data block size: " + i2);
    if ((k <= 0) || (m <= 0))
      throw new BitmapReader.BitmapFormatException("invalid image size: " + k + "x" + m);
    if (n <= 0)
      throw new BitmapReader.BitmapFormatException("invalid super sample factor: " + n);
    if ((k % n != 0) || (m % n != 0))
      throw new BitmapReader.BitmapFormatException("invalid image size: " + k + "x" + m);
    float[] arrayOfFloat1 = new float[k * m * 3];
    int i4 = 0;
    int i5 = 3 * (m - 1) * k;
    int i6;
    while (i4 < m)
    {
      i6 = 0;
      while (i6 < k)
      {
        arrayOfFloat1[(i5 + 0)] = read32f(localObject);
        arrayOfFloat1[(i5 + 1)] = read32f(localObject);
        arrayOfFloat1[(i5 + 2)] = read32f(localObject);
        i6++;
        i5 += 3;
      }
      i4++;
      i5 -= 6 * k;
    }
    localObject.close();
    if (n > 1)
    {
      float[] arrayOfFloat2 = new float[arrayOfFloat1.length / (n * n)];
      float f1 = 1.0F / (n * n);
      i6 = 0;
      int i7 = 0;
      while (i6 < m)
      {
        int i8 = 0;
        while (i8 < k)
        {
          float f2 = 0.0F;
          float f3 = 0.0F;
          float f4 = 0.0F;
          for (int i9 = 0; i9 < n; i9++)
            for (int i10 = 0; i10 < n; i10++)
            {
              int i11 = 3 * (i8 + i10 + (i6 + i9) * k);
              f2 += arrayOfFloat1[(i11 + 0)];
              f3 += arrayOfFloat1[(i11 + 1)];
              f4 += arrayOfFloat1[(i11 + 2)];
            }
          arrayOfFloat2[(i7 + 0)] = (f2 * f1);
          arrayOfFloat2[(i7 + 1)] = (f3 * f1);
          arrayOfFloat2[(i7 + 2)] = (f4 * f1);
          i8 += n;
          i7 += 3;
        }
        i6 += n;
      }
      return new BitmapXYZ(k / n, m / n, arrayOfFloat2);
    }
    return new BitmapXYZ(k, m, arrayOfFloat1);
  }

  private static final int read32i(InputStream paramInputStream)
    throws IOException
  {
    int i = paramInputStream.read();
    i |= paramInputStream.read() << 8;
    i |= paramInputStream.read() << 16;
    i |= paramInputStream.read() << 24;
    return i;
  }

  private static final float read32f(InputStream paramInputStream)
    throws IOException
  {
    return Float.intBitsToFloat(read32i(paramInputStream));
  }
}