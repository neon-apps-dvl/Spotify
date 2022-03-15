package com.pixel.spotify.spotify.models;

import java.util.ArrayList;
import java.util.List;

public class Playlists {
    public List <PlaylistModel> items;
    private List <String> ids;
    
    public Playlists () {
        items = new ArrayList <> ();
        ids = new ArrayList <> ();
    }

    public Playlists (List <PlaylistModel> items, List <String> ids) {
        this.items = new ArrayList <> (items);
        this.ids = new ArrayList <> (ids);
    }

    public Playlists (List <PlaylistModel> items) {
        this.items = new ArrayList <> (items);
    }

    public void add (PlaylistModel playlist) {
        items.add (playlist);
        ids.add (playlist.id);
    }
    
    public void add (List <PlaylistModel> playlists) {
        this.items.addAll (playlists);
        
        for (PlaylistModel p : playlists) {
            ids.add (p.id);
        }
    }
        
    public void add (List <PlaylistModel> playlists, List <String> ids) {
        this.items.addAll (playlists);
        this.ids.addAll (ids);
    }
    
    public boolean contains (PlaylistModel playlist) {
        if (this.ids.contains (playlist.id)) return true;
        
        return false;
    }
}
