package vn.library;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Ui.useModernDefaults();
            new LoginFrame().setVisible(true);
        });
    }
}

final class Db {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = Db.class.getResourceAsStream("/config.properties")) {
            if (in != null) PROPS.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Khong doc duoc config.properties", e);
        }
    }

    static Connection get() throws SQLException {
        return DriverManager.getConnection(
                PROPS.getProperty("db.url"),
                PROPS.getProperty("db.user"),
                PROPS.getProperty("db.password")
        );
    }

    static String nextId(String prefix) {
        String raw = UUID.randomUUID().toString().replace("-", "").substring(0, 7).toUpperCase();
        return (prefix + raw).substring(0, Math.min(10, prefix.length() + raw.length()));
    }

    static DefaultTableModel table(String sql, Object... args) {
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, args);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                DefaultTableModel model = new DefaultTableModel() {
                    @Override public boolean isCellEditable(int row, int column) { return false; }
                };
                for (int i = 1; i <= md.getColumnCount(); i++) model.addColumn(md.getColumnLabel(i));
                while (rs.next()) {
                    Object[] row = new Object[md.getColumnCount()];
                    for (int i = 1; i <= md.getColumnCount(); i++) row[i - 1] = rs.getObject(i);
                    model.addRow(row);
                }
                return model;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static int update(String sql, Object... args) throws SQLException {
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, args);
            return ps.executeUpdate();
        }
    }

    static void bind(PreparedStatement ps, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
    }
}

final class Ui {
    static final Color INK = new Color(255, 255, 255);
    static final Color PANEL = new Color(15, 23, 42, 215);
    static final Color PANEL_2 = new Color(30, 41, 59, 230);
    static final Color ACCENT = new Color(220, 38, 38);
    static final Color ACCENT_2 = new Color(14, 165, 233);
    static final Font TITLE = new Font("Segoe UI", Font.BOLD, 28);
    static final Font H2 = new Font("Segoe UI", Font.BOLD, 18);
    static final Font TEXT = new Font("Segoe UI", Font.PLAIN, 14);

    static void useModernDefaults() {
        UIManager.put("Button.font", TEXT);
        UIManager.put("Label.font", TEXT);
        UIManager.put("TextField.font", TEXT);
        UIManager.put("PasswordField.font", TEXT);
        UIManager.put("Table.font", TEXT);
        UIManager.put("Table.rowHeight", 30);
        UIManager.put("TabbedPane.font", TEXT);
    }

    static JButton button(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setForeground(Color.WHITE);
        b.setBackground(ACCENT);
        b.setBorder(new EmptyBorder(10, 18, 10, 18));
        return b;
    }

    static JButton secondary(String text) {
        JButton b = button(text);
        b.setBackground(new Color(51, 65, 85));
        return b;
    }

    static JTextField input() {
        JTextField t = new JTextField();
        t.setBorder(new EmptyBorder(9, 10, 9, 10));
        return t;
    }

    static JPasswordField password() {
        JPasswordField t = new JPasswordField();
        t.setBorder(new EmptyBorder(9, 10, 9, 10));
        return t;
    }

    static JPanel card(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(18, 18, 18, 18));
        return p;
    }

    static JLabel label(String text) {
        JLabel l = new OutlineLabel(text);
        l.setForeground(INK);
        return l;
    }

    static void info(Component parent, String text) {
        JOptionPane.showMessageDialog(parent, text, "Thong bao", JOptionPane.INFORMATION_MESSAGE);
    }

    static void error(Component parent, Throwable ex) {
        String msg = ex.getMessage();
        if (ex.getCause() != null) msg = ex.getCause().getMessage();
        JOptionPane.showMessageDialog(parent, msg, "Loi", JOptionPane.ERROR_MESSAGE);
    }

    static void showTicket(String title, String maDG, DefaultTableModel model, String extra) {
        StringBuilder sb = new StringBuilder();
        sb.append(title).append('\n');
        sb.append("Ma doc gia: ").append(maDG).append('\n');
        sb.append("Danh sach sach con muon:\n");
        for (int i = 0; i < model.getRowCount(); i++) {
            sb.append(model.getValueAt(i, 0)).append(" | ")
                    .append(model.getValueAt(i, 1)).append(" | ")
                    .append(model.getValueAt(i, 2)).append(" | ")
                    .append(model.getValueAt(i, 3)).append(" | Ngay muon: ")
                    .append(model.getValueAt(i, 4)).append(" | Ngay phai tra: ")
                    .append(model.getValueAt(i, 5)).append('\n');
        }
        sb.append("Tong so sach dang muon trong phieu: ").append(model.getRowCount()).append('\n');
        if (extra != null) sb.append(extra);
        JTextArea area = new JTextArea(sb.toString(), 18, 82);
        area.setFont(new Font("Consolas", Font.PLAIN, 14));
        area.setEditable(false);
        int choose = JOptionPane.showConfirmDialog(null, new JScrollPane(area), "Phieu", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (choose == JOptionPane.OK_OPTION) Ui.info(null, "In phieu muon thanh cong.");
    }

    static JScrollPane scroll(JTable table) {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(71, 85, 105)));
        return sp;
    }
}

