// InventoryForm.java
import javax.swing.*;
import java.awt.*;
import com.google.gson.JsonObject;

public class InventoryForm extends JPanel {
    private JTextField nameField;
    private JComboBox<String> typeComboBox;
    private JSpinner quantitySpinner;
    private JComboBox<String> statusComboBox;

    public InventoryForm(JsonObject initialData) {
        setLayout(new GridLayout(4, 2, 10, 10));

        add(new JLabel("Название:"));
        nameField = new JTextField();
        add(nameField);

        add(new JLabel("Тип:"));
        typeComboBox = new JComboBox<>(new String[]{"Мяч", "Форма", "Тренажёр", "Другое"});
        add(typeComboBox);

        add(new JLabel("Количество:"));
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        add(quantitySpinner);

        add(new JLabel("Статус:"));
        statusComboBox = new JComboBox<>(new String[]{"Доступен", "Используется", "Недоступен"});
        add(statusComboBox);

        if (initialData != null) {
            nameField.setText(initialData.get("name").getAsString());
            typeComboBox.setSelectedItem(initialData.get("type").getAsString());
            quantitySpinner.setValue(initialData.get("quantity").getAsInt());
            statusComboBox.setSelectedItem(initialData.get("status").getAsString());
        }
    }

    public JsonObject getFormData() {
        JsonObject data = new JsonObject();
        data.addProperty("name", nameField.getText().trim());
        data.addProperty("type", (String) typeComboBox.getSelectedItem());
        data.addProperty("quantity", (int) quantitySpinner.getValue());
        data.addProperty("status", (String) statusComboBox.getSelectedItem());
        return data;
    }
}
