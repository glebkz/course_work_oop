// src/main/java/com/example/client/ClientApp.java
package com.example.client;

import com.google.gson.Gson;
import com.example.client.model.Athlete;
import com.example.client.model.Coach;
import com.example.client.model.Equipment;
import com.example.client.model.enums.Rank;
import com.example.client.model.enums.Specialization;
import com.example.client.model.enums.EquipmentType;
import com.example.client.model.enums.EquipmentStatus;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Клиентское приложение для управления спортивными данными.
 */
public class ClientApp extends JFrame {
    private final Gson gson = new Gson();

    // Поля для моделей таблиц
    private DefaultTableModel athleteModel;
    private DefaultTableModel coachModel;
    private DefaultTableModel equipmentModel;

    public ClientApp() {
        super("Клиент управления спортом");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 600); // Увеличен размер для лучшей видимости
        setLocationRelativeTo(null);

        // Инициализация моделей таблиц
        initModels();

        // Создание вкладок
        JTabbedPane tabbedPane = new JTabbedPane();

        // Вкладка для спортсменов (Athletes)
        tabbedPane.addTab("Спортсмены", createAthletesPanel());

        // Вкладка для тренеров (Coaches)
        tabbedPane.addTab("Тренеры", createCoachesPanel());

        // Вкладка для оборудования (Equipment)
        tabbedPane.addTab("Оборудование", createEquipmentPanel());

