package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User u = new User(name,mobile);
        users.add(u);
        return u;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        artists.add(artist);
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        Album album = new Album(title);
        Artist artist = new Artist(artistName);
        boolean b = true;
        for(Artist a : artists)
        {
            if(a.getName().equals(artistName))
            {
                artist = a;
                b = false;
            }
        }
        if(b)
        {
            createArtist(artistName);
        }
        if(artistAlbumMap.containsKey(artist))
        {
            List<Album> al = artistAlbumMap.get(artist);
            al.add(album);
            artistAlbumMap.put(artist,al);
        }else{
            List<Album> al = new ArrayList<Album>();
            al.add(album);
            artistAlbumMap.put(artist,al);
        }
        albums.add(album);
        return album;


    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        boolean b = true;
        Album album = new Album(albumName);
        for(Album a : albums)
        {
            if(a.getTitle().equals(albumName))
            {
                album = a;
                b = false;
            }
        }
        if(b)
        {
            throw new Exception("Album does not exist");
        }else{
            Song song = new Song(title,length);
            songs.add(song);
            if(albumSongMap.containsKey(album))
            {
                List<Song> al = albumSongMap.get(album);
                al.add(song);
                albumSongMap.put(album,al);
            }else{
                List<Song> al = new ArrayList<Song>();
                al.add(song);
                albumSongMap.put(album,al);
            }
            return song;
        }





    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User user = null;
        for (User u : users) {
            if (u.getMobile().equals(mobile)) {
                user = u;
                break;
            }
        }
        if (user == null) {
            throw new Exception("User does not exist");
        }
        List<Song> match = new ArrayList<>();
        for(Song s : songs)
        {
            if(s.getTitle().length() == length)
                match.add(s);
        }
        Playlist playlist = new Playlist(title);
        playlistSongMap.put(playlist, match);


        creatorPlaylistMap.put(user,playlist);
        List<Playlist> playlists = userPlaylistMap.getOrDefault(user, new ArrayList<>());
        playlists.add(playlist);
        userPlaylistMap.put(user, playlists);
        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user = null;
        for (User u : users) {
            if (u.getMobile().equals(mobile)) {
                user = u;
                break;
            }
        }
        if (user == null) {
            throw new Exception("User does not exist");
        }

        Playlist playlist = new Playlist(title);
        List<Song> songsToAdd = new ArrayList<>();
        for (String songTitle : songTitles) {
            // Step 4: Find the song with the specified title
            boolean songFound = false;
            for (Song song : songs) {
                if (song.getTitle().equals(songTitle)) {
                    songsToAdd.add(song);
                    songFound = true;
                    break;
                }
            }
            if (!songFound) {
                throw new Exception("Song with title '" + songTitle + "' does not exist");
            }
        }

        // Step 5: Add the selected songs to the playlist
        playlistSongMap.put(playlist, songsToAdd);

        // Step 6: Associate the playlist with the user
        creatorPlaylistMap.put(user, playlist);
        List<Playlist> userPlaylists = userPlaylistMap.getOrDefault(user, new ArrayList<>());
        userPlaylists.add(playlist);
        userPlaylistMap.put(user, userPlaylists);

        // Step 7: Return the created playlist
        return playlist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User user = null;
        for (User u : users) {
            if (u.getMobile().equals(mobile)) {
                user = u;
                break;
            }
        }
        if (user == null) {
            throw new Exception("User does not exist");
        }

        // Step 2: Iterate through the playlists to find the matching playlist
        Playlist foundPlaylist = null;
        for (Map.Entry<Playlist, List<User>> entry : playlistListenerMap.entrySet()) {
            Playlist playlist = entry.getKey();
            if (playlist.getTitle().equals(playlistTitle)) {
                foundPlaylist = playlist;
                break;
            }
        }

        // Step 3: If the playlist is found, add the user as a listener
        if (foundPlaylist != null) {
            List<User> listeners = playlistListenerMap.getOrDefault(foundPlaylist, new ArrayList<>());
            if (!listeners.contains(user)) {
                listeners.add(user);
                playlistListenerMap.put(foundPlaylist, listeners);
            }
        } else {
            throw new Exception("Playlist with title '" + playlistTitle + "' does not exist");
        }

        // Step 4: Return the found playlist
        return foundPlaylist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        User user = null;
        for (User u : users) {
            if (u.getMobile().equals(mobile)) {
                user = u;
                break;
            }
        }
        if (user == null) {
            throw new Exception("User does not exist");
        }

        // Step 2: Find the song based on the provided title
        Song likedSong = null;
        for (Song song : songs) {
            if (song.getTitle().equals(songTitle)) {
                likedSong = song;
                break;
            }
        }
        if (likedSong == null) {
            throw new Exception("Song with title '" + songTitle + "' does not exist");
        }

        // Step 3: Check if the user has already liked the song
        List<User> likedUsers = songLikeMap.getOrDefault(likedSong, new ArrayList<>());
        if (likedUsers.contains(user)) {
            // User has already liked the song, do nothing
            return likedSong;
        }

        // Step 4: Add the user to the list of users who liked the song
        likedUsers.add(user);
        songLikeMap.put(likedSong, likedUsers);

        // Step 5: Retrieve the corresponding artist of the song
        Artist artist = null;
        for (Map.Entry<Artist, List<Album>> entry : artistAlbumMap.entrySet()) {
            List<Album> albums = entry.getValue();
            for (Album album : albums) {
                List<Song> songs = albumSongMap.getOrDefault(album, new ArrayList<>());
                if (songs.contains(likedSong)) {
                    artist = entry.getKey();
                    break;
                }
            }
            if (artist != null) {
                break;
            }
        }

        // Step 6: Increment the likes of the artist
        if (artist != null) {
            artist.setLikes(artist.getLikes() + 1);
        }

        // Step 7: Return the updated song
        return likedSong;
    }

    public String mostPopularArtist() {

        String mostPopularArtistName = null;
        int maxLikes = Integer.MIN_VALUE;

        // Step 1: Iterate through the artists to find the most popular artist
        for (Artist artist : artists) {
            if (artist.getLikes() > maxLikes) {
                mostPopularArtistName = artist.getName();
                maxLikes = artist.getLikes();
            }
        }

        // Step 2: Return the name of the most popular artist
        return mostPopularArtistName;
    }

    public String mostPopularSong() {

        String mostPopularSongTitle = null;
        int maxLikes = Integer.MIN_VALUE;

        // Step 1: Iterate through the songs to find the most popular song
        for (Song song : songs) {
            if (song.getLikes() > maxLikes) {
                mostPopularSongTitle = song.getTitle();
                maxLikes = song.getLikes();
            }
        }

        // Step 2: Return the title of the most popular song
        return mostPopularSongTitle;
    }
}
