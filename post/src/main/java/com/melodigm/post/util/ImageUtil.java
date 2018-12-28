package com.melodigm.post.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.media.ExifInterface;
import android.util.Log;
import android.view.View;

public class ImageUtil {

    /**
     * EXIF정보를 회전각도로 변환하는 메서드
     *
     * @param exifOrientation EXIF 회전각
     * @return 실제 각도
     */
    public static int exifOrientationToDegrees(int exifOrientation) {
        if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    /**
     * 이미지를 회전시킵니다.
     *
     * @param bitmap 비트맵 이미지
     * @param degrees 회전 각도
     * @return 회전된 이미지
     */
    public static Bitmap rotate(Bitmap bitmap, int degrees) {
        if(degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);

            try {
                Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                if(bitmap != converted) {
                    bitmap.recycle();
                    bitmap = converted;
                }
            } catch(OutOfMemoryError ex) {
                // 메모리가 부족하여 회전을 시키지 못할 경우 그냥 원본을 반환합니다.
            }
        }
        return bitmap;
    }

    /**
     * YCNOTE - Layout / View Drawing Cache
     * setDrawingCacheEnabled - 뷰가 업데이트 될 때마다 그 때의 뷰 이미지를 Drawing Cache에 저장할지 여부를 결정한다.(자동) - 변경(Invalidate) 될 때마다 Drawing Cache에 새로운 뷰 정보가 저장되므로 성능 저하의 원인이 될 수 있다.
     * buildDrawingCache - 뷰 이미지를 Drawing cache에 저장합니다.(수동) Drawing cache enabled 속성이 활성화되어 있다면 이 메서드를 호출하지 않아도 자동으로 Drawing cache에 뷰의 이미지가 저장됩니다.
     * destroyDrawingCache - 뷰 이미지를 Drawing cache에서 삭제합니다.
     * getDrawingCache - Drawing Cache에 저장된 뷰의 이미지를 Bitmap 형태로 반환한다.
     */
    public static Bitmap getDrawingCache(View v) {
        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        // Restore the view
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return bitmap;
    }

