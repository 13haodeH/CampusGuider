import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginGUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginGUI() {
        super("登录");
        // 创建界面组件
        JLabel usernameLabel = new JLabel("用户名:");
        JLabel passwordLabel = new JLabel("密码:");
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        JButton loginButton = new JButton("登录");
        JButton guestButton = new JButton("游客登录");

        // 设置布局
        setLayout(new GridLayout(3, 2));

        // 添加组件到界面
        add(usernameLabel);
        add(usernameField);
        add(passwordLabel);
        add(passwordField);
        add(loginButton);
        add(guestButton);

        // 登录按钮的事件处理
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                // 调用登录方法，例如验证管理员账号密码的逻辑
                boolean loggedIn = login(username, password);
                if (loggedIn) {
                    JOptionPane.showMessageDialog(LoginGUI.this, "管理员登录成功");
                    AdminGUI adminGUI = new AdminGUI();
                    adminGUI.setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(LoginGUI.this, "登录失败，请检查用户名和密码");
                }
            }
        });

        // 游客登录按钮的事件处理
        guestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 在此处添加游客登录的逻辑
                JOptionPane.showMessageDialog(LoginGUI.this, "游客登录成功");
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        CampusNavigationSystem campusNavigationSystem = new CampusNavigationSystem();
                        campusNavigationSystem.setVisible(true);
                        dispose();
                    }
                });
            }
        });

        // 设置窗口大小和关闭操作
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    // 登录方法的示例实现
    private boolean login(String username, String password) {
        // 在此处编写验证管理员账号密码的逻辑
        // 返回 true 表示登录成功，返回 false 表示登录失败
        return username.equals("admin") && password.equals("123");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginGUI loginGUI = new LoginGUI();
                loginGUI.setVisible(true);
            }
        });
    }
}
