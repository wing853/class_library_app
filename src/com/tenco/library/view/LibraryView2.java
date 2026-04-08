package com.tenco.library.view;

import com.tenco.library.dto.Book;
import com.tenco.library.dto.Borrow;
import com.tenco.library.dto.Student;
import com.tenco.library.service.LibraryService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;


// 사용자 입출력을 처리하는 View 클래스
public class LibraryView2 {

    private final LibraryService service = new LibraryService();
    private final Scanner scanner = new Scanner(System.in);

    private Integer currentStudentId = null; // 로그인 중인 학생의 DB ID 저장
    private String currentStudentName = null; // 로그인 중인 학생 이름

    // 프로그램 메인 루프
    public void start() {
        System.out.println("도서관리 시스템 시작...");

        while (true) {
            printMenu();
            int choice = readInt("선택 : ");

            try {
                switch (choice) {
                    case 1:
                        addBook();
                        break;
                    case 2:
                        listBooks();
                        break;
                    case 3:
                        searchBooks();
                        break;
                    case 4:
                        addStudent();
                        break;
                    case 5:
                        listStudents();
                        break;
                    case 6:
                        borrowBooks();
                        break;
                    case 7:
                        listBorrowedBooks();
                        break;
                    case 8:
                        returnBook();
                        break;
                    case 9:
                        login();
                        break;
                    case 10:
                        logout();
                        break;
                    case 0:
                        System.out.println("프로그램을 종료합니다");
                        scanner.close();
                        return; // while 종료 처리
                    default:
                        System.out.println("0 ~ 10 사이의 숫자를 입력하세요");
                }
            } catch (Exception e) {
                System.out.println("오류 : " + e.getMessage());
            }
        }
    }

    // 10. 로그아웃
    private void logout() {
        if (currentStudentId == null) {
            System.out.println("현재 로그인 상태가 아닙니다.");
        } else {
            System.out.println(currentStudentName + "님이 로그아웃되었습니다");
            currentStudentId = null;
            currentStudentName = null;
        }
    }

    // 9. 로그인
    private void login() throws SQLException {
        // 유효성 검사
        if (currentStudentId != null) {
            System.out.println("이미 로그인 중입니다(" + currentStudentId + ")");
            return;
        }
        System.out.print("학번: ");
        String studentId = scanner.nextLine().trim(); // 학번 PK 아님

        // 유효성 검사
        if (studentId.isEmpty()) {
            System.out.println("학번을 입력해주세요");
            return;
        }

        Student student = service.authenticateStudent(studentId);

        if (student == null) {
            System.out.println("존재하지 않는 학번입니다");
        } else {
            currentStudentId = student.getId();
            currentStudentName = student.getName();
            System.out.println(currentStudentName + " 님, 환영합니다");
        }
    }

    // 8. 도서 반납
    private void returnBook() throws SQLException {
        if (currentStudentId == null) {
            System.out.println("로그인 후 도서 반납을 이용해주세요");
            login();
        }
        List<Borrow> borrowList = listBorrowedBooks();
        if (borrowList.isEmpty()) {
            System.out.println("현재 대출중인 도서 목록이 존재하지 않습니다.");
        } else {
            int choice = readInt("반납 처리할 도서 선택(1부터 입력): ");
            choice--;
            if(choice  < 0) {
                System.out.println("해당 대출번호가 존재하지 않습니다.");
                return;
            }

            if(borrowList.get(choice).getStudentId() != currentStudentId){
                System.out.println("본인이 빌린 도서가 아닙니다.");
                return;
            }
            service.returnBook(borrowList.get(choice).getBookId(), borrowList.get(choice).getStudentId());
            System.out.println("도서 반납이 완료 되었습니다.");
        }
    }

    // 7. 대출 중인 도서
    private List<Borrow> listBorrowedBooks() throws SQLException {
        List<Borrow> borrowList = service.getBorrowedBooks();
        int index = 1;
        List<Student> studentList = service.getAllStudents();
        List<Book> bookList = service.getAllBooks();


        for (Borrow br : borrowList) {
            String name = "";
            String title = "";
            for (Student s : studentList){
                if(br.getStudentId() == s.getId()){
                    name = s.getName();
                    break;
                }
            }

            for (Book b : bookList) {
                if(br.getBookId() == b.getId()){
                    title = b.getTitle();
                }
            }

            System.out.printf("%d - 학생이름: %-15s | 책제목 : %-15s | 대출일: %-30s",
                    index,
                    name,
                    title,
                    br.getBorrowDate()
            );
            index++;
            System.out.println();
        }
        return borrowList;
    }

