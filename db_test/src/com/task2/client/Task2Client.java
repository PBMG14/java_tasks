package com.task2.client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Task2Client {
    private static final String URL = "jdbc:postgresql://localhost:5432/task2";
    private static final String USER = "postgres";
    private static final String PASSWORD = "20111974";

    public static void main(String[] args) {
        if (!checkDriver()) {
            return;
        }

        if (!testDatabaseConnection()) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            new MainForm().setVisible(true);
        });
    }

    private static boolean checkDriver() {
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("Драйвер PostgreSQL найден и зарегистрирован");
            return true;
        } catch (Exception e) {
            System.err.println("Драйвер PostgreSQL не найден: " + e.getMessage());
            showDriverError();
            return false;
        }
    }

    private static void showDriverError() {
        JOptionPane.showMessageDialog(null,
                "<html><b>Драйвер PostgreSQL не найден!</b><br><br>" +
                        "Для работы приложения необходимо добавить postgresql-42.6.2.jar в classpath</html>",
                "Ошибка драйвера",
                JOptionPane.ERROR_MESSAGE);
    }

    private static boolean testDatabaseConnection() {
        try (Connection conn = getConnection()) {
            JOptionPane.showMessageDialog(null,
                    "Подключение к базе данных успешно установлено!",
                    "Успех",
                    JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "<html><b>Ошибка подключения к базе данных:</b><br>" + e.getMessage() + "</html>",
                    "Ошибка подключения",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

class MainForm extends JFrame {
    public MainForm() {
        setTitle("Task2 Client - Управление отгрузками (CRUD)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setupMenu();
        setupUI();
    }

    private void setupUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel(
                "<html><center><h1>Система управления отгрузками</h1>" +
                        "<p>Полный CRUD функционал: Добавление, Редактирование, Удаление</p>" +
                        "<p>PostgreSQL 42.6.2 | Подключение: ✅ Активно</p></center></html>",
                SwingConstants.CENTER
        );
        titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 20, 20, 20));
        titleLabel.setForeground(new Color(0, 100, 0));

        mainPanel.add(titleLabel, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu tablesMenu = new JMenu("Таблицы");
        JMenuItem showTablesItem = new JMenuItem("Все таблицы");
        JMenuItem buyersItem = new JMenuItem("Покупатели (CRUD)");
        JMenuItem warehousesItem = new JMenuItem("Склады (CRUD)");
        JMenuItem shipmentsItem = new JMenuItem("Учет отгрузки (CRUD)");

        JMenu tasksMenu = new JMenu("Задачи");
        JMenuItem task1Item = new JMenuItem("1. Отгрузки после даты");
        JMenuItem task2Item = new JMenuItem("2. Нумерация отгрузок");
        JMenuItem task3Item = new JMenuItem("3. Склады для Казани");

        JMenu viewMenu = new JMenu("Представления");
        JMenuItem combinedViewItem = new JMenuItem("Объединенное представление");
        JMenuItem procedureItem = new JMenuItem("Вызвать хранимую процедуру");

        tablesMenu.add(showTablesItem);
        tablesMenu.addSeparator();
        tablesMenu.add(buyersItem);
        tablesMenu.add(warehousesItem);
        tablesMenu.add(shipmentsItem);

        tasksMenu.add(task1Item);
        tasksMenu.add(task2Item);
        tasksMenu.add(task3Item);

        viewMenu.add(combinedViewItem);
        viewMenu.add(procedureItem);

        menuBar.add(tablesMenu);
        menuBar.add(tasksMenu);
        menuBar.add(viewMenu);

        setJMenuBar(menuBar);

        showTablesItem.addActionListener(e -> new AllTablesForm().setVisible(true));
        buyersItem.addActionListener(e -> new CRUDForm("Покупатели", "код_покупателя").setVisible(true));
        warehousesItem.addActionListener(e -> new CRUDForm("Склады", "номер_склада").setVisible(true));
        shipmentsItem.addActionListener(e -> new ShipmentsCRUDForm().setVisible(true));

        task1Item.addActionListener(e -> new Task1Form().setVisible(true));
        task2Item.addActionListener(e -> new Task2Form().setVisible(true));
        task3Item.addActionListener(e -> new Task3Form().setVisible(true));

        combinedViewItem.addActionListener(e -> new CombinedViewForm().setVisible(true));
        procedureItem.addActionListener(e -> callStoredProcedure());
    }

    private void callStoredProcedure() {
        String date = JOptionPane.showInputDialog(this,
                "Введите дату для анализа отгрузок (гггг-мм-дд):",
                "2024-05-20");

        if (date != null && !date.trim().isEmpty()) {
            try (Connection conn = Task2Client.getConnection()) {
                String callSQL = "CALL GetInfoFromУчет(?, ?, ?)";

                try (CallableStatement cstmt = conn.prepareCall(callSQL)) {
                    cstmt.setDate(1, Date.valueOf(date));

                    cstmt.registerOutParameter(2, Types.INTEGER);
                    cstmt.registerOutParameter(3, Types.VARCHAR);

                    cstmt.execute();

                    int total = cstmt.getInt(2);
                    String warehouses = cstmt.getString(3);

                    JOptionPane.showMessageDialog(this,
                            "<html><h3>Результат выполнения процедуры</h3>" +
                                    "<b>Дата:</b> " + date + "<br>" +
                                    "<b>Итоговое количество:</b> " + total + " единиц<br>" +
                                    "<b>Задействованные склады:</b> " + warehouses + "</html>",
                            "Результат процедуры",
                            JOptionPane.INFORMATION_MESSAGE);
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Ошибка выполнения процедуры: " + e.getMessage(),
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

class CRUDForm extends JFrame {
    protected JTable table;
    protected String tableName;
    protected String primaryKey;
    protected DefaultTableModel tableModel;

    public CRUDForm(String tableName, String primaryKey) {
        this.tableName = tableName;
        this.primaryKey = primaryKey;
        initializeUI();
        loadData();
    }

    private void initializeUI() {
        setTitle(tableName + " - Управление данными (CRUD)");
        setSize(800, 600);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        table = new JTable();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton addButton = new JButton("Добавить");
        JButton editButton = new JButton("Редактировать");
        JButton deleteButton = new JButton("Удалить");
        JButton refreshButton = new JButton("Обновить");

        addButton.addActionListener(e -> addRecord());
        editButton.addActionListener(e -> editRecord());
        deleteButton.addActionListener(e -> deleteRecord());
        refreshButton.addActionListener(e -> loadData());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    protected void loadData() {
        try (Connection conn = Task2Client.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            String[] columns = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                columns[i] = metaData.getColumnName(i + 1);
            }

            List<Object[]> data = new ArrayList<>();
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                data.add(row);
            }

            Object[][] dataArray = data.toArray(new Object[0][]);
            tableModel = new DefaultTableModel(dataArray, columns) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            table.setModel(tableModel);

            setTitle(tableName + " - Загружено записей: " + data.size());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка загрузки данных: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    protected void addRecord() {
        try (Connection conn = Task2Client.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName + " LIMIT 0")) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            JPanel panel = new JPanel(new GridLayout(columnCount, 2, 5, 5));
            List<JTextField> fields = new ArrayList<>();

            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                panel.add(new JLabel(columnName + ":"));

                JTextField field = new JTextField(20);
                if (columnName.equalsIgnoreCase(primaryKey)) {
                    field.setText("(авто)");
                    field.setEditable(false);
                }
                panel.add(field);
                fields.add(field);
            }

            int result = JOptionPane.showConfirmDialog(this, panel,
                    "Добавить запись в " + tableName,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " (");
                StringBuilder values = new StringBuilder("VALUES (");
                List<Object> params = new ArrayList<>();

                for (int i = 0; i < columnCount; i++) {
                    String columnName = metaData.getColumnName(i + 1);
                    String value = fields.get(i).getText().trim();

                    if (columnName.equalsIgnoreCase(primaryKey) && value.equals("(авто)")) {
                        continue;
                    }

                    if (!value.isEmpty()) {
                        if (sql.toString().endsWith("(")) {
                            sql.append(columnName);
                            values.append("?");
                        } else {
                            sql.append(", ").append(columnName);
                            values.append(", ?");
                        }
                        params.add(value);
                    }
                }

                sql.append(") ").append(values).append(")");

                try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
                    for (int i = 0; i < params.size(); i++) {
                        pstmt.setObject(i + 1, params.get(i));
                    }
                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Запись успешно добавлена!");
                    loadData();
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка при добавлении записи: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    protected void editRecord() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите запись для редактирования!");
            return;
        }

        try (Connection conn = Task2Client.getConnection()) {
            String sql = "SELECT * FROM " + tableName + " LIMIT 0";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                JPanel panel = new JPanel(new GridLayout(columnCount, 2, 5, 5));
                List<JComponent> fields = new ArrayList<>();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String columnType = metaData.getColumnTypeName(i);
                    Object currentValue = tableModel.getValueAt(selectedRow, i - 1);

                    panel.add(new JLabel(columnName + ":"));

                    JComponent field;
                    if (columnName.equalsIgnoreCase(primaryKey)) {
                        JTextField textField = new JTextField(currentValue != null ? currentValue.toString() : "", 20);
                        textField.setEditable(false);
                        field = textField;
                    } else if (columnType.equals("int4") || columnType.equals("integer")) {
                        JTextField textField = new JTextField(currentValue != null ? currentValue.toString() : "", 20);
                        field = textField;
                    } else if (columnType.equals("date")) {
                        JTextField textField = new JTextField(currentValue != null ? currentValue.toString() : "", 20);
                        field = textField;
                    } else if (columnType.equals("timestamp") || columnType.equals("timestamp without time zone")) {
                        JTextField textField = new JTextField(currentValue != null ? currentValue.toString() : "", 20);
                        field = textField;
                    } else {
                        JTextField textField = new JTextField(currentValue != null ? currentValue.toString() : "", 20);
                        field = textField;
                    }

                    panel.add(field);
                    fields.add(field);
                }

                int result = JOptionPane.showConfirmDialog(this, panel,
                        "Редактировать запись",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    StringBuilder updateSql = new StringBuilder("UPDATE " + tableName + " SET ");
                    List<Object> params = new ArrayList<>();

                    for (int i = 0; i < columnCount; i++) {
                        String columnName = metaData.getColumnName(i + 1);
                        if (columnName.equalsIgnoreCase(primaryKey)) continue;

                        String value;
                        if (fields.get(i) instanceof JTextField) {
                            value = ((JTextField) fields.get(i)).getText().trim();
                        } else {
                            continue;
                        }

                        if (updateSql.toString().endsWith("SET ")) {
                            updateSql.append(columnName).append(" = ?");
                        } else {
                            updateSql.append(", ").append(columnName).append(" = ?");
                        }

                        String columnType = metaData.getColumnTypeName(i + 1);
                        if (columnType.equals("int4") || columnType.equals("integer")) {
                            try {
                                params.add(Integer.parseInt(value));
                            } catch (NumberFormatException e) {
                                params.add(0);
                            }
                        } else if (columnType.equals("date")) {
                            try {
                                params.add(Date.valueOf(value));
                            } catch (IllegalArgumentException e) {
                                params.add(value);
                            }
                        } else if (columnType.equals("timestamp") || columnType.equals("timestamp without time zone")) {
                            try {
                                params.add(Timestamp.valueOf(value));
                            } catch (IllegalArgumentException e) {
                                params.add(value);
                            }
                        } else {
                            params.add(value);
                        }
                    }

                    Object pkValue = tableModel.getValueAt(selectedRow, getColumnIndex(primaryKey));
                    updateSql.append(" WHERE ").append(primaryKey).append(" = ?");

                    String pkType = metaData.getColumnTypeName(getColumnIndex(primaryKey) + 1);
                    if (pkType.equals("int4") || pkType.equals("integer")) {
                        params.add(Integer.parseInt(pkValue.toString()));
                    } else {
                        params.add(pkValue);
                    }

                    try (PreparedStatement pstmt = conn.prepareStatement(updateSql.toString())) {
                        for (int i = 0; i < params.size(); i++) {
                            pstmt.setObject(i + 1, params.get(i));
                        }

                        int affectedRows = pstmt.executeUpdate();
                        if (affectedRows > 0) {
                            JOptionPane.showMessageDialog(this, "Запись успешно обновлена!");
                            loadData();
                        }
                    }
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка при редактировании записи: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    protected void deleteRecord() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите запись для удаления!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите удалить выбранную запись?",
                "Подтверждение удаления",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = Task2Client.getConnection()) {
                Object pkValue = tableModel.getValueAt(selectedRow, getColumnIndex(primaryKey));

                String sql = "DELETE FROM " + tableName + " WHERE " + primaryKey + " = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setObject(1, pkValue);
                    int affectedRows = pstmt.executeUpdate();

                    if (affectedRows > 0) {
                        JOptionPane.showMessageDialog(this, "Запись успешно удалена!");
                        loadData();
                    }
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Ошибка при удалении записи: " + e.getMessage(),
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    int getColumnIndex(String columnName) {
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            if (tableModel.getColumnName(i).equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }
}

class ShipmentsCRUDForm extends CRUDForm {
    public ShipmentsCRUDForm() {
        super("Учет_отгрузки_готовой_продукции", "номер_документа_об_отгрузке");
    }

    @Override
    protected void addRecord() {
        try (Connection conn = Task2Client.getConnection()) {
            List<String> warehouses = getWarehouses(conn);
            List<String> buyers = getBuyers(conn);

            JPanel panel = new JPanel(new GridLayout(7, 2, 5, 5));

            JComboBox<String> warehouseCombo = new JComboBox<>(warehouses.toArray(new String[0]));
            JComboBox<String> buyerCombo = new JComboBox<>(buyers.toArray(new String[0]));
            JTextField docNumberField = new JTextField(20);
            JTextField productCodeField = new JTextField(20);
            JTextField unitField = new JTextField("шт.", 20);
            JTextField quantityField = new JTextField(20);
            JTextField dateField = new JTextField("2024-05-20 00:00:00", 20);

            panel.add(new JLabel("Номер склада:"));
            panel.add(warehouseCombo);
            panel.add(new JLabel("Код покупателя:"));
            panel.add(buyerCombo);
            panel.add(new JLabel("Номер документа:"));
            panel.add(docNumberField);
            panel.add(new JLabel("Код изделия:"));
            panel.add(productCodeField);
            panel.add(new JLabel("Единица измерения:"));
            panel.add(unitField);
            panel.add(new JLabel("Количество:"));
            panel.add(quantityField);
            panel.add(new JLabel("Дата отгрузки (гггг-мм-дд чч:мм:сс):"));
            panel.add(dateField);

            int result = JOptionPane.showConfirmDialog(this, panel,
                    "Добавить отгрузку",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String sql = "INSERT INTO Учет_отгрузки_готовой_продукции " +
                        "(номер_склада, номер_документа_об_отгрузке, код_покупателя, " +
                        "код_готового_изделия, единица_измерения, количество, дата_отгрузки) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, Integer.parseInt(warehouseCombo.getSelectedItem().toString().split(" - ")[0]));
                    pstmt.setString(2, docNumberField.getText());
                    pstmt.setInt(3, Integer.parseInt(buyerCombo.getSelectedItem().toString().split(" - ")[0]));
                    pstmt.setInt(4, Integer.parseInt(productCodeField.getText()));
                    pstmt.setString(5, unitField.getText());
                    pstmt.setInt(6, Integer.parseInt(quantityField.getText()));
                    pstmt.setTimestamp(7, Timestamp.valueOf(dateField.getText()));

                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Отгрузка успешно добавлена!");
                    loadData();
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка при добавлении отгрузки: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void editRecord() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите запись для редактирования!");
            return;
        }

        try (Connection conn = Task2Client.getConnection()) {
            List<String> warehouses = getWarehouses(conn);
            List<String> buyers = getBuyers(conn);

            JPanel panel = new JPanel(new GridLayout(7, 2, 5, 5));

            JComboBox<String> warehouseCombo = new JComboBox<>(warehouses.toArray(new String[0]));
            JComboBox<String> buyerCombo = new JComboBox<>(buyers.toArray(new String[0]));
            JTextField docNumberField = new JTextField(20);
            JTextField productCodeField = new JTextField(20);
            JTextField unitField = new JTextField(20);
            JTextField quantityField = new JTextField(20);
            JTextField dateField = new JTextField(20);

            Object currentWarehouse = tableModel.getValueAt(selectedRow, getColumnIndex("номер_склада"));
            Object currentBuyer = tableModel.getValueAt(selectedRow, getColumnIndex("код_покупателя"));
            Object currentDocNumber = tableModel.getValueAt(selectedRow, getColumnIndex("номер_документа_об_отгрузке"));
            Object currentProductCode = tableModel.getValueAt(selectedRow, getColumnIndex("код_готового_изделия"));
            Object currentUnit = tableModel.getValueAt(selectedRow, getColumnIndex("единица_измерения"));
            Object currentQuantity = tableModel.getValueAt(selectedRow, getColumnIndex("количество"));
            Object currentDate = tableModel.getValueAt(selectedRow, getColumnIndex("дата_отгрузки"));

            for (int i = 0; i < warehouseCombo.getItemCount(); i++) {
                if (warehouseCombo.getItemAt(i).startsWith(currentWarehouse.toString())) {
                    warehouseCombo.setSelectedIndex(i);
                    break;
                }
            }

            for (int i = 0; i < buyerCombo.getItemCount(); i++) {
                if (buyerCombo.getItemAt(i).startsWith(currentBuyer.toString())) {
                    buyerCombo.setSelectedIndex(i);
                    break;
                }
            }

            docNumberField.setText(currentDocNumber != null ? currentDocNumber.toString() : "");
            docNumberField.setEditable(false);
            productCodeField.setText(currentProductCode != null ? currentProductCode.toString() : "");
            unitField.setText(currentUnit != null ? currentUnit.toString() : "");
            quantityField.setText(currentQuantity != null ? currentQuantity.toString() : "");
            dateField.setText(currentDate != null ? currentDate.toString() : "");

            panel.add(new JLabel("Номер склада:"));
            panel.add(warehouseCombo);
            panel.add(new JLabel("Код покупателя:"));
            panel.add(buyerCombo);
            panel.add(new JLabel("Номер документа:"));
            panel.add(docNumberField);
            panel.add(new JLabel("Код изделия:"));
            panel.add(productCodeField);
            panel.add(new JLabel("Единица измерения:"));
            panel.add(unitField);
            panel.add(new JLabel("Количество:"));
            panel.add(quantityField);
            panel.add(new JLabel("Дата отгрузки (гггг-мм-дд чч:мм:сс):"));
            panel.add(dateField);

            int result = JOptionPane.showConfirmDialog(this, panel,
                    "Редактировать отгрузку",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String sql = "UPDATE Учет_отгрузки_готовой_продукции SET " +
                        "номер_склада = ?, код_покупателя = ?, " +
                        "код_готового_изделия = ?, единица_измерения = ?, количество = ?, дата_отгрузки = ? " +
                        "WHERE номер_документа_об_отгрузке = ?";

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, Integer.parseInt(warehouseCombo.getSelectedItem().toString().split(" - ")[0]));
                    pstmt.setInt(2, Integer.parseInt(buyerCombo.getSelectedItem().toString().split(" - ")[0]));
                    pstmt.setInt(3, Integer.parseInt(productCodeField.getText()));
                    pstmt.setString(4, unitField.getText());
                    pstmt.setInt(5, Integer.parseInt(quantityField.getText()));
                    pstmt.setTimestamp(6, Timestamp.valueOf(dateField.getText()));
                    pstmt.setString(7, docNumberField.getText());

                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        JOptionPane.showMessageDialog(this, "Отгрузка успешно обновлена!");
                        loadData();
                    }
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка при редактировании отгрузки: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<String> getWarehouses(Connection conn) throws SQLException {
        List<String> warehouses = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT номер_склада, фамилия_МОЛ FROM Склады")) {
            while (rs.next()) {
                warehouses.add(rs.getInt("номер_склада") + " - " + rs.getString("фамилия_МОЛ"));
            }
        }
        return warehouses;
    }

    private List<String> getBuyers(Connection conn) throws SQLException {
        List<String> buyers = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT код_покупателя, наименование_покупателя FROM Покупатели")) {
            while (rs.next()) {
                buyers.add(rs.getInt("код_покупателя") + " - " + rs.getString("наименование_покупателя"));
            }
        }
        return buyers;
    }
}

class AllTablesForm extends JFrame {
    private JTabbedPane tabbedPane;

    public AllTablesForm() {
        setTitle("Все таблицы базы данных");
        setSize(800, 600);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        addTableTab("Покупатели", "SELECT * FROM Покупатели");
        addTableTab("Склады", "SELECT * FROM Склады");
        addTableTab("Учет отгрузки", "SELECT * FROM Учет_отгрузки_готовой_продукции");

        add(tabbedPane);
    }

    private void addTableTab(String title, String query) {
        JTable table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);

        new Thread(() -> {
            try (Connection conn = Task2Client.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                String[] columns = new String[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    columns[i] = metaData.getColumnName(i + 1);
                }

                List<Object[]> data = new ArrayList<>();
                while (rs.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 0; i < columnCount; i++) {
                        row[i] = rs.getObject(i + 1);
                    }
                    data.add(row);
                }

                Object[][] dataArray = data.toArray(new Object[0][]);

                SwingUtilities.invokeLater(() -> {
                    table.setModel(new DefaultTableModel(dataArray, columns));
                    int tabIndex = tabbedPane.indexOfTab(title);
                    if (tabIndex != -1) {
                        tabbedPane.setTitleAt(tabIndex, title + " (" + data.size() + ")");
                    }
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Ошибка загрузки " + title + ": " + e.getMessage());
                });
            }
        }).start();

        tabbedPane.addTab(title, scrollPane);
    }
}

class CombinedViewForm extends JFrame {
    private JTable table;

    public CombinedViewForm() {
        setTitle("Объединенное представление");
        setSize(800, 400);
        setLocationRelativeTo(null);

        table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        new Thread(() -> {
            createViewIfNotExists();
            loadViewData();
        }).start();
    }

    private void createViewIfNotExists() {
        String createViewSQL =
                "CREATE OR REPLACE VIEW CombinedShipmentsView AS " +
                        "SELECT " +
                        "    s.номер_склада, " +
                        "    s.фамилия_МОЛ, " +
                        "    u.номер_документа_об_отгрузке, " +
                        "    p.наименование_покупателя, " +
                        "    p.адрес_покупателя, " +
                        "    u.код_готового_изделия, " +
                        "    u.единица_измерения, " +
                        "    u.количество, " +
                        "    u.дата_отгрузки " +
                        "FROM Учет_отгрузки_готовой_продукции u " +
                        "JOIN Покупатели p ON u.код_покупателя = p.код_покупателя " +
                        "JOIN Склады s ON u.номер_склада = s.номер_склада";

        try (Connection conn = Task2Client.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createViewSQL);
            System.out.println("Представление создано успешно");
        } catch (Exception e) {
            System.out.println("View уже существует: " + e.getMessage());
        }
    }

    private void loadViewData() {
        try (Connection conn = Task2Client.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM CombinedShipmentsView")) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            String[] columns = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                columns[i] = metaData.getColumnName(i + 1);
            }

            List<Object[]> data = new ArrayList<>();
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                data.add(row);
            }

            Object[][] dataArray = data.toArray(new Object[0][]);

            SwingUtilities.invokeLater(() -> {
                table.setModel(new DefaultTableModel(dataArray, columns));
            });

        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "Ошибка загрузки представления: " + e.getMessage());
            });
        }
    }
}

