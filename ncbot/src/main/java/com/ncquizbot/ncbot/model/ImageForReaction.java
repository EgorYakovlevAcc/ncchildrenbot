package com.ncquizbot.ncbot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;

@Entity
@Table(name = "images_for_reactions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImageForReaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String type;
    @Lob
    private byte[] image;
}
