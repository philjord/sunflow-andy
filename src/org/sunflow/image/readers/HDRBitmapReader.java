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
import org.sunflow.image.formats.BitmapRGBE;

public class HDRBitmapReader
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
    int i = 0;
    int j = 0;
    int k = 0;
    int m = 0;
    int i1;
    for (int n = 0; (k == 0) || (m == 0) || (n != 10); n = i1)
    {
      i1 = localObject.read();
      switch (i1)
      {
      case 89:
        j = n == 45 ? 1 : 0;
        i = 0;
        break;
      case 88:
        j = 0;
        i = n == 43 ? 1 : 0;
        break;
      case 32:
        i &= (k == 0 ? 1 : 0);
        j &= (m == 0 ? 1 : 0);
        break;
      case 48:
      case 49:
      case 50:
      case 51:
      case 52:
      case 53:
      case 54:
      case 55:
      case 56:
      case 57:
        if (j != 0)
        {
          m = 10 * m + (i1 - 48);
        }
        else
        {
          if (i == 0)
            continue;
          k = 10 * k + (i1 - 48);
        }
        break;
      default:
        i = j = 0;
      }
    }
    int[] arrayOfInt1 = new int[k * m];
    int i5;
    int i6;
    int i7;
    if ((k < 8) || (k > 32767))
    {
      readFlatRGBE(localObject, 0, k * m, arrayOfInt1);
    }
    else
    {
     int  i2 = 0;
     int  i3 = m;
      int[] arrayOfInt2 = new int[4 * k];
      while (i3 > 0)
      {
        i5 = localObject.read();
        i6 = localObject.read();
        i7 = localObject.read();
        int i8 = localObject.read();
        if ((i5 != 2) || (i6 != 2) || ((i7 & 0x80) != 0))
        {
          arrayOfInt1[i2] = (i5 << 24 | i6 << 16 | i7 << 8 | i8);
          readFlatRGBE(localObject, i2 + 1, k * i3 - 1, arrayOfInt1);
          break;
        }
        if ((i7 << 8 | i8) != k)
          throw new BitmapReader.BitmapFormatException("Invalid scanline width");
        int i9 = 0;
        for (int i10 = 0; i10 < 4; i10++)
        {
          if (i9 % k != 0)
            throw new BitmapReader.BitmapFormatException("Unaligned access to scanline data");
          int i11 = (i10 + 1) * k;
          while (i9 < i11)
          {
            int i12 = localObject.read();
            int i13 = localObject.read();
            int i14;
            if (i12 > 128)
            {
              i14 = i12 - 128;
              if ((i14 == 0) || (i14 > i11 - i9))
                throw new BitmapReader.BitmapFormatException("Bad scanline data - invalid RLE run");
              while (i14-- > 0)
              {
                arrayOfInt2[i9] = i13;
                i9++;
              }
            }
            else
            {
              i14 = i12;
              if ((i14 == 0) || (i14 > i11 - i9))
                throw new BitmapReader.BitmapFormatException("Bad scanline data - invalid count");
              arrayOfInt2[i9] = i13;
              i9++;
              i14--;
              if (i14 > 0)
              {
                for (int i15 = 0; i15 < i14; i15++)
                  arrayOfInt2[(i9 + i15)] = localObject.read();
                i9 += i14;
              }
            }
          }
        }
        for (int i10 = 0; i10 < k; i10++)
        {
          i5 = arrayOfInt2[i10];
          i6 = arrayOfInt2[(i10 + k)];
          i7 = arrayOfInt2[(i10 + 2 * k)];
          i8 = arrayOfInt2[(i10 + 3 * k)];
          arrayOfInt1[i2] = (i5 << 24 | i6 << 16 | i7 << 8 | i8);
          i2++;
        }
        i3--;
      }
    }
    localObject.close();
    int i2 = 0;
    int i3 = 0;
    int i4 = (m - 1) * k;
    while (i2 < m / 2)
    {
      i5 = 0;
      for (i6 = i4; i5 < k; i6++)
      {
        i7 = arrayOfInt1[i3];
        arrayOfInt1[i3] = arrayOfInt1[i6];
        arrayOfInt1[i6] = i7;
        i5++;
        i3++;
      }
      i2++;
      i4 -= k;
    }
    return new BitmapRGBE(k, m, arrayOfInt1);
  }

  private static void readFlatRGBE(InputStream paramInputStream, int paramInt1, int paramInt2, int[] paramArrayOfInt)
    throws IOException
  {
    while (paramInt2-- > 0)
    {
      int i = paramInputStream.read();
      int j = paramInputStream.read();
      int k = paramInputStream.read();
      int m = paramInputStream.read();
      paramArrayOfInt[paramInt1] = (i << 24 | j << 16 | k << 8 | m);
      paramInt1++;
    }
  }
}