class Warehouse {
    private int id;
    private String managerName;

    public Warehouse(int id, String managerName) {
        this.id = id;
        this.managerName = managerName;
    }

    public int getId() { return id; }
    public String getManagerName() { return managerName; }
}

class Buyer {
    private int id;
    private String name;
    private String address;

    public Buyer(int id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
}

class Shipment {
    private int warehouseId;
    private int buyerId;
    private int productCode;
    private int quantity;
    private java.sql.Timestamp date;
    private String docNumber;
    private String unit;

    public Shipment(int warehouseId, int buyerId, int productCode, int quantity, java.sql.Timestamp date, String docNumber, String unit) {
        this.warehouseId = warehouseId;
        this.buyerId = buyerId;
        this.productCode = productCode;
        this.quantity = quantity;
        this.date = date;
        this.docNumber = docNumber;
        this.unit = unit;
    }

    public int getWarehouseId() { return warehouseId; }
    public int getBuyerId() { return buyerId; }
    public int getProductCode() { return productCode; }
    public int getQuantity() { return quantity; }
    public java.sql.Timestamp getDate() { return date; }
    public String getDocNumber() { return docNumber; }
    public String getUnit() { return unit; }
}

class Task1Form extends JFrame {
    private JTable table;
    private JTextField dateField;

