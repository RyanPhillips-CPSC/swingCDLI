import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;

/**
 * @author ryanphillips
 * @version 1.0.0
 */
public class Menu extends JFrame {

    private DefaultListModel<String> itemListModel;

    private static double currDiameter = 0.00;
    private static final int RPM = 1790;
    private static double SFM = 0.00;

    private Person selectedClient;              // will store selected client info
    private CRecord selectedConsultation;       // will store selected consultation info
    private Order selectedOrder;                // will store selected order info

    private CodeIdentifier selectedCode;        // will store selected code entry info
    private boolean expanded = false;           // will reflect whether the SplitPane is extended
    private boolean update = true;              /* will be used to ensure the code for updating a client profile
     * does not run when their row on the client interface is double-clicked.
     * It should only run if the user manually switches to the client profile tab */

    public Menu() {
        initComponents();
        initialize();
    }

    void loadProfile(DefaultTableModel dateData) {
        consultationTable.setEnabled(true);
        consultationViewer.setEnabled(true);

        orderFLLNM.setText(selectedClient.getLastname() + ", " + selectedClient.getFirstname());
        profileFN.setText(selectedClient.getFirstname());
        profileLN.setText(selectedClient.getLastname());
        profileE.setText(selectedClient.getEmail());
        profileP.setText(selectedClient.getPhone());
        profileA.setText(selectedClient.getAddress());
        profileFLLNM.setText(selectedClient.getLastname() + ", " + selectedClient.getFirstname());

        String sql = "SELECT COUNT(*) FROM client_consultation WHERE clientid = ?";
        Connection connection = null;
        int rowCount = 0;
        try {
            int cid = collectID();
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:earnhartDB.sqlite3");

            PreparedStatement prep = connection.prepareStatement(sql);
            prep.setInt(1, cid);
            ResultSet resultSet = prep.executeQuery();

            if (resultSet.next()) {
                rowCount = resultSet.getInt(1);
            }

            prep.close();
            resultSet.close();
            connection.close();
            if (rowCount == 0) {
                consultationViewer.setText("No consultation found for '" + profileLN.getText() + ", " + profileFN.getText() + "'");
                consultationTable.setEnabled(false);
                consultationViewer.setEnabled(true);
            } else {
                // Find the most recent date in the dateData DefaultTableModel
                String mostRecentDate = "";
                for (int i = 0; i < dateData.getRowCount(); i++) {
                    String currentDate = (String) dateData.getValueAt(i, 0);
                    if (mostRecentDate.compareTo(currentDate) < 0) {
                        mostRecentDate = currentDate;
                    }
                }

                String sqlTwo = "SELECT consultation FROM client_consultation WHERE cdate = ?";
                Connection connectionTwo = null;
                try {
                    Class.forName("org.sqlite.JDBC");
                    connectionTwo = DriverManager.getConnection("jdbc:sqlite:earnhartDB.sqlite3");

                    PreparedStatement preparedStatement = connectionTwo.prepareStatement(sqlTwo);
                    preparedStatement.setString(1, mostRecentDate);
                    ResultSet resultSetTwo = preparedStatement.executeQuery();

                    while (resultSetTwo.next()) {
                        consultationViewer.setText(resultSetTwo.getString("consultation"));
                    }

                    resultSetTwo.close();
                    preparedStatement.close();
                    connectionTwo.close();
                } catch (SQLException | ClassNotFoundException error) {
                    error.printStackTrace();
                }
            }
        } catch (SQLException | ClassNotFoundException s) {
            s.printStackTrace();
        }
    }

    void loadOrderProfile() {
        String sql = "SELECT odate, completed FROM orders WHERE clientid = ?";
        Connection connection = null;
        int cid = collectID();

        List<Order> orders = new ArrayList<>();

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:earnhartDB.sqlite3");

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, cid);

            ResultSet resultSet = preparedStatement.executeQuery();

            String[] columnNames = {"Date", "Completed"};
            DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
//            List<Order> orders = new ArrayList<>();
            while (resultSet.next()) {
                String date = resultSet.getString("odate");
                String completed = resultSet.getString("completed");
                Object[] rowData = {date, completed};
                tableModel.addRow(rowData);
                orders.add(new Order(date, completed));
            }

