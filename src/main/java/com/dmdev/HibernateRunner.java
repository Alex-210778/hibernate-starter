package com.dmdev;

import com.dmdev.entity.Birthday;
import com.dmdev.entity.Company;
import com.dmdev.entity.PersonalInfo;
import com.dmdev.entity.User;
import com.dmdev.util.HibernateUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;

@Slf4j
public class HibernateRunner {

    public static void main(String[] args) throws SQLException {
        Company company = Company.builder()
                .name("Google")
                .build();

        User user = User.builder()
                .userName("sveta@gmail.com")
                .personalInfo(PersonalInfo.builder()
                        .firstName("Sveta")
                        .lastName("Svetikova")
                        .birthDate(new Birthday(LocalDate.of(1998, 4, 10)))
                        .build())
                .company(company)
                .build();
        try (SessionFactory sessionFactory = HibernateUtil.buildSessionFactory()) {
            Session session1 = sessionFactory.openSession();
            try (session1) {
                session1.beginTransaction();

                User user1 = session1.get(User.class, 1L);
                Company company1 = user1.getCompany();
                System.out.println("id = " + company1.getName());

//                session1.save(company);
//                session1.save(user);

                session1.getTransaction().commit();

            }
        }
    }
}
