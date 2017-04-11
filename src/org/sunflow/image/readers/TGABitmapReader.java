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
import org.sunflow.image.Color;
import org.sunflow.image.formats.BitmapG8;
import org.sunflow.image.formats.BitmapRGB8;
import org.sunflow.image.formats.BitmapRGBA8;

public class TGABitmapReader
  implements BitmapReader
{
  private static final int[] CHANNEL_INDEX = { 2, 1, 0, 3 };

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
    byte[] arrayOfByte1 = new byte[4];
    int i = localObject.read();
    int j = localObject.read();
    if (j != 0)
      throw new BitmapReader.BitmapFormatException(String.format("Colormapping (type: %d) is unsupported", new Object[] { Integer.valueOf(j) }));
    int k = localObject.read();
    localObject.read();
    localObject.read();
    localObject.read();
    localObject.read();
    localObject.read();
    localObject.read();
    localObject.read();
    localObject.read();
    localObject.read();
    int m = localObject.read();
    m |= localObject.read() << 8;
    int n = localObject.read();
    n |= localObject.read() << 8;
    int i1 = localObject.read();
    int i2 = i1 / 8;
    int i3 = localObject.read();
    if (i != 0)
      localObject.skip(i);
    byte[] arrayOfByte2 = new byte[m * n * i2];
    int i4;
    int i5;
    int i6;
    int i7;
    int i8;
    if ((k == 2) || (k == 3))
    {
      if ((i2 != 1) && (i2 != 3) && (i2 != 4))
        throw new BitmapReader.BitmapFormatException(String.format("Invalid bit depth in uncompressed TGA: %d", new Object[] { Integer.valueOf(i1) }));
      i4 = 0;
      while (i4 < arrayOfByte2.length)
      {
        localObject.read(arrayOfByte1, 0, i2);
        for (i5 = 0; i5 < i2; i5++)
          arrayOfByte2[(i4 + CHANNEL_INDEX[i5])] = arrayOfByte1[i5];
        i4 += i2;
      }
    }
    else if (k == 10)
    {
      if ((i2 != 3) && (i2 != 4))
        throw new BitmapReader.BitmapFormatException(String.format("Invalid bit depth in run-length encoded TGA: %d", new Object[] { Integer.valueOf(i1) }));
      i4 = 0;
      while (i4 < arrayOfByte2.length)
      {
        i5 = localObject.read();
        i6 = 1 + (i5 & 0x7F);
        if ((i5 & 0x80) != 0)
        {
          localObject.read(arrayOfByte1, 0, i2);
          for (i7 = 0; i7 < i6; i7++)
          {
            for (i8 = 0; i8 < i2; i8++)
              arrayOfByte2[(i4 + CHANNEL_INDEX[i8])] = arrayOfByte1[i8];
            i4 += i2;
          }
        }
        else
        {
          for (i7 = 0; i7 < i6; i7++)
          {
            localObject.read(arrayOfByte1, 0, i2);
            for (i8 = 0; i8 < i2; i8++)
              arrayOfByte2[(i4 + CHANNEL_INDEX[i8])] = arrayOfByte1[i8];
            i4 += i2;
          }
        }
      }
    }
    else
    {
      throw new BitmapReader.BitmapFormatException(String.format("Unsupported TGA image type: %d", new Object[] { Integer.valueOf(k) }));
    }
    if (!paramBoolean)
    {
      i4 = 0;
      while (i4 < arrayOfByte2.length)
      {
        for (i5 = 0; (i5 < 3) && (i5 < i2); i5++)
          arrayOfByte2[(i4 + i5)] = Color.NATIVE_SPACE.rgbToLinear(arrayOfByte2[(i4 + i5)]);
        i4 += i2;
      }
    }
    if ((i3 & 0x20) == 32)
    {
      i4 = 0;
      i5 = 0;
      while (i4 < n / 2)
      {
        i6 = i2 * (n - i4 - 1) * m;
        for (i7 = 0; i7 < m; i7++)
        {
          for (i8 = 0; i8 < i2; i8++)
          {
            int i9 = arrayOfByte2[(i5 + i8)];
            arrayOfByte2[(i5 + i8)] = arrayOfByte2[(i6 + i8)];
            arrayOfByte2[(i6 + i8)] = (byte) i9;
          }
          i5 += i2;
          i6 += i2;
        }
        i4++;
      }
    }
    localObject.close();
    switch (i2)
    {
    case 1:
      return new BitmapG8(m, n, arrayOfByte2);
    case 3:
      return new BitmapRGB8(m, n, arrayOfByte2);
    case 4:
      return new BitmapRGBA8(m, n, arrayOfByte2);
    case 2:
    }
    throw new BitmapReader.BitmapFormatException("Inconsistent code in TGA reader");
  }
}