    /**
     * 성능/안정성 문제 이슈로 인하여 RoundedAvatarDrawable 로 대체
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

	public static void setBackgroundD(View v,Drawable drawable){
		if(v == null) return;
		Rect oldDrawablepadding = new Rect();
		if(v.getBackground() != null){
			v.getBackground().getPadding(oldDrawablepadding);
		}
		
		int sdk = android.os.Build.VERSION.SDK_INT;
		if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {			
			Rect drawablePadding = new Rect();
			drawable.getPadding(drawablePadding);
		    int top = v.getPaddingTop() + drawablePadding.top - oldDrawablepadding.top;
		    int left = v.getPaddingLeft() + drawablePadding.left - oldDrawablepadding.left;
		    int right = v.getPaddingRight() + drawablePadding.right - oldDrawablepadding.right;
		    int bottom = v.getPaddingBottom() + drawablePadding.bottom - oldDrawablepadding.bottom;

		    v.setBackgroundDrawable(drawable);
		    v.setPadding(left, top, right, bottom);
		} else {
			Rect drawablePadding = new Rect();
			drawable.getPadding(drawablePadding);
		    int top = v.getPaddingTop() + drawablePadding.top - oldDrawablepadding.top;
		    int left = v.getPaddingLeft() + drawablePadding.left - oldDrawablepadding.left;
		    int right = v.getPaddingRight() + drawablePadding.right - oldDrawablepadding.right;
		    int bottom = v.getPaddingBottom() + drawablePadding.bottom- oldDrawablepadding.bottom;

		    v.setBackground(drawable);
		    v.setPadding(left, top, right, bottom);			
		}
	}
	
	public static Bitmap getScaleImageBitmap(Bitmap src, int maxWidth, int maxHeight) {
    	Bitmap bm = null;
        int width = src.getWidth(); 
        int height = src.getHeight(); 
        float scale = 1;
        Matrix matrix = null;
        if (maxWidth == 0 && maxHeight == 0) {
        	scale = 0;
        } else {
            if (width > height && width > maxWidth) {
                scale = ((float) maxWidth) / width; 
            }
            if (width <= height && height > maxHeight){
                scale = ((float) maxHeight) / height; 
            }
            matrix = new Matrix(); 
            matrix.postScale(scale, scale);
        }
        if (scale == 0) {
            bm = src;
        } else {
            bm = Bitmap.createBitmap(src, 0, 0, width, height, matrix, true);
        }
     
        return bm;
    }
	
	// bitmap 사이즈가  가로는 baseWidth 고정으로 resize
	 public static Bitmap getScaleImageBitmap(Bitmap src, int baseWidth) {
     Bitmap bm = null;
       int width = src.getWidth(); 
       int height = src.getHeight(); 
       float scale = 1;
       Matrix matrix = null;
       if (baseWidth == 0) {
         scale = 0;
       } else {
           scale = ((float) baseWidth) / width; 
           matrix = new Matrix(); 
           matrix.postScale(scale, scale);
       }
       if (scale == 0) {
           bm = src;
       } else {
           bm = Bitmap.createBitmap(src, 0, 0, width, height, matrix, true);
       }
    
       return bm;
   }
	 
	 // 특정 높이로 이미지 resize
   public static Bitmap getScaleImageBitmapHeight(Bitmap src, int baseHeight) {
     Bitmap bm = null;
       int width = src.getWidth(); 
       int height = src.getHeight(); 
       float scale = 1;
       Matrix matrix = null;
       if (baseHeight == 0) {
         scale = 0;
       } else {
           scale = ((float) baseHeight) / height; 
           matrix = new Matrix(); 
           matrix.postScale(scale, scale);
       }
       if (scale == 0) {
           bm = src;
       } else {
           bm = Bitmap.createBitmap(src, 0, 0, width, height, matrix, true);
       }
    
       return bm;
   }
	
	public static Bitmap getScaleImageBitmap(String filepath, int maxWidth, int maxHeight) {
    	Bitmap bm = null;
        try {
            Bitmap src = BitmapFactory.decodeFile(filepath,getBitmapOption(filepath,maxWidth,maxHeight));
            
            int width = src.getWidth(); 
            int height = src.getHeight(); 
            float scale = 1;
            Matrix matrix = null;
            if (maxWidth == 0 && maxHeight == 0) {
            	scale = 0;
            } else {
                if (width > height && width > maxWidth) {
                    scale = ((float) maxWidth) / width; 
                }
                if (width <= height && height > maxHeight){
                    scale = ((float) maxHeight) / height; 
                }
                matrix = new Matrix(); 
                matrix.postScale(scale, scale);
            }
            if (scale == 0) {
                bm = src;
            } else {
                bm = Bitmap.createBitmap(src, 0, 0, width, height, matrix, true); 
                if (bm != src) {
                	src.recycle();
                }
            }
        } catch(Exception e) {
        	Log.e("getScaleImageBitmap","Path = " + filepath);
        }
        return bm;
    }
	
	public static BitmapFactory.Options getBitmapOption(String filepath,int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, options);
        int imgWidth = options.outWidth;
        int imgHeight = options.outHeight;
        int scale = 0;
        if (width > 0 && height > 0) {
            if (imgWidth > imgHeight && imgWidth > width) {
                scale = imgWidth / width; 
            }
            if (imgWidth <= imgHeight && imgHeight > height) {
                scale = imgHeight / height; 
            }
            options.inSampleSize = getSampleSize(scale);
        }
        options.inJustDecodeBounds = false;
        return options;
    }
    public static int getSampleSize(int scale) {
        int samplesize = 0;
        if (scale < 4 && scale > 2) {
            samplesize = 2;
        } else if (scale >= 4 && scale <= 8)
            samplesize = 4;
        else if (scale > 8)
            samplesize = 8;
        return samplesize;
    }

	public static StateListDrawable getMakeSelector(Context context, int normal, int press) {
		Drawable normalDraw = context.getResources().getDrawable(normal);
		Drawable pressDraw = context.getResources().getDrawable(press);
        return getMakeSelector(normalDraw,pressDraw); 
    }
	
	public static StateListDrawable getMakeSelector(Context context, int normal, int press, int selected_normal, int selected_press) {
		Drawable normalDraw = context.getResources().getDrawable(normal);
		Drawable pressDraw = context.getResources().getDrawable(press);
		Drawable selnormalDraw = context.getResources().getDrawable(selected_normal);
		Drawable selpressDraw = context.getResources().getDrawable(selected_press);
        return getMakeSelector(normalDraw,pressDraw,selnormalDraw, selpressDraw); 
    }
	
	public static StateListDrawable getMakeSelector(Context context, int normal, int press, int dim) {
		Drawable normalDraw = context.getResources().getDrawable(normal);
		Drawable pressDraw = context.getResources().getDrawable(press);
		Drawable dimDraw = context.getResources().getDrawable(dim);
        return getMakeSelector(normalDraw,pressDraw, dimDraw); 
    }
	
	public static StateListDrawable getMakeSelector(Context context, int normal, int press, int dim, int selected_normal, int selected_press, int selected_dim) {
		Drawable normalDraw = context.getResources().getDrawable(normal);
		Drawable pressDraw = context.getResources().getDrawable(press);
		Drawable dimDraw = context.getResources().getDrawable(dim);
		Drawable selected_normalDraw = context.getResources().getDrawable(selected_normal);
		Drawable selected_pressDraw = context.getResources().getDrawable(selected_press);
		Drawable selected_dimDraw = context.getResources().getDrawable(selected_dim);
        return getMakeSelector(normalDraw, pressDraw,dimDraw, selected_normalDraw, selected_pressDraw, selected_dimDraw); 
    }
	
	public static StateListDrawable getMakeSelector(Context context, String normal, String press, String dim, String selected_normal, String selected_press, String selected_dim) {
		ColorDrawable normalDraw = new ColorDrawable(Color.parseColor(normal));
		ColorDrawable pressDraw = new ColorDrawable(Color.parseColor(press));
		ColorDrawable dimDraw = new ColorDrawable(Color.parseColor(dim));
		ColorDrawable selected_normalDraw = new ColorDrawable(Color.parseColor(selected_normal));
		ColorDrawable selected_pressDraw = new ColorDrawable(Color.parseColor(selected_press));
		ColorDrawable selected_dimDraw = new ColorDrawable(Color.parseColor(selected_dim));
        return getMakeSelector(normalDraw, pressDraw,dimDraw, selected_normalDraw, selected_pressDraw, selected_dimDraw); 
    }
	
	public static StateListDrawable getMakeCheckedSelector(Context context, int normal, int press, int checked_normal, int checked_press) {
		Drawable normalDraw = context.getResources().getDrawable(normal);
		Drawable pressDraw = context.getResources().getDrawable(press);
		Drawable checknormalDraw = context.getResources().getDrawable(checked_normal);
		Drawable checkpressDraw = context.getResources().getDrawable(checked_press);
        return getMakeCheckedSelector(normalDraw, pressDraw, checknormalDraw, checkpressDraw); 
    }
	
	public static StateListDrawable getMakeCheckedSelector(Context context, int normal, int press, int dim, int checked_normal, int checked_press, int checked_dim) {
		Drawable normalDraw = context.getResources().getDrawable(normal);
		Drawable pressDraw = context.getResources().getDrawable(press);
		Drawable dimDraw = context.getResources().getDrawable(dim);
		Drawable checknormalDraw = context.getResources().getDrawable(checked_normal);
		Drawable checkpressDraw = context.getResources().getDrawable(checked_press);
		Drawable checkdimDraw = context.getResources().getDrawable(checked_dim);
        return getMakeCheckedSelector(normalDraw, pressDraw,dimDraw, checknormalDraw, checkpressDraw, checkdimDraw); 
    }
	
	public static StateListDrawable getMakeCheckedSelector(Context context, int normal, int checked) {
		Drawable normalDraw = context.getResources().getDrawable(normal);
		Drawable checkedDraw = context.getResources().getDrawable(checked);
        return getMakeCheckedSelector(normalDraw, checkedDraw); 
    }

	public static StateListDrawable getMakeFocusedSelector(Context context, int normal, int focused) {
		Drawable normalDraw = context.getResources().getDrawable(normal);
		Drawable focusedDraw = context.getResources().getDrawable(focused);
        return getMakeFocusedSelector(normalDraw, focusedDraw); 
    }
	
	public static StateListDrawable getMakeSelector(Drawable normal, Drawable press) {
        StateListDrawable imageDraw = new StateListDrawable();
        imageDraw.addState(new int[] { android.R.attr.state_pressed}, press);
        imageDraw.addState(new int[] { android.R.attr.state_activated}, press);
        imageDraw.addState(new int[] { -android.R.attr.state_pressed}, normal);
        return imageDraw; 
    }
	
	
	public static StateListDrawable getMakeSelector(Drawable normal, Drawable press, Drawable dim) {
        StateListDrawable imageDraw = new StateListDrawable();
        imageDraw.addState(new int[] { android.R.attr.state_pressed}, press);
        imageDraw.addState(new int[] {-android.R.attr.state_enabled}, dim);
        imageDraw.addState(new int[] {android.R.attr.state_enabled}, normal);
        return imageDraw; 
    }
	
	public static StateListDrawable getMakeSelector(Drawable normal, Drawable press, Drawable selected_normal, Drawable selected_press) {
        StateListDrawable imageDraw = new StateListDrawable();
        imageDraw.addState(new int[] { android.R.attr.state_pressed, android.R.attr.state_selected}, selected_press);
        imageDraw.addState(new int[] { android.R.attr.state_pressed, -android.R.attr.state_selected}, press);
        imageDraw.addState(new int[] { android.R.attr.state_selected}, selected_normal);
        imageDraw.addState(new int[] { -android.R.attr.state_selected}, normal);
        return imageDraw; 
    }
	
	public static StateListDrawable getMakeSelector(Drawable normal, Drawable press,Drawable dim, Drawable selected_n, Drawable selected_p, Drawable selected_d) {
        StateListDrawable imageDraw = new StateListDrawable();
        imageDraw.addState(new int[] { -android.R.attr.state_enabled, android.R.attr.state_selected}, selected_d);
        imageDraw.addState(new int[] { -android.R.attr.state_enabled, -android.R.attr.state_selected}, dim);
        imageDraw.addState(new int[] { android.R.attr.state_pressed, android.R.attr.state_selected}, selected_p);
        imageDraw.addState(new int[] { android.R.attr.state_pressed, -android.R.attr.state_selected}, press);
        imageDraw.addState(new int[] { android.R.attr.state_selected}, selected_n);
        imageDraw.addState(new int[] { -android.R.attr.state_selected}, normal);
        return imageDraw; 
    }
	
	public static StateListDrawable getMakeCheckedSelector(Drawable normal, Drawable checked) {
        StateListDrawable imageDraw = new StateListDrawable();
        imageDraw.addState(new int[] { android.R.attr.state_checked}, checked);
        imageDraw.addState(new int[] { -android.R.attr.state_checked}, normal);
        return imageDraw; 
    }
	
	public static StateListDrawable getMakeCheckedSelector(Drawable normal, Drawable press, Drawable checked_n, Drawable checked_p) {
        StateListDrawable imageDraw = new StateListDrawable();
        imageDraw.addState(new int[] { android.R.attr.state_pressed, android.R.attr.state_checked}, checked_p);
        imageDraw.addState(new int[] { android.R.attr.state_pressed, -android.R.attr.state_checked}, press);
        imageDraw.addState(new int[] { android.R.attr.state_checked}, checked_n);
        imageDraw.addState(new int[] { -android.R.attr.state_checked}, normal);
        return imageDraw; 
    }
	
	public static StateListDrawable getMakeCheckedSelector(Drawable normal, Drawable press,Drawable dim, Drawable checked_n, Drawable checked_p, Drawable checked_d) {
        StateListDrawable imageDraw = new StateListDrawable();
        imageDraw.addState(new int[] { -android.R.attr.state_enabled, android.R.attr.state_checked}, checked_d);
        imageDraw.addState(new int[] { -android.R.attr.state_enabled, -android.R.attr.state_checked}, dim);
        imageDraw.addState(new int[] { android.R.attr.state_pressed, android.R.attr.state_checked}, checked_p);
        imageDraw.addState(new int[] { android.R.attr.state_pressed, -android.R.attr.state_checked}, press);
        imageDraw.addState(new int[] { android.R.attr.state_checked}, checked_n);
        imageDraw.addState(new int[] { -android.R.attr.state_checked}, normal);
        return imageDraw; 
    }
	
	public static StateListDrawable getMakeFocusedSelector(Drawable normal, Drawable focused) {
        StateListDrawable imageDraw = new StateListDrawable();
        imageDraw.addState(new int[] { android.R.attr.state_focused}, focused);
        imageDraw.addState(new int[] { -android.R.attr.state_focused}, normal);
        return imageDraw; 
    }
	
	public static ColorStateList getMakeColorSelector(Context context, int normal, int press, int dim){
		int[][] state = new int[][]{
				new int[]{android.R.attr.state_pressed},
                new int[]{-android.R.attr.state_enabled},
                new int[]{android.R.attr.state_enabled}};
		
		int[] color = new int[]{
				context.getResources().getColor(press),
				context.getResources().getColor(dim),
				context.getResources().getColor(normal)};
		
		return new ColorStateList(state, color);
	}
	
	public static ColorStateList getMakeColorSelector(String normal, String press, String dim){
		int[][] state = new int[][]{
				new int[]{android.R.attr.state_pressed},
                new int[]{-android.R.attr.state_enabled},
                new int[]{android.R.attr.state_enabled}};
		
		int[] color = new int[]{
				Color.parseColor(press),
				Color.parseColor(dim),
				Color.parseColor(normal)};
		
		return new ColorStateList(state, color);
	}
	
	public static ColorStateList getMakeColorSelector(Context context, int normal, int press){
		int[][] state = new int[][]{
				new int[]{android.R.attr.state_pressed},
                new int[]{-android.R.attr.state_pressed}};
		
		int[] color = new int[]{
				context.getResources().getColor(press),
				context.getResources().getColor(normal)};
		
		return new ColorStateList(state, color);
	}
	
	public static ColorStateList getMakeColorSelector(String normal, String press){
		int[][] state = new int[][]{
				new int[]{android.R.attr.state_pressed},
                new int[]{-android.R.attr.state_pressed}};
		
		int[] color = new int[]{
				Color.parseColor(press),
				Color.parseColor(normal)};
		
		return new ColorStateList(state, color);
	}
	
	public static ColorStateList getMakeColorSelector(String normal, String press, String dim, String selected_normal, String selected_press, String selected_dim){
		int[][] state = new int[][]{
				new int[]{-android.R.attr.state_enabled, android.R.attr.state_selected},
                new int[]{-android.R.attr.state_enabled, -android.R.attr.state_selected},
                new int[]{android.R.attr.state_pressed, android.R.attr.state_selected},
				new int[]{android.R.attr.state_pressed, -android.R.attr.state_selected},
				new int[]{android.R.attr.state_selected},
				new int[]{-android.R.attr.state_selected}};
		
		int[] color = new int[]{
				Color.parseColor(selected_dim),
				Color.parseColor(dim),
				Color.parseColor(selected_press),
				Color.parseColor(press),
				Color.parseColor(selected_normal),
				Color.parseColor(normal)};
		
		return new ColorStateList(state, color);
	}
	
	public static ColorStateList getMakeColorSelector(Context context, int normal, int press, int dim, int selected_normal, int selected_press, int selected_dim){
		int[][] state = new int[][]{
				new int[]{-android.R.attr.state_enabled, android.R.attr.state_selected},
                new int[]{-android.R.attr.state_enabled, -android.R.attr.state_selected},
                new int[]{android.R.attr.state_pressed, android.R.attr.state_selected},
				new int[]{android.R.attr.state_pressed, -android.R.attr.state_selected},
				new int[]{android.R.attr.state_selected},
				new int[]{-android.R.attr.state_selected}};
		
		int[] color = new int[]{
				context.getResources().getColor(selected_dim),
				context.getResources().getColor(dim),
				context.getResources().getColor(selected_press),
				context.getResources().getColor(press),
				context.getResources().getColor(selected_normal),
				context.getResources().getColor(normal)};
		
		return new ColorStateList(state, color);
	}
}