class OutlineLabel extends JLabel {
    OutlineLabel(String text) { super(text); }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        FontMetrics fm = g2.getFontMetrics();
        int x = 0;
        int y = fm.getAscent();
        g2.setFont(getFont());
        g2.setColor(Color.BLACK);
        for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) if (dx != 0 || dy != 0) g2.drawString(getText(), x + dx, y + dy);
        g2.setColor(getForeground());
        g2.drawString(getText(), x, y);
        g2.dispose();
    }
}

class BackgroundPanel extends JPanel {
    private BufferedImage bg;
    BackgroundPanel(LayoutManager layout) {
        super(layout);
        setOpaque(true);
        try (InputStream in = getClass().getResourceAsStream("/assets/1.png")) {
            if (in != null) bg = ImageIO.read(in);
        } catch (Exception ignored) {
        }
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        if (bg != null) {
            double scale = Math.max(getWidth() / (double) bg.getWidth(), getHeight() / (double) bg.getHeight());
            int w = (int) (bg.getWidth() * scale);
            int h = (int) (bg.getHeight() * scale);
            g2.drawImage(bg, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
            g2.setColor(new Color(2, 6, 23, 155));
            g2.fillRect(0, 0, getWidth(), getHeight());
        } else {
            GradientPaint gp = new GradientPaint(0, 0, new Color(8, 13, 28), getWidth(), getHeight(), new Color(34, 48, 82));
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        g2.dispose();
    }
}

record User(String id, String name, String role) {}

class LoginFrame extends JFrame {
    private final JTextField idField = Ui.input();
    private final JPasswordField passwordField = Ui.password();
    private final JComboBox<String> roleBox = new JComboBox<>(new String[]{"Ban doc", "Staff", "Manager"});

    LoginFrame() {
        setTitle("Library Management System - Login");
        setSize(760, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        BackgroundPanel root = new BackgroundPanel(new GridBagLayout());
        JPanel box = new JPanel(new GridBagLayout());
        box.setBackground(Ui.PANEL);
        box.setBorder(new EmptyBorder(34, 42, 34, 42));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        JLabel title = Ui.label("LIBRARY MANAGEMENT SYSTEM");
        title.setFont(Ui.TITLE);
        box.add(title, c);

        c.gridy++; c.gridwidth = 1;
        box.add(Ui.label("Vai tro"), c);
        c.gridx = 1;
        roleBox.setBorder(new EmptyBorder(8, 8, 8, 8));
        box.add(roleBox, c);

        c.gridx = 0; c.gridy++;
        box.add(Ui.label("Ma dang nhap"), c);
        c.gridx = 1; idField.setColumns(22); box.add(idField, c);

        c.gridx = 0; c.gridy++;
        box.add(Ui.label("Password"), c);
        c.gridx = 1; box.add(passwordField, c);

        JButton login = Ui.button("LOGIN");
        login.addActionListener(e -> login());
        c.gridx = 0; c.gridy++; c.gridwidth = 2;
        box.add(login, c);
        root.add(box);
        setContentPane(root);
    }

    private void login() {
        String id = idField.getText().trim();
        String pass = new String(passwordField.getPassword());
        String role = (String) roleBox.getSelectedItem();
        try (Connection c = Db.get()) {
            if ("Ban doc".equals(role)) {
                try (PreparedStatement ps = c.prepareStatement("SELECT MaDG, HoTen FROM DOCGIA WHERE MaDG=? AND Password=?")) {
                    Db.bind(ps, id, pass);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) open(new ReaderFrame(new User(rs.getString(1), rs.getString(2), "Reader")));
                    else Ui.info(this, "Sai MaDG hoac password.");
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement("SELECT MaNV, HoTen, VaiTro FROM NHANVIEN WHERE MaNV=? AND Password=? AND VaiTro=?")) {
                    Db.bind(ps, id, pass, role);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        User u = new User(rs.getString(1), rs.getString(2), rs.getString(3));
                        if ("Manager".equals(role)) open(new ManagerFrame(u)); else open(new StaffFrame(u));
                    } else Ui.info(this, "Sai MaNV, password hoac vai tro.");
                }
            }
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }

    private void open(JFrame f) {
        f.setVisible(true);
        dispose();
    }
}

abstract class MainFrame extends JFrame {
    final User user;
    final JTabbedPane tabs = new JTabbedPane();

    MainFrame(String title, User user) {
        this.user = user;
        setTitle(title + " - " + user.name());
        setSize(1180, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        BackgroundPanel root = new BackgroundPanel(new BorderLayout());
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(16, 20, 8, 20));
        JLabel h = Ui.label(title + " | " + user.name() + " (" + user.role() + ")");
        h.setFont(Ui.H2);
        JButton logout = Ui.secondary("Dang xuat");
        logout.addActionListener(e -> { new LoginFrame().setVisible(true); dispose(); });
        top.add(h, BorderLayout.WEST);
        top.add(logout, BorderLayout.EAST);
        tabs.setOpaque(false);
        root.add(top, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
    }
}

class ReaderFrame extends MainFrame {
    ReaderFrame(User user) {
        super("Giao dien ban doc", user);
        tabs.add("Thong tin ca nhan", readerInfo());
        tabs.add("Sach dang muon", tablePanel(currentLoansSql(), user.id()));
        tabs.add("Lich su da tra", tablePanel(returnedLoansSql(), user.id()));
    }

    private JPanel readerInfo() {
        JPanel p = Ui.card(new BorderLayout(10, 10));
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement("SELECT * FROM DOCGIA WHERE MaDG=?")) {
            Db.bind(ps, user.id());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                area.setText("""
                        Ma doc gia: %s
                        Ho ten: %s
                        Ngay sinh: %s
                        Dia chi: %s
                        SDT: %s
                        """.formatted(rs.getString("MaDG"), rs.getString("HoTen"), rs.getDate("NgaySinh"), rs.getString("DiaChi"), rs.getString("SDT")));
            }
        } catch (Exception e) {
            area.setText(e.getMessage());
        }
        p.add(new JScrollPane(area));
        return p;
    }

    static String currentLoansSql() {
        return """
                SELECT s.MaSach, s.TenSach, s.TacGia, s.MaVach, pm.NgayMuon, ct.NgayPhaiTra
                FROM CHITIET_PHIEUMUON ct
                JOIN PHIEUMUON pm ON pm.MaPM=ct.MaPM
                JOIN SACH s ON s.MaSach=ct.MaSach
                WHERE pm.MaDG=? AND ct.NgayTra IS NULL
                ORDER BY pm.NgayMuon DESC
                """;
    }

    static String returnedLoansSql() {
        return """
                SELECT s.MaSach, s.TenSach, s.TacGia, s.MaVach, pm.NgayMuon, ct.NgayPhaiTra, ct.NgayTra
                FROM CHITIET_PHIEUMUON ct
                JOIN PHIEUMUON pm ON pm.MaPM=ct.MaPM
                JOIN SACH s ON s.MaSach=ct.MaSach
                WHERE pm.MaDG=? AND ct.NgayTra IS NOT NULL
                ORDER BY ct.NgayTra DESC
                """;
    }

    static JPanel tablePanel(String sql, Object... args) {
        JPanel p = Ui.card(new BorderLayout());
        JTable t = new JTable(Db.table(sql, args));
        p.add(Ui.scroll(t), BorderLayout.CENTER);
        return p;
    }
}

class ManagerFrame extends MainFrame {
    ManagerFrame(User user) {
        super("Giao dien Manager", user);
        tabs.add("Quan ly nhan vien", new StaffManagePanel());
        tabs.add("Quan ly ban doc", new ReaderManagePanel());
        tabs.add("Quan ly dau sach", new BookManagePanel());
    }
}

class StaffFrame extends MainFrame {
    StaffFrame(User user) {
        super("Giao dien Staff", user);
        tabs.add("Muon sach", new BorrowPanel(user));
        tabs.add("Tra sach", new ReturnPanel());
        tabs.add("Sach muon nhieu", new PopularBooksPanel());
        tabs.add("Ban doc muon nhieu", new PopularReadersPanel());
    }
}

abstract class CrudPanel extends JPanel {
    final JTable table = new JTable();
    final JTextField keyword = Ui.input();
    final List<JTextField> fields = new ArrayList<>();
    final String[] labels;

