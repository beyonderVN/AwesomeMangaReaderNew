package ngohoanglong.com.awesomemangareader;

import java.io.Serializable;
import java.util.List;

import ngohoanglong.com.awesomemangareader.model.Image;

/**
 * Created by Admin on 10/03/2017.
 */

public class Page implements Serializable {
    private String title;
    private List<Image> imageList;

    public Page() {
    }

    public Page(String title, List<Image> imageList) {
        this.title = title;
        this.imageList = imageList;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Image> getImageList() {
        return imageList;
    }

    public void setImageList(List<Image> imageList) {
        this.imageList = imageList;
    }

    @Override
    public String toString() {
        return "ChapterAdapter{" +
                "title='" + title + '\'' +
                ", imageList.size=" + imageList.size() +
                '}';
    }
}
