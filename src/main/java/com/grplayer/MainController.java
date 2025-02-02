package com.grplayer;


//todo make normal rowclicklistener


/** ФИЧИ
 * драг эн дроп для таблицы
 * авторазмер колонок
 * картинки для плеера
 * увеличить количество полос для спеткрометра
 *
*/

/* Баги и проблемы
*
*  слушатель таблицы
*  null песня при перемещении в списке
*
*
*/


import com.jfoenix.controls.JFXSlider;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioEqualizer;
import javafx.scene.media.EqualizerBand;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.nio.IntBuffer;

import static com.grplayer.Converter.secToMin;

public class MainController {
    ObservableList<EqualizerBand> bands;
    @FXML
    VBox window;

//---------------ТАБЛИЦА И ЕЁ КОЛОНКИ --------------------------------//
    @FXML
    private TableView<Track> trackTable;

    @FXML
    private TableColumn<Track, String> artistNameColumn;

    @FXML
    private TableColumn<Track, String> songNameColumn;

    @FXML
    private TableColumn<Track, String> durationColumn;

    @FXML
    private TableColumn<Track, String> sizeColumn;

    @FXML
    private TableColumn<Track, String> albumColumn;
//-----------------------------------------------//

//--------------- ЛЭЙБЛЫ В НИЖНЕЙ ЧАСТИ ИНТЕРФЕЙСА --------------------------------//
    @FXML
    private Label artistLabel;

    @FXML
    private Label albumLabel;

    @FXML
    private Label titleLabel;

    @FXML
    private Label totalDurationLabel;

    @FXML
    private Label currentTimeLabel;

    @FXML
    private Label tracksQuantityLabel;
//-----------------------------------------------//

    @FXML
    private JFXSlider trackSlider;

    @FXML
    private JFXSlider volumeSlider;

    @FXML
    private ImageView folderChooser;

    @FXML
    private ImageView playButton;

    @FXML
    private ImageView pauseButton;

    @FXML
    private ImageView nextTrackButton;

    @FXML
    private ImageView previousTrackButton;

    @FXML
    private ToggleButton autoPlaySwitch;

    @FXML
    private Stage stage;

    @FXML
    Canvas canvas;

    private static int canvasWidth;
    private static int canvasHeight;

    GraphicsContext canvasContext;
    PixelFormat<IntBuffer> pixelFormat = PixelFormat.getIntArgbInstance();
    int[] cleanBuffer;
    WritableImage image;
    PixelWriter writer;
    int bandWidth;
    @FXML
    ListView<String> radioList;

    @FXML
    private Button addTrackButton;

    @FXML
    private Button deleteTrackButton;

    @FXML
    private Button trackUpButton;

    @FXML
    private Button trackDownButton;

    @FXML
    ListView<String> eqList;

    private MediaPlayer currentPlayer;

    private int currentTrackId = -1;

    private boolean isRepeating;
    private double volume;
    private boolean isGUIActive = false;
    private boolean isSliderAdjusting = false;
    private ObservableList<Track> tracksList =  FXCollections.observableArrayList();
    private final TrackLoader loader =  new TrackLoader(tracksList);


    private CustomMouseEventHandler handler = new CustomMouseEventHandler() {
        @Override
        void longPressRelease() {
            currentPlayer.setRate(1);
        }

        @Override
        void longPress() {
            currentPlayer.setRate(2);
        }

        @Override
        void shortClick() {
            playNextTrack();
        }
    };

    ChangeListener<Duration> currentTimeChangedListener = (observableValue, oldTime, newTime) -> {
        if (!isSliderAdjusting){
            trackSlider.setValue(newTime.toMillis());
            String formattedTime = secToMin(((long) newTime.toSeconds()));
            currentTimeLabel.setText(formattedTime);
        }
    };
    private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");

    public MainController() {
        isRepeating = false;
        volume = 0.4;

    }
    private void changePlayIcon(boolean isPlaying) {
        playButton.setVisible(!isPlaying);
        playButton.setDisable(isPlaying);
        pauseButton.setDisable(!isPlaying);
    }

    private void showSongInfo(Track track) {

        artistLabel.setText(track.artistNameProperty().get());
        titleLabel.setText(track.songNameProperty().get());
        albumLabel.setText(track.albumProperty().get());

    }