    public Task1Form() {
        setTitle("Задача 1 - Отгрузки после даты");
        setSize(900, 500);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Дата (гггг-мм-дд):"));
        dateField = new JTextField(10);
        dateField.setText("2024-05-20");
        inputPanel.add(dateField);

        JButton sqlButton = new JButton("SQL запрос");
        JButton ormButton = new JButton("ORM обход");

        sqlButton.addActionListener(e -> executeSQLQuery());
        ormButton.addActionListener(e -> executeORMQuery());

        inputPanel.add(sqlButton);
        inputPanel.add(ormButton);

        panel.add(inputPanel, BorderLayout.NORTH);

        table = new JTable();
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        add(panel);
    }

    private void executeSQLQuery() {
        String date = dateField.getText();
        String sql = "SELECT s.номер_склада, p.наименование_покупателя, p.адрес_покупателя, u.код_готового_изделия, u.количество, u.дата_отгрузки, u.номер_документа_об_отгрузке, u.единица_измерения " +
                "FROM Учет_отгрузки_готовой_продукции u " +
                "JOIN Покупатели p ON u.код_покупателя = p.код_покупателя " +
                "JOIN Склады s ON u.номер_склада = s.номер_склада " +
                "WHERE u.дата_отгрузки > ? " +
                "ORDER BY u.дата_отгрузки";

        try (Connection conn = Task2Client.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            String[] columns = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                columns[i] = metaData.getColumnName(i + 1);
            }

            List<Object[]> data = new ArrayList<>();
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                data.add(row);
            }

