package zechs.zplex.adapter;

public class EpisodeItem {
    String show, episode, episodeTitle, playUrl, bytes;

    public EpisodeItem(String show, String episode, String episodeTitle, String playUrl, String bytes) {
        this.show = show;
        this.episode = episode;
        this.episodeTitle = episodeTitle;
        this.playUrl = playUrl;
        this.bytes = bytes;
    }

    public String getShow() {
        return show;
    }

    public String getEpisode() {
        return episode;
    }

    public String getEpisodeTitle() {
        return episodeTitle;
    }

    public String getPlayUrl() {
        return playUrl;
    }

    public String getBytes() {
        return bytes;
    }
}
