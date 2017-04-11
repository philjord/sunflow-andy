package org.sunflow.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.sunflow.PluginRegistry;
import org.sunflow.image.Bitmap;
import org.sunflow.image.BitmapReader;
import org.sunflow.image.Color;
import org.sunflow.image.formats.BitmapBlack;
import org.sunflow.math.MathUtils;
import org.sunflow.math.OrthoNormalBasis;
import org.sunflow.math.Vector3;
import org.sunflow.system.FileUtils;
import org.sunflow.system.UI;

public class Texture {
    private String filename;
    private boolean isLinear;
    private Bitmap bitmap;
    private int loaded;
    private boolean isTransparent;

    Texture(String paramString, boolean paramBoolean) {
        this.filename = paramString;
        this.isLinear = paramBoolean;
        this.loaded = 0;
    }

    private synchronized void load() {
        if (this.loaded != 0)
            return;
        String str = FileUtils.getExtension(this.filename);
        try {
            UI.printInfo(UI.Module.TEX, "Reading texture bitmap from: \"%s\" ...", new Object[] {
                    this.filename });
            BitmapReader localBitmapReader = PluginRegistry.bitmapReaderPlugins.createObject(str);
            if (localBitmapReader == null)
                try {
                    URL localURL1 = new URL(this.filename);
                    URLConnection localURLConnection = localURL1.openConnection();
                    if ((localURLConnection instanceof JarURLConnection)) {
                        URL localURL2 = ((JarURLConnection) localURLConnection).getJarFileURL();
                        if (localURL2.getProtocol().equalsIgnoreCase("file"))
                            try {
                                if (new File(localURL2.toURI()).canWrite())
                                    localURLConnection.setUseCaches(false);
                            } catch (URISyntaxException localURISyntaxException) {
                                throw new IOException(localURISyntaxException);
                            }
                    }
                    InputStream localObject = localURLConnection.getInputStream();
                    int k = localObject.read();
                    int m = localObject.read();
                    localObject.close();
                    localBitmapReader = (k == 255) && (m == 216) ? (BitmapReader) PluginRegistry.bitmapReaderPlugins.createObject("jpg") : (BitmapReader) PluginRegistry.bitmapReaderPlugins.createObject("png");
                } catch (IOException localIOException2) {
                }
            if (localBitmapReader != null) {
                this.bitmap = localBitmapReader.load(this.filename, this.isLinear);
                if ((this.bitmap.getWidth() == 0) || (this.bitmap.getHeight() == 0))
                    this.bitmap = null;
            }
            for (int i = 0; i < this.bitmap.getWidth(); i++)
                for (int j = 0; j < this.bitmap.getHeight(); j++) {
                    if (this.bitmap.readAlpha(i, j) >= 1.0F)
                        continue;
                    this.isTransparent = true;
                    break;
                }
            if (this.bitmap == null) {
                UI.printError(UI.Module.TEX, "Bitmap reading failed", new Object[0]);
                this.bitmap = new BitmapBlack();
            } else {
                UI.printDetailed(UI.Module.TEX, "Texture bitmap reading complete: %dx%d pixels found", new Object[] {
                        Integer.valueOf(this.bitmap.getWidth()),
                        Integer.valueOf(this.bitmap.getHeight()) });
            }
        } catch (IOException localIOException1) {
            UI.printError(UI.Module.TEX, "%s", new Object[] {
                    localIOException1.getMessage() });
        } catch (BitmapReader.BitmapFormatException localBitmapFormatException) {
            UI.printError(UI.Module.TEX, "%s format error: %s", new Object[] {
                    str, localBitmapFormatException.getMessage() });
        }
        this.loaded = 1;
    }

    public Bitmap getBitmap() {
        if (this.loaded == 0)
            load();
        return this.bitmap;
    }

    public Color getPixel(float paramFloat1, float paramFloat2) {
        Bitmap localBitmap = getBitmap();
        paramFloat1 = MathUtils.frac(paramFloat1);
        paramFloat2 = MathUtils.frac(paramFloat2);
        float f1 = paramFloat1 * (localBitmap.getWidth() - 1);
        float f2 = paramFloat2 * (localBitmap.getHeight() - 1);
        int i = (int) f1;
        int j = (int) f2;
        int k = (i + 1) % localBitmap.getWidth();
        int m = (j + 1) % localBitmap.getHeight();
        float f3 = f1 - i;
        float f4 = f2 - j;
        f3 = f3 * f3 * (3.0F - 2.0F * f3);
        f4 = f4 * f4 * (3.0F - 2.0F * f4);
        float f5 = (1.0F - f3) * (1.0F - f4);
        Color localColor1 = localBitmap.readColor(i, j);
        float f6 = (1.0F - f3) * f4;
        Color localColor2 = localBitmap.readColor(i, m);
        float f7 = f3 * (1.0F - f4);
        Color localColor3 = localBitmap.readColor(k, j);
        float f8 = f3 * f4;
        Color localColor4 = localBitmap.readColor(k, m);
        Color localColor5 = Color.mul(f5, localColor1);
        localColor5.madd(f6, localColor2);
        localColor5.madd(f7, localColor3);
        localColor5.madd(f8, localColor4);
        return localColor5;
    }

