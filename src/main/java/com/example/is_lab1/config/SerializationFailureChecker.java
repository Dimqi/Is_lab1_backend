package com.example.is_lab1.config;

import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component("serializationFailure")
public class SerializationFailureChecker {

    public boolean isSerializationFailure(Throwable exception) {
        Throwable current = exception;

        while (current != null) {

            if (current instanceof SQLException sqlException) {

                if ("40001".equals(sqlException.getSQLState())) {
                    return true;
                }

                SQLException next = sqlException.getNextException();

                while (next != null) {

                    if ("40001".equals(next.getSQLState())) {
                        return true;
                    }

                    next = next.getNextException();
                }
            }

            current = current.getCause();
        }

        return false;
    }
}