    CrudPanel(String[] labels) {
        super(new BorderLayout(12, 12));
        this.labels = labels;
        setOpaque(false);
        setBorder(new EmptyBorder(18, 18, 18, 18));
        add(toolbar(), BorderLayout.NORTH);
        add(Ui.scroll(table), BorderLayout.CENTER);
        add(form(), BorderLayout.SOUTH);
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { fillFromSelected(); }
        });
        reload();
    }

    private JPanel toolbar() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setOpaque(false);
        JLabel l = Ui.label("Tim theo ma/ten");
        JButton search = Ui.button("Tim kiem");
        JButton reload = Ui.secondary("Tai lai");
        search.addActionListener(e -> reload());
        reload.addActionListener(e -> { keyword.setText(""); reload(); });
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);
        right.add(search); right.add(reload);
        p.add(l, BorderLayout.WEST);
        p.add(keyword, BorderLayout.CENTER);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JPanel form() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        JPanel grid = new JPanel(new GridLayout(0, 4, 8, 8));
        grid.setOpaque(false);
        for (String label : labels) {
            grid.add(Ui.label(label));
            JTextField f = Ui.input();
            fields.add(f);
            grid.add(f);
        }
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton add = Ui.button("Them");
        JButton update = Ui.secondary("Cap nhat");
        JButton delete = Ui.secondary("Xoa");
        JButton reset = Ui.secondary("Reset");
        add.addActionListener(e -> run(this::insert));
        update.addActionListener(e -> run(this::update));
        delete.addActionListener(e -> run(this::delete));
        reset.addActionListener(e -> fields.forEach(f -> f.setText("")));
        actions.add(add); actions.add(update); actions.add(delete); actions.add(reset);
        wrap.add(grid, BorderLayout.CENTER);
        wrap.add(actions, BorderLayout.SOUTH);
        return wrap;
    }

    void fillFromSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        for (int i = 0; i < fields.size() && i < table.getColumnCount(); i++) {
            Object v = table.getValueAt(row, i);
            fields.get(i).setText(v == null ? "" : String.valueOf(v));
        }
    }

    String val(int i) { return fields.get(i).getText().trim(); }
    void run(SqlAction a) {
        try {
            a.call();
            reload();
            Ui.info(this, "Thao tac thanh cong.");
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }
    void reload() { table.setModel(Db.table(selectSql(), like(), like())); }
    String like() { return "%" + keyword.getText().trim() + "%"; }
    abstract String selectSql();
    abstract void insert() throws Exception;
    abstract void update() throws Exception;
    abstract void delete() throws Exception;
    interface SqlAction { void call() throws Exception; }
}

