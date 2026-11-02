import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ClientManagerApp extends JFrame {

    // === НАСТРОЙКИ ПОДКЛЮЧЕНИЯ К БД ===
    // Замените на ваши реальные данные
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/your_database_name";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "your_password";

    // Элементы интерфейса
    private JTextField nameField;
    private JTextField emailField;
    private JTable clientTable;
    private DefaultTableModel tableModel;

    public ClientManagerApp() {
        // Настройка главного окна
        setTitle("Управление клиентами (PostgreSQL)");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Центрирование окна

        // Инициализация таблицы
        String[] columns = {"ID", "Имя", "Email"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Запрещаем редактирование ячеек напрямую
            }
        };
        clientTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(clientTable);

        // Панель ввода данных
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("Имя:"));
        nameField = new JTextField(15);
        inputPanel.add(nameField);

        inputPanel.add(new JLabel("Email:"));
        emailField = new JTextField(15);
        inputPanel.add(emailField);

        JButton addButton = new JButton("Добавить клиента");
        inputPanel.add(addButton);

        // Кнопка загрузки списка
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton loadButton = new JButton("Загрузить список клиентов");
        buttonPanel.add(loadButton);

        // Размещение элементов на окне
        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Обработчики событий
        addButton.addActionListener(e -> addClient());
        loadButton.addActionListener(e -> loadClients());
    }

    // Метод для загрузки клиентов из БД
    private void loadClients() {
        // Очищаем таблицу перед загрузкой
        tableModel.setRowCount(0);

        String sql = "SELECT id, name, email FROM clients ORDER BY id";

        // try-with-resources автоматически закроет соединение и statement
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                tableModel.addRow(new Object[]{id, name, email});
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка загрузки данных: " + ex.getMessage(),
                    "Ошибка БД",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Метод для добавления нового клиента
    private void addClient() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите имя клиента!", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Используем PreparedStatement для защиты от SQL-инъекций
        String sql = "INSERT INTO clients (name, email) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.executeUpdate();

            // Очищаем поля ввода
            nameField.setText("");
            emailField.setText("");

            // Автоматически обновляем таблицу
            loadClients();

            JOptionPane.showMessageDialog(this, "Клиент успешно добавлен!", "Успех", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка добавления клиента: " + ex.getMessage(),
                    "Ошибка БД",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Запуск интерфейса в потоке Swing
        SwingUtilities.invokeLater(() -> {
            try {
                // Устанавливаем системный вид интерфейса
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new ClientManagerApp().setVisible(true);
        });
    }
}