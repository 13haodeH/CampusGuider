import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.List;


public class CampusNavigationSystem extends JFrame {
    private JPanel mapPanel;
    private BufferedImage mapImage;

    private ArrayList<Location> locations;
    private List<Point> points = new ArrayList<>();

    private List<Edge1> edges = new ArrayList<>();

    private List<Point> shortestPath = new ArrayList<>();
    String jdbcUrl = "jdbc:mysql://localhost:3306/test";
    String username = "root";
    String password = "abc123";
    Connection connection = null;


    public CampusNavigationSystem() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(jdbcUrl,username,password);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        setTitle("校园导航系统");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        // 加载校园地图图片
        try {
            mapImage = ImageIO.read(new File("D:\\桌面文件\\课设\\3.png"));
            int newWidth = 500; // 设置新的宽度
            int newHeight = 300; // 设置新的高度
            Image scaledImage = mapImage.getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT);
            mapImage = toBufferedImage(scaledImage);
        } catch (IOException e) {
            e.printStackTrace();
            // 在加载地图图片失败时进行错误处理
            JOptionPane.showMessageDialog(this, "无法加载地图图片！", "错误", JOptionPane.ERROR_MESSAGE);
        }

        // 初始化点和边的信息
        initializeData();
        loadPointsFromFile();
        loadEdgesFromFile();

        // 设置地图面板
        mapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // 绘制校园地图图片
                if (mapImage != null) {
                    g.drawImage(mapImage, 0, 0, 1000, 650, this);
                }
                setPreferredSize(new Dimension(1000, 650));
                revalidate();
                // 绘制建筑点和路径
                for (Edge1 edge : edges) {
                    int sourceIndex = edge.source;
                    int targetIndex = edge.target;
                    if (sourceIndex >= 0 && sourceIndex < points.size() && targetIndex >= 0 && targetIndex < points.size()) {
                        int pointAX = points.get(sourceIndex).x;
                        int pointAY = points.get(sourceIndex).y;
                        int pointBX = points.get(targetIndex).x;
                        int pointBY = points.get(targetIndex).y;
                        // 绘制路径
                        g.setColor(Color.white);
                        g.drawLine(pointAX, pointAY, pointBX, pointBY);
                        // 绘制建筑点
                        drawBuildingPoint(g, pointAX, pointAY);
                        drawBuildingPoint(g, pointBX, pointBY);
                        // 绘制边的距离
                        int distanceX = (pointAX + pointBX) / 2;
                        int distanceY = (pointAY + pointBY) / 2;
                        g.setColor(Color.white);
                        g.drawString(String.valueOf(edge.distance), distanceX, distanceY);
                        // 绘制建筑点的标签
                        for (Point point : points) {
                            g.setColor(Color.white);
                            g.drawString(point.getLabel(), point.getX(), point.getY() - 5);
                        }
                        drawShortestPath(g);
                    }


                }

            }
        };

        // 创建一个面板用于放置下拉框和按钮
        JPanel bottomPanel = new JPanel(new FlowLayout());

        // 创建下拉框并添加所有点
        JLabel startLabel = new JLabel("起点:");
        DefaultComboBoxModel<Point> startComboBoxModel = new DefaultComboBoxModel<>(new Vector<>(points));
        JComboBox<Point> startComboBox = new JComboBox<>(startComboBoxModel);

        JLabel endLabel = new JLabel("终点:");
        DefaultComboBoxModel<Point> endComboBoxModel = new DefaultComboBoxModel<>(new Vector<>(points));
        JComboBox<Point> endComboBox = new JComboBox<>(endComboBoxModel);
        // 创建查询最短路径按钮
        JButton findPathButton = new JButton("查询最短路径");
        findPathButton.addActionListener(e -> {
            Point startPoint = (Point) startComboBox.getSelectedItem();
            Point endPoint = (Point) endComboBox.getSelectedItem();
            System.out.println(startPoint);
            System.out.println(endPoint);
            if (startPoint != null && endPoint != null) {
                // 将坐标转换为对应的 Location 对象
                dijkstra(startPoint, endPoint);
                mapPanel.repaint();
            }
        });

        JButton et = new JButton("退出");
        et.addActionListener(e -> {
            LoginGUI loginGUI = new LoginGUI();
            loginGUI.setVisible(true);
            dispose();
        });

        // 将下拉框和按钮添加到面板中
        bottomPanel.add(startLabel);
        bottomPanel.add(startComboBox);
        bottomPanel.add(endLabel);
        bottomPanel.add(endComboBox);
        bottomPanel.add(findPathButton);
        bottomPanel.add(et);
        add(bottomPanel,BorderLayout.SOUTH); // 将面板放在窗口的底部
        add(mapPanel, BorderLayout.CENTER); // 将地图面板放在窗口的中央

        // 创建一个面板用于放置标题
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());
        JLabel titleLabel = new JLabel("校园导航系统");
        titleLabel.setFont(new Font("宋体", Font.BOLD, 36));
        topPanel.add(titleLabel);
        add(topPanel, BorderLayout.NORTH); // 将面板放在窗口的上方

        openFunctionDialog();

    }
    private void drawShortestPath(Graphics g) {
        if (shortestPath == null) {
            return;
        }

        g.setColor(Color.RED);
        for (int i = 0; i < shortestPath.size() - 1; i++) {
            Point pointA = shortestPath.get(i);
            Point pointB = shortestPath.get(i + 1);

            int pointAX = pointA.getX();
            int pointAY = pointA.getY();
            int pointBX = pointB.getX();
            int pointBY = pointB.getY();

            g.drawLine(pointAX, pointAY, pointBX, pointBY);
        }
    }

    private void dijkstra(Point start, Point end) {
        // 使用邻接矩阵表示图的连接关系和距离
        int[][] adjacencyMatrix = new int[points.size()][points.size()];
        System.out.println(points.size());
       
        }


    }

    private void loadPointsFromFile() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM point");

            points.clear();
            while (resultSet.next()) {
                int x = resultSet.getInt("x");
                int y = resultSet.getInt("y");
                String label = resultSet.getString("label");
                int index = resultSet.getInt("id");
                Point point = new Point(x, y, label,index);
                points.add(point);
            }

            resultSet.close();
            statement.close();
        }
         catch (SQLException e) {
            throw new RuntimeException(e);

         }
        System.out.println(points);
    }
    private void loadEdgesFromFile() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM edge");

            edges.clear();

            while (resultSet.next()) {
                int source = resultSet.getInt("source");
                int target = resultSet.getInt("target");
                int distance = resultSet.getInt("distance");

                Edge1 edge = new Edge1(source, target, distance);
                edges.add(edge);
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            // 处理从数据库加载边信息错误
        }
    }


    private void openFunctionDialog() {
        locations = new ArrayList<>();
        locations.add(new Location("校园南门", 700, 480, "D:\\桌面文件\\课设\\xiaoyuannanmeng.png"));
        locations.add(new Location("公园", 680, 360, "D:\\桌面文件\\课设\\gongyuan.png"));
        locations.add(new Location("朴园餐厅", 670, 180, "D:\\桌面文件\\课设\\puyuancanting.png"));
        locations.add(new Location("女生宿舍", 610, 90, "D:\\桌面文件\\课设\\nvshengsushe.png"));
        locations.add(new Location("健康驿站", 600, 160, "D:\\桌面文件\\课设\\jiankangyizhan.png"));
        locations.add(new Location("中日友好交流中心", 550, 150, "D:\\桌面文件\\课设\\zhongriyouhaojiaoliuzhongxin.png"));
        locations.add(new Location("锦宏超市", 490, 160, "D:\\桌面文件\\课设\\jinhongchaoshi.png"));
        locations.add(new Location("旧图书馆", 440, 50, "D:\\桌面文件\\课设\\jiutushuguan.png"));
        locations.add(new Location("校医院", 420, 120, "D:\\桌面文件\\课设\\xiaoyiyuan.png"));
        locations.add(new Location("校园北门", 310, 120, "D:\\桌面文件\\课设\\xiaoyuanbeimen.png"));
        locations.add(new Location("学生澡堂", 440, 200, "D:\\桌面文件\\课设\\xueshengzaotang.png"));
        locations.add(new Location("旧朴园餐厅", 510, 220, "D:\\桌面文件\\课设\\jiupuyuancanting.png"));
        locations.add(new Location("研究生男生宿舍", 420, 250, "D:\\桌面文件\\课设\\yanjiushengnanshengsushe.png"));
        locations.add(new Location("研究生女生宿舍", 270, 210, "D:\\桌面文件\\课设\\yanjiushengnvshengsushe.png"));
        locations.add(new Location("逸夫图书馆", 300, 320, "D:\\桌面文件\\课设\\yifutushuguan.png"));
        locations.add(new Location("电力大楼", 430, 350, "D:\\桌面文件\\课设\\dianlidalou.png"));
        locations.add(new Location("室外体育场", 390, 510, "D:\\桌面文件\\课设\\shiwaitiyuchang.png"));
        locations.add(new Location("综合体育馆", 210, 590, "D:\\桌面文件\\课设\\zonghetiyuguan.png"));
        locations.add(new Location("教学楼", 130, 450, "D:\\桌面文件\\课设\\jiaoxuelou.png"));
        locations.add(new Location("格物楼", 200, 400, "D:\\桌面文件\\课设\\gewulou.png"));
        locations.add(new Location("金川综合服务楼", 800, 250, "D:\\桌面文件\\课设\\jingchuanzonghefuwulou.png"));

        // 添加鼠标监听器来显示位置信息和图片
        mapPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 获取鼠标点击位置的坐标
                int x = e.getX();
                int y = e.getY();

                // 根据坐标获取对应的位置信息
                Location clickedLocation = getLocationByCoordinate(x, y, locations);
                // 如果找到对应位置，则显示位置信息和图片
                if (clickedLocation != null) {
                    // 创建一个面板，用于显示位置信息和图片
                    JPanel messagePanel = new JPanel(new BorderLayout());
                    JLabel messageLabel = new JLabel(clickedLocation.getName());
                    ImageIcon imageIcon = new ImageIcon(clickedLocation.getImagePath());
                    imageIcon.setImage(imageIcon.getImage().getScaledInstance(600, 400, Image.SCALE_DEFAULT));
                    JLabel imageLabel = new JLabel(imageIcon);
                    messagePanel.add(messageLabel, BorderLayout.NORTH);
                    messagePanel.add(imageLabel, BorderLayout.CENTER);
                    // 显示位置信息和图片
                    JOptionPane.showMessageDialog(mapPanel, messagePanel, "位置信息", JOptionPane.INFORMATION_MESSAGE);

                }
            }
        });

    }

    private Location getLocationByCoordinate(int x, int y, ArrayList<Location> locations) {
        for (Location location : locations) {
            if (Math.abs(location.getX() - x) < 10 && Math.abs(location.getY() - y) < 10) {
                return location;
            }
        }
        return null;
    }


    // 初始化点和边的信息
    private void initializeData() {
        // 初始化21个点（随机坐标和名称）
        points.add(new Point(700, 480, "校园南门",1));
        points.add(new Point(680, 360, "公园",2));
        points.add(new Point(670, 180, "朴园餐厅",3));
        points.add(new Point(610, 90, "女生宿舍",4));
        points.add(new Point(600, 160, "健康驿站",5));
        points.add(new Point(550, 150, "中日友好交流中心",6));
        points.add(new Point(490, 160, "锦宏超市",7));
        points.add(new Point(440, 50, "旧图书馆",8));
        points.add(new Point(420, 120, "校医院",9));
        points.add(new Point(310, 120, "校园北门",10));
        points.add(new Point(440, 200, "学生澡堂",11));
        points.add(new Point(510, 220, "旧朴园餐厅",12));
        points.add(new Point(420, 250, "研究生男生宿舍",13));
        points.add(new Point(270, 210, "研究生女生宿舍",14));
        points.add(new Point(300, 320, "逸夫图书馆",15));
        points.add(new Point(430, 350, "电力大楼",16));
        points.add(new Point(390, 510, "室外体育场",17));
        points.add(new Point(210, 590, "综合体育馆",18));
        points.add(new Point(130, 450, "教学楼",19));
        points.add(new Point(200, 400, "格物楼",20));
        points.add(new Point(800, 250, "金川综合服务楼",21));

        // 初始化边
        edges = new ArrayList<Edge1>(); // 23个点，共有51条边
        edges.add(new Edge1(0, 1, calculateDistance(points.get(0), points.get(1))));
        edges.add(new Edge1(0, 16, calculateDistance(points.get(0), points.get(16))));
        edges.add(new Edge1(0, 15, calculateDistance(points.get(0), points.get(15))));
        edges.add(new Edge1(1, 16, calculateDistance(points.get(1), points.get(16))));
        edges.add(new Edge1(1, 15, calculateDistance(points.get(1), points.get(15))));
        edges.add(new Edge1(1, 11, calculateDistance(points.get(1), points.get(11))));
        edges.add(new Edge1(1, 4, calculateDistance(points.get(1), points.get(4))));
        edges.add(new Edge1(1, 2, calculateDistance(points.get(1), points.get(2))));
        edges.add(new Edge1(1, 20, calculateDistance(points.get(1), points.get(20))));
        edges.add(new Edge1(2, 20, calculateDistance(points.get(2), points.get(20))));
        edges.add(new Edge1(2, 3, calculateDistance(points.get(2), points.get(3))));
        edges.add(new Edge1(2, 4, calculateDistance(points.get(2), points.get(4))));
        edges.add(new Edge1(3, 4, calculateDistance(points.get(3), points.get(4))));
        edges.add(new Edge1(3, 5, calculateDistance(points.get(3), points.get(5))));
        edges.add(new Edge1(3, 7, calculateDistance(points.get(3), points.get(7))));
        edges.add(new Edge1(3, 8, calculateDistance(points.get(3), points.get(8))));
        edges.add(new Edge1(4, 5, calculateDistance(points.get(4), points.get(5))));
        edges.add(new Edge1(4, 11, calculateDistance(points.get(4), points.get(11))));
        edges.add(new Edge1(5, 7, calculateDistance(points.get(5), points.get(7))));
        edges.add(new Edge1(5, 6, calculateDistance(points.get(5), points.get(6))));
        edges.add(new Edge1(5, 11, calculateDistance(points.get(5), points.get(11))));
        edges.add(new Edge1(6, 7, calculateDistance(points.get(6), points.get(7))));
        edges.add(new Edge1(6, 8, calculateDistance(points.get(6), points.get(8))));
        edges.add(new Edge1(6, 10, calculateDistance(points.get(6), points.get(10))));
        edges.add(new Edge1(6, 11, calculateDistance(points.get(6), points.get(11))));
        edges.add(new Edge1(7, 8, calculateDistance(points.get(7), points.get(8))));
        edges.add(new Edge1(8, 10, calculateDistance(points.get(8), points.get(10))));
        edges.add(new Edge1(8, 9, calculateDistance(points.get(8), points.get(9))));
        edges.add(new Edge1(8, 12, calculateDistance(points.get(8), points.get(12))));
        edges.add(new Edge1(9, 12, calculateDistance(points.get(9), points.get(12))));
        edges.add(new Edge1(9, 13, calculateDistance(points.get(9), points.get(13))));
        edges.add(new Edge1(10, 11, calculateDistance(points.get(10), points.get(11))));
        edges.add(new Edge1(10, 12, calculateDistance(points.get(10), points.get(12))));
        edges.add(new Edge1(10, 15, calculateDistance(points.get(10), points.get(15))));
        edges.add(new Edge1(11, 12, calculateDistance(points.get(11), points.get(12))));
        edges.add(new Edge1(11, 15, calculateDistance(points.get(11), points.get(15))));
        edges.add(new Edge1(12, 13, calculateDistance(points.get(12), points.get(13))));
        edges.add(new Edge1(12, 14, calculateDistance(points.get(12), points.get(14))));
        edges.add(new Edge1(12, 15, calculateDistance(points.get(12), points.get(15))));
        edges.add(new Edge1(13, 14, calculateDistance(points.get(13), points.get(14))));
        edges.add(new Edge1(14, 15, calculateDistance(points.get(14), points.get(15))));
        edges.add(new Edge1(14, 16, calculateDistance(points.get(14), points.get(16))));
        edges.add(new Edge1(14, 17, calculateDistance(points.get(14), points.get(17))));
        edges.add(new Edge1(14, 19, calculateDistance(points.get(14), points.get(19))));
        edges.add(new Edge1(15, 20, calculateDistance(points.get(15), points.get(20))));
        edges.add(new Edge1(15, 16, calculateDistance(points.get(15), points.get(16))));
        edges.add(new Edge1(16, 19, calculateDistance(points.get(16), points.get(19))));
        edges.add(new Edge1(16, 18, calculateDistance(points.get(16), points.get(18))));
        edges.add(new Edge1(16, 17, calculateDistance(points.get(16), points.get(17))));
        edges.add(new Edge1(17, 18, calculateDistance(points.get(17), points.get(18))));
        edges.add(new Edge1(18, 19, calculateDistance(points.get(18), points.get(19))));


    }
    private int calculateDistance(Point point1, Point point2) {
        int dx = point2.x - point1.x;
        int dy = point2.y - point1.y;
        return (int) Math.sqrt(dx * dx + dy * dy);
    }

    // 辅助方法：将Image对象转换为BufferedImage
    private BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return bufferedImage;
    }

    // 绘制建筑点和标签的方法
    private void drawBuildingPoint(Graphics g, int x, int y) {
        int pointSize = 10;
        g.setColor(Color.white);
        g.fillOval(x - pointSize / 2, y - pointSize / 2, pointSize, pointSize);

        // 绘制建筑点的标签
        for (Point p : points) {
            g.setColor(Color.white); // 设置标签颜色为黑色
            g.drawString(p.getLabel(), p.getX(), p.getY() - 5); // 绘制标签
        }
        g.setFont(new Font("宋体", Font.PLAIN, 12)); // 设置字体为宋体，大小为12
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CampusNavigationSystem().setVisible(true);
        });

    }
}