class StaffManagePanel extends CrudPanel {
    StaffManagePanel() {
        super(new String[]{"MaNV", "HoTen", "NgaySinh yyyy-mm-dd", "SDT", "DiaChi", "VaiTro", "Username", "Password"});
    }
    String selectSql() { return "SELECT MaNV,HoTen,NgaySinh,SDT,DiaChi,VaiTro,Username,Password FROM NHANVIEN WHERE MaNV LIKE ? OR HoTen LIKE ? ORDER BY MaNV"; }
    void insert() throws Exception {
        Db.update("INSERT INTO NHANVIEN(MaNV,HoTen,NgaySinh,SDT,DiaChi,VaiTro,Username,Password) VALUES(?,?,?,?,?,?,?,?)",
                val(0), val(1), emptyDate(2), val(3), val(4), val(5), val(6), val(7));
    }
    void update() throws Exception {
        Db.update("UPDATE NHANVIEN SET HoTen=?,NgaySinh=?,SDT=?,DiaChi=?,VaiTro=?,Username=?,Password=? WHERE MaNV=?",
                val(1), emptyDate(2), val(3), val(4), val(5), val(6), val(7), val(0));
    }
    void delete() throws Exception {
        Db.update("DELETE FROM NHANVIEN WHERE MaNV=? AND NOT EXISTS(SELECT 1 FROM PHIEUMUON WHERE MaNV=?)", val(0), val(0));
    }
    private Date emptyDate(int i) { return val(i).isEmpty() ? null : Date.valueOf(val(i)); }
}