    // 6. 도서 대출
    private void borrowBooks() throws SQLException {
        if (currentStudentId == null) {
            System.out.println("로그인 후 도서 대출을 이용해주세요");
            login();
        }

        List<Book> bookList = service.getAllBooks();
        if (bookList == null) {
            System.out.println("도서 대출에 실패 했습니다. 다시 시도해 주세요");
        } else if (bookList.size() == 1) {
            service.borrowBook(bookList.getFirst().getId(), currentStudentId);
            System.out.println("[" + bookList.getFirst().getTitle() + "] 도서가대출이 완료 되었습니다.");
        } else if (bookList.size() >= 2) {
            int choice = readInt("몇번째 책을 대출할 지 선택해주세요(1부터 입력): ");
            choice--;
            if (choice  < 0) {
                System.out.println("해당 번호의 책은 존재하지 않습니다.");
                return;
            }
            service.borrowBook(bookList.get(choice).getId(), currentStudentId);
            System.out.println("[" + bookList.get(choice).getTitle() + "] 도서가대출이 완료 되었습니다.");
        }
    }

    // 5. 학생 목록
    private void listStudents() throws SQLException {
        List<Student> studentList = service.getAllStudents();

        if (studentList.isEmpty()) {
            System.out.println("등록된 학생이 없습니다.");
        } else {
            int index = 1;
            System.out.println("------------------------------");
            for (Student s : studentList) {
                System.out.printf("%d - 학번: %-15s | 이름: %-15s ",
                        index,
                        s.getStudentId(),
                        s.getName()
                );
                index++;
                System.out.println();
            }
        }
    }

    // 4. 학생 등록
    private void addStudent() throws SQLException {
        System.out.print("학생 이름: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("학생 이름은 필수입니다.");
            return;
        }
        System.out.print("학번: ");
        String studentId = scanner.nextLine().trim();
        if (studentId.isEmpty()) {
            System.out.println("학번은 필수 입니다.");
            return;
        }

        Student student = Student.builder()
                .name(name)
                .studentId(studentId)
                .build();
        service.addStudent(student);
        System.out.println(name + "학생 추가 완료");
    }

    // 3. 도서 검색
    private void searchBooks() throws SQLException {
        System.out.print("검색 제목: ");
        String title = scanner.nextLine().trim();

        if (title.isEmpty()) {
            System.out.println("검색어를 입력하세요");
            return;
        }
        List<Book> bookList = service.searchBooksByTitle(title);
        if (bookList.isEmpty()) {
            System.out.println("검색 결과가 없습니다");
            return;
        } else {
            int index = 1;
            for (Book b : bookList) {
                System.out.printf("%d - ID: %2d | %-30s | %-15s | %s%n",
                        index,
                        b.getId(),
                        b.getTitle(),
                        b.getAuthor(),
                        b.isAvailable() ? "대출 가능" : "대출중");
                index++;
            }
        }
    }

    // 2. 도서 목록
    private void listBooks() throws SQLException {
        List<Book> bookList = service.getAllBooks();
        if (bookList.isEmpty()) {
            System.out.println("등록된 도서가 없습니다.");
        } else {
            int index = 1;
            System.out.println("------------------------------");
            for (Book b : bookList) {
                System.out.printf("%d - ID: %2d | %-30s | %-15s | %s%n",
                        index,
                        b.getId(),
                        b.getTitle(),
                        b.getAuthor(),
                        b.isAvailable() ? "대출 가능" : "대출중");
                index++;
            }
        }
    }

    // 1. 도서 추가
    private void addBook() throws SQLException {
        // 제목
        System.out.print("제목: ");
        String title = scanner.nextLine().trim();
        if (title.isEmpty()) {
            System.out.println("제목은 필수입니다");
            return;
        }

        // 저자
        System.out.print("저자: ");
        String author = scanner.nextLine().trim();
        if (author.isEmpty()) {
            System.out.println("저자는 필수입니다");
            return;
        }

        // 출판사
        System.out.print("출판사: ");
        String publisher = scanner.nextLine().trim();

        // 출판년도
        int publicationYear = readInt("출판년도: ");

        // ISBN
        System.out.print("ISBN: ");
        String isbn = scanner.nextLine().trim();

        Book book = Book.builder()
                .title(title)
                .author(author)
                .publisher(publisher.isEmpty() ? null : publisher)
                .publicationYear(publicationYear)
                .isbn(isbn.isEmpty() ? null : isbn)
                .available(true)
                .build();
        service.addBook(book);
        System.out.println("도서 추가: " + title);
    }


    private void printMenu() {
        System.out.println("\n==== 도서관리 시스템====");

        System.out.println("----------------------------------");
        System.out.println("0. 종료");
        System.out.println("1. 도서 추가");
        System.out.println("2. 도서 목록");
        System.out.println("3. 도서 검색");
        System.out.println("4. 학생 등록");
        System.out.println("5. 학생 목록");
        System.out.println("6. 도서 대출");
        System.out.println("7. 대출 중인 도서");
        System.out.println("8. 도서 반납");
        System.out.println("9. 로그인");
        System.out.println("10. 로그아웃");

    }


    // 숫자 입력을 안전하게 처리(잘못된 입력 시 재 요청)
    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력해주세요");
            }
        }
    }

} // end of class