            Object[][] dataArray = data.toArray(new Object[0][]);
            table.setModel(new DefaultTableModel(dataArray, columns));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка SQL запроса: " + e.getMessage());
        }
    }

    private void executeORMQuery() {
        String date = dateField.getText();
        try {
            List<Warehouse> allWarehouses = new ArrayList<>();
            List<Buyer> allBuyers = new ArrayList<>();
            List<Shipment> allShipments = new ArrayList<>();

            try (Connection conn = Task2Client.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM Склады")) {
                while (rs.next()) {
                    allWarehouses.add(new Warehouse(
                            rs.getInt("номер_склада"),
                            rs.getString("фамилия_МОЛ")
                    ));
                }
            }

            try (Connection conn = Task2Client.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM Покупатели")) {
                while (rs.next()) {
                    allBuyers.add(new Buyer(
                            rs.getInt("код_покупателя"),
                            rs.getString("наименование_покупателя"),
                            rs.getString("адрес_покупателя")
                    ));
                }
            }

            try (Connection conn = Task2Client.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM Учет_отгрузки_готовой_продукции")) {
                while (rs.next()) {
                    allShipments.add(new Shipment(
                            rs.getInt("номер_склада"),
                            rs.getInt("код_покупателя"),
                            rs.getInt("код_готового_изделия"),
                            rs.getInt("количество"),
                            rs.getTimestamp("дата_отгрузки"),
                            rs.getString("номер_документа_об_отгрузке"),
                            rs.getString("единица_измерения")
                    ));
                }
            }

            List<Object[]> result = new ArrayList<>();
            java.sql.Timestamp targetDate = java.sql.Timestamp.valueOf(date + " 00:00:00");

            for (Shipment shipment : allShipments) {
                if (shipment.getDate().after(targetDate)) {
                    Warehouse warehouse = findWarehouseById(allWarehouses, shipment.getWarehouseId());
                    Buyer buyer = findBuyerById(allBuyers, shipment.getBuyerId());

                    if (warehouse != null && buyer != null) {
                        result.add(new Object[]{
                                warehouse.getId(),
                                buyer.getName(),
                                buyer.getAddress(),
                                shipment.getProductCode(),
                                shipment.getQuantity(),
                                shipment.getDate(),
                                shipment.getDocNumber(),
                                shipment.getUnit()
                        });
                    }
                }
            }

            result.sort((a, b) -> ((java.util.Date) a[5]).compareTo((java.util.Date) b[5]));

            displayORMResult(result);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка ORM запроса: " + e.getMessage());
        }
    }

    private Warehouse findWarehouseById(List<Warehouse> warehouses, int id) {
        for (Warehouse warehouse : warehouses) {
            if (warehouse.getId() == id) {
                return warehouse;
            }
        }
        return null;
    }

    private Buyer findBuyerById(List<Buyer> buyers, int id) {
        for (Buyer buyer : buyers) {
            if (buyer.getId() == id) {
                return buyer;
            }
        }
        return null;
    }

    private void displayORMResult(List<Object[]> results) {
        Object[][] data = new Object[results.size()][8];
        for (int i = 0; i < results.size(); i++) {
            Object[] row = results.get(i);
            data[i][0] = row[0];
            data[i][1] = row[1];
            data[i][2] = row[2];
            data[i][3] = row[3];
            data[i][4] = row[4];
            data[i][5] = row[5];
            data[i][6] = row[6];
            data[i][7] = row[7];
        }

        String[] columns = {"Номер склада", "Наименование покупателя", "Адрес", "Код изделия", "Количество", "Дата отгрузки", "Номер документа", "Единица измерения"};
        table.setModel(new DefaultTableModel(data, columns));
    }
}

