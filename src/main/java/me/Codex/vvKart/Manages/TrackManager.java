package me.Codex.vvKart.Manages;

import me.Codex.vvKart.Main;
import me.Codex.vvKart.Models.Track;
import org.bukkit.Location;

import java.util.*;

public class TrackManager {

    private final Main plugin;
    private final Map<String, Track> tracks;

    public TrackManager(Main plugin) {
        this.plugin = plugin;
        this.tracks = new HashMap<>();
    }

    /**
     * Create a new track
     */
    public boolean createTrack(String name) {
        if (tracks.containsKey(name. toLowerCase())) {
            return false;
        }

        Track track = new Track(name, plugin);
        tracks.put(name.toLowerCase(), track);
        plugin.getDataManager().saveTrack(track);

        return true;
    }

    /**
     * Delete a track
     */
    public boolean deleteTrack(String name) {
        Track track = tracks.remove(name. toLowerCase());
        if (track == null) {
            return false;
        }

        // Remove from active races if any
        plugin.getRaceManager().cancelRace(track);

        // Delete file
        plugin.getDataManager(). deleteTrack(name);

        return true;
    }

    /**
     * Get a track by name
     */
    public Track getTrack(String name) {
        return tracks.get(name.toLowerCase());
    }

    /**
     * Check if a track exists
     */
    public boolean trackExists(String name) {
        return tracks.containsKey(name.toLowerCase());
    }

    /**
     * Get all tracks
     */
    public Collection<Track> getAllTracks() {
        return tracks.values();
    }

    /**
     * Get all track names
     */
    public Set<String> getTrackNames() {
        return tracks.keySet();
    }

    /**
     * Add a track (used by DataManager)
     */
    public void addTrack(Track track) {
        tracks.put(track.getName().toLowerCase(), track);
    }

//    /**
//     * Get track at location (for interaction)
//     */
//    public Track getTrackAtLocation(Location location) {
//        for (Track track : tracks.values()) {
//            if (track.getFinishPos1() != null && track.getFinish().distance(location) < 3) {
//                return track;
//            }
//        }
//        return null;
//    }

    public boolean isTrackReady(Track track) {
        if (track.getHub() == null) return false;

        // ========== FIX: HAAKJES TOEVOEGEN!  ==========
        boolean hasFinish = (track.getFinish() != null) ||
                (track.getFinishPos1() != null && track.getFinishPos2() != null);
        if (!hasFinish) return false;
        // ============================================

        if (track.getStartPositions().isEmpty()) return false;
        if (track.getCheckpoints().isEmpty()) return false;

        return true;
    }

    public List<String> getTrackErrors(Track track) {
        List<String> errors = new ArrayList<>();

        if (track.getHub() == null) {
            errors.add("Hub locatie is niet ingesteld");
        }

        // ========== FIX: HAAKJES EN LOGICA ==========
        boolean hasOldFinish = track.getFinish() != null;
        boolean hasNewFinish = track.getFinishPos1() != null && track. getFinishPos2() != null;
        boolean hasFinish = hasOldFinish || hasNewFinish;

        if (!hasFinish) {
            errors.add("Finish lijn is niet ingesteld");
        } else if (track.getFinishPos1() != null && track.getFinishPos2() == null) {
            errors.add("Finish zone is incompleet (alleen pos1 ingesteld)");
        } else if (track.getFinishPos1() == null && track.getFinishPos2() != null) {
            errors.add("Finish zone is incompleet (alleen pos2 ingesteld)");
        }
        // ===========================================

        if (track.getStartPositions().isEmpty()) {
            errors.add("Geen startposities ingesteld");
        }
        if (track.getCheckpoints().isEmpty()) {
            errors.add("Geen checkpoints ingesteld");
        }

        return errors;
    }
}