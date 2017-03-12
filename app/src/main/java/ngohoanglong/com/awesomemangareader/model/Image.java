package ngohoanglong.com.awesomemangareader.model;

import android.graphics.Bitmap;

/**
 * Created by Admin on 12/03/2017.
 */;

public class Image {
    String url;
    Bitmap bitmap;



    public void setStatus(int status) {
        this.status = status;
    }

    int status = 0;
    public Image() {
    }

    public Image(String url, Bitmap bitmap) {
        this.url = url;
        this.bitmap = bitmap;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Image{" +
                "url='" + url + '\'' +
                ", bitmap=" + bitmap +
                ", status=" + status +
                '}';
    }




}
