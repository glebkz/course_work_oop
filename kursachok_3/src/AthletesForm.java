// AthletesForm.java
import javax.swing.*;
import java.awt.*;
import com.google.gson.JsonObject;
import java.util.List;

public class AthletesForm extends JPanel {
    private JTextField nameField;
    private JSpinner ageSpinner;
    private JComboBox<String> rankComboBox;
    private JComboBox<CoachItem> coachComboBox;
    private ClientConnection connection;

    public AthletesForm(JsonObject initialData, ClientConnection connection) {
        this.connection = connection;
        setLayout(new GridLayout(4, 2, 10, 10));

        add(new JLabel("Имя:"));
        nameField = new JTextField();
        add(nameField);

        add(new JLabel("Возраст:"));
        ageSpinner = new JSpinner(new SpinnerNumberModel(18, 5, 100, 1));
        add(ageSpinner);

        add(new JLabel("Разряд:"));
        rankComboBox = new JComboBox<>(new String[]{"Новичок", "Средний", "Продвинутый", "Эксперт"});
        add(rankComboBox);

        add(new JLabel("Тренер:"));
        coachComboBox = new JComboBox<>();
        loadCoaches();
        add(coachComboBox);

        if (initialData != null) {
            nameField.setText(initialData.get("name").getAsString());
            ageSpinner.setValue(initialData.get("age").getAsInt());
            rankComboBox.setSelectedItem(initialData.get("rank").getAsString());
            // Предположим, что тренер выбран по имени
            String coachName = initialData.has("coach_name") && !initialData.get("coach_name").isJsonNull() ? initialData.get("coach_name").getAsString() : "";
            for (int i = 0; i < coachComboBox.getItemCount(); i++) {
                CoachItem item = coachComboBox.getItemAt(i);
                if (item.getName().equals(coachName)) {
                    coachComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void loadCoaches() {
        try {
            JsonObject response = connection.sendRequest("getCoaches", new JsonObject());
            if (response.get("status").getAsString().equals("success")) {
                response.getAsJsonArray("data").forEach(item -> {
                    JsonObject coach = item.getAsJsonObject();
                    coachComboBox.addItem(new CoachItem(coach.get("id").getAsInt(), coach.get("name").getAsString()));
                });
            } else {
                JOptionPane.showMessageDialog(this, "Не удалось загрузить список тренеров", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при загрузке тренеров", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    public JsonObject getFormData() {
        JsonObject data = new JsonObject();
        data.addProperty("name", nameField.getText().trim());
        data.addProperty("age", (int) ageSpinner.getValue());
        data.addProperty("rank", (String) rankComboBox.getSelectedItem());
        CoachItem selectedCoach = (CoachItem) coachComboBox.getSelectedItem();
        if (selectedCoach != null) {
            data.addProperty("coach_id", selectedCoach.getId());
        } else {
            data.add("coach_id", null);
        }
        return data;
    }

    // Вспомогательный класс для отображения тренеров
    class CoachItem {
        private int id;
        private String name;

        public CoachItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public String getName() { return name; }

        @Override
        public String toString() {
            return name;
        }
    }
}
