// src/main/java/com/example/client/model/Athlete.java
package com.example.client.model;

import com.example.client.model.enums.Rank;

/**
 * Класс модели для спортсмена.
 */
public class Athlete {
    private int id;
    private String name;
    private int age;
    private Rank rank;
    private int coachId;

    // Конструкторы
    public Athlete() {
    }

    public Athlete(int id, String name, int age, Rank rank, int coachId) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.rank = rank;
        this.coachId = coachId;
    }

    // Геттеры и Сеттеры

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    } 

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    } 

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    } 

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    } 

    public int getCoachId() {
        return coachId;
    }

    public void setCoachId(int coachId) {
        this.coachId = coachId;
    } 
}