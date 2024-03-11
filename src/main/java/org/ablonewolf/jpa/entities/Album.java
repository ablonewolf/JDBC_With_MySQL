package org.ablonewolf.jpa.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "albums")
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@ToString
public class Album implements Comparable<Album> {
    @Id
    @Column(name = "album_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Integer id;

    @NonNull
    @Column(name = "album_name")
    private String name;

    @Override
    public int compareTo(Album album) {
        return this.getName().compareTo(album.getName());
    }
}
