// src/main/java/com/example/server/ServerApp.java
package com.example.server;

import com.example.dao.AthleteDAO;
import com.example.dao.CoachDAO;
import com.example.dao.EquipmentDAO;
import com.example.client.model.Athlete;
import com.example.client.model.Coach;
import com.example.client.model.Equipment;
import com.google.gson.Gson;

import java.sql.SQLException;
import java.util.List;

import static spark.Spark.*;

public class ServerApp {
    public static void main(String[] args) {
        // Инициализация базы данных
        Database.init();

        // Инициализация DAO
        AthleteDAO athleteDAO = new AthleteDAO();
        CoachDAO coachDAO = new CoachDAO();
        EquipmentDAO equipmentDAO = new EquipmentDAO();
        Gson gson = new Gson();

        // Настройка SparkJava
        port(4567); // Порт сервера

        // CORS настройки (если требуется)
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.type("application/json");
        });

        // Эндпоинты для Athletes
        path("/athletes", () -> {
            // Получение всех спортсменов
            get("", (req, res) -> {
                try {
                    List<Athlete> athletes = athleteDAO.getAll();
                    return gson.toJson(athletes);
                } catch (SQLException e) {
                    res.status(500);
                    return gson.toJson("Ошибка получения спортсменов: " + e.getMessage());
                }
            });

            // Получение спортсмена по ID
            get("/:id", (req, res) -> {
                int id;
                try {
                    id = Integer.parseInt(req.params(":id"));
                } catch (NumberFormatException e) {
                    res.status(400);
                    return gson.toJson("Неверный формат ID.");
                }

                try {
                    Athlete athlete = athleteDAO.getById(id);
                    if (athlete == null) {
                        res.status(404);
                        return gson.toJson("Спортсмен не найден.");
                    }
                    return gson.toJson(athlete);
                } catch (SQLException e) {
                    res.status(500);
                    return gson.toJson("Ошибка получения спортсмена: " + e.getMessage());
                }
            });

            // Создание нового спортсмена
            post("", (req, res) -> {
                Athlete athlete;
                try {
                    athlete = gson.fromJson(req.body(), Athlete.class);
                    athlete = athleteDAO.create(athlete);
                    res.status(201); // Created
                    return gson.toJson(athlete);
                } catch (SQLException e) {
                    res.status(500);
                    return gson.toJson("Ошибка создания спортсмена: " + e.getMessage());
                } catch (Exception e) {
                    res.status(400);
                    return gson.toJson("Неверный формат данных: " + e.getMessage());
                }
            });

            // Обновление существующего спортсмена
            put("/:id", (req, res) -> {
                int id;
                try {
                    id = Integer.parseInt(req.params(":id"));
                } catch (NumberFormatException e) {
                    res.status(400);
                    return gson.toJson("Неверный формат ID.");
                }

                Athlete athlete;
                try {
                    athlete = gson.fromJson(req.body(), Athlete.class);
                    athlete.setId(id); // Установка ID из URL
                    boolean updated = athleteDAO.update(athlete);
                    if (updated) {
                        return gson.toJson(athlete);
                    } else {
                        res.status(404);
                        return gson.toJson("Спортсмен не найден для обновления.");
                    }
                } catch (SQLException e) {
                    res.status(500);
                    return gson.toJson("Ошибка обновления спортсмена: " + e.getMessage());
                } catch (Exception e) {
                    res.status(400);
                    return gson.toJson("Неверный формат данных: " + e.getMessage());
                }
            });

            // Удаление спортсмена по ID
            delete("/:id", (req, res) -> {
                int id;
                try {
                    id = Integer.parseInt(req.params(":id"));
                } catch (NumberFormatException e) {
                    res.status(400);
                    return gson.toJson("Неверный формат ID.");
                }

                try {
                    boolean deleted = athleteDAO.delete(id);
                    if (deleted) {
                        return gson.toJson("Спортсмен успешно удален.");
                    } else {
                        res.status(404);
                        return gson.toJson("Спортсмен не найден.");
                    }
                } catch (SQLException e) {
                    res.status(500);
                    return gson.toJson("Ошибка удаления спортсмена: " + e.getMessage());
                }
            });
        });

        // Эндпоинты для Coaches
        path("/coaches", () -> {
            // Получение всех тренеров
            get("", (req, res) -> {
                try {
                    List<Coach> coaches = coachDAO.getAll();
                    return gson.toJson(coaches);
                } catch (SQLException e) {
                    res.status(500);
                    return gson.toJson("Ошибка получения тренеров: " + e.getMessage());
                }
            });

            // Получение тренера по ID
            get("/:id", (req, res) -> {
                int id;
                try {
                    id = Integer.parseInt(req.params(":id"));
                } catch (NumberFormatException e) {
                    res.status(400);
                    return gson.toJson("Неверный формат ID.");
                }

                try {
                    Coach coach = coachDAO.getById(id);
                    if (coach == null) {
                        res.status(404);
                        return gson.toJson("Тренер не найден.");
                    }
                    return gson.toJson(coach);
                } catch (SQLException e) {
                    res.status(500);
                    return gson.toJson("Ошибка получения тренера: " + e.getMessage());
                }
            });

            // Создание нового тренера
            post("", (req, res) -> {
                Coach coach;
                try {
                    coach = gson.fromJson(req.body(), Coach.class);
                    coach = coachDAO.create(coach);
                    res.status(201); // Created
                    return gson.toJson(coach);
                } catch (SQLException e) {
                    res.status(500);
                    return gson.toJson("Ошибка создания тренера: " + e.getMessage());
                } catch (Exception e) {
                    res.status(400);
                    return gson.toJson("Неверный формат данных: " + e.getMessage());
                }
            });

            // Обновление существующего тренера
            put("/:id", (req, res) -> {
                int id;
                try {
                    id = Integer.parseInt(req.params(":id"));
                } catch (NumberFormatException e) {
                    res.status(400);
                    return gson.toJson("Неверный формат ID.");
                }

                Coach coach;
                try {
                    coach = gson.fromJson(req.body(), Coach.class);
                    coach.setId(id); // Установка ID из URL
                    boolean updated = coachDAO.update(coach);
                    if (updated) {
                        return gson.toJson(coach);
                    } else {
                        res.status(404);
                        return gson.toJson("Тренер не найден для обновления.");
                    }
                } catch (SQLException e) {
                    res.status(500);
                    return gson.toJson("Ошибка обновления тренера: " + e.getMessage());
                } catch (Exception e) {
                    res.status(400);
                    return gson.toJson("Неверный формат данных: " + e.getMessage());
                }
            });

            // Удаление тренера по ID
            delete("/:id", (req, res) -> {
                int id;
                try {
                    id = Integer.parseInt(req.params(":id"));
                } catch (NumberFormatException e) {
                    res.status(400);
                    return gson.toJson("Неверный формат ID.");
                }

                try {
                    boolean deleted = coachDAO.delete(id);
                    if (deleted) {
                        return gson.toJson("Тренер успешно удален.");
                    } else {
                        res.status(404);
                        return gson.toJson("Тренер не найден.");
                    }
                } catch (SQLException e) {
                    res.status(500);
                    return gson.toJson("Ошибка удаления тренера: " + e.getMessage());
                }
            });
        });

        // Эндпоинты для Equipment
        path("/equipment", () -> {
            // Получение всего оборудования
            get("", (req, res) -> {
                try {
                    List<Equipment> equipmentList = equipmentDAO.getAll();
                    return gson.toJson(equipmentList);
                } catch (SQLException e) {
                    res.status(500);
                    return gson.toJson("Ошибка получения оборудования: " + e.getMessage());
                }
            });

            // Получение оборудования по ID
            get("/:id", (req, res) -> {
                int id;
                try {
                    id = Integer.parseInt(req.params(":id"));
                } catch (NumberFormatException e) {
                    res.status(400);
                    return gson.toJson("Неверный формат ID.");
                }

                try {
                    Equipment equipment = equipmentDAO.getById(id);
                    if (equipment == null) {
                        res.status(404);
                        return gson.toJson("Оборудование не найдено.");
                    }
                    return gson.toJson(equipment);
                } catch (SQLException e) {
                    res.status(500);
                    return gson.toJson("Ошибка получения оборудования: " + e.getMessage());
                }
            });

            // Создание нового оборудования
            post("", (req, res) -> {
                Equipment equipment;
                try {
                    equipment = gson.fromJson(req.body(), Equipment.class);
                    equipment = equipmentDAO.create(equipment);
                    res.status(201); // Created
                    return gson.toJson(equipment);
                } catch (SQLException e) {
                    res.status(500);
                    return gson.toJson("Ошибка создания оборудования: " + e.getMessage());
                } catch (Exception e) {
                    res.status(400);
                    return gson.toJson("Неверный формат данных: " + e.getMessage());
                }
            });

            // Обновление существующего оборудования
            put("/:id", (req, res) -> {
                int id;
                try {
                    id = Integer.parseInt(req.params(":id"));
                } catch (NumberFormatException e) {
                    res.status(400);
                    return gson.toJson("Неверный формат ID.");
                }

                Equipment equipment;
                try {
                    equipment = gson.fromJson(req.body(), Equipment.class);
                    equipment.setId(id); // Установка ID из URL
                    boolean updated = equipmentDAO.update(equipment);
                    if (updated) {
                        return gson.toJson(equipment);
                    } else {
                        res.status(404);
                        return gson.toJson("Оборудование не найдено для обновления.");
                    }
                } catch (SQLException e) {
                    res.status(500);
                    return gson.toJson("Ошибка обновления оборудования: " + e.getMessage());
                } catch (Exception e) {
                    res.status(400);
                    return gson.toJson("Неверный формат данных: " + e.getMessage());
                }
            });

            // Удаление оборудования по ID
            delete("/:id", (req, res) -> {
                int id;
                try {
                    id = Integer.parseInt(req.params(":id"));
                } catch (NumberFormatException e) {
                    res.status(400);
                    return gson.toJson("Неверный формат ID.");
                }

                try {
                    boolean deleted = equipmentDAO.delete(id);
                    if (deleted) {
                        return gson.toJson("Оборудование успешно удалено.");
                    } else {
                        res.status(404);
                        return gson.toJson("Оборудование не найдено.");
                    }
                } catch (SQLException e) {
                    res.status(500);
                    return gson.toJson("Ошибка удаления оборудования: " + e.getMessage());
                }
            });
        });

        // Эндпоинт для проверки работоспособности сервера
        get("/hello", (req, res) -> "Hello from Spark!");

        System.out.println("Сервер запущен на порту 4567.");
    }
}