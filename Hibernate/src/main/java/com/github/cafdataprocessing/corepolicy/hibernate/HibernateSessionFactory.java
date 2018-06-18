/*
 * Copyright 2015-2018 Micro Focus or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.cafdataprocessing.corepolicy.hibernate;

import com.google.common.base.Strings;
import com.github.cafdataprocessing.corepolicy.common.exceptions.TransitoryBackEndFailureCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.BackEndRequestFailedErrors;
import com.github.cafdataprocessing.corepolicy.common.logging.LogHelper;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Factory to manage Hibernate session
 */
@Component
class HibernateSessionFactory {

    private static Lock lock = new ReentrantLock();
    private static SessionFactory factory;
    private static Logger logger = LoggerFactory.getLogger(HibernateSessionFactory.class);

    private final PreInsertInterceptor preInsertInterceptor;
    private final HibernateProperties hibernateProperties;
    private final String JDBC_PREFIX = "jdbc:";

    @Autowired
    public HibernateSessionFactory(PreInsertInterceptor preInsertInterceptor, HibernateProperties hibernateProperties){
        this.preInsertInterceptor = preInsertInterceptor;
        this.hibernateProperties = hibernateProperties;
    }

    private SessionFactory getSessionFactory() {
        if (factory == null) {
            lock.lock();
            try {
                if (factory == null) {
                    Configuration configuration = new Configuration();
                    configuration.configure();
                    configuration.setInterceptor(preInsertInterceptor);
                    configuration.setProperty("hibernate.connection.url", hibernateProperties.getConnectionString());
                    configuration.setProperty("hibernate.connection.username", hibernateProperties.getUser());
                    configuration.setProperty("hibernate.connection.password", hibernateProperties.getPassword());

                    // we have to setup the drivers to use depending on our jdbc connection string.  This is really
                    // only to ensure that the correct drivers is picked up in Tomcat.  It has its own class loader rules
                    // which mean this is a requirement.
                    configuration.setProperty("hibernate.connection.driver_class", getDriverClass());

                    ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(
                            configuration.getProperties()).build();

                    factory = configuration.buildSessionFactory(serviceRegistry);
                }
            } finally {
                lock.unlock();
            }
        }
        return factory;
    }

    private String getDriverClass() {

        // look at connection string, and decide what to use.
        // e.g. hibernate.connectionstring=jdbc:postgresql://localhost:5432/<dbname>
        // should use postgres driver
        final String connection = hibernateProperties.getConnectionString().toLowerCase();

        if ( !connection.startsWith(JDBC_PREFIX) )
        {
            throw new TransitoryBackEndFailureCpeException(BackEndRequestFailedErrors.DatabaseConnectionFailed, new Exception("Invalid hibernate connection string format. Must start with jdbc:"));
        }

        // Get the next : after the jdbc prefix.
        int index = connection.indexOf( ":", JDBC_PREFIX.length());

        if ( index == -1 )
        {
            throw new TransitoryBackEndFailureCpeException(BackEndRequestFailedErrors.DatabaseConnectionFailed, new Exception("Invalid hibernate connection string format. Must be of the format jdbc:xxx:"));
        }

        //take string from jdbc: to the next : as our connection type.
        String connectionType = connection.substring(JDBC_PREFIX.length(), index);

        switch( connectionType.toLowerCase() ) {

            case "postgresql":
                logger.info("Detected driver required as postgresql");
                return "org.postgresql.Driver";
            case "h2":
                logger.info("Detected driver required as h2");
                return "org.h2.Driver";
            case "mysql":
                logger.info("Detected driver required as mysql");
                return "com.mysql.jdbc.Driver";
            default:
                // use my sql and log.
                logger.warn("Unknown jdbc driver type - defaulting to mysql.");
                // before we error, check incase someone has specified the property to use externally.
                String driverClass = System.getProperty("hibernate.connection.driver_class");

                if (Strings.isNullOrEmpty( driverClass ))
                    throw new TransitoryBackEndFailureCpeException(BackEndRequestFailedErrors.DatabaseConnectionFailed, new Exception("Unknown hibernate driver type - to add support please specify environment option hibernate.connection.driver_class."));

                // someone has already passed it in, just use it.
                logger.info(LogHelper.removeWhiteSpace("Using externally supplied driver class: " + driverClass));
                return driverClass;
        }
    }

    public void closeSession() {
        if (factory == null) {
            return;
        }

        try {
            lock.lock();
            factory.close();
            factory = null;
        } finally {
            lock.unlock();
        }
    }

    public Session getSession(){
        try {
            return getSessionFactory().openSession();
        } catch(HibernateException e){
            throw new TransitoryBackEndFailureCpeException(BackEndRequestFailedErrors.DatabaseConnectionFailed, e);
        }
    }
}