class ReaderManagePanel extends CrudPanel {
    ReaderManagePanel() {
        super(new String[]{"MaDG", "HoTen", "NgaySinh yyyy-mm-dd", "DiaChi", "SDT", "Password"});
    }
    String selectSql() { return "SELECT MaDG,HoTen,NgaySinh,DiaChi,SDT,Password FROM DOCGIA WHERE MaDG LIKE ? OR HoTen LIKE ? ORDER BY MaDG"; }
    void insert() throws Exception {
        Db.update("INSERT INTO DOCGIA(MaDG,HoTen,NgaySinh,DiaChi,SDT,Password) VALUES(?,?,?,?,?,?)",
                val(0), val(1), emptyDate(2), val(3), val(4), val(5));
    }
    void update() throws Exception {
        Db.update("UPDATE DOCGIA SET HoTen=?,NgaySinh=?,DiaChi=?,SDT=?,Password=? WHERE MaDG=?",
                val(1), emptyDate(2), val(3), val(4), val(5), val(0));
    }
    void delete() throws Exception {
        int open = ((Number) Db.table("""
                SELECT COUNT(*) AS SoSach
                FROM PHIEUMUON pm JOIN CHITIET_PHIEUMUON ct ON pm.MaPM=ct.MaPM
                WHERE pm.MaDG=? AND ct.NgayTra IS NULL
                """, val(0)).getValueAt(0, 0)).intValue();
        if (open > 0) throw new SQLException("Khong the xoa doc gia vi van con " + open + " quyen sach chua tra.");
        int fines = ((Number) Db.table("""
                SELECT COUNT(*)
                FROM PHIEUPHAT pp
                JOIN CHITIET_PHIEUMUON ct ON ct.MaCT=pp.MaCT
                JOIN PHIEUMUON pm ON pm.MaPM=ct.MaPM
                WHERE pm.MaDG=?
                """, val(0)).getValueAt(0, 0)).intValue();
        if (fines > 0) throw new SQLException("Yeu cau thanh toan/xu ly het phi phat qua han truoc khi huy the.");
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement("""
                    DELETE ct FROM CHITIET_PHIEUMUON ct
                    JOIN PHIEUMUON pm ON pm.MaPM=ct.MaPM
                    WHERE pm.MaDG=?
                    """)) {
                Db.bind(ps, val(0));
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM PHIEUMUON WHERE MaDG=?")) {
                Db.bind(ps, val(0));
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM DOCGIA WHERE MaDG=?")) {
                Db.bind(ps, val(0));
                ps.executeUpdate();
            }
            c.commit();
        }
    }
    private Date emptyDate(int i) { return val(i).isEmpty() ? null : Date.valueOf(val(i)); }
}

class BookManagePanel extends CrudPanel {
    BookManagePanel() {
        super(new String[]{"MaSach", "TenSach", "TacGia", "NamXB", "GiaBia", "SoLuong", "MaVach", "MoTa"});
    }
    String selectSql() { return "SELECT MaSach,TenSach,TacGia,NamXB,GiaBia,SoLuong,MaVach,MoTa FROM SACH WHERE MaSach LIKE ? OR TenSach LIKE ? ORDER BY MaSach"; }
    void insert() throws Exception {
        Db.update("INSERT INTO SACH(MaSach,TenSach,TacGia,NamXB,GiaBia,SoLuong,MaVach,MoTa) VALUES(?,?,?,?,?,?,?,?)",
                val(0), val(1), val(2), intVal(3), money(4), intVal(5), val(6), val(7));
    }
    void update() throws Exception {
        Db.update("UPDATE SACH SET TenSach=?,TacGia=?,NamXB=?,GiaBia=?,SoLuong=?,MaVach=?,MoTa=? WHERE MaSach=?",
                val(1), val(2), intVal(3), money(4), intVal(5), val(6), val(7), val(0));
    }
    void delete() throws Exception {
        int open = ((Number) Db.table("SELECT COUNT(*) FROM CHITIET_PHIEUMUON WHERE MaSach=? AND NgayTra IS NULL", val(0)).getValueAt(0, 0)).intValue();
        if (open > 0) throw new SQLException("Khong the xoa vi sach dang co nguoi muon.");
        Db.update("DELETE FROM SACH WHERE MaSach=?", val(0));
    }
    private Integer intVal(int i) { return val(i).isEmpty() ? null : Integer.parseInt(val(i)); }
    private BigDecimal money(int i) { return val(i).isEmpty() ? BigDecimal.ZERO : new BigDecimal(val(i)); }
}

