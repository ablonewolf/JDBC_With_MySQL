package org.ablonewolf.jpa.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

@Entity
@Table(name = "artists")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Artist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "artist_id")
    @Setter(AccessLevel.NONE)
    private Integer id;

    @Column(name = "artist_name")
    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "artist_id")
    @Setter(AccessLevel.NONE)
    private List<Album> albums = new ArrayList<>();

    public Artist(String name) {
        this.name = name;
    }

    public void removeDuplicates() {
        var albumSet = new TreeSet<>(albums);
        albums.clear();
        albums.addAll(albumSet);
    }

    public void addAlbum(String albumName) {
        this.albums.add(new Album(albumName));
    }

}