        add(tabbedPane);
    }

    /**
     * Инициализация моделей таблиц.
     */
    private void initModels() {
        athleteModel = new DefaultTableModel(new Object[]{"ID", "Имя", "Возраст", "Разряд", "ID Тренера"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Запрет редактирования ячеек напрямую
            }
        };

        coachModel = new DefaultTableModel(new Object[]{"ID", "Имя", "Специализация", "Опыт (лет)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Запрет редактирования ячеек напрямую
            }
        };

        equipmentModel = new DefaultTableModel(new Object[]{"ID", "Название", "Тип", "Количество", "Статус"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Запрет редактирования ячеек напрямую
            }
        };
    }

    /**
     * Создание панели для управления спортсменами.
     */
    private JPanel createAthletesPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Панель поиска
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.add(new JLabel("Имя:"));
        JTextField nameField = new JTextField(15);
        searchPanel.add(nameField);

        searchPanel.add(new JLabel("Возраст:"));
        JTextField ageField = new JTextField(5);
        searchPanel.add(ageField);

        searchPanel.add(new JLabel("Разряд:"));
        JComboBox<Rank> rankComboBox = new JComboBox<>(Rank.values());
        rankComboBox.insertItemAt(null, 0); // Добавление пустого варианта
        rankComboBox.setSelectedIndex(0);
        searchPanel.add(rankComboBox);

        searchPanel.add(new JLabel("ID Тренера:"));
        JTextField coachIdField = new JTextField(5);
        searchPanel.add(coachIdField);

        JButton searchButton = new JButton("Поиск");
        searchButton.addActionListener(e -> {
            Rank selectedRank = (Rank) rankComboBox.getSelectedItem();
            loadAthletes(
                    nameField.getText().trim(),
                    ageField.getText().trim(),
                    selectedRank != null ? selectedRank.toString() : "",
                    coachIdField.getText().trim()
            );
        });
        searchPanel.add(searchButton);

        panel.add(searchPanel, BorderLayout.NORTH);

        // Таблица для отображения спортсменов
        JTable athleteTable = new JTable(athleteModel);
        JScrollPane scrollPane = new JScrollPane(athleteTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Панель кнопок CRUD
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Добавить");
        addButton.addActionListener(e -> showAthleteDialog(null));
        JButton editButton = new JButton("Редактировать");
        editButton.addActionListener(e -> {
            int selectedRow = athleteTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Пожалуйста, выберите спортсмена для редактирования.");
                return;
            }
            Athlete athlete = getAthleteFromTable(selectedRow);
            showAthleteDialog(athlete);
        });
        JButton deleteButton = new JButton("Удалить");
        deleteButton.addActionListener(e -> {
            int selectedRow = athleteTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Пожалуйста, выберите спортсмена для удаления.");
                return;
            }
            int athleteId = (int) athleteModel.getValueAt(selectedRow, 0);
            deleteAthlete(athleteId);
        });

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Загрузка данных при запуске вкладки
        loadAthletes("", "", "", "");

        return panel;
    }

    /**
     * Создание панели для управления тренерами.
     */
    private JPanel createCoachesPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Панель поиска
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.add(new JLabel("Имя:"));
        JTextField nameField = new JTextField(15);
        searchPanel.add(nameField);

        searchPanel.add(new JLabel("Специализация:"));
        JComboBox<Specialization> specializationComboBox = new JComboBox<>(Specialization.values());
        specializationComboBox.insertItemAt(null, 0); // Добавление пустого варианта
        specializationComboBox.setSelectedIndex(0);
        searchPanel.add(specializationComboBox);

        searchPanel.add(new JLabel("Опыт (лет):"));
        JTextField experienceField = new JTextField(5);
        searchPanel.add(experienceField);

        JButton searchButton = new JButton("Поиск");
        searchButton.addActionListener(e -> {
            Specialization selectedSpec = (Specialization) specializationComboBox.getSelectedItem();
            loadCoaches(
                    nameField.getText().trim(),
                    selectedSpec != null ? selectedSpec.toString() : "",
                    experienceField.getText().trim()
            );
        });
        searchPanel.add(searchButton);

        panel.add(searchPanel, BorderLayout.NORTH);

        // Таблица для отображения тренеров
        JTable coachTable = new JTable(coachModel);
        JScrollPane scrollPane = new JScrollPane(coachTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Панель кнопок CRUD
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Добавить");
        addButton.addActionListener(e -> showCoachDialog(null));
        JButton editButton = new JButton("Редактировать");
        editButton.addActionListener(e -> {
            int selectedRow = coachTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Пожалуйста, выберите тренера для редактирования.");
                return;
            }
            Coach coach = getCoachFromTable(selectedRow);
            showCoachDialog(coach);
        });
        JButton deleteButton = new JButton("Удалить");
        deleteButton.addActionListener(e -> {
            int selectedRow = coachTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Пожалуйста, выберите тренера для удаления.");
                return;
            }
            int coachId = (int) coachModel.getValueAt(selectedRow, 0);
            deleteCoach(coachId);
        });

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Загрузка данных при запуске вкладки
        loadCoaches("", "", "");

        return panel;
    }

    /**
     * Создание панели для управления оборудованием.
     */
    private JPanel createEquipmentPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Панель поиска
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.add(new JLabel("Название:"));
        JTextField nameField = new JTextField(15);
        searchPanel.add(nameField);

        searchPanel.add(new JLabel("Тип:"));
        JComboBox<EquipmentType> typeComboBox = new JComboBox<>(EquipmentType.values());
        typeComboBox.insertItemAt(null, 0); // Добавление пустого варианта
        typeComboBox.setSelectedIndex(0);
        searchPanel.add(typeComboBox);

        searchPanel.add(new JLabel("Количество:"));
        JTextField quantityField = new JTextField(5);
        searchPanel.add(quantityField);

        searchPanel.add(new JLabel("Статус:"));
        JComboBox<EquipmentStatus> statusComboBox = new JComboBox<>(EquipmentStatus.values());
        statusComboBox.insertItemAt(null, 0); // Добавление пустого варианта
        statusComboBox.setSelectedIndex(0);
        searchPanel.add(statusComboBox);

        JButton searchButton = new JButton("Поиск");
        searchButton.addActionListener(e -> {
            EquipmentType selectedType = (EquipmentType) typeComboBox.getSelectedItem();
            EquipmentStatus selectedStatus = (EquipmentStatus) statusComboBox.getSelectedItem();
            loadEquipment(
                    nameField.getText().trim(),
                    selectedType != null ? selectedType.toString() : "",
                    quantityField.getText().trim(),
                    selectedStatus != null ? selectedStatus.toString() : ""
            );
        });
        searchPanel.add(searchButton);

        panel.add(searchPanel, BorderLayout.NORTH);

        // Таблица для отображения оборудования
        JTable equipmentTable = new JTable(equipmentModel);
        JScrollPane scrollPane = new JScrollPane(equipmentTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Панель кнопок CRUD
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Добавить");
        addButton.addActionListener(e -> showEquipmentDialog(null));
        JButton editButton = new JButton("Редактировать");
        editButton.addActionListener(e -> {
            int selectedRow = equipmentTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Пожалуйста, выберите оборудование для редактирования.");
                return;
            }
            Equipment equipment = getEquipmentFromTable(selectedRow);
            showEquipmentDialog(equipment);
        });
        JButton deleteButton = new JButton("Удалить");
        deleteButton.addActionListener(e -> {
            int selectedRow = equipmentTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Пожалуйста, выберите оборудование для удаления.");
                return;
            }
            int equipmentId = (int) equipmentModel.getValueAt(selectedRow, 0);
            deleteEquipment(equipmentId);
        });

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Загрузка данных при запуске вкладки
        loadEquipment("", "", "", "");

        return panel;
    }

    // --------------------- Методы для Athletes ---------------------

    /**
     * Загрузка спортсменов с фильтрами и отображение в таблице.
     *
     * @param nameFilter     Фильтр по имени
     * @param ageFilter      Фильтр по возрасту
     * @param rankFilter     Фильтр по разряду
     * @param coachIdFilter  Фильтр по ID тренера
     */
    private void loadAthletes(String nameFilter, String ageFilter, String rankFilter, String coachIdFilter) {
        SwingUtilities.invokeLater(() -> {
            athleteModel.setRowCount(0); // Очистка таблицы
            try {
                StringBuilder urlStr = new StringBuilder("http://localhost:4567/athletes");
                boolean hasParam = false;
                if (!nameFilter.isEmpty() || !ageFilter.isEmpty() || !rankFilter.isEmpty() || !coachIdFilter.isEmpty()) {
                    urlStr.append("?");
                    if (!nameFilter.isEmpty()) {
                        urlStr.append("name=").append(encodeValue(nameFilter));
                        hasParam = true;
                    }
                    if (!ageFilter.isEmpty()) {
                        if (hasParam) urlStr.append("&");
                        urlStr.append("age=").append(encodeValue(ageFilter));
                        hasParam = true;
                    }
                    if (!rankFilter.isEmpty()) {
                        if (hasParam) urlStr.append("&");
                        urlStr.append("rank=").append(encodeValue(rankFilter));
                        hasParam = true;
                    }
                    if (!coachIdFilter.isEmpty()) {
                        if (hasParam) urlStr.append("&");
                        urlStr.append("coach_id=").append(encodeValue(coachIdFilter));
                    }
                }

                URL url = new URL(urlStr.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
                    Athlete[] athletes = gson.fromJson(reader, Athlete[].class);
                    for (Athlete athlete : athletes) {
                        athleteModel.addRow(new Object[]{
                                athlete.getId(),
                                athlete.getName(),
                                athlete.getAge(),
                                athlete.getRank(),
                                athlete.getCoachId()
                        });
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Ошибка при загрузке спортсменов: " + responseCode);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ошибка при загрузке спортсменов: " + e.getMessage());
            }
        });
    }

    /**
     * Отображение диалогового окна для добавления или редактирования спортсмена.
     *
     * @param athlete Спортсмен для редактирования или null для добавления нового
     */
    private void showAthleteDialog(Athlete athlete) {
        JDialog dialog = new JDialog(this, athlete == null ? "Добавить спортсмена" : "Редактировать спортсмена", true);
        dialog.setSize(400, 250);
        dialog.setLayout(new GridLayout(6, 2));

        dialog.add(new JLabel("Имя:"));
        JTextField nameField = new JTextField();
        dialog.add(nameField);

        dialog.add(new JLabel("Возраст:"));
        JTextField ageField = new JTextField();
        dialog.add(ageField);

        dialog.add(new JLabel("Разряд:"));
        JComboBox<Rank> rankComboBox = new JComboBox<>(Rank.values());
        dialog.add(rankComboBox);

        dialog.add(new JLabel("ID Тренера:"));
        JTextField coachIdField = new JTextField();
        dialog.add(coachIdField);

        JButton saveButton = new JButton("Сохранить");
        JButton cancelButton = new JButton("Отмена");
        dialog.add(saveButton);
        dialog.add(cancelButton);

        if (athlete != null) {
            nameField.setText(athlete.getName());
            ageField.setText(String.valueOf(athlete.getAge()));
            rankComboBox.setSelectedItem(athlete.getRank());
            coachIdField.setText(String.valueOf(athlete.getCoachId()));
        }

        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String ageStr = ageField.getText().trim();
            Rank rank = (Rank) rankComboBox.getSelectedItem();
            String coachIdStr = coachIdField.getText().trim();

            if (name.isEmpty() || ageStr.isEmpty() || rank == null) {
                JOptionPane.showMessageDialog(dialog, "Пожалуйста, заполните все обязательные поля (Имя, Возраст, Разряд).");
                return;
            }

            int age;
            try {
                age = Integer.parseInt(ageStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Возраст должен быть числом.");
                return;
            }

            Integer coachId = null;
            if (!coachIdStr.isEmpty()) {
                try {
                    coachId = Integer.parseInt(coachIdStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "ID Тренера должен быть числом.");
                    return;
                }
            }

            if (athlete == null) {
                // Создание нового спортсмена
                Athlete newAthlete = new Athlete();
                newAthlete.setName(name);
                newAthlete.setAge(age);
                newAthlete.setRank(rank);
                newAthlete.setCoachId(coachId != null ? coachId : 0);

                createAthlete(newAthlete);
            } else {
                // Обновление существующего спортсмена
                athlete.setName(name);
                athlete.setAge(age);
                athlete.setRank(rank);
                athlete.setCoachId(coachId != null ? coachId : 0);

                updateAthlete(athlete);
            }

            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Создание нового спортсмена.
     *
     * @param athlete Спортсмен для создания
     */
    private void createAthlete(Athlete athlete) {
        SwingUtilities.invokeLater(() -> {
            try {
                URL url = new URL("http://localhost:4567/athletes");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                String jsonInputString = gson.toJson(athlete);
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 201) { // Created
                    InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
                    Athlete createdAthlete = gson.fromJson(reader, Athlete.class);
                    athleteModel.addRow(new Object[]{
                            createdAthlete.getId(),
                            createdAthlete.getName(),
                            createdAthlete.getAge(),
                            createdAthlete.getRank(),
                            createdAthlete.getCoachId()
                    });
                    JOptionPane.showMessageDialog(this, "Спортсмен успешно добавлен.");
                } else {
                    InputStreamReader reader = new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8);
                    String error = gson.fromJson(reader, String.class);
                    JOptionPane.showMessageDialog(this, "Ошибка при добавлении спортсмена: " + error);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ошибка при добавлении спортсмена: " + e.getMessage());
            }
        });
    }

    /**
     * Обновление существующего спортсмена.
     *
     * @param athlete Спортсмен для обновления
     */
    private void updateAthlete(Athlete athlete) {
        SwingUtilities.invokeLater(() -> {
            try {
                URL url = new URL("http://localhost:4567/athletes/" + athlete.getId());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                String jsonInputString = gson.toJson(athlete);
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) { // OK
                    // Обновление строки в таблице
                    for (int i = 0; i < athleteModel.getRowCount(); i++) {
                        if ((int) athleteModel.getValueAt(i, 0) == athlete.getId()) {
                            athleteModel.setValueAt(athlete.getName(), i, 1);
                            athleteModel.setValueAt(athlete.getAge(), i, 2);
                            athleteModel.setValueAt(athlete.getRank(), i, 3);
                            athleteModel.setValueAt(athlete.getCoachId(), i, 4);
                            break;
                        }
                    }
                    JOptionPane.showMessageDialog(this, "Спортсмен успешно обновлен.");
                } else {
                    InputStreamReader reader = new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8);
                    String error = gson.fromJson(reader, String.class);
                    JOptionPane.showMessageDialog(this, "Ошибка при обновлении спортсмена: " + error);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ошибка при обновлении спортсмена: " + e.getMessage());
            }
        });
    }

    /**
     * Удаление спортсмена по ID.
     *
     * @param athleteId ID спортсмена для удаления
     */
    private void deleteAthlete(int athleteId) {
        SwingUtilities.invokeLater(() -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Вы уверены, что хотите удалить этого спортсмена?", "Подтверждение удаления", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            try {
                URL url = new URL("http://localhost:4567/athletes/" + athleteId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) { // OK
                    // Удаление строки из таблицы
                    for (int i = 0; i < athleteModel.getRowCount(); i++) {
                        if ((int) athleteModel.getValueAt(i, 0) == athleteId) {
                            athleteModel.removeRow(i);
                            break;
                        }
                    }
                    JOptionPane.showMessageDialog(this, "Спортсмен успешно удален.");
                } else {
                    InputStreamReader reader = new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8);
                    String error = gson.fromJson(reader, String.class);
                    JOptionPane.showMessageDialog(this, "Ошибка при удалении спортсмена: " + error);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ошибка при удалении спортсмена: " + e.getMessage());
            }
        });
    }

    /**
     * Получение объекта Athlete из выбранной строки таблицы.
     *
     * @param row Номер строки
     * @return Объект Athlete
     */
    private Athlete getAthleteFromTable(int row) {
        Athlete athlete = new Athlete();
        athlete.setId((int) athleteModel.getValueAt(row, 0));
        athlete.setName((String) athleteModel.getValueAt(row, 1));
        athlete.setAge((int) athleteModel.getValueAt(row, 2));
        athlete.setRank(Rank.valueOf(((String) athleteModel.getValueAt(row, 3)).toUpperCase()));
        athlete.setCoachId((int) athleteModel.getValueAt(row, 4));
        return athlete;
    }

    // --------------------- Методы для Coaches ---------------------

    /**
     * Загрузка тренеров с фильтрами и отображение в таблице.
     *
     * @param nameFilter          Фильтр по имени
     * @param specializationFilter Фильтр по специализации
     * @param experienceFilter     Фильтр по опыту
     */
    private void loadCoaches(String nameFilter, String specializationFilter, String experienceFilter) {
        SwingUtilities.invokeLater(() -> {
            coachModel.setRowCount(0); // Очистка таблицы
            try {
                StringBuilder urlStr = new StringBuilder("http://localhost:4567/coaches");
                boolean hasParam = false;
                if (!nameFilter.isEmpty() || !specializationFilter.isEmpty() || !experienceFilter.isEmpty()) {
                    urlStr.append("?");
                    if (!nameFilter.isEmpty()) {
                        urlStr.append("name=").append(encodeValue(nameFilter));
                        hasParam = true;
                    }
                    if (!specializationFilter.isEmpty()) {
                        if (hasParam) urlStr.append("&");
                        urlStr.append("specialization=").append(encodeValue(specializationFilter));
                        hasParam = true;
                    }
                    if (!experienceFilter.isEmpty()) {
                        if (hasParam) urlStr.append("&");
                        urlStr.append("experience_years=").append(encodeValue(experienceFilter));
                    }
                }

                URL url = new URL(urlStr.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
                    Coach[] coaches = gson.fromJson(reader, Coach[].class);
                    for (Coach coach : coaches) {
                        coachModel.addRow(new Object[]{
                                coach.getId(),
                                coach.getName(),
                                coach.getSpecialization(),
                                coach.getExperienceYears()
                        });
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Ошибка при загрузке тренеров: " + responseCode);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ошибка при загрузке тренеров: " + e.getMessage());
            }
        });
    }

    /**
     * Отображение диалогового окна для добавления или редактирования тренера.
     *
     * @param coach Тренер для редактирования или null для добавления нового
     */
    private void showCoachDialog(Coach coach) {
        JDialog dialog = new JDialog(this, coach == null ? "Добавить тренера" : "Редактировать тренера", true);
        dialog.setSize(400, 250);
        dialog.setLayout(new GridLayout(5, 2));

        dialog.add(new JLabel("Имя:"));
        JTextField nameField = new JTextField();
        dialog.add(nameField);

        dialog.add(new JLabel("Специализация:"));
        JComboBox<Specialization> specializationComboBox = new JComboBox<>(Specialization.values());
        dialog.add(specializationComboBox);

        dialog.add(new JLabel("Опыт (лет):"));
        JTextField experienceField = new JTextField();
        dialog.add(experienceField);

        JButton saveButton = new JButton("Сохранить");
        JButton cancelButton = new JButton("Отмена");
        dialog.add(saveButton);
        dialog.add(cancelButton);

        if (coach != null) {
            nameField.setText(coach.getName());
            specializationComboBox.setSelectedItem(coach.getSpecialization());
            experienceField.setText(String.valueOf(coach.getExperienceYears()));
        }

        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            Specialization specialization = (Specialization) specializationComboBox.getSelectedItem();
            String experienceStr = experienceField.getText().trim();

            if (name.isEmpty() || specialization == null || experienceStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Пожалуйста, заполните все обязательные поля (Имя, Специализация, Опыт).");
                return;
            }

            int experience;
            try {
                experience = Integer.parseInt(experienceStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Опыт должен быть числом.");
                return;
            }

            if (coach == null) {
                // Создание нового тренера
                Coach newCoach = new Coach();
                newCoach.setName(name);
                newCoach.setSpecialization(specialization);
                newCoach.setExperienceYears(experience);

                createCoach(newCoach);
            } else {
                // Обновление существующего тренера
                coach.setName(name);
                coach.setSpecialization(specialization);
                coach.setExperienceYears(experience);

                updateCoach(coach);
            }

            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Создание нового тренера.
     *
     * @param coach Тренер для создания
     */
    private void createCoach(Coach coach) {
        SwingUtilities.invokeLater(() -> {
            try {
                URL url = new URL("http://localhost:4567/coaches");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                String jsonInputString = gson.toJson(coach);
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 201) { // Created
                    InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
                    Coach createdCoach = gson.fromJson(reader, Coach.class);
                    coachModel.addRow(new Object[]{
                            createdCoach.getId(),
                            createdCoach.getName(),
                            createdCoach.getSpecialization(),
                            createdCoach.getExperienceYears()
                    });
                    JOptionPane.showMessageDialog(this, "Тренер успешно добавлен.");
                } else {
                    InputStreamReader reader = new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8);
                    String error = gson.fromJson(reader, String.class);
                    JOptionPane.showMessageDialog(this, "Ошибка при добавлении тренера: " + error);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ошибка при добавлении тренера: " + e.getMessage());
            }
        });
    }

    /**
     * Обновление существующего тренера.
     *
     * @param coach Тренер для обновления
     */
    private void updateCoach(Coach coach) {
        SwingUtilities.invokeLater(() -> {
            try {
                URL url = new URL("http://localhost:4567/coaches/" + coach.getId());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                String jsonInputString = gson.toJson(coach);
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) { // OK
                    // Обновление строки в таблице
                    for (int i = 0; i < coachModel.getRowCount(); i++) {
                        if ((int) coachModel.getValueAt(i, 0) == coach.getId()) {
                            coachModel.setValueAt(coach.getName(), i, 1);
                            coachModel.setValueAt(coach.getSpecialization(), i, 2);
                            coachModel.setValueAt(coach.getExperienceYears(), i, 3);
                            break;
                        }
                    }
                    JOptionPane.showMessageDialog(this, "Тренер успешно обновлен.");
                } else {
                    InputStreamReader reader = new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8);
                    String error = gson.fromJson(reader, String.class);
                    JOptionPane.showMessageDialog(this, "Ошибка при обновлении тренера: " + error);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ошибка при обновлении тренера: " + e.getMessage());
            }
        });
    }

    /**
     * Удаление тренера по ID.
     *
     * @param coachId ID тренера для удаления
     */
    private void deleteCoach(int coachId) {
        SwingUtilities.invokeLater(() -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Вы уверены, что хотите удалить этого тренера?", "Подтверждение удаления", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            try {
                URL url = new URL("http://localhost:4567/coaches/" + coachId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) { // OK
                    // Удаление строки из таблицы
                    for (int i = 0; i < coachModel.getRowCount(); i++) {
                        if ((int) coachModel.getValueAt(i, 0) == coachId) {
                            coachModel.removeRow(i);
                            break;
                        }
                    }
                    JOptionPane.showMessageDialog(this, "Тренер успешно удален.");
                } else {
                    InputStreamReader reader = new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8);
                    String error = gson.fromJson(reader, String.class);
                    JOptionPane.showMessageDialog(this, "Ошибка при удалении тренера: " + error);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ошибка при удалении тренера: " + e.getMessage());
            }
        });
    }

    /**
     * Получение объекта Coach из выбранной строки таблицы.
     *
     * @param row Номер строки
     * @return Объект Coach
     */
    private Coach getCoachFromTable(int row) {
        Coach coach = new Coach();
        coach.setId((int) coachModel.getValueAt(row, 0));
        coach.setName((String) coachModel.getValueAt(row, 1));
        coach.setSpecialization(Specialization.valueOf(((String) coachModel.getValueAt(row, 2)).toUpperCase().replace(" ", "_")));
        coach.setExperienceYears((int) coachModel.getValueAt(row, 3));
        return coach;
    }

    // --------------------- Методы для Equipment ---------------------

    /**
     * Загрузка оборудования с фильтрами и отображение в таблице.
     *
     * @param nameFilter     Фильтр по названию
     * @param typeFilter     Фильтр по типу
     * @param quantityFilter Фильтр по количеству
     * @param statusFilter   Фильтр по статусу
     */
    private void loadEquipment(String nameFilter, String typeFilter, String quantityFilter, String statusFilter) {
        SwingUtilities.invokeLater(() -> {
            equipmentModel.setRowCount(0); // Очистка таблицы
            try {
                StringBuilder urlStr = new StringBuilder("http://localhost:4567/equipment");
                boolean hasParam = false;
                if (!nameFilter.isEmpty() || !typeFilter.isEmpty() || !quantityFilter.isEmpty() || !statusFilter.isEmpty()) {
                    urlStr.append("?");
                    if (!nameFilter.isEmpty()) {
                        urlStr.append("name=").append(encodeValue(nameFilter));
                        hasParam = true;
                    }
                    if (!typeFilter.isEmpty()) {
                        if (hasParam) urlStr.append("&");
                        urlStr.append("type=").append(encodeValue(typeFilter));
                        hasParam = true;
                    }
                    if (!quantityFilter.isEmpty()) {
                        if (hasParam) urlStr.append("&");
                        urlStr.append("quantity=").append(encodeValue(quantityFilter));
                        hasParam = true;
                    }
                    if (!statusFilter.isEmpty()) {
                        if (hasParam) urlStr.append("&");
                        urlStr.append("status=").append(encodeValue(statusFilter));
                    }
                }

                URL url = new URL(urlStr.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
                    Equipment[] equipments = gson.fromJson(reader, Equipment[].class);
                    for (Equipment eq : equipments) {
                        equipmentModel.addRow(new Object[]{
                                eq.getId(),
                                eq.getName(),
                                eq.getType(),
                                eq.getQuantity(),
                                eq.getStatus()
                        });
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Ошибка при загрузке оборудования: " + responseCode);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ошибка при загрузке оборудования: " + e.getMessage());
            }
        });
    }

    /**
     * Отображение диалогового окна для добавления или редактирования оборудования.
     *
     * @param equipment Оборудование для редактирования или null для добавления нового
     */
    private void showEquipmentDialog(Equipment equipment) {
        JDialog dialog = new JDialog(this, equipment == null ? "Добавить оборудование" : "Редактировать оборудование", true);
        dialog.setSize(400, 250);
        dialog.setLayout(new GridLayout(6, 2));

        dialog.add(new JLabel("Название:"));
        JTextField nameField = new JTextField();
        dialog.add(nameField);

        dialog.add(new JLabel("Тип:"));
        JComboBox<EquipmentType> typeComboBox = new JComboBox<>(EquipmentType.values());
        dialog.add(typeComboBox);

        dialog.add(new JLabel("Количество:"));
        JTextField quantityField = new JTextField();
        dialog.add(quantityField);

        dialog.add(new JLabel("Статус:"));
        JComboBox<EquipmentStatus> statusComboBox = new JComboBox<>(EquipmentStatus.values());
        dialog.add(statusComboBox);

        JButton saveButton = new JButton("Сохранить");
        JButton cancelButton = new JButton("Отмена");
        dialog.add(saveButton);
        dialog.add(cancelButton);

        if (equipment != null) {
            nameField.setText(equipment.getName());
            typeComboBox.setSelectedItem(equipment.getType());
            quantityField.setText(String.valueOf(equipment.getQuantity()));
            statusComboBox.setSelectedItem(equipment.getStatus());
        }

        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            EquipmentType type = (EquipmentType) typeComboBox.getSelectedItem();
            String quantityStr = quantityField.getText().trim();
            EquipmentStatus status = (EquipmentStatus) statusComboBox.getSelectedItem();

            if (name.isEmpty() || type == null || quantityStr.isEmpty() || status == null) {
                JOptionPane.showMessageDialog(dialog, "Пожалуйста, заполните все обязательные поля (Название, Тип, Количество, Статус).");
                return;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(quantityStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Количество должно быть числом.");
                return;
            }

            if (equipment == null) {
                // Создание нового оборудования
                Equipment newEquipment = new Equipment();
                newEquipment.setName(name);
                newEquipment.setType(type);
                newEquipment.setQuantity(quantity);
                newEquipment.setStatus(status);

                createEquipment(newEquipment);
            } else {
                // Обновление существующего оборудования
                equipment.setName(name);
                equipment.setType(type);
                equipment.setQuantity(quantity);
                equipment.setStatus(status);

                updateEquipment(equipment);
            }

            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Создание нового оборудования.
     *
     * @param equipment Оборудование для создания
     */
    private void createEquipment(Equipment equipment) {
        SwingUtilities.invokeLater(() -> {
            try {
                URL url = new URL("http://localhost:4567/equipment");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                String jsonInputString = gson.toJson(equipment);
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 201) { // Created
                    InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
                    Equipment createdEquipment = gson.fromJson(reader, Equipment.class);
                    equipmentModel.addRow(new Object[]{
                            createdEquipment.getId(),
                            createdEquipment.getName(),
                            createdEquipment.getType(),
                            createdEquipment.getQuantity(),
                            createdEquipment.getStatus()
                    });
                    JOptionPane.showMessageDialog(this, "Оборудование успешно добавлено.");
                } else {
                    InputStreamReader reader = new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8);
                    String error = gson.fromJson(reader, String.class);
                    JOptionPane.showMessageDialog(this, "Ошибка при добавлении оборудования: " + error);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ошибка при добавлении оборудования: " + e.getMessage());
            }
        });
    }

    /**
     * Обновление существующего оборудования.
     *
     * @param equipment Оборудование для обновления
     */
    private void updateEquipment(Equipment equipment) {
        SwingUtilities.invokeLater(() -> {
            try {
                URL url = new URL("http://localhost:4567/equipment/" + equipment.getId());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                String jsonInputString = gson.toJson(equipment);
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) { // OK
                    // Обновление строки в таблице
                    for (int i = 0; i < equipmentModel.getRowCount(); i++) {
                        if ((int) equipmentModel.getValueAt(i, 0) == equipment.getId()) {
                            equipmentModel.setValueAt(equipment.getName(), i, 1);
                            equipmentModel.setValueAt(equipment.getType(), i, 2);
                            equipmentModel.setValueAt(equipment.getQuantity(), i, 3);
                            equipmentModel.setValueAt(equipment.getStatus(), i, 4);
                            break;
                        }
                    }
                    JOptionPane.showMessageDialog(this, "Оборудование успешно обновлено.");
                } else {
                    InputStreamReader reader = new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8);
                    String error = gson.fromJson(reader, String.class);
                    JOptionPane.showMessageDialog(this, "Ошибка при обновлении оборудования: " + error);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ошибка при обновлении оборудования: " + e.getMessage());
            }
        });
    }

    /**
     * Удаление оборудования по ID.
     *
     * @param equipmentId ID оборудования для удаления
     */
    private void deleteEquipment(int equipmentId) {
        SwingUtilities.invokeLater(() -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Вы уверены, что хотите удалить это оборудование?", "Подтверждение удаления", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            try {
                URL url = new URL("http://localhost:4567/equipment/" + equipmentId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) { // OK
                    // Удаление строки из таблицы
                    for (int i = 0; i < equipmentModel.getRowCount(); i++) {
                        if ((int) equipmentModel.getValueAt(i, 0) == equipmentId) {
                            equipmentModel.removeRow(i);
                            break;
                        }
                    }
                    JOptionPane.showMessageDialog(this, "Оборудование успешно удалено.");
                } else {
                    InputStreamReader reader = new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8);
                    String error = gson.fromJson(reader, String.class);
                    JOptionPane.showMessageDialog(this, "Ошибка при удалении оборудования: " + error);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ошибка при удалении оборудования: " + e.getMessage());
            }
        });
    }

    /**
     * Получение объекта Equipment из выбранной строки таблицы.
     *
     * @param row Номер строки
     * @return Объект Equipment
     */
    private Equipment getEquipmentFromTable(int row) {
        Equipment equipment = new Equipment();
        equipment.setId((int) equipmentModel.getValueAt(row, 0));
        equipment.setName((String) equipmentModel.getValueAt(row, 1));
        equipment.setType(EquipmentType.valueOf(((String) equipmentModel.getValueAt(row, 2)).toUpperCase()));
        equipment.setQuantity((int) equipmentModel.getValueAt(row, 3));
        equipment.setStatus(EquipmentStatus.valueOf(((String) equipmentModel.getValueAt(row, 4)).toUpperCase()));
        return equipment;
    }

    // --------------------- Вспомогательные Методы ---------------------

    /**
     * Кодирование параметра для URL.
     *
     * @param value Значение параметра
     * @return Закодированное значение
     */
    private String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            return "";
        }
    }

    /**
     * Главный метод для запуска клиентского приложения.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientApp app = new ClientApp();
            app.setVisible(true);
        });
    }
}