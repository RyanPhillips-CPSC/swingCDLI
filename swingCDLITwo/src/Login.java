import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

/**
 * @author Ryan Phillips
 * @version 1.0.0
 */
public class Login extends JFrame {

    public Login() {
        initComponents();
    }

    public static void main(String[] args) {
        Login login = new Login();
        login.setTitle("CDLI");
        login.setSize(400, 400);
        login.setResizable(false);
        login.setLocationRelativeTo(null);
        login.pack();
        login.setVisible(true);
    }

    public void login(ActionEvent event) {
        String username = "";
        String password = "";
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:earnhartDB.sqlite3");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM login");

            while (resultSet.next()) {
                username = resultSet.getString("username");
                password = resultSet.getString("password");
            }

            if ((usernameField.getText().strip()).equals(username) && passwordField.getText().equals(password)) {
                setVisible(false);
                Menu menu = new Menu();
                menu.setTitle("CDLI");
                menu.setSize(new Dimension(1100, 600));
                menu.setResizable(false);
                menu.setLocationRelativeTo(null);
                menu.pack();
                menu.setVisible(true);
            } else {
                usernameField.setText("");
                passwordField.setText("");
            }

            statement.close();
            connection.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner Educational license - Ryan Phillips  (Ryan Phillips)
        panel1 = new JPanel();
        passwordField = new JPasswordField();
        usernameField = new JTextField();
        label1 = new JLabel();
        label2 = new JLabel();
        loginButton = new JButton();
        label3 = new JLabel();

        //======== this ========
        setPreferredSize(new Dimension(400, 400));
        setMinimumSize(new Dimension(400, 400));
        setMaximumSize(new Dimension(400, 400));
        var contentPane = getContentPane();

        //======== panel1 ========
        {

            //---- label1 ----
            label1.setText("username");
            label1.setForeground(Color.black);

            //---- label2 ----
            label2.setText("password");
            label2.setForeground(Color.black);

            GroupLayout panel1Layout = new GroupLayout(panel1);
            panel1.setLayout(panel1Layout);
            panel1Layout.setHorizontalGroup(
                panel1Layout.createParallelGroup()
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addComponent(label1)
                            .addComponent(label2))
                        .addGap(18, 18, 18)
                        .addGroup(panel1Layout.createParallelGroup()
                            .addComponent(passwordField, GroupLayout.PREFERRED_SIZE, 220, GroupLayout.PREFERRED_SIZE)
                            .addComponent(usernameField, GroupLayout.PREFERRED_SIZE, 220, GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(19, Short.MAX_VALUE))
            );
            panel1Layout.setVerticalGroup(
                panel1Layout.createParallelGroup()
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(panel1Layout.createParallelGroup()
                            .addGroup(GroupLayout.Alignment.TRAILING, panel1Layout.createSequentialGroup()
                                .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(label1)
                                    .addComponent(usernameField, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE))
                                .addGap(79, 79, 79))
                            .addGroup(GroupLayout.Alignment.TRAILING, panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(passwordField, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
                                .addComponent(label2)))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
        }

        //---- loginButton ----
        loginButton.setText("Sign In");
        loginButton.addActionListener(e -> login(e));

        //---- label3 ----
        label3.setText("CDLI");
        label3.setFont(new Font("Nimbus Sans L", Font.PLAIN, 28));
        label3.setForeground(Color.black);
        label3.setHorizontalAlignment(SwingConstants.CENTER);

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .addComponent(label3, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addGap(45, 45, 45)
                    .addComponent(panel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(loginButton, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                    .addGap(135, 135, 135))
        );
        contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addGap(32, 32, 32)
                    .addComponent(label3)
                    .addGap(33, 33, 33)
                    .addComponent(panel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGap(27, 27, 27)
                    .addComponent(loginButton, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addGap(35, 35, 35))
        );
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Ryan Phillips  (Ryan Phillips)
    private JPanel panel1;
    private JPasswordField passwordField;
    private JTextField usernameField;
    private JLabel label1;
    private JLabel label2;
    private JButton loginButton;
    private JLabel label3;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}