    public Color getOpacity(float paramFloat1, float paramFloat2) {
        Bitmap localBitmap = getBitmap();
        paramFloat1 = MathUtils.frac(paramFloat1);
        paramFloat2 = MathUtils.frac(paramFloat2);
        float f1 = paramFloat1 * (localBitmap.getWidth() - 1);
        float f2 = paramFloat2 * (localBitmap.getHeight() - 1);
        int i = (int) f1;
        int j = (int) f2;
        int k = (i + 1) % localBitmap.getWidth();
        int m = (j + 1) % localBitmap.getHeight();
        float f3 = f1 - i;
        float f4 = f2 - j;
        f3 = f3 * f3 * (3.0F - 2.0F * f3);
        f4 = f4 * f4 * (3.0F - 2.0F * f4);
        float f5 = (1.0F - f3) * (1.0F - f4);
        float f6 = localBitmap.readAlpha(i, j);
        float f7 = (1.0F - f3) * f4;
        float f8 = localBitmap.readAlpha(i, m);
        float f9 = f3 * (1.0F - f4);
        float f10 = localBitmap.readAlpha(k, j);
        float f11 = f3 * f4;
        float f12 = localBitmap.readAlpha(k, m);
        float f13 = f5 * f6 + f7 * f8 + f9 * f10 + f11 * f12;
        if (f13 < 0.005D)
            return Color.BLACK;
        if (f13 > 0.995D)
            return Color.WHITE;
        Color localColor1 = localBitmap.readColor(i, j).mul(1.0F - f6);
        Color localColor2 = localBitmap.readColor(i, m).mul(1.0F - f8);
        Color localColor3 = localBitmap.readColor(k, j).mul(1.0F - f10);
        Color localColor4 = localBitmap.readColor(k, m).mul(1.0F - f12);
        Color localColor5 = Color.mul(f5, localColor1);
        localColor5.madd(f7, localColor2);
        localColor5.madd(f9, localColor3);
        localColor5.madd(f11, localColor4);
        return localColor5.opposite();
    }

    public float getOpacityAlpha(float paramFloat1, float paramFloat2) {
        Bitmap localBitmap = getBitmap();
        paramFloat1 = MathUtils.frac(paramFloat1);
        paramFloat2 = MathUtils.frac(paramFloat2);
        float f1 = paramFloat1 * (localBitmap.getWidth() - 1);
        float f2 = paramFloat2 * (localBitmap.getHeight() - 1);
        int i = (int) f1;
        int j = (int) f2;
        int k = (i + 1) % localBitmap.getWidth();
        int m = (j + 1) % localBitmap.getHeight();
        float f3 = f1 - i;
        float f4 = f2 - j;
        f3 = f3 * f3 * (3.0F - 2.0F * f3);
        f4 = f4 * f4 * (3.0F - 2.0F * f4);
        float f5 = (1.0F - f3) * (1.0F - f4);
        float f6 = localBitmap.readAlpha(i, j);
        float f7 = (1.0F - f3) * f4;
        float f8 = localBitmap.readAlpha(i, m);
        float f9 = f3 * (1.0F - f4);
        float f10 = localBitmap.readAlpha(k, j);
        float f11 = f3 * f4;
        float f12 = localBitmap.readAlpha(k, m);
        return f5 * f6 + f7 * f8 + f9 * f10 + f11 * f12;
    }

    public boolean isTransparent() {
        return this.isTransparent;
    }

    public Vector3 getNormal(float paramFloat1, float paramFloat2, OrthoNormalBasis paramOrthoNormalBasis) {
        float[] arrayOfFloat = getPixel(paramFloat1, paramFloat2).getRGB();
        return paramOrthoNormalBasis.transform(new Vector3(2.0F * arrayOfFloat[0] - 1.0F, 2.0F * arrayOfFloat[1] - 1.0F, 2.0F * arrayOfFloat[2] - 1.0F)).normalize();
    }

    public Vector3 getBump(float paramFloat1, float paramFloat2, OrthoNormalBasis paramOrthoNormalBasis, float paramFloat3) {
        Bitmap localBitmap = getBitmap();
        float f1 = 1.0F / localBitmap.getWidth();
        float f2 = 1.0F / localBitmap.getHeight();
        float f3 = getPixel(paramFloat1, paramFloat2).getLuminance();
        float f4 = getPixel(paramFloat1 + f1, paramFloat2).getLuminance();
        float f5 = getPixel(paramFloat1, paramFloat2 + f2).getLuminance();
        return paramOrthoNormalBasis.transform(new Vector3(paramFloat3 * (f3 - f4), paramFloat3 * (f3 - f5), 1.0F)).normalize();
    }
}