    @FXML
    private void initialize() {
        canvasContext = canvas.getGraphicsContext2D();
        canvasHeight = (int) canvas.getHeight();
        canvasWidth = (int) canvas.getWidth();
        image = new WritableImage(canvasWidth, canvasHeight);
        writer = image.getPixelWriter();
        cleanBuffer = new int[(canvasWidth)*(canvasHeight)];
        for (int i = 0; i < cleanBuffer.length; i++) cleanBuffer[i] = 0xff1a1a1a;
        lockUI();
        setupSliders();
        eqList.getItems().addAll("Flat", "Bass Boost", "Bass Low", "Rock","Alt.Bass","Dance","Treble");

        radioList.getItems().addAll("Europa +", "Nashe radio","Phonk radio", "RetroFM");
//        radioList.setFixedCellSize(25);
//        radioList.prefHeightProperty().bind(Bindings.size(radioList.getItems()).multiply(radioList.getFixedCellSize()).add(2));
        radioList.setOnMouseClicked(mouseEvent -> {
            String selected = radioList.getSelectionModel().getSelectedItem();
            if (selected==null)return;
            switch (selected) {
                case "Europa +":
                    playTrackByLink("http://europaplus.hostingradio.ru:8014/europaplus320.mp3");
                    break;
                case "Nashe radio":

                    playTrackByLink("http://nashe2.hostingradio.ru:80/ultra-128.mp3");
                    break;
                case "Phonk radio":
                    playTrackByLink("http://phonk.stream.laut.fm/phonk");
                    break;
                case "RetroFM":
                    playTrackByLink("http://retro.hostingradio.ru:8014/retro320.mp3");
                    break;
                case "Action 4":
//                        performAction4();
                    break;
                case "Action 5":
//                        performAction5();
                    break;
            }
        });
        autoPlaySwitch.setSelected(isRepeating);
        pauseButton.visibleProperty().bind(playButton.visibleProperty().not());
        BooleanBinding elementFocusedCondition = trackTable.getSelectionModel().selectedItemProperty().isNull();
        deleteTrackButton.disableProperty().bind(elementFocusedCondition);
        //фокусированность также через куррент айди проперти
        trackUpButton.disableProperty().bind(elementFocusedCondition);
        trackDownButton.disableProperty().bind(elementFocusedCondition);
/*поставить кастомную фабрику ячеек*/
        artistNameColumn.setCellValueFactory(cellData -> cellData.getValue().artistNameProperty());
        songNameColumn.setCellValueFactory(cellData -> cellData.getValue().songNameProperty());
        durationColumn.setCellValueFactory(cellData -> cellData.getValue().durationProperty());
        sizeColumn.setCellValueFactory(cellData -> cellData.getValue().sizeProperty());
        albumColumn.setCellValueFactory(cellData -> cellData.getValue().albumProperty());
        trackTable.getColumns().forEach(trackTableColumn -> trackTableColumn.prefWidthProperty().bind(trackTable.widthProperty().multiply(1/trackTable.getColumns().size())));
        trackTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        trackTable.setOnMouseClicked((MouseEvent e) -> onTrackSelected());
        trackTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showSongInfo(newValue));
        trackTable.setRowFactory(tv -> {
            TableRow<Track> row = new TableRow<>();

            row.setOnDragDetected(event -> {
                if (!row.isEmpty()) {
                    Integer index = row.getIndex();
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    db.setDragView(row.snapshot(null, null));
                    ClipboardContent cc = new ClipboardContent();
                    cc.put(SERIALIZED_MIME_TYPE, index);
                    db.setContent(cc);
                    event.consume();
                }
            });

            row.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                    if (row.getIndex() != ((Integer)db.getContent(SERIALIZED_MIME_TYPE)).intValue()) {
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                        event.consume();
                    }
                }
            });

            row.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                    int draggedIndex = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
                    Track draggedTrack = trackTable.getItems().remove(draggedIndex);

                    int dropIndex ;

                    if (row.isEmpty()) {
                        dropIndex = trackTable.getItems().size() ;
                    } else {
                        dropIndex = row.getIndex();
                    }

                    trackTable.getItems().add(dropIndex, draggedTrack);

                    event.setDropCompleted(true);
                    trackTable.getSelectionModel().select(dropIndex);
                    event.consume();
                }
            });
            return row ;
        });
        addTrackButton.setOnMouseClicked(mouseEvent -> {
            FileChooser chooser = new FileChooser();
            File selectedFile = chooser.showOpenDialog(stage);
            if (selectedFile == null) {
                System.out.println("No file selected!");
                return;
            }
            loader.loadTrackFromFile(selectedFile);
            if (trackTable.isDisable()) trackTable.setDisable(false);
        });

        folderChooser.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            DirectoryChooser chooser = new DirectoryChooser();
            File selectedDirectory = chooser.showDialog(stage);
            if(selectedDirectory == null) {
                System.out.println("No directory selected!");
                return;
            }
            if (!(tracksList.isEmpty())) {
                tracksList.clear();
                System.out.println("new array list");
            }
            loader.loadTracksFromDirectory(selectedDirectory);
            if (trackTable.isDisable()) trackTable.setDisable(false);
        });

        tracksList.addListener((ListChangeListener<Track>) change -> {

            tracksQuantityLabel.setText("Songs: " + tracksList.size());
            trackTable.setItems(tracksList);
        });

        deleteTrackButton.setOnMouseClicked(mouseEvent -> {
            Track selectedTrack = trackTable.getSelectionModel().getSelectedItem();
            if (selectedTrack == null){
                return;
            }
            int idOfSelectedTrack = tracksList.indexOf(selectedTrack);
            if (idOfSelectedTrack == currentTrackId){
                stopPlaying();
                trackTable.getItems().remove(idOfSelectedTrack);
            }


        });

        trackUpButton.setOnMouseClicked(mouseEvent -> {
            Track selectedTrack = tracksList.get(currentTrackId);
            if (selectedTrack == null){
                return;
            }
            if (currentTrackId == 0) return;
            Track upperTrack = tracksList.get(currentTrackId-1);
            tracksList.set(currentTrackId, upperTrack);
            tracksList.set(currentTrackId-1, selectedTrack);

        });

        playButton.setOnMouseClicked(mouseEvent -> {
            changePlayIcon(true);
            currentPlayer.play();
        });
        pauseButton.setOnMouseClicked(mouseEvent -> {
            changePlayIcon(false);
            currentPlayer.pause();
        });
        volumeSlider.valueProperty().addListener((observableValue, number, t1) -> {
            double sliderValue = volumeSlider.getValue();
            volume = sliderValue / 100;
            if (currentPlayer != null)
                currentPlayer.setVolume(volume);
        });


        trackSlider.setOnMousePressed(event -> isSliderAdjusting = true);
        trackSlider.setOnMouseReleased(event -> {
            isSliderAdjusting = false;
            currentPlayer.seek(Duration.millis(trackSlider.getValue()));
        });

        nextTrackButton.setOnMousePressed(event -> handler.handleMousePressed());
        nextTrackButton.setOnMouseReleased(event -> handler.handleMouseReleased());


        previousTrackButton.setOnMouseClicked(mouseEvent -> playPreviousTrack());

        autoPlaySwitch.setOnMouseClicked(mouseEvent -> {
            isRepeating = !isRepeating;
            autoPlaySwitch.setSelected(isRepeating);
            System.out.println(isRepeating);
        });

    }

    private void stopPlaying(){
        lockUI();
        currentTrackId = -1;
        currentPlayer.stop();
        currentPlayer.dispose();
    }
    private void setupSliders(){
        volumeSlider.adjustValue(volume*100);
        trackSlider.setValue(0);
        trackSlider.setBlockIncrement(1000);
        trackSlider.setValueFactory(slider -> Bindings.createStringBinding(() ->
                (secToMin((long) slider.getValue()/1000)), trackSlider.valueProperty()));
    }

    private void lockUI(){
        previousTrackButton.setDisable(true);
        nextTrackButton.setDisable(true);
        trackSlider.setDisable(true);
        playButton.setVisible(true);
        playButton.setDisable(true);
        eqList.setDisable(true);
    }
    private void unlockUI(){
        previousTrackButton.setDisable(false);
        nextTrackButton.setDisable(false);
        trackSlider.setDisable(false);
        changePlayIcon(true);
        eqList.setDisable(false);
    }


    public MediaPlayer createPlayer(String url) {
        url.replace("\\", "/");
        final Media media = new Media(new File(url).toURI().toString());
        final MediaPlayer player = new MediaPlayer(media);

        return player;
    }
    public void onTrackSelected() {
        if (!isGUIActive) {
            isGUIActive = true;
        }
        if(trackTable.getSelectionModel().getSelectedItem() != null) {
            Track selectedTrack = trackTable.getSelectionModel().getSelectedItem();
            int selectedSongIdx = tracksList.indexOf(selectedTrack);
            playTrackByIdx(selectedSongIdx);
        }

    }

    public void playNextTrack(){
        if (currentTrackId >= tracksList.size()-1) return;
        currentTrackId++;
        trackTable.getSelectionModel().select(currentTrackId);
        trackTable.requestFocus();
        showSongInfo(tracksList.get(currentTrackId));
        playTrackByIdx(currentTrackId);
    }
    public void playPreviousTrack(){
        double currentTime = currentPlayer.getCurrentTime().toSeconds();
        if (currentTime <= 2) {
            if (currentTrackId > 0) {
                showSongInfo(tracksList.get(--currentTrackId));
                trackTable.getSelectionModel().select(currentTrackId);
                trackTable.requestFocus();
            }
        }
        playTrackByIdx(currentTrackId);
    }

    private void visualize(MediaPlayer player){
        //попробовать интерполяцию чтобы то ни было
        //сделать так чтобы рамки не ломались
        int NUM_BANDS = player.getAudioSpectrumNumBands();
//        final int NUM_BANDS = 10;
        bandWidth = canvasWidth / NUM_BANDS;
        player.setAudioSpectrumInterval(0.085);
        player.setAudioSpectrumListener((timestamp, duration, magnitudes, phases) -> {
            clearCanvas();
            for (int i = 0; i < NUM_BANDS; i++) {
                int bandX =  i * bandWidth;
                double magnitude = magnitudes[i];
                double deltaMagnitude = magnitude + 60.0;
                double percent = 1-deltaMagnitude/60.0;

                int bandY = (int) (canvasHeight * percent);
                int bandHeight =  (canvasHeight - bandY);
                if (bandHeight == 0)
                    continue;
                int[] bandBuffer = new int[bandWidth * bandHeight];
                for (int j = 0; j < bandBuffer.length; j++) {
                    bandBuffer[j] = 0xFFE12B35;

                }
                for (int j = 0; j < bandHeight; j++) {
                    bandBuffer[j * bandWidth] = 0xFF1A1A1A;
                }
//                System.out.println("len = "+bandBuffer.length);
//                System.out.printf("band = %d\nx = %d\ny = %d\nw = %d\nh = %d\n\n",i,bandX,bandY,bandWidth,bandHeight);

                writer.setPixels(bandX, bandY, bandWidth, bandHeight, pixelFormat, bandBuffer,0, bandWidth);
                canvasContext.drawImage(image,0,0);
            }
        });

    }

    private void clearCanvas() {
        writer.setPixels(0,0, canvasWidth, canvasHeight, pixelFormat, cleanBuffer, 0, canvasWidth);
        canvasContext.drawImage(image,0,0);
    }



    public void playTrackByIdx(int trackIdx){
        Track track = tracksList.get(trackIdx);
        currentTrackId = trackIdx;
        if (currentPlayer != null)
            currentPlayer.dispose();
        currentPlayer = createPlayer(track.urlProperty().get());
        currentPlayer.setOnReady(() -> {
            currentPlayer.currentTimeProperty().addListener(currentTimeChangedListener);
            currentPlayer.setVolume(volume);
            Duration trackTotalDur = currentPlayer.getTotalDuration();
            trackSlider.setValue(0);
            trackSlider.setMax(trackTotalDur.toMillis());
            totalDurationLabel.setVisible(true);
            currentTimeLabel.setVisible(true);
            totalDurationLabel.setText(secToMin((long) trackTotalDur.toSeconds()));
            currentPlayer.setOnEndOfMedia(() -> {
                clearCanvas();
                if (isRepeating) playTrackByIdx(currentTrackId);
                else playNextTrack();
            });
            visualize(currentPlayer);
            setupEqualizer();
            unlockUI();
            currentPlayer.play();
        });

    }

    public void playTrackByLink(String url) {
        lockUI();
        if (currentPlayer != null)
            currentPlayer.dispose();
        currentPlayer = new MediaPlayer(new Media(url));
        currentPlayer.setOnReady(() -> {
            currentPlayer.setVolume(volume);
            trackSlider.setValue(0);
            totalDurationLabel.setVisible(false);
            currentTimeLabel.setVisible(false);
            visualize(currentPlayer);
            setupEqualizer();
            currentPlayer.play();
        });
    }
 private void makePreset(double... magnitude){
     for (int i = 0; i < magnitude.length; i++) {
         if (bands == null) return;
         bands.get(i).setGain(magnitude[i]);
     }

 }
    private void setupEqualizer() {
        AudioEqualizer equalizer = currentPlayer.getAudioEqualizer();
        bands = equalizer.getBands();
        eqList.setOnMouseClicked(mouseEvent -> {
            String selected = eqList.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            System.out.println(selected);
            switch (selected) {
                case "Flat":
                    bands.forEach(equalizerBand -> equalizerBand.setGain(0));
                    break;
                case "Bass Boost":
                    makePreset(6,5,4,3,2);
                    break;
                case "Bass Low":
                    makePreset(-6,-5,-4,-3,-2);
                    break;
                case "Rock":
                    bands.get(0).setGain(3);
                    bands.get(1).setGain(2.4);
                    bands.get(2).setGain(1.2);
                    bands.get(3).setGain(0);
                    bands.get(4).setGain(-0.6);
                    bands.get(5).setGain(-1.2);
                    bands.get(6).setGain(0.4);
                    bands.get(7).setGain(1.4);
                    bands.get(8).setGain(3);
                    break;
                case "Treble":
                    makePreset(9,9,9,5,0,4,11,11,11,11);
                    break;
                case "Dance":
                    makePreset(6, 4.8, 2.4, 0.6, 1.2, 1.8, 2.6, 1.2, 0);
                    break;
                case "Alt. Bass":
                    makePreset(12,11,10,4,0,-4,-6,-8,-9,-9);
                    break;
                default:
                    break;

            }
        });
    }
}