class BorrowPanel extends JPanel {
    private final User staff;
    private final JTextField readerId = Ui.input();
    private final JTextField bookId = Ui.input();
    private final JTable current = new JTable();
    private final JTable returned = new JTable();
    private final JTable selected = new JTable(new DefaultTableModel(new Object[]{"MaSach", "TenSach", "TacGia", "MaVach", "NgayMuon", "NgayPhaiTra"}, 0));
    private String loadedReader;

    BorrowPanel(User staff) {
        super(new BorderLayout(10, 10));
        this.staff = staff;
        setOpaque(false);
        setBorder(new EmptyBorder(18, 18, 18, 18));
        add(top(), BorderLayout.NORTH);
        add(center(), BorderLayout.CENTER);
        add(bottom(), BorderLayout.SOUTH);
    }

    private JPanel top() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setOpaque(false);
        p.add(Ui.label("Nhap MaDG"), BorderLayout.WEST);
        p.add(readerId, BorderLayout.CENTER);
        JButton load = Ui.button("Lay thong tin");
        load.addActionListener(e -> loadReader());
        p.add(load, BorderLayout.EAST);
        return p;
    }

    private JSplitPane center() {
        JTabbedPane history = new JTabbedPane();
        history.add("Sach muon chua tra", Ui.scroll(current));
        history.add("Sach da tra", Ui.scroll(returned));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, history, Ui.scroll(selected));
        split.setResizeWeight(0.58);
        split.setOpaque(false);
        return split;
    }

    private JPanel bottom() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setOpaque(false);
        p.add(Ui.label("Nhap MaSach"), BorderLayout.WEST);
        p.add(bookId, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton add = Ui.button("Them sach");
        JButton remove = Ui.secondary("Bo chon");
        JButton submit = Ui.button("Xac nhan muon");
        add.addActionListener(e -> addBook());
        remove.addActionListener(e -> removeSelected());
        submit.addActionListener(e -> submit());
        actions.add(add); actions.add(remove); actions.add(submit);
        p.add(actions, BorderLayout.EAST);
        return p;
    }

    private void loadReader() {
        try {
            loadedReader = readerId.getText().trim();
            current.setModel(Db.table(ReaderFrame.currentLoansSql(), loadedReader));
            returned.setModel(Db.table(ReaderFrame.returnedLoansSql(), loadedReader));
            Ui.info(this, "Da tai thong tin doc gia " + loadedReader);
        } catch (Exception e) { Ui.error(this, e); }
    }

    private void addBook() {
        try {
            if (loadedReader == null || loadedReader.isBlank()) throw new SQLException("Vui long nhap MaDG truoc.");
            DefaultTableModel m = (DefaultTableModel) selected.getModel();
            int booksInThisBorrow = m.getRowCount() + 1;
            if (booksInThisBorrow > 5) throw new SQLException("Moi lan muon chi duoc them toi da 5 quyen.");
            String id = bookId.getText().trim();
            for (int i = 0; i < m.getRowCount(); i++) if (id.equals(m.getValueAt(i, 0))) throw new SQLException("Sach nay da nam trong danh sach chon.");
            DefaultTableModel found = Db.table("""
                    SELECT MaSach, TenSach, TacGia, MaVach,
                    CAST(GETDATE() AS DATE) AS NgayMuon,
                    DATEADD(MONTH, 1, CAST(GETDATE() AS DATE)) AS NgayPhaiTra
                    FROM SACH
                    WHERE MaSach=?
                    AND SoLuong > (SELECT COUNT(*) FROM CHITIET_PHIEUMUON WHERE MaSach=? AND NgayTra IS NULL)
                    """, id, id);
            if (found.getRowCount() == 0) throw new SQLException("Khong tim thay sach hoac sach da het so luong co san.");
            m.addRow(new Object[]{found.getValueAt(0,0), found.getValueAt(0,1), found.getValueAt(0,2), found.getValueAt(0,3), found.getValueAt(0,4), found.getValueAt(0,5)});
            bookId.setText("");
        } catch (Exception e) { Ui.error(this, e); }
    }

    private void removeSelected() {
        int row = selected.getSelectedRow();
        if (row >= 0) ((DefaultTableModel) selected.getModel()).removeRow(row);
    }

    private void submit() {
        DefaultTableModel m = (DefaultTableModel) selected.getModel();
        if (m.getRowCount() == 0) { Ui.info(this, "Chua co sach nao de muon."); return; }
        String maPM = Db.nextId("PM");
        LocalDate now = LocalDate.now();
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try (PreparedStatement pm = c.prepareStatement("INSERT INTO PHIEUMUON(MaPM,MaDG,MaNV,NgayMuon) VALUES(?,?,?,?)")) {
                Db.bind(pm, maPM, loadedReader, staff.id(), Date.valueOf(now));
                pm.executeUpdate();
            }
            try (PreparedStatement ct = c.prepareStatement("INSERT INTO CHITIET_PHIEUMUON(MaCT,MaPM,MaSach,NgayPhaiTra,NgayTra,TrangThai) VALUES(?,?,?,?,NULL,'DangMuon')")) {
                for (int i = 0; i < m.getRowCount(); i++) {
                    Db.bind(ct, Db.nextId("CT"), maPM, m.getValueAt(i, 0), Date.valueOf(now.plusMonths(1)));
                    ct.executeUpdate();
                }
            }
            c.commit();
            DefaultTableModel allCurrent = Db.table(ReaderFrame.currentLoansSql(), loadedReader);
            Ui.showTicket("PHIEU MUON " + maPM, loadedReader, allCurrent, null);
            m.setRowCount(0);
            loadReader();
        } catch (Exception e) { Ui.error(this, e); }
    }
}