class Task2Form extends JFrame {
    private JTable table;

    public Task2Form() {
        setTitle("Задача 2 - Нумерация отгрузок");
        setSize(800, 400);
        setLocationRelativeTo(null);

        table = new JTable();
        add(new JScrollPane(table), BorderLayout.CENTER);

        executeQuery();
    }

    private void executeQuery() {
        String sql = "SELECT " +
                "    u.номер_склада, " +
                "    s.фамилия_МОЛ, " +
                "    p.наименование_покупателя, " +
                "    u.номер_документа_об_отгрузке, " +
                "    ROW_NUMBER() OVER (PARTITION BY u.код_покупателя ORDER BY u.дата_отгрузки) as номер_отгрузки, " +
                "    COUNT(*) OVER (PARTITION BY u.код_покупателя) as всего_отгрузок, " +
                "    SUM(u.количество) OVER (PARTITION BY u.код_покупателя) as общее_количество " +
                "FROM Учет_отгрузки_готовой_продукции u " +
                "JOIN Покупатели p ON u.код_покупателя = p.код_покупателя " +
                "JOIN Склады s ON u.номер_склада = s.номер_склада " +
                "ORDER BY u.код_покупателя, u.дата_отгрузки";

        try (Connection conn = Task2Client.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            String[] columns = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                columns[i] = metaData.getColumnName(i + 1);
            }

            List<Object[]> data = new ArrayList<>();
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                data.add(row);
            }

