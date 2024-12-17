// CoachesPanel.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import com.google.gson.JsonObject;
import java.util.Vector;

public class CoachesPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton searchButton, addButton, editButton, deleteButton;
    private ClientConnection connection;

    public CoachesPanel() {
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

        // Таблица для отображения тренеров
        tableModel = new DefaultTableModel(new String[]{"ID", "Имя", "Специализация", "Стаж"}, 0) {
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
            loadCoaches();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Не удалось подключиться к серверу", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }

        // Обработчики событий
        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> deleteSelectedCoach());
        searchButton.addActionListener(e -> searchCoaches());
    }

    private void loadCoaches() {
        try {
            JsonObject response = connection.sendRequest("getCoaches", new JsonObject());
            if (response.get("status").getAsString().equals("success")) {
                tableModel.setRowCount(0);
                response.getAsJsonArray("data").forEach(item -> {
                    JsonObject coach = item.getAsJsonObject();
                    tableModel.addRow(new Object[]{
                            coach.get("id").getAsInt(),
                            coach.get("name").getAsString(),
                            coach.get("specialization").getAsString(),
                            coach.get("experience").getAsInt()
                    });
                });
            } else {
                JOptionPane.showMessageDialog(this, response.get("message").getAsString(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при загрузке тренеров", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddDialog() {
        CoachesForm form = new CoachesForm(null);
        int result = JOptionPane.showConfirmDialog(this, form, "Добавить тренера", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            JsonObject data = form.getFormData();
            try {
                JsonObject response = connection.sendRequest("addCoach", data);
                if (response.get("status").getAsString().equals("success")) {
                    JOptionPane.showMessageDialog(this, response.get("message").getAsString(), "Успех", JOptionPane.INFORMATION_MESSAGE);
                    loadCoaches();
                } else {
                    JOptionPane.showMessageDialog(this, response.get("message").getAsString(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Ошибка при добавлении тренера", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String name = (String) tableModel.getValueAt(selectedRow, 1);
            String specialization = (String) tableModel.getValueAt(selectedRow, 2);
            int experience = (int) tableModel.getValueAt(selectedRow, 3);

            JsonObject initialData = new JsonObject();
            initialData.addProperty("id", id);
            initialData.addProperty("name", name);
            initialData.addProperty("specialization", specialization);
            initialData.addProperty("experience", experience);

            CoachesForm form = new CoachesForm(initialData);
            int result = JOptionPane.showConfirmDialog(this, form, "Редактировать тренера", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                JsonObject data = form.getFormData();
                data.addProperty("id", id); // Убедимся, что ID передан
                try {
                    JsonObject response = connection.sendRequest("updateCoach", data);
                    if (response.get("status").getAsString().equals("success")) {
                        JOptionPane.showMessageDialog(this, response.get("message").getAsString(), "Успех", JOptionPane.INFORMATION_MESSAGE);
                        loadCoaches();
                    } else {
                        JOptionPane.showMessageDialog(this, response.get("message").getAsString(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Ошибка при обновлении тренера", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Выберите тренера для редактирования", "Информация", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteSelectedCoach() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Вы уверены, что хотите удалить этого тренера?", "Подтверждение", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                JsonObject data = new JsonObject();
                data.addProperty("id", id);
                try {
                    JsonObject response = connection.sendRequest("deleteCoach", data);
                    if (response.get("status").getAsString().equals("success")) {
                        JOptionPane.showMessageDialog(this, response.get("message").getAsString(), "Успех", JOptionPane.INFORMATION_MESSAGE);
                        loadCoaches();
                    } else {
                        JOptionPane.showMessageDialog(this, response.get("message").getAsString(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Ошибка при удалении тренера", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Выберите тренера для удаления", "Информация", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void searchCoaches() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadCoaches();
            return;
        }

        try {
            JsonObject requestData = new JsonObject();
            requestData.addProperty("keyword", keyword);
            JsonObject response = connection.sendRequest("searchCoaches", requestData);
            if (response.get("status").getAsString().equals("success")) {
                tableModel.setRowCount(0);
                response.getAsJsonArray("data").forEach(item -> {
                    JsonObject coach = item.getAsJsonObject();
                    tableModel.addRow(new Object[]{
                            coach.get("id").getAsInt(),
                            coach.get("name").getAsString(),
                            coach.get("specialization").getAsString(),
                            coach.get("experience").getAsInt()
                    });
                });
            } else {
                JOptionPane.showMessageDialog(this, response.get("message").getAsString(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при поиске тренеров", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
}
