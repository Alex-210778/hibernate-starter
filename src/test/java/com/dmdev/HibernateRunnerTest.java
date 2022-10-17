package com.dmdev;

import com.dmdev.entity.Birthday;
import com.dmdev.entity.Chat;
import com.dmdev.entity.Company;
import com.dmdev.entity.LocaleInfo;
import com.dmdev.entity.PersonalInfo;
import com.dmdev.entity.Profile;
import com.dmdev.entity.Role;
import com.dmdev.entity.User;
import com.dmdev.entity.UserChat;
import com.dmdev.util.HibernateUtil;
import lombok.Cleanup;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;

import javax.persistence.Column;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.*;

class HibernateRunnerTest {

    @Test
    void localeInfo() {
        try (SessionFactory sessionFactory = HibernateUtil.buildSessionFactory();
             Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            Company company = session.get(Company.class, 3);
//            company.getLocales().add(LocaleInfo.builder()
//                    .lang("en")
//                    .description("english")
//                    .build());

            System.out.println(company.getUsers());

            session.getTransaction().commit();
        }

    }

    @Test
    void checkManyToMany() {
        try (SessionFactory sessionFactory = HibernateUtil.buildSessionFactory();
             Session session = sessionFactory.openSession()) {

            session.beginTransaction();
            User user = session.get(User.class, 5L);
            Chat chat = session.get(Chat.class, 1L);

            UserChat userChat = UserChat.builder()
                    .user(user)
                    .chat(chat)
                    .createdAt(Instant.now())
                    .createdBy("Alex")
                    .build();

            session.save(userChat);

            session.getTransaction().commit();
        }
    }

    @Test
    void checkOneToOne() {
        try (SessionFactory sessionFactory = HibernateUtil.buildSessionFactory();
             Session session = sessionFactory.openSession()) {

            session.beginTransaction();

            User user = User.builder()
                    .userName("Anna@gmail.com")
                    .personalInfo(PersonalInfo.builder()
                            .firstName("Anna")
                            .lastName("Krug")
                            .birthDate(new Birthday(LocalDate.of(2000, 7, 14)))
                            .build())
                    .role(Role.USER)
                    .info("""
                            {
                            "name": "Anna",
                            "age": 22
                            }
                            """)
                    .build();
            Profile profile = Profile.builder()
                    .language("ru")
                    .street("Nekrasova")
                    .build();

            profile.setUser(user);
            session.save(user);


            session.getTransaction().commit();
        }
    }

    @Test
    void checkLazyInitialisation() {
        Company company = null;
        try (SessionFactory sessionFactory = HibernateUtil.buildSessionFactory();
             Session session = sessionFactory.openSession()) {

            session.beginTransaction();

            company = session.get(Company.class, 3);

            session.getTransaction().commit();
        }
        Set<User> users = company.getUsers();
        System.out.println(users.size());
    }

    @Test
    void deleteCompany() {
        @Cleanup SessionFactory sessionFactory = HibernateUtil.buildSessionFactory();
        @Cleanup Session session = sessionFactory.openSession();

        session.beginTransaction();

        Company company = session.get(Company.class, 2);
        session.delete(company);

        session.getTransaction().commit();
    }

    @Test
    void addUserToNewCompany() {
        @Cleanup SessionFactory sessionFactory = HibernateUtil.buildSessionFactory();
        @Cleanup Session session = sessionFactory.openSession();

        Company company = Company.builder()
                .name("Amazon")
                .build();
        User user = User.builder()
                .userName("Alex@gmail.com")
                .personalInfo(PersonalInfo.builder()
                        .firstName("Aleksandr")
                        .lastName("Aleksandrov")
                        .birthDate(new Birthday(LocalDate.of(1995, 4, 3)))
                        .build())
                .role(Role.USER)
                .info("""
                        {
                        "name": "Alex",
                        "age": 27
                        }
                        """)
                .company(company)
                .build();
        session.beginTransaction();

        company.addUser(user);
        session.save(company);

        session.getTransaction().commit();
    }

    @Test
    void oneToMany() {
        @Cleanup SessionFactory sessionFactory = HibernateUtil.buildSessionFactory();
        @Cleanup Session session = sessionFactory.openSession();

        session.beginTransaction();

        Company company = session.get(Company.class, 1);
        System.out.println(company);


        session.getTransaction().commit();
    }

    @Test
    void checkReflectionApi() throws SQLException, IllegalAccessException {
        User user = User.builder()
                .build();

        String sql = """
                insert
                into
                %s
                (%s)
                values
                (%s)
                """;

        String tableName = Optional.ofNullable(user.getClass().getAnnotation(Table.class))
                .map(tableAnnotation -> tableAnnotation.schema() + "." + tableAnnotation.name())
                .orElse(user.getClass().getName());

        Field[] declaredFields = user.getClass().getDeclaredFields();

        String columnNames = Arrays.stream(declaredFields)
                .map(field -> Optional.ofNullable(field.getAnnotation(Column.class))
                        .map(Column::name)
                        .orElse(field.getName()))
                .collect(joining(", "));

        String columnValues = Arrays.stream(declaredFields)
                .map(field -> "?")
                .collect(joining(", "));

        System.out.println(sql.formatted(tableName, columnNames, columnValues));

        Connection connection = null;
        PreparedStatement preparedStatement = connection.prepareStatement(
                sql.formatted(tableName, columnNames, columnValues));

        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            preparedStatement.setObject(1, declaredField.get(user));
//        }
        }
    }

}