            Object[][] dataArray = data.toArray(new Object[0][]);
            table.setModel(new DefaultTableModel(dataArray, columns));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка запроса: " + e.getMessage());
        }
    }
}

class Task3Form extends JFrame {
    private JTable table;
    private JLabel resultLabel;

    public Task3Form() {
        setTitle("Задача 3 - Склады для покупателей из Казани");
        setSize(800, 500);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        JButton quantifierButton = new JButton("Кванторный SQL");
        JButton recordButton = new JButton("Record-ориентированный");

        quantifierButton.addActionListener(e -> executeQuantifierQuery());
        recordButton.addActionListener(e -> executeRecordQuery());

        buttonPanel.add(quantifierButton);
        buttonPanel.add(recordButton);
        panel.add(buttonPanel, BorderLayout.NORTH);

        table = new JTable();
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        resultLabel = new JLabel("Выберите метод выполнения");
        resultLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(resultLabel, BorderLayout.SOUTH);

        add(panel);
    }

    private void executeQuantifierQuery() {
        String sql = "SELECT DISTINCT s.номер_склада, s.фамилия_МОЛ " +
                "FROM Склады s " +
                "WHERE NOT EXISTS (" +
                "    SELECT 1 FROM Покупатели p " +
                "    WHERE p.адрес_покупателя LIKE '%Казань%' " +
                "    AND NOT EXISTS (" +
                "        SELECT 1 FROM Учет_отгрузки_готовой_продукции u " +
                "        WHERE u.номер_склада = s.номер_склада " +
                "        AND u.код_покупателя = p.код_покупателя " +
                "        AND u.количество > 100" +
                "    )" +
                ")";

        try (Connection conn = Task2Client.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            String[] columns = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                columns[i] = metaData.getColumnName(i + 1);
            }

            List<Object[]> data = new ArrayList<>();
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                data.add(row);
            }