class ReturnPanel extends JPanel {
    private final JTextField readerId = Ui.input();
    private final JTable current = new JTable();
    private final JTable returned = new JTable();
    private String loadedReader;

    ReturnPanel() {
        super(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(new EmptyBorder(18, 18, 18, 18));
        add(top(), BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Chon sach dang muon de tra", Ui.scroll(current));
        tabs.add("Lich su da tra", Ui.scroll(returned));
        add(tabs, BorderLayout.CENTER);
        JButton submit = Ui.button("Xac nhan tra sach da chon");
        submit.addActionListener(e -> submit());
        add(submit, BorderLayout.SOUTH);
    }

    private JPanel top() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setOpaque(false);
        p.add(Ui.label("Nhap MaDG"), BorderLayout.WEST);
        p.add(readerId, BorderLayout.CENTER);
        JButton load = Ui.button("Lay thong tin");
        load.addActionListener(e -> loadReader());
        p.add(load, BorderLayout.EAST);
        return p;
    }

    private void loadReader() {
        loadedReader = readerId.getText().trim();
        current.setModel(Db.table("""
                SELECT ct.MaCT, s.MaSach, s.TenSach, s.TacGia, s.MaVach, pm.NgayMuon, ct.NgayPhaiTra, s.GiaBia
                FROM CHITIET_PHIEUMUON ct
                JOIN PHIEUMUON pm ON pm.MaPM=ct.MaPM
                JOIN SACH s ON s.MaSach=ct.MaSach
                WHERE pm.MaDG=? AND ct.NgayTra IS NULL
                ORDER BY pm.NgayMuon
                """, loadedReader));
        returned.setModel(Db.table(ReaderFrame.returnedLoansSql(), loadedReader));
    }

    private void submit() {
        int[] rows = current.getSelectedRows();
        if (rows.length == 0) { Ui.info(this, "Hay chon sach can tra."); return; }
        LocalDate today = LocalDate.now();
        StringBuilder fineText = new StringBuilder();
        BigDecimal totalFine = BigDecimal.ZERO;
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            for (int row : rows) {
                String maCT = String.valueOf(current.getValueAt(row, 0));
                LocalDate due = ((Date) current.getValueAt(row, 6)).toLocalDate();
                BigDecimal price = (BigDecimal) current.getValueAt(row, 7);
                boolean late = today.isAfter(due);
                BigDecimal fine = late ? price.multiply(new BigDecimal("0.20")) : BigDecimal.ZERO;
                try (PreparedStatement ps = c.prepareStatement("UPDATE CHITIET_PHIEUMUON SET NgayTra=?, TrangThai=? WHERE MaCT=?")) {
                    Db.bind(ps, Date.valueOf(today), late ? "QuaHan" : "DaTra", maCT);
                    ps.executeUpdate();
                }
                if (late) {
                    try (PreparedStatement fp = c.prepareStatement("INSERT INTO PHIEUPHAT(MaPhat,MaCT,SoTienPhat,LyDo) VALUES(?,?,?,?)")) {
                        Db.bind(fp, Db.nextId("PP"), maCT, fine, "Tra sach qua han");
                        fp.executeUpdate();
                    }
                    totalFine = totalFine.add(fine);
                    fineText.append(current.getValueAt(row, 1)).append(" - ").append(current.getValueAt(row, 2))
                            .append(" | Han tra: ").append(due)
                            .append(" | Ngay tra: ").append(today)
                            .append(" | Phat: ").append(fine).append('\n');
                }
            }
            c.commit();
            loadReader();
            String msg = "Tra sach thanh cong.";
            if (totalFine.compareTo(BigDecimal.ZERO) > 0) msg += "\n\nPHIEU PHAT\n" + fineText + "Tong tien phat: " + totalFine;
            JTextArea area = new JTextArea(msg, 16, 70);
            area.setFont(new Font("Consolas", Font.PLAIN, 14));
            JOptionPane.showMessageDialog(this, new JScrollPane(area), "Ket qua tra sach", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) { Ui.error(this, e); }
    }
}

class PopularBooksPanel extends StatsPanel {
    String sql() {
        return """
                SELECT s.MaSach, s.TenSach, s.TacGia, s.MaVach, COUNT(*) AS TongSoLuotMuon
                FROM CHITIET_PHIEUMUON ct
                JOIN PHIEUMUON pm ON pm.MaPM=ct.MaPM
                JOIN SACH s ON s.MaSach=ct.MaSach
                WHERE pm.NgayMuon BETWEEN ? AND ?
                GROUP BY s.MaSach, s.TenSach, s.TacGia, s.MaVach
                ORDER BY TongSoLuotMuon DESC
                """;
    }
}

class PopularReadersPanel extends StatsPanel {
    String sql() {
        return """
                SELECT dg.MaDG, dg.HoTen, dg.NgaySinh, dg.DiaChi, COUNT(*) AS TongSoLuongSachMuon
                FROM CHITIET_PHIEUMUON ct
                JOIN PHIEUMUON pm ON pm.MaPM=ct.MaPM
                JOIN DOCGIA dg ON dg.MaDG=pm.MaDG
                WHERE pm.NgayMuon BETWEEN ? AND ?
                GROUP BY dg.MaDG, dg.HoTen, dg.NgaySinh, dg.DiaChi
                ORDER BY TongSoLuongSachMuon DESC
                """;
    }
}

abstract class StatsPanel extends JPanel {
    private final JTextField from = Ui.input();
    private final JTextField to = Ui.input();
    private final JTable table = new JTable();

    StatsPanel() {
        super(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(new EmptyBorder(18, 18, 18, 18));
        JPanel top = new JPanel(new GridLayout(1, 6, 8, 8));
        top.setOpaque(false);
        top.add(Ui.label("Tu ngay yyyy-mm-dd"));
        top.add(from);
        top.add(Ui.label("Den ngay yyyy-mm-dd"));
        top.add(to);
        JButton run = Ui.button("Thong ke");
        run.addActionListener(e -> run());
        top.add(run);
        add(top, BorderLayout.NORTH);
        add(Ui.scroll(table), BorderLayout.CENTER);
    }

    private void run() {
        try {
            Date f = Date.valueOf(from.getText().trim());
            Date t = Date.valueOf(to.getText().trim());
            if (f.after(t)) throw new SQLException("Ngay ket thuc khong duoc truoc ngay bat dau.");
            table.setModel(Db.table(sql(), f, t));
            if (table.getRowCount() == 0) Ui.info(this, "Khong tim thay du lieu muon sach trong khoang thoi gian nay.");
        } catch (Exception e) { Ui.error(this, e); }
    }

    abstract String sql();
}
