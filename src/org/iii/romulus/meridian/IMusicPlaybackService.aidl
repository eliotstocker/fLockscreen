package org.iii.romulus.meridian;

interface IMusicPlaybackService
{
    void openfile(String path);
    boolean isPlaying();
    void stop();
    void pause();
    void play();
    void prev();
    void next();
    long duration();
    long position();
    long seek(long pos);
    String getTrackName();
    String getAlbumName();
    long getAlbumId();
    String getArtistName();
    String getComposerName();
    int getRating();
    String getPath();
    void setShuffleMode(int shufflemode);
    int getShuffleMode();
    void setRepeatMode(int repeatmode);
    int getRepeatMode();
    int getMediaMountedCount();
    void setRating(int rating);
    void setStopTime(long delay);
    void setQueue(int type, String fetch, in String[] filepaths);
}