            Object[][] dataArray = data.toArray(new Object[0][]);
            table.setModel(new DefaultTableModel(dataArray, columns));
            resultLabel.setText("Кванторный SQL: найдено " + data.size() + " складов");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка запроса: " + e.getMessage());
        }
    }

    private void executeRecordQuery() {
        try (Connection conn = Task2Client.getConnection()) {
            java.util.Map<Integer, String> warehouseMap = new java.util.HashMap<>();
            java.util.Set<Integer> kazanBuyerIds = new java.util.HashSet<>();
            java.util.Set<String> largeShipmentKeys = new java.util.HashSet<>();

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT номер_склада, фамилия_МОЛ FROM Склады")) {
                while (rs.next()) {
                    warehouseMap.put(rs.getInt("номер_склада"), rs.getString("фамилия_МОЛ"));
                }
            }

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "SELECT код_покупателя FROM Покупатели WHERE адрес_покупателя LIKE '%Казань%'")) {
                while (rs.next()) {
                    kazanBuyerIds.add(rs.getInt("код_покупателя"));
                }
            }

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "SELECT номер_склада, код_покупателя FROM Учет_отгрузки_готовой_продукции WHERE количество > 100")) {
                while (rs.next()) {
                    String key = rs.getInt("номер_склада") + "-" + rs.getInt("код_покупателя");
                    largeShipmentKeys.add(key);
                }
            }

            java.util.List<Object[]> result = new java.util.ArrayList<>();

            for (Integer warehouseId : warehouseMap.keySet()) {
                boolean servesAllKazan = true;

                for (Integer buyerId : kazanBuyerIds) {
                    String shipmentKey = warehouseId + "-" + buyerId;
                    if (!largeShipmentKeys.contains(shipmentKey)) {
                        servesAllKazan = false;
                        break;
                    }
                }

                if (servesAllKazan) {
                    result.add(new Object[]{
                            warehouseId,
                            warehouseMap.get(warehouseId)
                    });
                }
            }

            displayRecordResult(result);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка record-ориентированного запроса: " + e.getMessage());
        }
    }

    private void displayRecordResult(java.util.List<Object[]> results) {
        Object[][] data = new Object[results.size()][2];
        for (int i = 0; i < results.size(); i++) {
            data[i][0] = results.get(i)[0];
            data[i][1] = results.get(i)[1];
        }

        String[] columns = {"Номер склада", "Фамилия МОЛ"};
        table.setModel(new DefaultTableModel(data, columns));
        resultLabel.setText("Record-ориентированный: найдено " + results.size() + " складов");
    }
}