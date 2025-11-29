package com.university.library.config;

import com.university.library.entity.Book;
import com.university.library.entity.Loan;
import com.university.library.entity.User;
import com.university.library.model.LoanStatus;
import com.university.library.model.UserRole;
import com.university.library.repository.BookRepository;
import com.university.library.repository.LoanRepository;
import com.university.library.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, BookRepository bookRepository, LoanRepository loanRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedBooks();
        seedLoans();
    }

    private void seedUsers() {
        upsertUser("admin", "admin123", "系统管理员", "admin@university.edu", UserRole.ADMIN);
        upsertUser("librarian", "lib123", "图书馆员", "librarian@university.edu", UserRole.LIBRARIAN);
        upsertUser("student", "student123", "测试学生", "student@university.edu", UserRole.STUDENT);
        upsertUser("zhangli", "lib2024", "张丽", "zhangli@university.edu", UserRole.LIBRARIAN);
        upsertUser("chenli", "stu2024", "陈力", "chenli@student.edu", UserRole.STUDENT);
        upsertUser("wangjuan", "stu2025", "王娟", "wangjuan@student.edu", UserRole.STUDENT);
        upsertUser("lihao", "stu2026", "李浩", "lihao@student.edu", UserRole.STUDENT);
    }

    private void seedBooks() {
        upsertBook("9787121316242", "计算机网络", "谢希仁", "计算机", "经典计算机网络教材", 6);
        upsertBook("9787111490887", "操作系统概念", "Silberschatz", "计算机", "OS Concepts 中文版", 5);
        upsertBook("9787506365437", "活着", "余华", "文学", "当代文学经典", 4);
        upsertBook("978-7-111-21382-6", "Java编程思想", "Bruce Eckel", "计算机", "Java 语言进阶经典", 10);
        upsertBook("978-7-115-45678-9", "Spring Boot实战", "王松", "后端", "Spring Boot 入门与项目实践", 15);
        upsertBook("978-7-121-34567-8", "Vue.js权威指南", "李炎恢", "前端", "Vue.js 深入浅出教程", 20);
        upsertBook("978-7-115-23456-7", "MySQL必知必会", "Ben Forta", "数据库", "MySQL 实用入门", 12);
        upsertBook("978-7-111-40701-0", "算法导论", "Thomas H. Cormen", "算法", "算法与数据结构权威教材", 8);
        upsertBook("978-7-111-07575-2", "设计模式", "Gang of Four", "软件工程", "23 种经典设计模式", 10);
        upsertBook("978-7-111-32133-0", "深入理解计算机系统", "Randal E. Bryant", "计算机体系结构", "CSAPP 经典教材", 6);
        upsertBook("978-7-115-42802-2", "Python编程从入门到实践", "Eric Matthes", "编程语言", "Python 快速实战指南", 18);
        upsertBook("9787302512345", "数据结构与算法分析", "Mark Allen Weiss", "数据结构", "数据结构经典教材", 12);
        upsertBook("9787302581234", "人工智能：一种现代方法", "Stuart Russell", "人工智能", "AI 领域权威教材", 6);
        upsertBook("9787508631713", "三体", "刘慈欣", "科幻", "科幻代表作，地球往事三部曲之一", 10);
        upsertBook("9787020042494", "围城", "钱钟书", "文学", "现代文学经典长篇小说", 8);
        upsertBook("9787111213820", "计算机组成原理", "唐朔飞", "计算机", "国内畅销的计算机组成教材", 14);
        upsertBook("9787121387334", "Linux就该这么学", "鸟哥", "操作系统", "Linux 基础与实战", 16);
    }

    private void seedLoans() {
        if (loanRepository.count() > 0) {
            return;
        }
        User student = userRepository.findByUsername("student").orElse(null);
        Book book = bookRepository.findAll().stream().findFirst().orElse(null);
        if (student != null && book != null && book.getAvailableCopies() > 0) {
            book.setAvailableCopies(book.getAvailableCopies() - 1);
            Loan loan = new Loan(student, book, LocalDate.now().minusDays(2), LocalDate.now().plusDays(12), "示例借阅");
            loan.setStatus(LoanStatus.BORROWED);
            loanRepository.save(loan);
            bookRepository.save(book);
        }
    }

    private void upsertUser(String username, String rawPassword, String fullName, String email, UserRole role) {
        userRepository.findByUsername(username).ifPresentOrElse(existing -> {
            existing.setFullName(fullName);
            existing.setEmail(email);
            existing.setRole(role);
            existing.setActive(true);
            existing.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(existing);
        }, () -> {
            User user = new User(username, passwordEncoder.encode(rawPassword), fullName, email, role);
            user.setActive(true);
            userRepository.save(user);
        });
    }

    private void upsertBook(String isbn, String title, String author, String category, String description, int desiredTotal) {
        bookRepository.findAll().stream()
                .filter(b -> isbn != null && isbn.equals(b.getIsbn()))
                .findFirst()
                .ifPresentOrElse(existing -> {
                    int borrowed = existing.getTotalCopies() - existing.getAvailableCopies();
                    int newTotal = Math.max(desiredTotal, borrowed);
                    existing.setTitle(title);
                    existing.setAuthor(author);
                    existing.setCategory(category);
                    existing.setDescription(description);
                    existing.setTotalCopies(newTotal);
                    existing.setAvailableCopies(Math.max(newTotal - borrowed, 0));
                    bookRepository.save(existing);
                }, () -> {
                    Book book = new Book(title, author, isbn, category, description, desiredTotal);
                    bookRepository.save(book);
                });
    }
}
