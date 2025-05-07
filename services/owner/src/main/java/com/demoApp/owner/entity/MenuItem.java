package com.demoApp.owner.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "menu_items")
@Data
@NoArgsConstructor
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuType menuType;

    private boolean available = true;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    public enum MenuType {
        APPETIZER, MAIN_COURSE, DESSERT, BEVERAGE
    }

    public BigDecimal getPrice() {
        return price;
    }
}