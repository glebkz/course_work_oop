// CoachesForm.java
import javax.swing.*;
import java.awt.*;
import com.google.gson.JsonObject;

public class CoachesForm extends JPanel {
    private JTextField nameField;
    private JComboBox<String> specializationComboBox;
    private JSpinner experienceSpinner;

    public CoachesForm(JsonObject initialData) {
        setLayout(new GridLayout(3, 2, 10, 10));

        add(new JLabel("Имя:"));
        nameField = new JTextField();
        add(nameField);

        add(new JLabel("Специализация:"));
        specializationComboBox = new JComboBox<>(new String[]{"Футбол", "Лёгкая атлетика", "Баскетбол", "Теннис", "Другое"});
        add(specializationComboBox);

        add(new JLabel("Стаж (лет):"));
        experienceSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 100, 1));
        add(experienceSpinner);

        if (initialData != null) {
            nameField.setText(initialData.get("name").getAsString());
            specializationComboBox.setSelectedItem(initialData.get("specialization").getAsString());
            experienceSpinner.setValue(initialData.get("experience").getAsInt());
        }
    }

    public JsonObject getFormData() {
        JsonObject data = new JsonObject();
        data.addProperty("name", nameField.getText().trim());
        data.addProperty("specialization", (String) specializationComboBox.getSelectedItem());
        data.addProperty("experience", (int) experienceSpinner.getValue());
        return data;
    }
}
