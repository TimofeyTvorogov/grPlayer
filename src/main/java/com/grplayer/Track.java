package com.grplayer;

import javafx.beans.property.*;
import javafx.scene.image.Image;

public class Track {
    private StringProperty artistNameProperty;
    private StringProperty trackNameProperty;
    private StringProperty sizeProperty;
    private StringProperty durationProperty;
    private StringProperty albumProperty;
    private StringProperty urlProperty;

    private Property<Image> imageProperty;
    public Track(){}
    public Track(String artistName, String trackName, String size, String duration, String album, String url) {
//        this.idProperty = new SimpleIntegerProperty(id);
        this.artistNameProperty = new SimpleStringProperty(artistName);
        this.trackNameProperty = new SimpleStringProperty(trackName);
        this.sizeProperty = new SimpleStringProperty(size);
        this.durationProperty = new SimpleStringProperty(duration);
        this.albumProperty = new SimpleStringProperty(album);
        this.urlProperty = new SimpleStringProperty(url);
    }

    public StringProperty artistNameProperty() {
        return artistNameProperty;
    }

    public StringProperty songNameProperty() {
        return trackNameProperty;
    }

    public StringProperty durationProperty() {
        return durationProperty;
    }

    public StringProperty sizeProperty(){
        return sizeProperty;
    }

    public StringProperty urlProperty() {
        return urlProperty;
    }

    public StringProperty albumProperty(){return albumProperty;}

//    public Image getImage() {
//        return image;
//    }
//
//    public void setImage(Image image) {
//        this.image = image;
//    }
}

