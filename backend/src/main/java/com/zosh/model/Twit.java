package com.zosh.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@RequiredArgsConstructor
public class Twit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // 외래키를 매핑할 때 사용; name 속성에는 매핑할 외래키 이름 지정
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "com_id") // 외래키를 매핑할 때 사용; name 속성에는 매핑할 외래키 이름 지정
    private Community community;

    @Column(nullable = false)
    private String content; 

    @OneToMany(mappedBy = "twit", cascade = CascadeType.ALL) // 붙는 엔티티가 List 1, 상대가 M
    private List<Like> likes = new ArrayList<>(); // 여러개의 twit에 하나의 like 리스트

//    @OneToMany(mappedBy = "twit", cascade = CascadeType.ALL) // 붙는 엔티티가 List 1, 상대가 M
//    private List<Notification> notifications = new ArrayList<>(); // 여러개의 twit에 하나의 like 리스트

    @OneToMany(cascade = {CascadeType.ALL, CascadeType.REMOVE}) // 붙는 엔티티가 List 1, 상대가 M
    @JoinColumn(name = "twit_id")
    private List<Twit> replyTwits = new ArrayList<>(); //여러개의 twit에 하나의 reply 리스트

    @ManyToMany(cascade = CascadeType.DETACH)
    private List<User> retwitUser = new ArrayList<>();

    @ManyToOne // 붙는 엔티티가 M, 상대가 1
    private Twit replyFor; // 하나의 트윗에 대해 M개의 댓글, Twit의 내용을 넣기 위해 사용

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime retwitAt;

    private String editedAt; 
    
    @Column(nullable = false)
    private int viewCount;

    private String image; 
    private String video;
    private String thumbnail;
    
    @Column(nullable = false)
    private boolean isEdited = false; 

    private String location;
    
    private boolean isCom = false;
    private boolean isReply; 
    private boolean isTwit; 
    private boolean is_liked = false;
    private boolean is_notification = false; 
    private boolean is_retwit = false;
    
    private String ethicrate; //윤리수치 저장
    private int ethiclabel; //수치중 가장 큰것을 라벨값으로 저장
    private int ethicrateMAX;// 최고수치 저장
}
