package com.tenco.library.view.swing;

import com.tenco.library.dto.*;
import com.tenco.library.service.LibraryService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class LibraryViewSwing extends JFrame {

    private final LibraryService service = new LibraryService();

    private Integer currentStudentId = null;
    private String currentStudentName = null;
    private Integer currentAdminId = null;
    private String currentAdminName = null;

    private JLabel loginLabel;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField loginField;

    private final Color PRIMARY = new Color(52, 152, 219);
    private final Color DARK = new Color(44, 62, 80);
    private final Color LIGHT = new Color(245, 245, 245);
    private final Color SUCCESS = new Color(46, 204, 113);

    public LibraryViewSwing() {
        setTitle("📖 Library System");
        setSize(1000, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createCenter(), BorderLayout.CENTER);
        add(createBottom(), BorderLayout.SOUTH);
    }

    // ===== 헤더 =====
    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("📚 Library Dashboard");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("맑은 고딕", Font.BOLD, 18));

        JPanel right = new JPanel();
        right.setOpaque(false);

        loginLabel = new JLabel("로그인 안됨");
        loginLabel.setForeground(Color.WHITE);

        loginField = new JTextField(10);

        JButton studentBtn = createButton("학생", PRIMARY);
        JButton adminBtn = createButton("관리자", SUCCESS);
        JButton logoutBtn = createButton("로그아웃", Color.GRAY);

        studentBtn.addActionListener(e -> studentLogin());
        adminBtn.addActionListener(e -> adminLogin());
        logoutBtn.addActionListener(e -> logout());

        right.add(loginLabel);
        right.add(loginField);
        right.add(studentBtn);
        right.add(adminBtn);
        right.add(logoutBtn);

        panel.add(title, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);

        return panel;
    }

    // ===== 중앙 =====
    private JPanel createCenter() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT);

        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);

        table.setRowHeight(28);
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // ===== 하단 =====
    private JPanel createBottom() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        addMenuButton(panel, "도서 목록", e -> listBooks());
        addMenuButton(panel, "도서 추가", e -> addBook());
        addMenuButton(panel, "도서 검색", e -> searchBooks());
        addMenuButton(panel, "학생 목록", e -> listStudents());

        addMenuButton(panel, "학생 등록", e -> addStudent());
        addMenuButton(panel, "대출", e -> borrowBook());
        addMenuButton(panel, "반납", e -> returnBook());
        addMenuButton(panel, "대출 목록", e -> listBorrowedBooks());

        return panel;
    }

    // ===== 버튼 스타일 =====
    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFocusPainted(false);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(color.darker());
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(color);
            }
        });

        return btn;
    }

    private void addMenuButton(JPanel panel, String text, java.awt.event.ActionListener action) {
        JButton btn = createButton(text, PRIMARY);
        btn.addActionListener(action);
        panel.add(btn);
    }

    // ===== 상태 =====
    private void updateLoginLabel() {
        if (currentAdminId != null) {
            loginLabel.setText("관리자: " + currentAdminName);
        } else if (currentStudentId != null) {
            loginLabel.setText("학생: " + currentStudentName);
        } else {
            loginLabel.setText("로그인 안됨");
        }
    }

    // ===== 로그인 =====
    private void studentLogin() {
        try {
            if (currentStudentId != null || currentAdminId != null) {
                JOptionPane.showMessageDialog(this, "이미 로그인 상태");
                return;
            }

            String id = loginField.getText().trim();
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(this, "학번 입력");
                return;
            }

            Student s = service.authenticateStudent(id);

            if (s != null) {
                currentStudentId = s.getId();
                currentStudentName = s.getName();
                updateLoginLabel();
                loginField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "로그인 실패");
            }
        } catch (Exception e) { showError(e); }
    }

    private void adminLogin() {
        try {
            if (currentStudentId != null || currentAdminId != null) {
                JOptionPane.showMessageDialog(this, "이미 로그인 상태");
                return;
            }

            String id = loginField.getText().trim();
            String pw = JOptionPane.showInputDialog(this, "비밀번호");

            Admin a = service.authenticateAdmin(id, pw);

            if (a != null) {
                currentAdminId = a.getId();
                currentAdminName = a.getName();
                updateLoginLabel();
                loginField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "로그인 실패");
            }
        } catch (Exception e) { showError(e); }
    }

    private void logout() {
        if (currentStudentId == null && currentAdminId == null) {
            JOptionPane.showMessageDialog(this, "로그인 상태 아님");
            return;
        }

        String name = (currentStudentId != null) ? currentStudentName : currentAdminName;

        currentStudentId = null;
        currentAdminId = null;
        currentStudentName = null;
        currentAdminName = null;

        updateLoginLabel();
        JOptionPane.showMessageDialog(this, name + "님 로그아웃");
    }

    // ===== 기능 =====
    private void setTable(String... cols) {
        tableModel.setColumnIdentifiers(cols);
        tableModel.setRowCount(0);
    }

    private void listBooks() {
        try {
            setTable("ID","제목","저자","상태");
            for (Book b : service.getAllBooks()) {
                tableModel.addRow(new Object[]{
                        b.getId(), b.getTitle(), b.getAuthor(),
                        b.isAvailable() ? "가능" : "대출중"
                });
            }
        } catch (Exception e) { showError(e); }
    }

    private void addBook() {
        if (currentAdminId == null) {
            JOptionPane.showMessageDialog(this,"관리자만 가능");
            return;
        }

        JTextField t = new JTextField();
        JTextField a = new JTextField();

        if (JOptionPane.showConfirmDialog(this,new Object[]{"제목",t,"저자",a})
                == JOptionPane.OK_OPTION) {
            try {
                service.addBook(Book.builder()
                        .title(t.getText())
                        .author(a.getText())
                        .available(true)
                        .build());
                listBooks();
            } catch (Exception e){ showError(e); }
        }
    }

    private void searchBooks() {
        String k = JOptionPane.showInputDialog("검색어");
        setTable("ID","제목","저자");
        try {
            for (Book b : service.searchBooksByTitle(k)) {
                tableModel.addRow(new Object[]{b.getId(),b.getTitle(),b.getAuthor()});
            }
        } catch (Exception e){ showError(e); }
    }

    private void listStudents() {
        if (currentAdminId == null) {
            JOptionPane.showMessageDialog(this,"관리자만 조회 가능");
            return;
        }

        try {
            setTable("ID","이름","학번");
            for (Student s : service.getAllStudents()) {
                tableModel.addRow(new Object[]{s.getId(),s.getName(),s.getStudentId()});
            }
        } catch (Exception e){ showError(e); }
    }

    private void addStudent() {
        if (currentAdminId == null) {
            JOptionPane.showMessageDialog(this,"관리자만 등록 가능");
            return;
        }

        JTextField n = new JTextField();
        JTextField id = new JTextField();

        if (JOptionPane.showConfirmDialog(this,new Object[]{"이름",n,"학번",id})
                == JOptionPane.OK_OPTION) {
            try {
                service.addStudent(Student.builder()
                        .name(n.getText())
                        .studentId(id.getText())
                        .build());
            } catch (Exception e){ showError(e); }
        }
    }

    // 🔥 대출 (상태 체크 포함)
    private void borrowBook() {
        if (currentStudentId == null) {
            JOptionPane.showMessageDialog(this,"학생 로그인 필요");
            return;
        }

        int row = table.getSelectedRow();
        if (row == -1) return;

        try {
            String status = (String) tableModel.getValueAt(row, 3);
            if ("대출중".equals(status)) {
                JOptionPane.showMessageDialog(this, "이미 대출중인 도서");
                return;
            }

            int id = (int) tableModel.getValueAt(row,0);
            service.borrowBook(id,currentStudentId);

            JOptionPane.showMessageDialog(this,"대출 완료");
            listBooks();

        } catch (Exception e){ showError(e); }
    }

    private void listBorrowedBooks() {
        try {
            setTable("학생","도서","날짜");
            for (Borrow b : service.getBorrowedBooks()) {
                tableModel.addRow(new Object[]{
                        b.getName(), b.getTitle(), b.getBorrowDate()
                });
            }
        } catch (Exception e){ showError(e); }
    }

    // 🔥 반납 (본인만 가능)
    private void returnBook() {
        if (currentStudentId == null) {
            JOptionPane.showMessageDialog(this,"학생 로그인 필요");
            return;
        }

        int row = table.getSelectedRow();
        if (row == -1) return;

        try {
            List<Borrow> list = service.getBorrowedBooks();
            Borrow b = list.get(row);

            if (!(b.getStudentId() == currentStudentId)) {
                JOptionPane.showMessageDialog(this,"본인만 반납 가능");
                return;
            }

            service.returnBook(b.getBookId(), b.getStudentId());

            JOptionPane.showMessageDialog(this,"반납 완료");
            listBorrowedBooks();

        } catch (Exception e){ showError(e); }
    }

    private void showError(Exception e){
        JOptionPane.showMessageDialog(this,e.getMessage());
    }
}