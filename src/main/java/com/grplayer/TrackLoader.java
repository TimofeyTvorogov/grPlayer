package com.grplayer;

import com.mpatric.mp3agic.*;
import javafx.collections.ObservableList;

import java.io.*;

import static com.grplayer.Converter.kbToMb;
import static com.grplayer.Converter.secToMin;

public class TrackLoader {
    private final ObservableList<Track> tracksList;
    private String title;
    private String artist;
    private String album;
    private String duration;
    private String size;
    private String url;

    public TrackLoader(ObservableList<Track> tracksList){
        this.tracksList = tracksList;
    }

    public void loadTrackFromFile(File file){
        if (!file.isFile()) return;
        String name = file.getName();
        if (!hasValidExtension(name)) return;
        loadFileWithTags(file);

    }
    private boolean hasValidExtension(String fileName){
        return fileName.endsWith("mp3") || fileName.endsWith("wav")||fileName.endsWith("m4a");
    }
    private void checkAndReplaceEmptyFields(){
        if (title.equals("")) title.replace("","Unknown title");
        if (album.equals("")) album.replace("","Unknown album");
        if (artist.equals("")) artist.replace("","Unknown artist");
    }
    private void fetchBasicFields(ID3v1 tag){
        title = tag.getTitle();
        artist = tag.getArtist();
        album = tag.getAlbum();
    }
    private void handleTag(ID3v1 tag){
        fetchBasicFields(tag);
        checkAndReplaceEmptyFields();
    }
    public void loadTracksFromDirectory(File dir) {
        File[] files = dir.listFiles();
        for(File file : files) {
            if (!file.isFile())
                continue;
            String fileName = file.getName();
            if (!hasValidExtension(fileName)) {
                continue;
            }
            loadFileWithTags(file);
            //todo setImages
        }


    }

    private void loadFileWithTags(File file) {
        try {
            Mp3File mp3 = new Mp3File(file.getPath());
            url = file.getAbsolutePath();
            duration = secToMin(mp3.getLengthInMilliseconds()/1000);
            size = kbToMb(file.length());
            if (mp3.hasId3v2Tag()){
                handleTag(mp3.getId3v2Tag());
            }
            else if (mp3.hasId3v1Tag()) {
                handleTag(mp3.getId3v1Tag());
            }
            else {
                throw new UnsupportedTagException("Track has no tag");
            }
//                  byte[] imageBytes = new byte[5];
//                  ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
//                  Image image = new Image(bais);
        }
        catch(IOException | InvalidDataException | UnsupportedTagException | NullPointerException e ) {
            artist = "Unknown artist";
            title = "Unknown title";
            album = "Unknown album";
        }
        finally {
            Track track = new Track(artist, title, size, duration, album, url);
            tracksList.add(track);
        }
    }

}
