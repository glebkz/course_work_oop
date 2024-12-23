// src/main/java/com/example/client/model/Coach.java
package com.example.client.model;

import com.example.client.model.enums.Specialization;

/**
 * Класс модели для тренера.
 */
public class Coach {
    private int id;
    private String name;
    private Specialization specialization;
    private int experienceYears;

    // Конструкторы
    public Coach() {
    }

    public Coach(int id, String name, Specialization specialization, int experienceYears) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.experienceYears = experienceYears;
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

    public Specialization getSpecialization() {
        return specialization;
    }

    public void setSpecialization(Specialization specialization) {
        this.specialization = specialization;
    } 

    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }
}