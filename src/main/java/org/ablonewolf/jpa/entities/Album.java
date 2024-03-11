package org.ablonewolf.jpa.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "albums")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Album implements Comparable<Album> {
    @Id
    @Column(name = "album_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Integer id;

    @Column(name = "album_name")
    private String name;

    public Album(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Album album) {
        return this.getName().compareTo(album.getName());
    }
}
