// AthletesPanel.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class AthletesPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton searchButton, addButton, editButton, deleteButton;
    private ClientConnection connection;

    public AthletesPanel() {
        setLayout(new BorderLayout());

        // Верхняя панель с кнопками и поиском
        JPanel topPanel = new JPanel();
        addButton = new JButton("Добавить");
        editButton = new JButton("Редактировать");
        deleteButton = new JButton("Удалить");
        searchField = new JTextField(20);
        searchButton = new JButton("Поиск");
        topPanel.add(addButton);
        topPanel.add(editButton);
        topPanel.add(deleteButton);
        topPanel.add(new JLabel("Поиск:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);
        add(topPanel, BorderLayout.NORTH);

        // Таблица для отображения спортсменов
        tableModel = new DefaultTableModel(new String[]{"ID", "Имя", "Возраст", "Разряд", "Тренер"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Запрет редактирования ячеек напрямую
            }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Подключение к серверу
        try {
            connection = new ClientConnection();
            loadAthletes();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Не удалось подключиться к серверу", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }

        // Обработчики событий
        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> deleteSelectedAthlete());
        searchButton.addActionListener(e -> searchAthletes());
    }

    private void loadAthletes() {
        try {
            JsonObject response = connection.sendRequest("getAthletes", new JsonObject());
            if (response.get("status").getAsString().equals("success")) {
                tableModel.setRowCount(0);
                response.getAsJsonArray("data").forEach(item -> {
                    JsonObject athlete = item.getAsJsonObject();
                    String coachName = athlete.has("coach_name") && !athlete.get("coach_name").isJsonNull() ? athlete.get("coach_name").getAsString() : "";
                    tableModel.addRow(new Object[]{
                            athlete.get("id").getAsInt(),
                            athlete.get("name").getAsString(),
                            athlete.get("age").getAsInt(),
                            athlete.get("rank").getAsString(),
                            coachName
                    });
                });
            } else {
                JOptionPane.showMessageDialog(this, response.get("message").getAsString(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при загрузке спортсменов", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddDialog() {
        AthletesForm form = new AthletesForm(null, connection);
        int result = JOptionPane.showConfirmDialog(this, form, "Добавить спортсмена", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            JsonObject data = form.getFormData();
            try {
                JsonObject response = connection.sendRequest("addAthlete", data);
                if (response.get("status").getAsString().equals("success")) {
                    JOptionPane.showMessageDialog(this, response.get("message").getAsString(), "Успех", JOptionPane.INFORMATION_MESSAGE);
                    loadAthletes();
                } else {
                    JOptionPane.showMessageDialog(this, response.get("message").getAsString(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Ошибка при добавлении спортсмена", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String name = (String) tableModel.getValueAt(selectedRow, 1);
            int age = (int) tableModel.getValueAt(selectedRow, 2);
            String rank = (String) tableModel.getValueAt(selectedRow, 3);
            String coachName = (String) tableModel.getValueAt(selectedRow, 4);

            JsonObject initialData = new JsonObject();
            initialData.addProperty("id", id);
            initialData.addProperty("name", name);
            initialData.addProperty("age", age);
            initialData.addProperty("rank", rank);
            initialData.addProperty("coach_name", coachName);

            AthletesForm form = new AthletesForm(initialData, connection);
            int result = JOptionPane.showConfirmDialog(this, form, "Редактировать спортсмена", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                JsonObject data = form.getFormData();
                data.addProperty("id", id); // Убедимся, что ID передан
                try {
                    JsonObject response = connection.sendRequest("updateAthlete", data);
                    if (response.get("status").getAsString().equals("success")) {
                        JOptionPane.showMessageDialog(this, response.get("message").getAsString(), "Успех", JOptionPane.INFORMATION_MESSAGE);
                        loadAthletes();
                    } else {
                        JOptionPane.showMessageDialog(this, response.get("message").getAsString(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Ошибка при обновлении спортсмена", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Выберите спортсмена для редактирования", "Информация", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteSelectedAthlete() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Вы уверены, что хотите удалить этого спортсмена?", "Подтверждение", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                JsonObject data = new JsonObject();
                data.addProperty("id", id);
                try {
                    JsonObject response = connection.sendRequest("deleteAthlete", data);
                    if (response.get("status").getAsString().equals("success")) {
                        JOptionPane.showMessageDialog(this, response.get("message").getAsString(), "Успех", JOptionPane.INFORMATION_MESSAGE);
                        loadAthletes();
                    } else {
                        JOptionPane.showMessageDialog(this, response.get("message").getAsString(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Ошибка при удалении спортсмена", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Выберите спортсмена для удаления", "Информация", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void searchAthletes() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadAthletes();
            return;
        }

        try {
            JsonObject requestData = new JsonObject();
            requestData.addProperty("keyword", keyword);
            JsonObject response = connection.sendRequest("searchAthletes", requestData);
            if (response.get("status").getAsString().equals("success")) {
                tableModel.setRowCount(0);
                response.getAsJsonArray("data").forEach(item -> {
                    JsonObject athlete = item.getAsJsonObject();
                    String coachName = athlete.has("coach_name") && !athlete.get("coach_name").isJsonNull() ? athlete.get("coach_name").getAsString() : "";
                    tableModel.addRow(new Object[]{
                            athlete.get("id").getAsInt(),
                            athlete.get("name").getAsString(),
                            athlete.get("age").getAsInt(),
                            athlete.get("rank").getAsString(),
                            coachName
                    });
                });
            } else {
                JOptionPane.showMessageDialog(this, response.get("message").getAsString(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при поиске спортсменов", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
}
