// InventoryPanel.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import com.google.gson.JsonObject;
import java.util.Vector;

public class InventoryPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton searchButton, addButton, editButton, deleteButton;
    private ClientConnection connection;

    public InventoryPanel() {
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

        // Таблица для отображения инвентаря
        tableModel = new DefaultTableModel(new String[]{"ID", "Название", "Тип", "Количество", "Статус"}, 0) {
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
            loadInventory();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Не удалось подключиться к серверу", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }

        // Обработчики событий
        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> deleteSelectedInventory());
        searchButton.addActionListener(e -> searchInventory());
    }

    private void loadInventory() {
        try {
            JsonObject response = connection.sendRequest("getInventory", new JsonObject());
            if (response.get("status").getAsString().equals("success")) {
                tableModel.setRowCount(0);
                response.getAsJsonArray("data").forEach(item -> {
                    JsonObject inventory = item.getAsJsonObject();
                    tableModel.addRow(new Object[]{
                            inventory.get("id").getAsInt(),
                            inventory.get("name").getAsString(),
                            inventory.get("type").getAsString(),
                            inventory.get("quantity").getAsInt(),
                            inventory.get("status").getAsString()
                    });
                });
            } else {
                JOptionPane.showMessageDialog(this, response.get("message").getAsString(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при загрузке инвентаря", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddDialog() {
        InventoryForm form = new InventoryForm(null);
        int result = JOptionPane.showConfirmDialog(this, form, "Добавить инвентарь", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            JsonObject data = form.getFormData();
            try {
                JsonObject response = connection.sendRequest("addInventory", data);
                if (response.get("status").getAsString().equals("success")) {
                    JOptionPane.showMessageDialog(this, response.get("message").getAsString(), "Успех", JOptionPane.INFORMATION_MESSAGE);
                    loadInventory();
                } else {
                    JOptionPane.showMessageDialog(this, response.get("message").getAsString(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Ошибка при добавлении инвентаря", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String name = (String) tableModel.getValueAt(selectedRow, 1);
            String type = (String) tableModel.getValueAt(selectedRow, 2);
            int quantity = (int) tableModel.getValueAt(selectedRow, 3);
            String status = (String) tableModel.getValueAt(selectedRow, 4);

            JsonObject initialData = new JsonObject();
            initialData.addProperty("id", id);
            initialData.addProperty("name", name);
            initialData.addProperty("type", type);
            initialData.addProperty("quantity", quantity);
            initialData.addProperty("status", status);

            InventoryForm form = new InventoryForm(initialData);
            int result = JOptionPane.showConfirmDialog(this, form, "Редактировать инвентарь", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                JsonObject data = form.getFormData();
                data.addProperty("id", id); // Убедимся, что ID передан
                try {
                    JsonObject response = connection.sendRequest("updateInventory", data);
                    if (response.get("status").getAsString().equals("success")) {
                        JOptionPane.showMessageDialog(this, response.get("message").getAsString(), "Успех", JOptionPane.INFORMATION_MESSAGE);
                        loadInventory();
                    } else {
                        JOptionPane.showMessageDialog(this, response.get("message").getAsString(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Ошибка при обновлении инвентаря", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Выберите инвентарь для редактирования", "Информация", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteSelectedInventory() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Вы уверены, что хотите удалить этот инвентарь?", "Подтверждение", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                JsonObject data = new JsonObject();
                data.addProperty("id", id);
                try {
                    JsonObject response = connection.sendRequest("deleteInventory", data);
                    if (response.get("status").getAsString().equals("success")) {
                        JOptionPane.showMessageDialog(this, response.get("message").getAsString(), "Успех", JOptionPane.INFORMATION_MESSAGE);
                        loadInventory();
                    } else {
                        JOptionPane.showMessageDialog(this, response.get("message").getAsString(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Ошибка при удалении инвентаря", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Выберите инвентарь для удаления", "Информация", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void searchInventory() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadInventory();
            return;
        }

        try {
            JsonObject requestData = new JsonObject();
            requestData.addProperty("keyword", keyword);
            JsonObject response = connection.sendRequest("searchInventory", requestData);
            if (response.get("status").getAsString().equals("success")) {
                tableModel.setRowCount(0);
                response.getAsJsonArray("data").forEach(item -> {
                    JsonObject inventory = item.getAsJsonObject();
                    tableModel.addRow(new Object[]{
                            inventory.get("id").getAsInt(),
                            inventory.get("name").getAsString(),
                            inventory.get("type").getAsString(),
                            inventory.get("quantity").getAsInt(),
                            inventory.get("status").getAsString()
                    });
                });
            } else {
                JOptionPane.showMessageDialog(this, response.get("message").getAsString(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при поиске инвентаря", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
}
