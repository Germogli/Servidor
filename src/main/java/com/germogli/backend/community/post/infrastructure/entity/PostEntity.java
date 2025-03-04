package com.germogli.backend.community.post.infrastructure.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "posts")
public class PostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "post_type", nullable = false, length = 50)
    private String postType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "multimedia_content", length = 255)
    private String multimediaContent;

    @Column(name = "post_date", nullable = false)
    private LocalDateTime postDate;

    @Column(name = "group_id")
    private Integer groupId;

    @Column(name = "thread_id")
    private Integer threadId;
}