            orderTable.setModel(tableModel);

            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException | ClassNotFoundException error) {
            error.printStackTrace();
        }

        String sqlTwo = "SELECT COUNT(*) FROM orders WHERE clientid = ?";
        Connection connectionTwo = null;
        int rowCount = 0;
        try {
            Class.forName("org.sqlite.JDBC");
            connectionTwo = DriverManager.getConnection("jdbc:sqlite:earnhartDB.sqlite3");

            PreparedStatement prep = connectionTwo.prepareStatement(sqlTwo);
            prep.setInt(1, cid);
            ResultSet resultSet = prep.executeQuery();

            if (resultSet.next()) {
                rowCount = resultSet.getInt(1);
            }

            if (rowCount != 0) {
                connectionTwo.close();
                //find the most recent data in the list
                Order mostRecent = Collections.max(orders, Comparator.comparing(Order::getOdate));
                selectedOrder = mostRecent;
                String sqlThree = "SELECT * FROM orders WHERE odate = ?";
                Connection connectionThree = null;
                try {
                    Class.forName("org.sqlite.JDBC");
                    connectionThree = DriverManager.getConnection("jdbc:sqlite:earnhartDB.sqlite3");

                    PreparedStatement preparedStatement = connectionThree.prepareStatement(sqlThree);
                    preparedStatement.setString(1, mostRecent.getOdate());
                    ResultSet resultSetTwo = preparedStatement.executeQuery();

                    double price = 0, shipping = 0, tax = 0, total = 0;
                    String status = "";

                    while (resultSetTwo.next()) {
                        price = resultSetTwo.getDouble("price");
                        shipping = resultSetTwo.getDouble("shipping");
                        tax = resultSetTwo.getDouble("tax");
                        total = price + shipping + tax;

                        orderPrice.setText("$ " + String.valueOf(price));
                        orderShipping.setText("$ " + String.valueOf(shipping));
                        orderTax.setText("$ " + String.valueOf(tax));
                        orderTotal.setText("$ " + String.valueOf(total));

                        wixInvoice.setText(resultSetTwo.getString("wixinvoicenumber"));
                        status = resultSetTwo.getString("completed");
                        fulfilled.setText("Completed: " + status);

                        orderItems.setText(resultSetTwo.getString("items"));
                        orderNotes.setText(resultSetTwo.getString("notes"));
                    }

                    if (status.equals("YES")) {
                        fulfillButton.setEnabled(false);
                    } else {
                        fulfillButton.setEnabled(true);
                    }

                    resultSetTwo.close();
                    preparedStatement.close();
                    connectionThree.close();
                } catch (SQLException | ClassNotFoundException error) {
                    error.printStackTrace();
                }
            }
        } catch (SQLException | ClassNotFoundException error) {
            error.printStackTrace();
        }
    }

    int collectID() {
        String sql = "SELECT clientid FROM client WHERE firstname = ? AND lastname = ? AND email = ? AND phone = ? AND address = ?";
        int cid = 5;
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:earnhartDB.sqlite3");
            connection.setAutoCommit(false);

            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setString(1, selectedClient.getFirstname());
            pst.setString(2, selectedClient.getLastname());
            pst.setString(3, selectedClient.getEmail());
            pst.setString(4, selectedClient.getPhone());
            pst.setString(5, selectedClient.getAddress());
            ResultSet rs = pst.executeQuery();
            cid = rs.getInt("clientid");

            pst.close();
            rs.close();
            connection.close();
        } catch (SQLException | ClassNotFoundException error) {
            error.printStackTrace();
        }
        return cid;
    }

    void loadClientData() {
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:earnhartDB.sqlite3");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM client");

            String[] columnNames = {"Name", "Email", "Phone", "Address"};
            DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
            while (resultSet.next()) {
                String firstName = resultSet.getString("firstname");
                String lastName = resultSet.getString("lastname");
                String name = firstName + " " + lastName;
                String email = resultSet.getString("email");
                String phone = resultSet.getString("phone");
                String address = resultSet.getString("address");
                Object[] rowData = {name, email, phone, address};
                tableModel.addRow(rowData);
            }

            clientTable.setModel(tableModel);

            resultSet.close();
            statement.close();
            connection.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    void setupClientTableRowListener() {
        clientTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                int row = clientTable.rowAtPoint(event.getPoint());
                if (row >= 0) {
                    Person selectedPerson = getPersonFromRow(row);
                    if (event.getClickCount() == 2) {
                        tabPane.setEnabledAt(5, false);
                        int clientID = collectID();
                        update = false;
                        tabPane.setEnabledAt(4, false);
                        String sql = "SELECT cdate FROM client_consultation WHERE clientid = ?";
                        Connection connectionTwo = null;
                        DefaultTableModel dataModel = new DefaultTableModel();
                        try {
                            Class.forName("org.sqlite.JDBC");
                            connectionTwo = DriverManager.getConnection("jdbc:sqlite:earnhartDB.sqlite3");

                            PreparedStatement preparedStatement = connectionTwo.prepareStatement(sql);
                            preparedStatement.setInt(1, clientID);
                            ResultSet resultSet = preparedStatement.executeQuery();

                            String[] columnNames = {"Date"};
                            dataModel.setColumnIdentifiers(columnNames);

                            while (resultSet.next()) {
                                String cdate = resultSet.getString("cdate");
                                Object[] rowData = {cdate};
                                dataModel.addRow(rowData);
                            }

                            consultationTable.setModel(dataModel);

                            preparedStatement.close();
                            resultSet.close();
                            connectionTwo.close();
                        } catch (SQLException | ClassNotFoundException c) {
                            c.printStackTrace();
                        }
                        tabPane.setSelectedIndex(4);
                        selectedClient = selectedPerson;

                        loadProfile(dataModel);
                        loadOrderProfile();
                    } else {
                        selectedClient = selectedPerson;
                    }
                }
            }
        });
    }

    private Person getPersonFromRow(int row) {
        String fullname = (String) clientTable.getValueAt(row, 0);
        String[] nameParts = fullname.split(" ");
        String lastname = nameParts[0];
        String firstname = nameParts[1];
        String email = (String) clientTable.getValueAt(row, 1);
        String phone = (String) clientTable.getValueAt(row, 2);
        String address = (String) clientTable.getValueAt(row, 3);
        return new Person(firstname, lastname, email, phone, address);
    }

    @SuppressWarnings("unchecked")
    public void initialize() {
        itemListModel = new DefaultListModel<>();
        itemList.setModel(itemListModel);
//        clientErrorMessageOne.setVisible(false);
//        newOrderErrorMessage.setVisible(false);
//        newOrderErrorMessageTwo.setVisible(false);
        tabPane.setEnabledAt(10, false);
//        newOrderTab.setDisable(true);
        tabPane.setEnabledAt(5, false);
//        orderTab.setDisable(true);
//        disableMachiningErrors();
//        disableClientErrors();
//        qgErrorMessage.setVisible(false);
//        qgErrorMessageTwo.setVisible(false);
        tabPane.setEnabledAt(4, false);
//        profileTab.setDisable(true);
        tabPane.setEnabledAt(2, false);
//        editPane.setDisable(true);
//        splitPane.setDividerPositions(.95);

        loadClientData();
        setupClientTableRowListener();

//        try {
//            Class.forName("org.sqlite.JDBC");
//            connection = DriverManager.getConnection("jdbc:sqlite:earnhartDB.sqlite3");
//            Statement statement = connection.createStatement();
//            ResultSet resultSet = statement.executeQuery("SELECT gdate, title FROM g_code");
//
//            ObservableList<CodeIdentifier> data = FXCollections.observableArrayList();
//
//            while (resultSet.next()) {
//                CodeIdentifier codeIdentifier = new CodeIdentifier(resultSet.getString("title"), resultSet.getString("gdate"));
//                data.add(codeIdentifier);
//            }
//
//            codeTable.setItems(data);
//
//            TableColumn<CodeIdentifier, String> titleCol = new TableColumn<>("Title");
//            titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
//            codeTable.getColumns().add(titleCol);
//
//            TableColumn<CodeIdentifier, String> dateCol = new TableColumn<>("Date");
//            dateCol.setCellValueFactory(new PropertyValueFactory<>("gdate"));
//            codeTable.getColumns().add(dateCol);
//
//            resultSet.close();
//            statement.close();
//            connection.close();
//        } catch (ClassNotFoundException | SQLException e) {
//            e.printStackTrace();
//        }
//
//        codeTable.setRowFactory(tv -> {
//            TableRow<CodeIdentifier> row = new TableRow<>();
//            row.setOnMouseClicked(event -> {
//                if (!row.isEmpty()) {
//                    CodeIdentifier rowData = row.getItem();
//                    if (event.getClickCount() == 2) {
//                        String getCode = "SELECT code FROM g_code WHERE title = ?";
//                        Connection connection1 = null;
//                        try {
//                            Class.forName("org.sqlite.JDBC");
//                            connection1 = DriverManager.getConnection("jdbc:sqlite:earnhartDB.sqlite3");
//
//                            PreparedStatement prep = connection1.prepareStatement(getCode);
//                            prep.setString(1, selectedCode.getTitle());
//                            ResultSet resultSet = prep.executeQuery();
//
//                            fileDisplay.setText(resultSet.getString("code"));
//                            titleEntry.setText(selectedCode.getTitle());
//
//                            prep.close();
//                            connection1.close();
//                            resultSet.close();
//
//                        } catch (SQLException | ClassNotFoundException c) {
//                            c.printStackTrace();
//                        }
//                        this.selectedCode = rowData;
//                    } else {
//                        this.selectedCode = rowData;
//                    }
//                }
//            });
//            return row;
//        });
//
//        consultationTable.setRowFactory(tv -> {
//            TableRow<CRecord> row = new TableRow<>();
//            row.setOnMouseClicked(event -> {
//                if (!row.isEmpty()) {
//                    CRecord rowData = row.getItem();
//                    if (event.getClickCount() == 2) {
//                        this.selectedConsultation = rowData;
//                        String sql = "SELECT consultation FROM client_consultation WHERE cdate = ?";
//                        Connection connectionTwo = null;
//                        try {
//                            Class.forName("org.sqlite.JDBC");
//                            connectionTwo = DriverManager.getConnection("jdbc:sqlite:earnhartDB.sqlite3");
//
//                            PreparedStatement preparedStatement = connectionTwo.prepareStatement(sql);
//                            preparedStatement.setString(1, selectedConsultation.getCdate());
//                            ResultSet resultSet = preparedStatement.executeQuery();
//
//                            while (resultSet.next()) {
//                                consultationViewer.setText(resultSet.getString("consultation"));
//                            }
//
//                            connectionTwo.close();
//                            resultSet.close();
//                            preparedStatement.close();
//                        } catch (SQLException | ClassNotFoundException error) {
//                            error.printStackTrace();
//                        }
//                    } else {
//                        this.selectedConsultation = rowData;
//                    }
//                }
//            });
//            return row;
//        });
//
//        orderTable.setRowFactory(tv -> {
//            TableRow<Order> row = new TableRow<>();
//            row.setOnMouseClicked(event -> {
//                if (!row.isEmpty()) {
//                    Order rowData = row.getItem();
//                    if (event.getClickCount() == 2) {
//                        selectedOrder = rowData;
//                        Connection connectionTwo = null;
//                        String sql = "SELECT * FROM orders WHERE odate = ?";
//                        try {
//                            Class.forName("org.sqlite.JDBC");
//                            connectionTwo = DriverManager.getConnection("jdbc:sqlite:earnhartDB.sqlite3");
//
//                            PreparedStatement preparedStatement = connectionTwo.prepareStatement(sql);
//                            preparedStatement.setString(1, rowData.getOdate());
//
//                            ResultSet resultSet = preparedStatement.executeQuery();
//
//                            double price = 0, shipping = 0, tax = 0, total = 0;
//                            String status = "";
//
//                            while (resultSet.next()) {
//                                price = resultSet.getDouble("price");
//                                shipping = resultSet.getDouble("shipping");
//                                tax = resultSet.getDouble("tax");
//                                total = price + shipping + tax;
//
//                                orderPrice.setText("$ " + String.valueOf(price));
//                                orderShipping.setText("$ " + String.valueOf(shipping));
//                                orderTax.setText("$ " + String.valueOf(tax));
//                                orderTotal.setText("$ " + String.valueOf(total));
//
//                                wixInvoice.setText(resultSet.getString("wixinvoicenumber"));
//
//                                status = resultSet.getString("completed");
//                                fulfilled.setText("Completed: " + status);
//
//                                orderItems.setText(resultSet.getString("items"));
//                                orderNotes.setText(resultSet.getString("notes"));
//                            }
//
//                            System.out.println(status);
//                            if (status.equals("YES")) {
//                                fulfillButton.setDisable(true);
//                            } else {
//                                fulfillButton.setDisable(false);
//                            }
//
//                            preparedStatement.close();
//                            resultSet.close();
//                            connectionTwo.close();
//                        } catch (SQLException | ClassNotFoundException error) {
//                            error.printStackTrace();
//                        }
//                    } else {
//                        this.selectedOrder = rowData;
//                    }
//                }
//            });
//            return row;
//        });
    }

    void searchClients(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!searchBar.getText().isBlank()) {
                String userText = searchBar.getText().strip();

                Connection connection = null;
                String sql = "SELECT * FROM client WHERE firstname LIKE ? OR lastname LIKE ?";
                try {
                    Class.forName("org.sqlite.JDBC");
                    connection = DriverManager.getConnection("jdbc:sqlite:earnhartDB.sqlite3");

                    PreparedStatement prep = connection.prepareStatement(sql);
                    prep.setString(1, "%" + userText + "%");
                    prep.setString(2, "%" + userText + "%");

                    ResultSet resultSet = prep.executeQuery();

                    DefaultTableModel dataModel = new DefaultTableModel();
                    String[] columnNames = {"Full Name", "Email", "Phone", "Address"};
                    dataModel.setColumnIdentifiers(columnNames);

                    while (resultSet.next()) {
                        Person person = new Person(resultSet.getString("firstname"), resultSet.getString("lastname"), resultSet.getString("email"), resultSet.getString("phone"), resultSet.getString("address"));
                        Object[] rowData = {person.getFullname(), person.getEmail(), person.getPhone(), person.getAddress()};
                        dataModel.addRow(rowData);
                    }

                    clientTable.setModel(dataModel);
                    resultSet.close();
                    prep.close();
                    connection.close();
                } catch (SQLException | ClassNotFoundException error) {
                    error.printStackTrace();
                }
            } else {
                 updateClientTable();
            }
        }
    }

    void updateClientTable() {
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:earnhartDB.sqlite3");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM client");

            DefaultTableModel dataModel = new DefaultTableModel();
            String[] columnNames = {"Full Name", "Email", "Phone", "Address"};
            dataModel.setColumnIdentifiers(columnNames);

            while (resultSet.next()) {
                Person person = new Person(resultSet.getString("firstname"), resultSet.getString("lastname"), resultSet.getString("email"), resultSet.getString("phone"), resultSet.getString("address"));
                Object[] rowData = {person.getFullname(), person.getEmail(), person.getPhone(), person.getAddress()};
                dataModel.addRow(rowData);
            }

            clientTable.setModel(dataModel);

            connection.close();
            statement.close();
            resultSet.close();
        } catch (SQLException | ClassNotFoundException s) {
            s.printStackTrace();
        }
    }

    private void g52Submit(ActionEvent event) {
        if ((gOne.getText().strip()).equals("") || (gTwo.getText().strip()).equals("") ||
                (gThree.getText().strip()).equals("") || (gFour.getText().strip()).equals("")) {
//            disableMachiningErrors();
//            errorMessageFive.setVisible(true);
        } else {
//            disableMachiningErrors();
            String a,b,c,d;
            double A = 0,B = 0,C = 0,D = 0,stepOne,stepTwo,stepThree;
            try {
                a = gOne.getText().strip();
                A = Double.parseDouble(a);

                b = gTwo.getText().strip();
                B = Double.parseDouble(b);

                c = gThree.getText().strip();
                C = Double.parseDouble(c);

                d = gFour.getText().strip();
                D = Double.parseDouble(d);

                stepOne = C - D;
                stepTwo = B - A;
                stepThree = stepOne + stepTwo;

                String result = String.format("G52=X0.Z-%.3f", stepThree);
                answerLabel.setText(result);
            } catch (NumberFormatException L) {
//                errorMessageSix.setVisible(true);
            }
        }
    }

    private void searchEntries(KeyEvent e) {
        // TODO add your code here
    }

    private void sfmCalculator(ActionEvent e) {
        if ((sfmInput.getText().isEmpty()) || (sfm.getText().isEmpty())) {
            return;
        }
        String input = sfmInput.getText().strip();
        try {
            SFM = Double.parseDouble(sfm.getText().strip());
        } catch (InputMismatchException error) {
            return;
        }
        // Read input from the inputTextArea
        InputStream inputStream = new ByteArrayInputStream(sfmInput.getText().getBytes());
        Scanner scanner = new Scanner(inputStream);

        String currLine; // stores a line of code
        String updatedLine; // stores an updated line of code
        Scanner set; // creates a Scanner to read through each line
        ArrayList<String> updatedLines = new ArrayList<>(); // creates an ArrayList to store updated lines of code
        ArrayList<String> pieces = new ArrayList<>(); // creates an ArrayList to store the pieces of a line of code
        boolean newFeedRate = false; // will be true if a diameter breaks the threshold
        String feedRate = ""; // will store newly calculated feed rates
        while (scanner.hasNext()) { // reads through the entire file
            updatedLine = "";
            pieces.clear();
            currLine = scanner.nextLine();
            // if "G0" or "G00" are present, or no X value is present, skip line
            if (!(currLine.contains("G0") || currLine.contains("G00")) && (currLine.contains("X"))) {
                set = new Scanner(currLine);
                while (set.hasNext()) { // add all line pieces to ArrayList pieces
                    pieces.add(set.next());
                }

                if (currLine.contains("F")) { // if there is already a feed rate present in line, store it in currFeedRate and reset currDiameter
                    for (int i = 0; i < pieces.size(); i++) { // iterate through line pieces
                        char firstChar = pieces.get(i).charAt(0);
                        if (firstChar == 'X') {
                            currDiameter = Double.parseDouble(pieces.get(i).substring(1));
                        }
                    }
                    updatedLines.add(currLine);
//                    System.out.println(currLine);
                } else {
                    for (int i = 0; i < pieces.size(); i++) { // iterate through line pieces
                        char firstChar = pieces.get(i).charAt(0);
                        switch (firstChar) {
                            case 'N', 'G', 'Z', 'R':
                                updatedLine += pieces.get(i) + " ";
                                break;
                            case 'X': // calculate the feed rate
                                updatedLine += pieces.get(i) + " ";
                                feedRate = newFeedRate(Double.parseDouble(pieces.get(i).substring(1)));
                                if (!(feedRate.equals(""))) {
                                    newFeedRate = true;
                                }
                                break;
                        }
                    }
                    if (newFeedRate) {
                        updatedLine += "F" + feedRate;
                        newFeedRate = false;
                    }
                    updatedLines.add(updatedLine);
//                    System.out.println(updatedLine);
                }
            } else { // if the line was skipped
                updatedLines.add(currLine);
//                System.out.println(currLine);
            }
        }

        String updatedCode = "";
        for (int i = 0; i < updatedLines.size(); i++) {
            updatedCode += updatedLines.get(i) + "\n";
        }

        sfmOutput.setText(updatedCode);
    }

    public static String newFeedRate(double diameter) {
        if (currDiameter == 0.00) {
            currDiameter = diameter;
        } else {
            // round both diameters to the thousandth place
            double roundedDiameter = Math.round(diameter * 1000.0) / 1000.0;
            double roundedCurrDiameter = Math.round(currDiameter * 1000.0) / 1000.0;

            // if the threshold breaks, return a new feed rate
            if (Math.abs(roundedDiameter - roundedCurrDiameter) >= 0.001) {
                double currFeedRateValue = ((SFM * 12) / (diameter * RPM * Math.PI)) / RPM;
                DecimalFormat decimalFormat = new DecimalFormat("#.####");
                currDiameter = diameter;
                return decimalFormat.format(currFeedRateValue);
            }
        }
        return "";
    }

    private void quoteGenerator(ActionEvent e) {
        quoteTotal.setText("$ 0.00");
        double conceptModelRimCost = 0.00;
        double platingCost = 0.00;
        double totalCost = 0.00;

        String rim = (String) rimCombo.getSelectedItem();
        boolean isChecked = screwRim.isSelected();
        if (isChecked) {
            totalCost += 235.00;
        }

        switch (Objects.requireNonNull(rim)) {
            case "ss + $45":
                conceptModelRimCost = 42.00;
                break;
            case "ti + $65":
                conceptModelRimCost = 65.00;
                break;
            case "lx + $20":
                conceptModelRimCost = 20.00;
                break;
            case "dl + $8":
                conceptModelRimCost = 8.00;
                break;
            default:
                conceptModelRimCost = 0.00;
                break;
        }

        String plating = (String) materialCombo.getSelectedItem();
        switch (Objects.requireNonNull(plating)) {
            case "silver + $14":
                platingCost = 14.00;
                break;
            case "gold (rc) + $80", "gold (full) + $80":
                platingCost = 80.00;
                break;
            case "rhodium (full) + $90":
                platingCost = 90.00;
                break;
            case "deposited diamond (full) + $240":
                platingCost = 240.00;
                break;
            default:
                platingCost = 0.00;
                break;
        }

        String designTimeText = designTime.getText().strip();
        if (designTimeText.equals("")) {
            designTimeText = "0.00";
        }
        String manualTimeText = manualMachiningTime.getText().strip();
        if (manualTimeText.equals("")) {
            manualTimeText = "0.00";
        }
        String cncTimeText = cncMachiningTime.getText().strip();
        if (cncTimeText.equals("")) {
            cncTimeText = "0.00";
        }
        String finishingTimeText = finishingTime.getText().strip();
        if (finishingTimeText.equals("")) {
            finishingTimeText = "0.00";
        }
        String customCostText = customCost.getText().strip();
        if (customCostText.equals("")) {
            customCostText = "0.00";
        }
        String shippingCostText = shippingCost.getText().strip();
        if (shippingCostText.equals("")) {
            shippingCostText = "0.00";
        }

        double subCost;
        try {
            subCost = (Double.parseDouble(designTimeText) / 60f * 75f) + (Double.parseDouble(manualTimeText) / 60f * 60f)
                    + (Double.parseDouble(cncTimeText) / 60f * 80f) + (Double.parseDouble(finishingTimeText) / 60f * 60f)
                    + conceptModelRimCost + Double.parseDouble(customCostText) + platingCost + Double.parseDouble(shippingCostText);
        } catch (NumberFormatException error) {
            return;
        }

        totalCost += subCost;
        quoteTotal.setText(String.format("Total: $ %.2f", totalCost));
    }

    private void calculator(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            String equation = calculator.getText().trim();
            if (!isValidArithmeticOperation(equation)) {
                return;
            }

            double result = evaluateArithmeticOperation(equation);
            calculatorAnswer.setText(String.valueOf(result));
        }
    }

    private boolean isValidArithmeticOperation(String input) {
        // check if input contains only valid arithmetic operators and numbers
        return input.matches("[0-9+\\-*/. ]+");
    }

    private double evaluateArithmeticOperation(String input) {
        // split the input into numbers and operators and evaluate the operation
        String[] tokens = input.split("(?<=\\d)(?=\\D)|(?<=\\D)(?=\\d)");
        double result = Double.parseDouble(tokens[0]);

        for (int i = 1; i < tokens.length; i += 2) {
            String operator = tokens[i];
            double operand = Double.parseDouble(tokens[i + 1]);

            switch (operator) {
                case "+":
                    result += operand;
                    break;
                case "-":
                    result -= operand;
                    break;
                case "*":
                    result *= operand;
                    break;
                case "/":
                    result /= operand;
                    break;
            }
        }

        return result;
    }

    private void addItem(ActionEvent e) {
        if (productEntry.getText().isEmpty() || quantityEntry.getText().isEmpty()) {
            return;
        }
        String product = productEntry.getText();
        String quantity = quantityEntry.getText();
        String plating = (String) platingDropdown.getSelectedItem();
//        packingList.add(new String[]{product, quantity, plating}); TODO
        itemListModel.addElement(product + " - " + quantity + " - " + plating);
        productEntry.setText("");
        quantityEntry.setText("");
    }

    private void exportPDF(ActionEvent e) {
//        String filename = "packing_list_" + LocalDateTime.now().toString().replace(':', '-') + ".pdf";
//        File desktop = new File(System.getProperty("user.home"), "Desktop");
//        File pdfPath = new File(desktop, filename);
//
//        try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfPath));
//             Document document = new Document(writer)) {
//
//            Paragraph title = new Paragraph("Packing List")
//                    .setFont(com.itextpdf.layout.font.FontProvider.getFont("Helvetica"))
//                    .setFontSize(14)
//                    .setTextAlignment(TextAlignment.CENTER);
//            document.add(title);
//
//            for (String[] item : packingList) {
//                Paragraph product = new Paragraph("Product Name: " + item[0])
//                        .setFont(com.itextpdf.layout.font.FontProvider.getFont("Helvetica"))
//                        .setFontSize(12);
//                document.add(product);
//
//                Paragraph quantity = new Paragraph("Quantity: " + item[1])
//                        .setFont(com.itextpdf.layout.font.FontProvider.getFont("Helvetica"))
//                        .setFontSize(12);
//                document.add(quantity);
//
//                Paragraph plating = new Paragraph("Plating Type: " + item[2])
//                        .setFont(com.itextpdf.layout.font.FontProvider.getFont("Helvetica"))
//                        .setFontSize(12);
//                document.add(plating);
//
//                document.add(new Paragraph("\n"));
//            }
//        }
//
//        JOptionPane.showMessageDialog(frame, "Packing list saved as " + filename);
//        packingList.clear();
//        itemListModel.clear();
    }

    private void createUIComponents() {
        // TODO: add custom component creation code here
    }

    private void clearItemList(ActionEvent e) {
        itemListModel.clear();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner Educational license - Ryan Phillips  (Ryan Phillips)
        tabPane = new JTabbedPane();
        panel1 = new JPanel();
        searchBar = new JTextField();
        scrollPane1 = new JScrollPane();
        clientTable = new JTable();
        button1 = new JButton();
        editClient = new JButton();
        clientOrder = new JButton();
        consultation = new JButton();
        removeClient = new JButton();
        panel10 = new JPanel();
        panel11 = new JPanel();
        panel2 = new JPanel();
        label1 = new JLabel();
        label2 = new JLabel();
        gOne = new JTextField();
        gTwo = new JTextField();
        gThree = new JTextField();
        gFour = new JTextField();
        answerLabel = new JLabel();
        button2 = new JButton();
        entrySearchBar = new JTextField();
        scrollPane2 = new JScrollPane();
        codeTable = new JTable();
        button3 = new JButton();
        button4 = new JButton();
        button5 = new JButton();
        titleEntry = new JTextField();
        scrollPane3 = new JScrollPane();
        fileDisplay = new JTextArea();
        panel3 = new JPanel();
        profileFLLNM = new JLabel();
        label5 = new JLabel();
        label6 = new JLabel();
        label7 = new JLabel();
        label8 = new JLabel();
        label9 = new JLabel();
        profileFN = new JLabel();
        profileLN = new JLabel();
        profileP = new JLabel();
        profileA = new JLabel();
        profileE = new JLabel();
        button6 = new JButton();
        button7 = new JButton();
        button8 = new JButton();
        scrollPane4 = new JScrollPane();
        consultationTable = new JTable();
        scrollPane5 = new JScrollPane();
        consultationViewer = new JTextArea();
        panel4 = new JPanel();
        orderFLLNM = new JLabel();
        label10 = new JLabel();
        label11 = new JLabel();
        label12 = new JLabel();
        label13 = new JLabel();
        label14 = new JLabel();
        orderPrice = new JLabel();
        orderShipping = new JLabel();
        orderTax = new JLabel();
        orderTotal = new JLabel();
        label19 = new JLabel();
        wixInvoice = new JLabel();
        fulfilled = new JLabel();
        button9 = new JButton();
        fulfillButton = new JButton();
        scrollPane6 = new JScrollPane();
        orderTable = new JTable();
        scrollPane7 = new JScrollPane();
        orderNotes = new JTextArea();
        scrollPane8 = new JScrollPane();
        orderItems = new JTextArea();
        panel5 = new JPanel();
        scrollPane9 = new JScrollPane();
        sfmInput = new JTextArea();
        scrollPane10 = new JScrollPane();
        sfmOutput = new JTextArea();
        button10 = new JButton();
        label16 = new JLabel();
        sfm = new JTextField();
        label17 = new JLabel();
        panel6 = new JPanel();
        label15 = new JLabel();
        designTime = new JTextField();
        manualMachiningTime = new JTextField();
        customCost = new JTextField();
        cncMachiningTime = new JTextField();
        finishingTime = new JTextField();
        label18 = new JLabel();
        label20 = new JLabel();
        label21 = new JLabel();
        label22 = new JLabel();
        label23 = new JLabel();
        shippingCost = new JTextField();
        label24 = new JLabel();
        calculator = new JTextField();
        label25 = new JLabel();
        rimCombo = new JComboBox<>();
        materialCombo = new JComboBox<>();
        screwRim = new JCheckBox();
        button11 = new JButton();
        quoteTotal = new JLabel();
        calculatorAnswer = new JLabel();
        panel7 = new JPanel();
        productEntry = new JTextField();
        quantityEntry = new JTextField();
        label26 = new JLabel();
        label28 = new JLabel();
        label27 = new JLabel();
        platingDropdown = new JComboBox<>();
        label29 = new JLabel();
        button12 = new JButton();
        export = new JButton();
        scrollPane11 = new JScrollPane();
        itemList = new JList<String>();
        button13 = new JButton();
        panel8 = new JPanel();
        panel9 = new JPanel();

        //======== this ========
        var contentPane = getContentPane();
        contentPane.setLayout(null);

        //======== tabPane ========
        {

            //======== panel1 ========
            {
                panel1.setLayout(null);

                //---- searchBar ----
                searchBar.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        searchClients(e);
                    }
                });
                panel1.add(searchBar);
                searchBar.setBounds(20, 15, 265, 40);

                //======== scrollPane1 ========
                {

                    //---- clientTable ----
                    clientTable.setEnabled(false);
                    scrollPane1.setViewportView(clientTable);
                }
                panel1.add(scrollPane1);
                scrollPane1.setBounds(20, 60, 875, 435);

                //---- button1 ----
                button1.setText("New Client");
                panel1.add(button1);
                button1.setBounds(925, 75, 150, 40);

                //---- editClient ----
                editClient.setText("Edit Client");
                panel1.add(editClient);
                editClient.setBounds(925, 135, 150, 40);

                //---- clientOrder ----
                clientOrder.setText("Add Order");
                panel1.add(clientOrder);
                clientOrder.setBounds(925, 255, 150, 40);

                //---- consultation ----
                consultation.setText("Consultation");
                panel1.add(consultation);
                consultation.setBounds(925, 315, 150, 40);

                //---- removeClient ----
                removeClient.setText("Remove Client");
                panel1.add(removeClient);
                removeClient.setBounds(925, 195, 150, 40);

                {
                    // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < panel1.getComponentCount(); i++) {
                        Rectangle bounds = panel1.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = panel1.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    panel1.setMinimumSize(preferredSize);
                    panel1.setPreferredSize(preferredSize);
                }
            }
            tabPane.addTab("Client Interface", panel1);

            //======== panel10 ========
            {
                panel10.setLayout(null);

                {
                    // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < panel10.getComponentCount(); i++) {
                        Rectangle bounds = panel10.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = panel10.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    panel10.setMinimumSize(preferredSize);
                    panel10.setPreferredSize(preferredSize);
                }
            }
            tabPane.addTab("New Client", panel10);

            //======== panel11 ========
            {
                panel11.setLayout(null);

                {
                    // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < panel11.getComponentCount(); i++) {
                        Rectangle bounds = panel11.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = panel11.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    panel11.setMinimumSize(preferredSize);
                    panel11.setPreferredSize(preferredSize);
                }
            }
            tabPane.addTab("|||", panel11);

            //======== panel2 ========
            {
                panel2.setLayout(null);

                //---- label1 ----
                label1.setText("Zo Calibration Procedure");
                label1.setFont(new Font("Inter", Font.PLAIN, 21));
                panel2.add(label1);
                label1.setBounds(new Rectangle(new Point(20, 20), label1.getPreferredSize()));

                //---- label2 ----
                label2.setText("<html><center>Insert mouthpiece into collet. Face stock, calibrate Z0 for all tools on face</center></html>");
                label2.setHorizontalAlignment(SwingConstants.CENTER);
                panel2.add(label2);
                label2.setBounds(35, 50, 220, 88);
                panel2.add(gOne);
                gOne.setBounds(15, 145, 260, 45);
                panel2.add(gTwo);
                gTwo.setBounds(15, 195, 260, 45);
                panel2.add(gThree);
                gThree.setBounds(15, 245, 260, 45);
                panel2.add(gFour);
                gFour.setBounds(15, 295, 260, 45);

                //---- answerLabel ----
                answerLabel.setHorizontalTextPosition(SwingConstants.CENTER);
                answerLabel.setHorizontalAlignment(SwingConstants.CENTER);
                answerLabel.setBackground(Color.black);
                answerLabel.setText("...");
                panel2.add(answerLabel);
                answerLabel.setBounds(0, 435, 285, 60);

                //---- button2 ----
                button2.setText("Submit");
                button2.addActionListener(e -> g52Submit(e));
                panel2.add(button2);
                button2.setBounds(80, 365, 120, 40);

                //---- entrySearchBar ----
                entrySearchBar.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        searchEntries(e);
                    }
                });
                panel2.add(entrySearchBar);
                entrySearchBar.setBounds(305, 20, 300, 40);

                //======== scrollPane2 ========
                {
                    scrollPane2.setViewportView(codeTable);
                }
                panel2.add(scrollPane2);
                scrollPane2.setBounds(305, 65, 300, 435);

                //---- button3 ----
                button3.setText("Edit");
                panel2.add(button3);
                button3.setBounds(615, 465, button3.getPreferredSize().width, 35);

                //---- button4 ----
                button4.setText("X");
                panel2.add(button4);
                button4.setBounds(685, 465, 70, 35);

                //---- button5 ----
                button5.setText("Submit");
                panel2.add(button5);
                button5.setBounds(995, 465, 90, 35);
                panel2.add(titleEntry);
                titleEntry.setBounds(760, 465, 240, 36);

                //======== scrollPane3 ========
                {
                    scrollPane3.setViewportView(fileDisplay);
                }
                panel2.add(scrollPane3);
                scrollPane3.setBounds(615, 20, 470, 435);

                {
                    // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < panel2.getComponentCount(); i++) {
                        Rectangle bounds = panel2.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = panel2.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    panel2.setMinimumSize(preferredSize);
                    panel2.setPreferredSize(preferredSize);
                }
            }
            tabPane.addTab("Machining Interface", panel2);

            //======== panel3 ========
            {
                panel3.setLayout(null);

                //---- profileFLLNM ----
                profileFLLNM.setText("Doe, John");
                profileFLLNM.setFont(new Font("Inter", Font.PLAIN, 35));
                panel3.add(profileFLLNM);
                profileFLLNM.setBounds(25, 10, 505, 65);

                //---- label5 ----
                label5.setText("Firstname");
                label5.setFont(label5.getFont().deriveFont(label5.getFont().getStyle() | Font.BOLD));
                panel3.add(label5);
                label5.setBounds(new Rectangle(new Point(25, 90), label5.getPreferredSize()));

                //---- label6 ----
                label6.setText("Lastname");
                label6.setFont(label6.getFont().deriveFont(label6.getFont().getStyle() | Font.BOLD));
                panel3.add(label6);
                label6.setBounds(25, 120, 95, 17);

                //---- label7 ----
                label7.setText("Email");
                label7.setFont(label7.getFont().deriveFont(label7.getFont().getStyle() | Font.BOLD));
                panel3.add(label7);
                label7.setBounds(25, 190, 62, 17);

                //---- label8 ----
                label8.setText("Phone");
                label8.setFont(label8.getFont().deriveFont(label8.getFont().getStyle() | Font.BOLD));
                panel3.add(label8);
                label8.setBounds(25, 155, 62, 17);

                //---- label9 ----
                label9.setText("Address");
                label9.setFont(label9.getFont().deriveFont(label9.getFont().getStyle() | Font.BOLD));
                panel3.add(label9);
                label9.setBounds(25, 265, 62, 17);

                //---- profileFN ----
                profileFN.setText("John");
                panel3.add(profileFN);
                profileFN.setBounds(135, 90, 180, 17);

                //---- profileLN ----
                profileLN.setText("Doe");
                panel3.add(profileLN);
                profileLN.setBounds(135, 120, 180, 17);

                //---- profileP ----
                profileP.setText("5409030781");
                panel3.add(profileP);
                profileP.setBounds(135, 155, 185, 17);

                //---- profileA ----
                profileA.setText("7405 Stonegate Estates Dr");
                profileA.setVerticalAlignment(SwingConstants.TOP);
                panel3.add(profileA);
                profileA.setBounds(25, 290, 280, 50);

                //---- profileE ----
                profileE.setText("ryanphillipsmusic41@gmail.com");
                profileE.setVerticalAlignment(SwingConstants.TOP);
                panel3.add(profileE);
                profileE.setBounds(25, 215, 280, 45);

                //---- button6 ----
                button6.setText("Order Info");
                panel3.add(button6);
                button6.setBounds(50, 345, 220, 40);

                //---- button7 ----
                button7.setText("Edit Consultation");
                panel3.add(button7);
                button7.setBounds(50, 400, 220, 40);

                //---- button8 ----
                button8.setText("Delete Consultation");
                panel3.add(button8);
                button8.setBounds(50, 455, 220, 40);

                //======== scrollPane4 ========
                {
                    scrollPane4.setViewportView(consultationTable);
                }
                panel3.add(scrollPane4);
                scrollPane4.setBounds(325, 90, 205, 410);

                //======== scrollPane5 ========
                {
                    scrollPane5.setViewportView(consultationViewer);
                }
                panel3.add(scrollPane5);
                scrollPane5.setBounds(545, 15, 540, 485);

                {
                    // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < panel3.getComponentCount(); i++) {
                        Rectangle bounds = panel3.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = panel3.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    panel3.setMinimumSize(preferredSize);
                    panel3.setPreferredSize(preferredSize);
                }
            }
            tabPane.addTab("Profile", panel3);

            //======== panel4 ========
            {
                panel4.setLayout(null);

                //---- orderFLLNM ----
                orderFLLNM.setText("Doe, John");
                orderFLLNM.setFont(new Font("Inter", Font.PLAIN, 35));
                panel4.add(orderFLLNM);
                orderFLLNM.setBounds(25, 10, 630, 65);

                //---- label10 ----
                label10.setText("Order Summary");
                panel4.add(label10);
                label10.setBounds(new Rectangle(new Point(85, 85), label10.getPreferredSize()));

                //---- label11 ----
                label11.setText("Price:");
                panel4.add(label11);
                label11.setBounds(25, 125, 60, 17);

                //---- label12 ----
                label12.setText("Shipping:");
                panel4.add(label12);
                label12.setBounds(25, 150, 60, 17);

                //---- label13 ----
                label13.setText("Tax:");
                panel4.add(label13);
                label13.setBounds(25, 175, 60, 17);

                //---- label14 ----
                label14.setText("Total:");
                panel4.add(label14);
                label14.setBounds(25, 220, 60, 17);

                //---- orderPrice ----
                orderPrice.setText("$ 0.00");
                panel4.add(orderPrice);
                orderPrice.setBounds(110, 125, 155, 17);

                //---- orderShipping ----
                orderShipping.setText("$ 0.00");
                panel4.add(orderShipping);
                orderShipping.setBounds(110, 150, 155, 17);

                //---- orderTax ----
                orderTax.setText("$ 0.00");
                panel4.add(orderTax);
                orderTax.setBounds(110, 175, 155, 17);

                //---- orderTotal ----
                orderTotal.setText("$ 0.00");
                panel4.add(orderTotal);
                orderTotal.setBounds(110, 220, 155, 17);

                //---- label19 ----
                label19.setText("Invoice #:");
                panel4.add(label19);
                label19.setBounds(100, 260, 75, 17);

                //---- wixInvoice ----
                wixInvoice.setText("000000000000000000");
                wixInvoice.setHorizontalAlignment(SwingConstants.CENTER);
                panel4.add(wixInvoice);
                wixInvoice.setBounds(0, 290, 260, 17);

                //---- fulfilled ----
                fulfilled.setText("Not Fulfilled");
                panel4.add(fulfilled);
                fulfilled.setBounds(90, 340, 85, 17);

                //---- button9 ----
                button9.setText("Remove Order");
                panel4.add(button9);
                button9.setBounds(35, 400, 185, 40);

                //---- fulfillButton ----
                fulfillButton.setText("Fulfill Order");
                panel4.add(fulfillButton);
                fulfillButton.setBounds(35, 455, 185, 40);

                //======== scrollPane6 ========
                {
                    scrollPane6.setViewportView(orderTable);
                }
                panel4.add(scrollPane6);
                scrollPane6.setBounds(270, 90, 385, 411);

                //======== scrollPane7 ========
                {
                    scrollPane7.setViewportView(orderNotes);
                }
                panel4.add(scrollPane7);
                scrollPane7.setBounds(670, 265, 415, 235);

                //======== scrollPane8 ========
                {
                    scrollPane8.setViewportView(orderItems);
                }
                panel4.add(scrollPane8);
                scrollPane8.setBounds(670, 15, 415, 235);

                {
                    // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < panel4.getComponentCount(); i++) {
                        Rectangle bounds = panel4.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = panel4.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    panel4.setMinimumSize(preferredSize);
                    panel4.setPreferredSize(preferredSize);
                }
            }
            tabPane.addTab("Order", panel4);

            //======== panel5 ========
            {
                panel5.setLayout(null);

                //======== scrollPane9 ========
                {
                    scrollPane9.setViewportView(sfmInput);
                }
                panel5.add(scrollPane9);
                scrollPane9.setBounds(5, 10, 540, 450);

                //======== scrollPane10 ========
                {

                    //---- sfmOutput ----
                    sfmOutput.setEditable(false);
                    scrollPane10.setViewportView(sfmOutput);
                }
                panel5.add(scrollPane10);
                scrollPane10.setBounds(550, 10, 540, 450);

                //---- button10 ----
                button10.setText("Submit");
                button10.addActionListener(e -> sfmCalculator(e));
                panel5.add(button10);
                button10.setBounds(315, 465, 230, 45);

                //---- label16 ----
                label16.setText("Input");
                label16.setFont(new Font("Inter", Font.PLAIN, 18));
                panel5.add(label16);
                label16.setBounds(10, 475, 59, 23);
                panel5.add(sfm);
                sfm.setBounds(115, 465, 194, 45);

                //---- label17 ----
                label17.setText("Modified Feed Rate");
                label17.setFont(new Font("Inter", Font.PLAIN, 18));
                label17.setHorizontalAlignment(SwingConstants.CENTER);
                panel5.add(label17);
                label17.setBounds(550, 470, 540, 23);

                {
                    // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < panel5.getComponentCount(); i++) {
                        Rectangle bounds = panel5.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = panel5.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    panel5.setMinimumSize(preferredSize);
                    panel5.setPreferredSize(preferredSize);
                }
            }
            tabPane.addTab("SFM Calculator", panel5);

            //======== panel6 ========
            {
                panel6.setLayout(null);

                //---- label15 ----
                label15.setText(" Earnhart Company, LLC Mouthpiece Quoting Engine");
                label15.setFont(new Font("Inter", Font.PLAIN, 18));
                label15.setHorizontalAlignment(SwingConstants.CENTER);
                panel6.add(label15);
                label15.setBounds(0, 15, 1100, label15.getPreferredSize().height);
                panel6.add(designTime);
                designTime.setBounds(330, 90, 215, 45);
                panel6.add(manualMachiningTime);
                manualMachiningTime.setBounds(330, 155, 215, 45);
                panel6.add(customCost);
                customCost.setBounds(330, 355, 215, 45);
                panel6.add(cncMachiningTime);
                cncMachiningTime.setBounds(330, 220, 215, 45);
                panel6.add(finishingTime);
                finishingTime.setBounds(330, 285, 215, 45);

                //---- label18 ----
                label18.setText("Design Time (min)");
                panel6.add(label18);
                label18.setBounds(90, 105, 190, label18.getPreferredSize().height);

                //---- label20 ----
                label20.setText("Manual Machining Time (min)");
                panel6.add(label20);
                label20.setBounds(90, 170, 225, 17);

                //---- label21 ----
                label21.setText("CNC Machining Time (min)");
                panel6.add(label21);
                label21.setBounds(90, 235, 190, 17);

                //---- label22 ----
                label22.setText("Finishing Time (min)");
                panel6.add(label22);
                label22.setBounds(90, 300, 190, 17);

                //---- label23 ----
                label23.setText("Custom Cost");
                panel6.add(label23);
                label23.setBounds(90, 370, 190, 17);
                panel6.add(shippingCost);
                shippingCost.setBounds(330, 425, 215, 45);

                //---- label24 ----
                label24.setText("Shipping Cost");
                panel6.add(label24);
                label24.setBounds(90, 440, 190, 17);

                //---- calculator ----
                calculator.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        calculator(e);
                    }
                });
                panel6.add(calculator);
                calculator.setBounds(730, 85, 315, 45);

                //---- label25 ----
                label25.setText("Calculator");
                panel6.add(label25);
                label25.setBounds(635, 100, 190, 17);

                //---- rimCombo ----
                rimCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                    "br(standard) + $0",
                    "dl + $8",
                    "lx + $20",
                    "ss + $45",
                    "ti + $65"
                }));
                panel6.add(rimCombo);
                rimCombo.setBounds(700, 275, 280, 40);

                //---- materialCombo ----
                materialCombo.setModel(new DefaultComboBoxModel<>(new String[] {
                    "quoting as concept model + $0",
                    "silver + $14",
                    "gold (rc) + $80",
                    "gold (full) + $80",
                    "rhodium (full) + $90",
                    "deposited diamond (full) + $240"
                }));
                panel6.add(materialCombo);
                materialCombo.setBounds(700, 340, 280, 40);

                //---- screwRim ----
                screwRim.setText("<html><center>Concept Model Screw Rim (includes all finishing and setup processes)</center></html>");
                screwRim.setHorizontalAlignment(SwingConstants.CENTER);
                screwRim.setActionCommand("<html><center>Concept Model Screw Rim (includes all finishing and setup processes)</center></html>");
                panel6.add(screwRim);
                screwRim.setBounds(705, 190, 275, 60);

                //---- button11 ----
                button11.setText("Calculate Total");
                button11.addActionListener(e -> quoteGenerator(e));
                panel6.add(button11);
                button11.setBounds(740, 445, 195, 45);

                //---- quoteTotal ----
                quoteTotal.setText("-- Total --");
                quoteTotal.setHorizontalAlignment(SwingConstants.CENTER);
                panel6.add(quoteTotal);
                quoteTotal.setBounds(625, 405, 425, quoteTotal.getPreferredSize().height);

                //---- calculatorAnswer ----
                calculatorAnswer.setText("- XXXXXX -");
                calculatorAnswer.setHorizontalAlignment(SwingConstants.CENTER);
                panel6.add(calculatorAnswer);
                calculatorAnswer.setBounds(735, 135, 305, 20);

                {
                    // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < panel6.getComponentCount(); i++) {
                        Rectangle bounds = panel6.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = panel6.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    panel6.setMinimumSize(preferredSize);
                    panel6.setPreferredSize(preferredSize);
                }
            }
            tabPane.addTab("Quote", panel6);

            //======== panel7 ========
            {
                panel7.setLayout(null);
                panel7.add(productEntry);
                productEntry.setBounds(180, 85, 280, 40);
                panel7.add(quantityEntry);
                quantityEntry.setBounds(180, 140, 280, 40);

                //---- label26 ----
                label26.setText("Product Name");
                label26.setFont(new Font("Inter", Font.PLAIN, 14));
                panel7.add(label26);
                label26.setBounds(45, 95, 135, label26.getPreferredSize().height);

                //---- label28 ----
                label28.setText("Quantity");
                label28.setFont(new Font("Inter", Font.PLAIN, 14));
                panel7.add(label28);
                label28.setBounds(45, 150, 109, 20);

                //---- label27 ----
                label27.setText("Packing Slip Generator");
                label27.setFont(new Font("Inter", Font.PLAIN, 24));
                label27.setHorizontalAlignment(SwingConstants.CENTER);
                panel7.add(label27);
                label27.setBounds(0, 15, 1100, label27.getPreferredSize().height);

                //---- platingDropdown ----
                platingDropdown.setModel(new DefaultComboBoxModel<>(new String[] {
                    "Silver",
                    "Gold (Full Mouthpiece)",
                    "Gold (Rim and Cup)"
                }));
                panel7.add(platingDropdown);
                platingDropdown.setBounds(180, 215, 275, 40);

                //---- label29 ----
                label29.setText("Finish");
                label29.setFont(new Font("Inter", Font.PLAIN, 14));
                panel7.add(label29);
                label29.setBounds(45, 225, 109, 20);

                //---- button12 ----
                button12.setText("Append");
                button12.addActionListener(e -> addItem(e));
                panel7.add(button12);
                button12.setBounds(115, 305, 280, 45);

                //---- export ----
                export.setText("Export PDF to Desktop");
                export.setEnabled(false);
                export.addActionListener(e -> exportPDF(e));
                panel7.add(export);
                export.setBounds(115, 375, 280, 45);

                //======== scrollPane11 ========
                {
                    scrollPane11.setViewportView(itemList);
                }
                panel7.add(scrollPane11);
                scrollPane11.setBounds(525, 85, 525, 380);

                //---- button13 ----
                button13.setText("C");
                button13.addActionListener(e -> clearItemList(e));
                panel7.add(button13);
                button13.setBounds(1000, 45, 50, button13.getPreferredSize().height);

                {
                    // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < panel7.getComponentCount(); i++) {
                        Rectangle bounds = panel7.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = panel7.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    panel7.setMinimumSize(preferredSize);
                    panel7.setPreferredSize(preferredSize);
                }
            }
            tabPane.addTab("Packing", panel7);

            //======== panel8 ========
            {
                panel8.setLayout(null);

                {
                    // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < panel8.getComponentCount(); i++) {
                        Rectangle bounds = panel8.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = panel8.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    panel8.setMinimumSize(preferredSize);
                    panel8.setPreferredSize(preferredSize);
                }
            }
            tabPane.addTab("Q & A", panel8);

            //======== panel9 ========
            {
                panel9.setLayout(null);

                {
                    // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < panel9.getComponentCount(); i++) {
                        Rectangle bounds = panel9.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = panel9.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    panel9.setMinimumSize(preferredSize);
                    panel9.setPreferredSize(preferredSize);
                }
            }
            tabPane.addTab("|||", panel9);
        }
        contentPane.add(tabPane);
        tabPane.setBounds(0, 0, 1100, 545);

        {
            // compute preferred size
            Dimension preferredSize = new Dimension();
            for(int i = 0; i < contentPane.getComponentCount(); i++) {
                Rectangle bounds = contentPane.getComponent(i).getBounds();
                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
            }
            Insets insets = contentPane.getInsets();
            preferredSize.width += insets.right;
            preferredSize.height += insets.bottom;
            contentPane.setMinimumSize(preferredSize);
            contentPane.setPreferredSize(preferredSize);
        }
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Ryan Phillips  (Ryan Phillips)
    private JTabbedPane tabPane;
    private JPanel panel1;
    private JTextField searchBar;
    private JScrollPane scrollPane1;
    private JTable clientTable;
    private JButton button1;
    private JButton editClient;
    private JButton clientOrder;
    private JButton consultation;
    private JButton removeClient;
    private JPanel panel10;
    private JPanel panel11;
    private JPanel panel2;
    private JLabel label1;
    private JLabel label2;
    private JTextField gOne;
    private JTextField gTwo;
    private JTextField gThree;
    private JTextField gFour;
    private JLabel answerLabel;
    private JButton button2;
    private JTextField entrySearchBar;
    private JScrollPane scrollPane2;
    private JTable codeTable;
    private JButton button3;
    private JButton button4;
    private JButton button5;
    private JTextField titleEntry;
    private JScrollPane scrollPane3;
    private JTextArea fileDisplay;
    private JPanel panel3;
    private JLabel profileFLLNM;
    private JLabel label5;
    private JLabel label6;
    private JLabel label7;
    private JLabel label8;
    private JLabel label9;
    private JLabel profileFN;
    private JLabel profileLN;
    private JLabel profileP;
    private JLabel profileA;
    private JLabel profileE;
    private JButton button6;
    private JButton button7;
    private JButton button8;
    private JScrollPane scrollPane4;
    private JTable consultationTable;
    private JScrollPane scrollPane5;
    private JTextArea consultationViewer;
    private JPanel panel4;
    private JLabel orderFLLNM;
    private JLabel label10;
    private JLabel label11;
    private JLabel label12;
    private JLabel label13;
    private JLabel label14;
    private JLabel orderPrice;
    private JLabel orderShipping;
    private JLabel orderTax;
    private JLabel orderTotal;
    private JLabel label19;
    private JLabel wixInvoice;
    private JLabel fulfilled;
    private JButton button9;
    private JButton fulfillButton;
    private JScrollPane scrollPane6;
    private JTable orderTable;
    private JScrollPane scrollPane7;
    private JTextArea orderNotes;
    private JScrollPane scrollPane8;
    private JTextArea orderItems;
    private JPanel panel5;
    private JScrollPane scrollPane9;
    private JTextArea sfmInput;
    private JScrollPane scrollPane10;
    private JTextArea sfmOutput;
    private JButton button10;
    private JLabel label16;
    private JTextField sfm;
    private JLabel label17;
    private JPanel panel6;
    private JLabel label15;
    private JTextField designTime;
    private JTextField manualMachiningTime;
    private JTextField customCost;
    private JTextField cncMachiningTime;
    private JTextField finishingTime;
    private JLabel label18;
    private JLabel label20;
    private JLabel label21;
    private JLabel label22;
    private JLabel label23;
    private JTextField shippingCost;
    private JLabel label24;
    private JTextField calculator;
    private JLabel label25;
    private JComboBox<String> rimCombo;
    private JComboBox<String> materialCombo;
    private JCheckBox screwRim;
    private JButton button11;
    private JLabel quoteTotal;
    private JLabel calculatorAnswer;
    private JPanel panel7;
    private JTextField productEntry;
    private JTextField quantityEntry;
    private JLabel label26;
    private JLabel label28;
    private JLabel label27;
    private JComboBox<String> platingDropdown;
    private JLabel label29;
    private JButton button12;
    private JButton export;
    private JScrollPane scrollPane11;
    private JList itemList;
    private JButton button13;
    private JPanel panel8;
    private JPanel panel9;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
